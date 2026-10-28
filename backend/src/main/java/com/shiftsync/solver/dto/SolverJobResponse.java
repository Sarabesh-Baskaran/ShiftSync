package com.shiftsync.solver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolverJobResponse {
    private String jobId;
    private String status;
    private String score;
    private int hardScore;
    private int mediumScore;
    private int softScore;
    private boolean feasible; // true if hardScore >= 0
    private int totalShifts;
    private int assignedShifts;
    private int unassignedShifts;
    private int timeoutSeconds;
    private long solvingDurationMs;
    private Double fairnessScore;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private CapacityAnalysisDTO capacityAnalysis;
    private List<ConstraintExplanationDTO> constraintBreakdown;
    private String scoreSummary;
}
