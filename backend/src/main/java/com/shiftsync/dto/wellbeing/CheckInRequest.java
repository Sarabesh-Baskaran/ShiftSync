package com.shiftsync.dto.wellbeing;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private LocalDate checkInDate;

    @Min(value = 1, message = "Fatigue score must be between 1 and 5")
    @Max(value = 5, message = "Fatigue score must be between 1 and 5")
    private int fatigueScore;

    @Min(value = 0, message = "Sleep hours cannot be negative")
    @Max(value = 24, message = "Sleep hours cannot exceed 24")
    private double sleepHours;

    @Min(value = 1, message = "Stress level must be between 1 and 5")
    @Max(value = 5, message = "Stress level must be between 1 and 5")
    private int stressLevel;

    private String notes;
}
