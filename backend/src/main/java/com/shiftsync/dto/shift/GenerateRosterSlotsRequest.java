package com.shiftsync.dto.shift;

import com.shiftsync.domain.enums.ShiftType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateRosterSlotsRequest {

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private List<Long> departmentIds;

    @Builder.Default
    private List<ShiftType> shiftTypes = List.of(ShiftType.MORNING, ShiftType.EVENING, ShiftType.NIGHT);
}
