package com.shiftsync.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Risk classification for staff cognitive burnout and fatigue.
 * Drives visual UI heat map colors (Green, Yellow, Orange, Red)
 * and helps the scheduler prioritize rest rebalancing.
 */
@Getter
@RequiredArgsConstructor
public enum BurnoutRiskLevel {
    LOW("Low Risk", "#10B981", "Optimal cognitive balance with adequate recovery gaps."),
    MODERATE("Moderate Risk", "#F59E0B", "Elevated workload or mild sleep deficit. Monitoring recommended."),
    HIGH("High Risk", "#F97316", "Significant fatigue accumulation or consecutive intensive shifts. Rebalance advised."),
    CRITICAL("Critical Burnout Risk", "#EF4444", "Mandatory rest required: severe rest-gap violation or extreme cognitive load.");

    private final String label;
    private final String colorHex;
    private final String description;
}
