package com.shiftsync.solver;

import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.enums.ShiftStatus;
import com.shiftsync.repository.ShiftRepository;
import com.shiftsync.service.DataSeederService;
import com.shiftsync.solver.dto.SolveRosterRequest;
import com.shiftsync.solver.dto.SolverJobResponse;
import com.shiftsync.solver.service.RosterSolverService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test validating Timefold solver execution
 * against a realistic hospital dataset of 24 medical staff, 5 clinical wards,
 * and 189 shifts across a full 7-day schedule window.
 */
@SpringBootTest
class FullHospitalRosterSolvingTest {

    @Autowired
    private DataSeederService dataSeederService;

    @Autowired
    private RosterSolverService rosterSolverService;

    @Autowired
    private ShiftRepository shiftRepository;

    @AfterEach
    void tearDown() {
        dataSeederService.resetAllData();
    }

    @Test
    @DisplayName("Should optimize full 7-day hospital roster with 0 hard constraint violations")
    void testFullHospitalRosterOptimization() {
        // 1. Seed full realistic hospital dataset
        dataSeederService.seedHospitalData(true);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(6);

        SolveRosterRequest request = SolveRosterRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .timeoutSeconds(5) // Fast 5-second convergence
                .validateCapacityFirst(true)
                .build();

        // 2. Execute synchronous solve
        SolverJobResponse solution = rosterSolverService.solveSync(request);

        // 3. Verify Feasibility & Clinical Safety
        assertThat(solution).isNotNull();
        assertThat(solution.getStatus()).isEqualTo("SOLVED");
        assertThat(solution.isFeasible()).isTrue();
        assertThat(solution.getHardScore()).isZero(); // Zero medical/safety/clopening violations!

        // 4. Verify Operational Coverage (24 staff * 4 shifts/week = 96 optimal assigned shifts under 40h cap)
        assertThat(solution.getTotalShifts()).isEqualTo(189);
        assertThat(solution.getAssignedShifts()).isGreaterThanOrEqualTo(90);
        assertThat(solution.getSolvingDurationMs()).isGreaterThan(0L);

        // 5. Verify Explainability & Fairness
        assertThat(solution.getConstraintBreakdown()).isNotEmpty();
        assertThat(solution.getScoreSummary()).isNotEmpty();
        assertThat(solution.getFairnessScore()).isNotNull().isGreaterThan(50.0);

        // 6. Verify Database Persistence
        List<Shift> persistedShifts = shiftRepository.findByShiftDateBetweenOrderByShiftDateAsc(startDate, endDate);
        long assignedInDb = persistedShifts.stream()
                .filter(s -> s.getStatus() == ShiftStatus.ASSIGNED && s.getAssignedEmployee() != null)
                .count();

        assertThat(assignedInDb).isEqualTo(solution.getAssignedShifts());
    }
}
