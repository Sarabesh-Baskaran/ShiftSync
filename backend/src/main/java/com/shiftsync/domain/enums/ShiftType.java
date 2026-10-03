package com.shiftsync.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Standard hospital shift windows with circadian rhythm disruption weights.
 * Night shifts carry higher fatigue penalties due to biological clock disruption.
 */
@Getter
@RequiredArgsConstructor
public enum ShiftType {
    MORNING("Morning Shift", LocalTime.of(7, 0), LocalTime.of(15, 30), 8.5, false, 1.0),
    EVENING("Evening Shift", LocalTime.of(15, 0), LocalTime.of(23, 30), 8.5, false, 1.2),
    NIGHT("Night Shift", LocalTime.of(23, 0), LocalTime.of(7, 30), 8.5, true, 2.0);

    private final String displayName;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final double durationHours;
    private final boolean crossesMidnight;
    private final double circadianDisruptionWeight;

    /**
     * Calculates the exact start LocalDateTime for a given calendar date.
     */
    public LocalDateTime getStartDateTime(LocalDate date) {
        return LocalDateTime.of(date, this.startTime);
    }

    /**
     * Calculates the exact end LocalDateTime, correctly advancing by 1 day
     * if the shift crosses midnight (e.g., Night Shift 23:00 to 07:30 next day).
     */
    public LocalDateTime getEndDateTime(LocalDate date) {
        if (this.crossesMidnight) {
            return LocalDateTime.of(date.plusDays(1), this.endTime);
        }
        return LocalDateTime.of(date, this.endTime);
    }
}
