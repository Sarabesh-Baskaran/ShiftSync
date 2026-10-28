package com.shiftsync.solver.service;

import ai.timefold.solver.core.api.score.ScoreExplanation;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatchTotal;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.enums.ShiftStatus;
import com.shiftsync.repository.DepartmentRepository;
import com.shiftsync.repository.EmployeeRepository;
import com.shiftsync.repository.ShiftRepository;
import com.shiftsync.solver.dto.CapacityAnalysisDTO;
import com.shiftsync.solver.dto.ConstraintExplanationDTO;
import com.shiftsync.solver.dto.SolveRosterRequest;
import com.shiftsync.solver.dto.SolverJobResponse;
import com.shiftsync.dto.analytics.TeamScheduleAnalyticsDTO;
import com.shiftsync.service.CognitiveLoadCalculator;
import com.shiftsync.solver.model.RosterSchedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service managing asynchronous and synchronous Timefold optimization runs for hospital rosters.
 * Integrates pre-solve capacity analysis, time budgets, post-solve fairness calculations,
 * and detailed score explanations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RosterSolverService {

    private final SolverManager<RosterSchedule, String> solverManager;
    private final SolutionManager<RosterSchedule, HardMediumSoftScore> solutionManager;
    private final PreSolveCapacityValidator capacityValidator;
    private final CognitiveLoadCalculator cognitiveLoadCalculator;
    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    private final Map<String, SolverJobResponse> jobRegistry = new ConcurrentHashMap<>();
    private final Map<String, RosterSchedule> latestSolutions = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<SolverJobResponse>> syncFutures = new ConcurrentHashMap<>();

    /**
     * Backward-compatible helper: Solves with default parameters.
     */
    public SolverJobResponse startSolving(LocalDate startDate, LocalDate endDate) {
        return startSolving(SolveRosterRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .timeoutSeconds(30)
                .validateCapacityFirst(true)
                .build());
    }

    /**
     * Triggers asynchronous solving of the roster using the full request configuration.
     */
    public SolverJobResponse startSolving(SolveRosterRequest request) {
        String jobId = UUID.randomUUID().toString();
        return startSolvingWithJobId(jobId, request);
    }

    public SolverJobResponse startSolvingWithJobId(String jobId, SolveRosterRequest request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        List<Department> departments;
        if (request.getDepartmentIds() != null && !request.getDepartmentIds().isEmpty()) {
            departments = departmentRepository.findAllById(request.getDepartmentIds());
        } else {
            departments = departmentRepository.findAll();
        }

        List<Employee> employees = employeeRepository.findByActiveTrue();
        List<Shift> shifts;
        if (request.getDepartmentIds() != null && !request.getDepartmentIds().isEmpty()) {
            shifts = new ArrayList<>();
            for (Department dept : departments) {
                shifts.addAll(shiftRepository.findByDepartmentAndShiftDateBetween(dept, startDate, endDate));
            }
            shifts.sort(Comparator.comparing(Shift::getStartDateTime));
        } else {
            shifts = shiftRepository.findByShiftDateBetweenOrderByShiftDateAsc(startDate, endDate);
        }

        if (shifts.isEmpty()) {
            throw new IllegalArgumentException("No shift slots found between " + startDate + " and " + endDate + ". Please generate slots first.");
        }
        if (employees.isEmpty()) {
            throw new IllegalArgumentException("No active healthcare staff found to assign shifts to.");
        }

        // Run Pre-solve Capacity Analysis if enabled
        CapacityAnalysisDTO capacityAnalysis = null;
        if (request.isValidateCapacityFirst()) {
            capacityAnalysis = capacityValidator.validateCapacity(employees, shifts);
            log.info("Pre-solve capacity check: feasible={}, demand={}h, capacity={}h, warnings={}",
                    capacityAnalysis.isTheoreticallyFeasible(),
                    capacityAnalysis.getTotalHoursDemand(),
                    capacityAnalysis.getTotalStaffCapacityHours(),
                    capacityAnalysis.getWarnings().size());
        }

        RosterSchedule problem = RosterSchedule.builder()
                .id(1L)
                .startDate(startDate)
                .endDate(endDate)
                .departmentList(departments)
                .employeeList(employees)
                .shiftList(shifts)
                .build();

        SolverJobResponse initialStatus = SolverJobResponse.builder()
                .jobId(jobId)
                .status("SOLVING")
                .score("0hard/0medium/0soft")
                .totalShifts(shifts.size())
                .assignedShifts(0)
                .timeoutSeconds(request.getTimeoutSeconds())
                .startDate(startDate)
                .endDate(endDate)
                .startedAt(LocalDateTime.now())
                .capacityAnalysis(capacityAnalysis)
                .build();

        jobRegistry.put(jobId, initialStatus);

        solverManager.solveBuilder()
                .withProblemId(jobId)
                .withProblem(problem)
                .withBestSolutionConsumer(intermediateSolution -> updateJobProgress(jobId, intermediateSolution))
                .withFinalBestSolutionConsumer(finalSolution -> onSolvingFinished(jobId, finalSolution))
                .withExceptionHandler((id, exception) -> onSolvingError(jobId, exception))
                .run();

        // Enforce request timeout dynamically
        if (request.getTimeoutSeconds() > 0) {
            CompletableFuture.delayedExecutor(request.getTimeoutSeconds(), TimeUnit.SECONDS)
                    .execute(() -> {
                        try {
                            if (solverManager.getSolverStatus(jobId) == SolverStatus.SOLVING_ACTIVE) {
                                log.info("Terminating solver job {} after reaching timeout of {}s", jobId, request.getTimeoutSeconds());
                                solverManager.terminateEarly(jobId);
                            }
                        } catch (Exception ignored) {
                        }
                    });
        }

        return initialStatus;
    }

    /**
     * Dry-run capacity analysis without starting an optimization job.
     */
    @Transactional(readOnly = true)
    public CapacityAnalysisDTO validateCapacityOnly(LocalDate startDate, LocalDate endDate, List<Long> departmentIds) {
        List<Employee> staff = employeeRepository.findByActiveTrue();
        List<Shift> shifts;

        if (departmentIds != null && !departmentIds.isEmpty()) {
            List<Department> depts = departmentRepository.findAllById(departmentIds);
            shifts = new ArrayList<>();
            for (Department dept : depts) {
                shifts.addAll(shiftRepository.findByDepartmentAndShiftDateBetween(dept, startDate, endDate));
            }
        } else {
            shifts = shiftRepository.findByShiftDateBetweenOrderByShiftDateAsc(startDate, endDate);
        }

        return capacityValidator.validateCapacity(staff, shifts);
    }

    private void updateJobProgress(String jobId, RosterSchedule solution) {
        latestSolutions.put(jobId, solution);
        HardMediumSoftScore score = solution.getScore();

        int assigned = 0;
        if (solution.getShiftList() != null) {
            assigned = (int) solution.getShiftList().stream()
                    .filter(s -> s.getAssignedEmployee() != null)
                    .count();
        }

        SolverJobResponse current = jobRegistry.get(jobId);
        if (current != null && score != null) {
            current.setScore(score.toString());
            current.setHardScore(score.hardScore());
            current.setMediumScore(score.mediumScore());
            current.setSoftScore(score.softScore());
            current.setFeasible(score.isFeasible());
            current.setAssignedShifts(assigned);
        }
    }

    @Transactional
    public void onSolvingFinished(String jobId, RosterSchedule finalSolution) {
        log.info("Optimization job {} completed with score: {}", jobId, finalSolution.getScore());
        latestSolutions.put(jobId, finalSolution);

        // Persist optimal shift assignments to database
        if (finalSolution.getShiftList() != null) {
            for (Shift shift : finalSolution.getShiftList()) {
                if (shift.getAssignedEmployee() != null) {
                    shift.setStatus(ShiftStatus.ASSIGNED);
                }
            }
            shiftRepository.saveAll(finalSolution.getShiftList());
        }

        HardMediumSoftScore score = finalSolution.getScore();
        SolverJobResponse current = jobRegistry.get(jobId);
        if (current != null) {
            current.setStatus("SOLVED");
            current.setCompletedAt(LocalDateTime.now());
            if (score != null) {
                current.setScore(score.toString());
                current.setHardScore(score.hardScore());
                current.setMediumScore(score.mediumScore());
                current.setSoftScore(score.softScore());
                current.setFeasible(score.isFeasible());
            }
            if (finalSolution.getShiftList() != null) {
                int assigned = (int) finalSolution.getShiftList().stream()
                        .filter(s -> s.getAssignedEmployee() != null)
                        .count();
                current.setAssignedShifts(assigned);
                current.setUnassignedShifts(current.getTotalShifts() - assigned);
            }

            if (current.getStartedAt() != null && current.getCompletedAt() != null) {
                current.setSolvingDurationMs(Duration.between(current.getStartedAt(), current.getCompletedAt()).toMillis());
            }

            // Post-solve Fairness Analytics Calculation
            try {
                TeamScheduleAnalyticsDTO analytics = cognitiveLoadCalculator.calculateTeamAnalytics(
                        finalSolution.getStartDate(), finalSolution.getEndDate(), null);
                current.setFairnessScore(analytics.getFairnessScore());
            } catch (Exception ex) {
                log.warn("Could not calculate team fairness score for job {}: {}", jobId, ex.getMessage());
            }

            // Extract detailed ScoreExplanation breakdown from Timefold
            try {
                ScoreExplanation<RosterSchedule, HardMediumSoftScore> explanation = solutionManager.explain(finalSolution);
                current.setScoreSummary(explanation.getSummary());

                List<ConstraintExplanationDTO> breakdown = new ArrayList<>();
                for (ConstraintMatchTotal<HardMediumSoftScore> matchTotal : explanation.getConstraintMatchTotalMap().values()) {
                    HardMediumSoftScore constraintScore = matchTotal.getScore();
                    String level = "SOFT";
                    if (constraintScore.hardScore() != 0) {
                        level = "HARD";
                    } else if (constraintScore.mediumScore() != 0) {
                        level = "MEDIUM";
                    }

                    breakdown.add(ConstraintExplanationDTO.builder()
                            .constraintName(matchTotal.getConstraintRef().constraintName())
                            .scoreLevel(level)
                            .scoreImpact(constraintScore.toString())
                            .matchCount(matchTotal.getConstraintMatchCount())
                            .description(matchTotal.getConstraintId())
                            .build());
                }
                current.setConstraintBreakdown(breakdown);
            } catch (Exception ex) {
                log.warn("Could not generate constraint explanation for job {}: {}", jobId, ex.getMessage());
            }

            // Complete synchronous waiting future if registered
            CompletableFuture<SolverJobResponse> syncFuture = syncFutures.remove(jobId);
            if (syncFuture != null) {
                syncFuture.complete(current);
            }
        }
    }

    private void onSolvingError(String jobId, Throwable exception) {
        log.error("Optimization job {} encountered a fatal error", jobId, exception);
        SolverJobResponse current = jobRegistry.get(jobId);
        if (current != null) {
            current.setStatus("FAILED: " + exception.getMessage());
            current.setCompletedAt(LocalDateTime.now());
            CompletableFuture<SolverJobResponse> syncFuture = syncFutures.remove(jobId);
            if (syncFuture != null) {
                syncFuture.complete(current);
            }
        }
    }

    /**
     * Executes synchronous roster optimization, blocking until the solver converges or reaches timeout.
     */
    public SolverJobResponse solveSync(SolveRosterRequest request) {
        String jobId = UUID.randomUUID().toString();
        CompletableFuture<SolverJobResponse> future = new CompletableFuture<>();
        syncFutures.put(jobId, future);

        startSolvingWithJobId(jobId, request);

        try {
            return future.get(request.getTimeoutSeconds() + 10L, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Synchronous solver execution timed out or was interrupted for job {}", jobId, e);
            terminateEarly(jobId);
            return getJobStatus(jobId);
        } finally {
            syncFutures.remove(jobId);
        }
    }

    public SolverJobResponse getJobStatus(String jobId) {
        SolverJobResponse status = jobRegistry.get(jobId);
        if (status == null) {
            throw new IllegalArgumentException("No solver job found with id: " + jobId);
        }
        SolverStatus solverStatus = solverManager.getSolverStatus(jobId);
        if (solverStatus == SolverStatus.SOLVING_ACTIVE) {
            status.setStatus("SOLVING");
        }
        return status;
    }

    public RosterSchedule getLatestSolution(String jobId) {
        return latestSolutions.get(jobId);
    }

    public void terminateEarly(String jobId) {
        solverManager.terminateEarly(jobId);
    }
}
