package com.shiftsync.solver;

import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.enums.DepartmentType;
import com.shiftsync.domain.enums.ShiftType;
import com.shiftsync.domain.enums.Skill;
import com.shiftsync.solver.dto.CapacityAnalysisDTO;
import com.shiftsync.solver.service.PreSolveCapacityValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PreSolveCapacityValidatorTest {

    private final PreSolveCapacityValidator validator = new PreSolveCapacityValidator();

    @Test
    @DisplayName("Should detect critical bottleneck when 0 staff hold mandatory certification")
    void testDetectCriticalSkillBottleneck() {
        Department icu = Department.builder()
                .id(1L)
                .name("ICU")
                .type(DepartmentType.ICU)
                .mandatorySkill(Skill.ICU_CERTIFIED)
                .build();

        // General nurse only - NOT ICU certified
        List<Employee> staff = List.of(
                Employee.builder()
                        .id(101L)
                        .fullName("General Nurse")
                        .maxWeeklyHours(40)
                        .skills(Set.of(Skill.REGISTERED_NURSE))
                        .build()
        );

        List<Shift> shifts = List.of(
                Shift.builder()
                        .id(1L)
                        .shiftDate(LocalDate.of(2026, 10, 28))
                        .shiftType(ShiftType.MORNING)
                        .department(icu)
                        .requiredSkill(Skill.ICU_CERTIFIED)
                        .build()
        );

        CapacityAnalysisDTO analysis = validator.validateCapacity(staff, shifts);

        assertThat(analysis.isTheoreticallyFeasible()).isFalse();
        assertThat(analysis.getCriticalDeficits()).hasSize(1);
        assertThat(analysis.getCriticalDeficits().get(0)).contains("Critical Skill Bottleneck");
    }

    @Test
    @DisplayName("Should detect capacity deficit when shift demand exceeds total staff hours")
    void testDetectCapacityDeficit() {
        Department general = Department.builder().id(1L).name("Ward").type(DepartmentType.GENERAL_WARD).build();

        // 1 nurse with 20h part-time limit
        List<Employee> staff = List.of(
                Employee.builder()
                        .id(102L)
                        .fullName("Part-time Nurse")
                        .maxWeeklyHours(20)
                        .skills(Set.of(Skill.REGISTERED_NURSE))
                        .build()
        );

        // 4 shifts * 8.5h = 34 hours demand in a 7-day span
        List<Shift> shifts = new ArrayList<>();
        LocalDate start = LocalDate.of(2026, 10, 20);
        for (int i = 0; i < 4; i++) {
            shifts.add(Shift.builder()
                    .id((long) (i + 1))
                    .shiftDate(start.plusDays(i))
                    .shiftType(ShiftType.MORNING)
                    .department(general)
                    .build());
        }

        CapacityAnalysisDTO analysis = validator.validateCapacity(staff, shifts);

        assertThat(analysis.isTheoreticallyFeasible()).isFalse();
        assertThat(analysis.getWarnings()).anyMatch(w -> w.contains("Capacity Deficit"));
        assertThat(analysis.getCapacityBufferHours()).isLessThan(0.0);
    }

    @Test
    @DisplayName("Should confirm feasibility when staffing capacity and skills have adequate buffer")
    void testHealthyStaffingCapacity() {
        Department general = Department.builder().id(1L).name("Ward").type(DepartmentType.GENERAL_WARD).build();

        List<Employee> staff = List.of(
                Employee.builder().id(1L).fullName("Nurse 1").maxWeeklyHours(40).skills(Set.of(Skill.REGISTERED_NURSE)).build(),
                Employee.builder().id(2L).fullName("Nurse 2").maxWeeklyHours(40).skills(Set.of(Skill.REGISTERED_NURSE)).build(),
                Employee.builder().id(3L).fullName("Nurse 3").maxWeeklyHours(40).skills(Set.of(Skill.REGISTERED_NURSE)).build()
        );

        // 5 shifts * 8.5h = 42.5h demand vs 120h capacity
        List<Shift> shifts = new ArrayList<>();
        LocalDate start = LocalDate.of(2026, 10, 20);
        for (int i = 0; i < 5; i++) {
            shifts.add(Shift.builder()
                    .id((long) (i + 1))
                    .shiftDate(start.plusDays(i))
                    .shiftType(ShiftType.MORNING)
                    .department(general)
                    .build());
        }

        CapacityAnalysisDTO analysis = validator.validateCapacity(staff, shifts);

        assertThat(analysis.isTheoreticallyFeasible()).isTrue();
        assertThat(analysis.getCriticalDeficits()).isEmpty();
        assertThat(analysis.getStaffingRatio()).isGreaterThan(2.0);
    }
}
