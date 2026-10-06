package com.shiftsync.dto.shift;

import com.shiftsync.domain.enums.ShiftType;
import com.shiftsync.domain.enums.Skill;
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
public class CreateShiftRequest {

    @NotNull(message = "Shift date is required")
    private LocalDate shiftDate;

    @NotNull(message = "Shift type is required")
    private ShiftType shiftType;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private Long employeeId;

    private Skill requiredSkill;
}
