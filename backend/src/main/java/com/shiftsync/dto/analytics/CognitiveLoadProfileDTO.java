package com.shiftsync.dto.analytics;

import com.shiftsync.domain.enums.BurnoutRiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CognitiveLoadProfileDTO {
    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private double currentFatigueFactor;

    private int totalShifts;
    private double totalHoursWorked;
    private double totalCognitiveLoadScore;
    private double averageShiftLoad;

    private int maxConsecutiveHighStressShifts;
    private double shortestRestGapHours;

    private BurnoutRiskLevel burnoutRiskLevel;
    private String riskColorHex;
    private List<String> riskFactors;
    private List<RestGapViolation> restGapViolations;
}
