package com.shiftsync.dto.employee;

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
public class UpdateEmployeeRequest {
    private String fullName;
    private String email;
    private String phoneNumber;
    private Set<Skill> skills;
    private Integer maxConsecutiveHighStressShifts;
    private Integer maxWeeklyHours;
    private Double currentFatigueFactor;
    private Boolean active;
}
