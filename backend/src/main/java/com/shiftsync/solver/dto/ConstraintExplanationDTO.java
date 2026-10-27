package com.shiftsync.solver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Details of individual constraint scoring impacts from Timefold's ScoreExplanation engine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConstraintExplanationDTO {
    private String constraintName;
    private String scoreLevel; // HARD, MEDIUM, or SOFT
    private String scoreImpact;
    private int matchCount;
    private String description;
}
