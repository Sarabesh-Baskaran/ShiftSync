package com.shiftsync.solver.service;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.enums.ShiftStatus;
import com.shiftsync.repository.DepartmentRepository;
import com.shiftsync.repository.EmployeeRepository;
import com.shiftsync.repository.ShiftRepository;
import com.shiftsync.solver.dto.SolverJobResponse;
import com.shiftsync.solver.model.RosterSchedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing asynchronous Timefold optimization runs for hospital rosters.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RosterSolverService {

    private final SolverManager<RosterSchedule, String> solverManager;
    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    private final Map<String, SolverJobResponse> jobRegistry = new ConcurrentHashMap<>();
    private final Map<String, RosterSchedule> latestSolutions = new ConcurrentHashMap<>();

    /**
     * Triggers asynchronous solving of the roster for the given date range.
     */
    public SolverJobResponse startSolving(LocalDate startDate, LocalDate endDate) {
        String jobId = UUID.randomUUID().toString();

        List<Department> departments = departmentRepository.findAll();
        List<Employee> employees = employeeRepository.findByActiveTrue();
        List<Shift> shifts = shiftRepository.findByShiftDateBetweenOrderByShiftDateAsc(startDate, endDate);

        if (shifts.isEmpty()) {
            throw new IllegalArgumentException("No shift slots found between " + startDate + " and " + endDate + ". Please generate slots first.");
        }
        if (employees.isEmpty()) {
            throw new IllegalArgumentException("No active healthcare staff found to assign shifts to.");
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
                .startDate(startDate)
                .endDate(endDate)
                .startedAt(LocalDateTime.now())
                .build();

        jobRegistry.put(jobId, initialStatus);

        solverManager.solveBuilder()
                .withProblemId(jobId)
                .withProblem(problem)
                .withBestSolutionConsumer(intermediateSolution -> updateJobProgress(jobId, intermediateSolution))
                .withFinalBestSolutionConsumer(finalSolution -> onSolvingFinished(jobId, finalSolution))
                .withExceptionHandler((id, exception) -> onSolvingError(jobId, exception))
                .run();

        return initialStatus;
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
                current.setAssignedShifts((int) finalSolution.getShiftList().stream()
                        .filter(s -> s.getAssignedEmployee() != null)
                        .count());
            }
        }
    }

    private void onSolvingError(String jobId, Throwable exception) {
        log.error("Optimization job {} encountered a fatal error", jobId, exception);
        SolverJobResponse current = jobRegistry.get(jobId);
        if (current != null) {
            current.setStatus("FAILED: " + exception.getMessage());
            current.setCompletedAt(LocalDateTime.now());
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
