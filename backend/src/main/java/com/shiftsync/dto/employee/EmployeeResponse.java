package com.shiftsync.dto.employee;

import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.enums.Skill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Set<Skill> skills;
    private int maxConsecutiveHighStressShifts;
    private int maxWeeklyHours;
    private double currentFatigueFactor;
    private boolean active;

    public static EmployeeResponse fromEntity(Employee entity) {
        if (entity == null) return null;
        return EmployeeResponse.builder()
                .id(entity.getId())
                .employeeCode(entity.getEmployeeCode())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .skills(entity.getSkills())
                .maxConsecutiveHighStressShifts(entity.getMaxConsecutiveHighStressShifts())
                .maxWeeklyHours(entity.getMaxWeeklyHours())
                .currentFatigueFactor(entity.getCurrentFatigueFactor())
                .active(entity.isActive())
                .build();
    }
}
