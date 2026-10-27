package com.shiftsync.solver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Pre-solve capacity and feasibility analysis comparing hospital staffing supply
 * against scheduled shift demand.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapacityAnalysisDTO {
    private boolean theoreticallyFeasible;
    private int totalActiveStaff;
    private int totalShiftsRequired;
    private double totalHoursDemand;
    private double totalStaffCapacityHours;
    private double capacityBufferHours; // positive = surplus, negative = deficit
    private double staffingRatio; // Capacity / Demand (e.g. 1.25 = healthy 25% buffer)

    private List<String> warnings;
    private List<String> criticalDeficits;
    private Map<String, Integer> staffCountBySkill;
    private Map<String, Integer> shiftCountByRequiredSkill;
}
