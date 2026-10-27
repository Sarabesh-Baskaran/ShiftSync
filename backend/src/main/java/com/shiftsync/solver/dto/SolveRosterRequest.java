package com.shiftsync.solver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request payload to initiate a Timefold roster optimization run.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolveRosterRequest {

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    /**
     * Optional filter to only solve shifts in specific departments.
     * If null or empty, solves for the entire hospital.
     */
    private List<Long> departmentIds;

    /**
     * Maximum time (in seconds) the solver is allowed to optimize.
     * Default is 30 seconds for deep, balanced schedules; 5-10s for quick previews.
     */
    @Min(value = 2, message = "Solver timeout must be at least 2 seconds")
    @Max(value = 300, message = "Solver timeout cannot exceed 300 seconds (5 minutes)")
    @Builder.Default
    private int timeoutSeconds = 30;

    /**
     * Whether to execute pre-solve capacity validation before initiating solving.
     */
    @Builder.Default
    private boolean validateCapacityFirst = true;
}
