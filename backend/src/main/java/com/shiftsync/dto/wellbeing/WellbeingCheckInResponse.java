package com.shiftsync.dto.wellbeing;

import com.shiftsync.domain.entity.WellbeingCheckIn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WellbeingCheckInResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate checkInDate;
    private LocalDateTime recordedAt;
    private int fatigueScore;
    private double sleepHours;
    private int stressLevel;
    private String notes;
    private double computedFatigueFactor;

    public static WellbeingCheckInResponse fromEntity(WellbeingCheckIn entity) {
        if (entity == null) return null;
        return WellbeingCheckInResponse.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(entity.getEmployee() != null ? entity.getEmployee().getFullName() : null)
                .checkInDate(entity.getCheckInDate())
                .recordedAt(entity.getRecordedAt())
                .fatigueScore(entity.getFatigueScore())
                .sleepHours(entity.getSleepHours())
                .stressLevel(entity.getStressLevel())
                .notes(entity.getNotes())
                .computedFatigueFactor(entity.computeDynamicFatigueFactor())
                .build();
    }
}
