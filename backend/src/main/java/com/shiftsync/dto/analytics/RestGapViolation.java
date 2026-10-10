package com.shiftsync.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestGapViolation {
    private Long precedingShiftId;
    private LocalDateTime precedingShiftEnd;
    private String precedingDepartment;

    private Long followingShiftId;
    private LocalDateTime followingShiftStart;
    private String followingDepartment;

    private double gapHours;
    private boolean severe; // true if gap < 12 hours (hard legal constraint breach)
}
