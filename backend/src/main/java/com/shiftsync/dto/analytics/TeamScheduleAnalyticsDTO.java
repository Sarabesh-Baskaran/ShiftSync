package com.shiftsync.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamScheduleAnalyticsDTO {
    private LocalDate startDate;
    private LocalDate endDate;

    private int totalStaffEvaluated;
    private int totalShiftsScheduled;
    private double totalTeamCognitiveLoad;

    private double averageLoadPerStaff;
    private double standardDeviation;

    /**
     * Fairness score normalized to 0-100 scale.
     * 100 means perfectly balanced cognitive workload distribution across all staff.
     */
    private double fairnessScore;

    private int lowRiskStaffCount;
    private int moderateRiskStaffCount;
    private int highRiskStaffCount;
    private int criticalRiskStaffCount;

    private List<CognitiveLoadProfileDTO> staffProfiles;
}
