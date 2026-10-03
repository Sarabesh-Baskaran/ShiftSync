package com.shiftsync.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Healthcare clinical certifications and competencies.
 * Used by the constraint solver to guarantee that specialized departments
 * (e.g. ICU, Emergency) have staff with mandatory certifications.
 */
@Getter
@RequiredArgsConstructor
public enum Skill {
    REGISTERED_NURSE("Registered Nurse (RN)"),
    ICU_CERTIFIED("ICU Critical Care Certified"),
    EMERGENCY_TRAINED("Emergency & Trauma Trained"),
    PEDIATRIC_CERTIFIED("Pediatric Care Certified"),
    SURGICAL_SPECIALIST("Surgical & Post-Op Specialist"),
    CHARGE_NURSE("Charge Nurse / Team Lead");

    private final String description;
}
