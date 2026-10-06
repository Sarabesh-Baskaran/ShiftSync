package com.shiftsync.dto.shift;

import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.enums.DepartmentType;
import com.shiftsync.domain.enums.ShiftStatus;
import com.shiftsync.domain.enums.ShiftType;
import com.shiftsync.domain.enums.Skill;
import com.shiftsync.dto.department.DepartmentResponse;
import com.shiftsync.dto.employee.EmployeeResponse;
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
public class ShiftResponse {
    private Long id;
    private LocalDate shiftDate;
    private ShiftType shiftType;
    private String shiftTypeName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private double durationHours;
    private DepartmentResponse department;
    private EmployeeResponse assignedEmployee;
    private Skill requiredSkill;
    private ShiftStatus status;
    private boolean highStress;
    private double cognitiveLoadScore;

    public static ShiftResponse fromEntity(Shift entity) {
        if (entity == null) return null;
        return ShiftResponse.builder()
                .id(entity.getId())
                .shiftDate(entity.getShiftDate())
                .shiftType(entity.getShiftType())
                .shiftTypeName(entity.getShiftType() != null ? entity.getShiftType().getDisplayName() : null)
                .startDateTime(entity.getStartDateTime())
                .endDateTime(entity.getEndDateTime())
                .durationHours(entity.getDurationHours())
                .department(DepartmentResponse.fromEntity(entity.getDepartment()))
                .assignedEmployee(EmployeeResponse.fromEntity(entity.getAssignedEmployee()))
                .requiredSkill(entity.getRequiredSkill())
                .status(entity.getStatus())
                .highStress(entity.isHighStress())
                .cognitiveLoadScore(entity.calculateCognitiveLoadScore())
                .build();
    }
}
