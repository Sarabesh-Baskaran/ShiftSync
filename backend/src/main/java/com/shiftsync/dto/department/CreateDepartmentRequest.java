package com.shiftsync.dto.department;

import com.shiftsync.domain.enums.DepartmentType;
import com.shiftsync.domain.enums.Skill;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    private String name;

    @NotNull(message = "Department type is required")
    private DepartmentType type;

    @Min(value = 1, message = "Minimum staff per shift must be at least 1")
    @Builder.Default
    private int minStaffPerShift = 1;

    private Skill mandatorySkill;
}
