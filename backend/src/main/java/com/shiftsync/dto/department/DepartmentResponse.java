package com.shiftsync.dto.department;

import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.enums.DepartmentType;
import com.shiftsync.domain.enums.Skill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String name;
    private DepartmentType type;
    private String displayName;
    private double stressMultiplier;
    private boolean highStress;
    private int minStaffPerShift;
    private Skill mandatorySkill;

    public static DepartmentResponse fromEntity(Department entity) {
        if (entity == null) return null;
        return DepartmentResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .displayName(entity.getType() != null ? entity.getType().getDisplayName() : null)
                .stressMultiplier(entity.getStressMultiplier())
                .highStress(entity.isHighStress())
                .minStaffPerShift(entity.getMinStaffPerShift())
                .mandatorySkill(entity.getMandatorySkill())
                .build();
    }
}
