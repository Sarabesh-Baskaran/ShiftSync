package com.shiftsync.dto.employee;

import com.shiftsync.domain.enums.Skill;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {

    @NotBlank(message = "Employee code is required")
    private String employeeCode;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    private String phoneNumber;

    private Set<Skill> skills;

    @Min(value = 1, message = "Max consecutive high stress shifts must be at least 1")
    @Builder.Default
    private int maxConsecutiveHighStressShifts = 2;

    @Min(value = 10, message = "Max weekly hours must be at least 10")
    @Builder.Default
    private int maxWeeklyHours = 40;
}
