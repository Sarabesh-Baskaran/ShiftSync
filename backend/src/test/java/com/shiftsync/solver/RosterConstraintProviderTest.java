package com.shiftsync.solver;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.enums.DepartmentType;
import com.shiftsync.domain.enums.ShiftType;
import com.shiftsync.domain.enums.Skill;
import com.shiftsync.solver.constraints.RosterConstraintProvider;
import com.shiftsync.solver.model.RosterSchedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

class RosterConstraintProviderTest {

    private final ConstraintVerifier<RosterConstraintProvider, RosterSchedule> constraintVerifier =
            ConstraintVerifier.build(new RosterConstraintProvider(), RosterSchedule.class, Shift.class);

    @Test
    @DisplayName("Should penalize assignment if employee lacks required clinical certification")
    void testRequiredClinicalQualificationPenalty() {
        Department icu = Department.builder()
                .id(1L)
                .name("Intensive Care Unit")
                .type(DepartmentType.ICU)
                .mandatorySkill(Skill.ICU_CERTIFIED)
                .build();

        Employee uncertifiedNurse = Employee.builder()
                .id(101L)
                .employeeCode("RN-UN")
                .fullName("Uncertified Nurse")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .build();

        Employee icuNurse = Employee.builder()
                .id(102L)
                .employeeCode("RN-ICU")
                .fullName("ICU Certified Nurse")
                .skills(Set.of(Skill.REGISTERED_NURSE, Skill.ICU_CERTIFIED))
                .build();

        Shift unqualifiedShift = Shift.builder()
                .id(1L)
                .shiftDate(LocalDate.of(2026, 10, 20))
                .shiftType(ShiftType.MORNING)
                .department(icu)
                .assignedEmployee(uncertifiedNurse)
                .build();

        Shift qualifiedShift = Shift.builder()
                .id(2L)
                .shiftDate(LocalDate.of(2026, 10, 20))
                .shiftType(ShiftType.MORNING)
                .department(icu)
                .assignedEmployee(icuNurse)
                .build();

        // Uncertified nurse penalized 1 Hard penalty
        constraintVerifier.verifyThat(RosterConstraintProvider::requiredClinicalQualification)
                .given(unqualifiedShift)
                .penalizesBy(1);

        // Certified nurse has zero penalties
        constraintVerifier.verifyThat(RosterConstraintProvider::requiredClinicalQualification)
                .given(qualifiedShift)
                .penalizesBy(0);
    }

    @Test
    @DisplayName("Should penalize when employee is double booked for the same shift slot")
    void testNoSimultaneousDoubleBooking() {
        Department wardA = Department.builder().id(1L).name("Ward A").type(DepartmentType.GENERAL_WARD).build();
        Department wardB = Department.builder().id(2L).name("Ward B").type(DepartmentType.GENERAL_WARD).build();

        Employee nurse = Employee.builder()
                .id(103L)
                .employeeCode("RN-DB")
                .fullName("Busy Nurse")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .build();

        LocalDate today = LocalDate.of(2026, 10, 20);

        Shift shift1 = Shift.builder()
                .id(1L)
                .shiftDate(today)
                .shiftType(ShiftType.MORNING)
                .department(wardA)
                .assignedEmployee(nurse)
                .build();

        Shift shift2 = Shift.builder()
                .id(2L)
                .shiftDate(today)
                .shiftType(ShiftType.MORNING) // Same date and same shift type!
                .department(wardB)
                .assignedEmployee(nurse)
                .build();

        constraintVerifier.verifyThat(RosterConstraintProvider::noSimultaneousDoubleBooking)
                .given(shift1, shift2)
                .penalizesBy(1);
    }

    @Test
    @DisplayName("Should penalize unassigned shift slot with medium constraint")
    void testMinimizeUnassignedShifts() {
        Department general = Department.builder().id(1L).name("General").type(DepartmentType.GENERAL_WARD).build();

        Shift unassignedSlot = Shift.builder()
                .id(1L)
                .shiftDate(LocalDate.of(2026, 10, 20))
                .shiftType(ShiftType.EVENING)
                .department(general)
                .assignedEmployee(null) // Unassigned
                .build();

        constraintVerifier.verifyThat(RosterConstraintProvider::minimizeUnassignedShifts)
                .given(unassignedSlot)
                .penalizesBy(1);
    }
}
