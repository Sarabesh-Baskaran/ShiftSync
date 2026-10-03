package com.shiftsync.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Hospital department types with associated stress multipliers.
 * High-intensity wards (ICU, Emergency, Surgery) generate significantly higher cognitive load.
 */
@Getter
@RequiredArgsConstructor
public enum DepartmentType {
    GENERAL_WARD("General Medical Ward", 1.0),
    PEDIATRICS("Pediatrics Unit", 1.4),
    EMERGENCY("Emergency Department (ER)", 2.5),
    SURGERY("Surgical / Post-Op Care", 2.8),
    ICU("Intensive Care Unit (ICU)", 3.0);

    private final String displayName;
    private final double stressMultiplier;

    public boolean isHighStress() {
        return this.stressMultiplier >= 2.5;
    }
}
