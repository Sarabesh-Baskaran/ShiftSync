package com.shiftsync.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Daily subjective and objective well-being check-in reported by staff.
 * Informs the cognitive load engine and triggers automatic schedule adaptations.
 */
@Entity
@Table(name = "wellbeing_checkins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WellbeingCheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Column(nullable = false)
    private LocalDate checkInDate;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    /**
     * Subjective fatigue rating on a scale of 1 (Fully Energized) to 5 (Exhausted / Burned Out).
     */
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int fatigueScore;

    /**
     * Hours of uninterrupted sleep obtained in the prior 24 hours.
     */
    @Min(0)
    @Max(24)
    @Column(nullable = false)
    private double sleepHours;

    /**
     * Subjective psychological stress rating from 1 (Calm) to 5 (Overwhelmed).
     */
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int stressLevel;

    @Column(length = 500)
    private String notes;

    /**
     * Computes the dynamic fatigue multiplier derived from this check-in.
     * Healthy baseline (fatigue=1, stress=1, sleep>=7.5h) yields 1.0.
     * Severe sleep deprivation and high stress scale up to ~2.5x cognitive vulnerability.
     */
    public double computeDynamicFatigueFactor() {
        // Base factor
        double factor = 1.0;

        // Fatigue penalty: +0.15 for each point above 1
        factor += (fatigueScore - 1) * 0.15;

        // Stress penalty: +0.10 for each point above 1
        factor += (stressLevel - 1) * 0.10;

        // Sleep deficit penalty: if less than 7 hours, add penalty for lost sleep
        if (sleepHours < 7.0) {
            factor += (7.0 - Math.max(0.0, sleepHours)) * 0.12;
        }

        return Math.max(0.8, factor); // Minimum clamp 0.8
    }
}
