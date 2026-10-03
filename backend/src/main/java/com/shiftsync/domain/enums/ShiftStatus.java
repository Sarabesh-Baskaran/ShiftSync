package com.shiftsync.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Lifecycle status of an individual hospital shift.
 */
@Getter
@RequiredArgsConstructor
public enum ShiftStatus {
    UNASSIGNED("Unassigned Slot"),
    ASSIGNED("Assigned"),
    COMPLETED("Completed"),
    SICK_CALLOUT("Sick Leave / Emergency Vacancy"),
    SWAP_REQUESTED("Swap Requested by Staff");

    private final String description;
}
