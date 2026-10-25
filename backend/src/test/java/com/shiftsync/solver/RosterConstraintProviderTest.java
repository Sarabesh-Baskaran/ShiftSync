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
    @DisplayName("Should penalize when employee is assigned more than one shift on the same day")
    void testAtMostOneShiftPerDay() {
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
                .shiftType(ShiftType.EVENING) // Different shift type, but same date!
                .department(wardB)
                .assignedEmployee(nurse)
                .build();

        constraintVerifier.verifyThat(RosterConstraintProvider::atMostOneShiftPerDay)
                .given(shift1, shift2)
                .penalizesBy(1);
    }

    @Test
    @DisplayName("Should penalize 12-hour rest gap violation (e.g. Evening into Morning clopening)")
    void testMinimumRestGapBetweenShifts() {
        Department general = Department.builder().id(1L).name("General").type(DepartmentType.GENERAL_WARD).build();

        Employee nurse = Employee.builder()
                .id(104L)
                .employeeCode("RN-REST")
                .fullName("Tired Nurse")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .build();

        LocalDate day1 = LocalDate.of(2026, 10, 20);
        LocalDate day2 = LocalDate.of(2026, 10, 21);

        // Day 1 Evening: 15:00 to 23:30
        Shift eveningShift = Shift.builder()
                .id(1L)
                .shiftDate(day1)
                .shiftType(ShiftType.EVENING)
                .department(general)
                .assignedEmployee(nurse)
                .build();

        // Day 2 Morning: 07:00 to 15:30 -> Rest gap is 7.5 hours (deficit: 12 - 7.5 = 4.5 -> 5 hours)
        Shift morningShift = Shift.builder()
                .id(2L)
                .shiftDate(day2)
                .shiftType(ShiftType.MORNING)
                .department(general)
                .assignedEmployee(nurse)
                .build();

        constraintVerifier.verifyThat(RosterConstraintProvider::minimumRestGapBetweenShifts)
                .given(eveningShift, morningShift)
                .penalizesBy(5); // 5 hours deficit penalty

        // Day 3 Morning (24h+ later) -> Zero rest gap penalty with Day 1
        LocalDate day3 = LocalDate.of(2026, 10, 22);
        Shift legalShift = Shift.builder()
                .id(3L)
                .shiftDate(day3)
                .shiftType(ShiftType.MORNING)
                .department(general)
                .assignedEmployee(nurse)
                .build();

        constraintVerifier.verifyThat(RosterConstraintProvider::minimumRestGapBetweenShifts)
                .given(eveningShift, legalShift)
                .penalizesBy(0);
    }

    @Test
    @DisplayName("Should penalize weekly overtime hours exceeding employee maximum limit")
    void testMaxWeeklyWorkingHours() {
        Department general = Department.builder().id(1L).name("General").type(DepartmentType.GENERAL_WARD).build();

        Employee nurse = Employee.builder()
                .id(105L)
                .employeeCode("RN-OT")
                .fullName("Overtime Nurse")
                .maxWeeklyHours(40) // 40h limit
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .build();

        // 5 shifts of 8.5 hours = 42.5 hours (rounds to 9h each or sum is 43 hours -> 3 hours overtime)
        Shift s1 = Shift.builder().id(1L).shiftDate(LocalDate.of(2026, 10, 19)).shiftType(ShiftType.MORNING).department(general).assignedEmployee(nurse).build();
        Shift s2 = Shift.builder().id(2L).shiftDate(LocalDate.of(2026, 10, 20)).shiftType(ShiftType.MORNING).department(general).assignedEmployee(nurse).build();
        Shift s3 = Shift.builder().id(3L).shiftDate(LocalDate.of(2026, 10, 21)).shiftType(ShiftType.MORNING).department(general).assignedEmployee(nurse).build();
        Shift s4 = Shift.builder().id(4L).shiftDate(LocalDate.of(2026, 10, 22)).shiftType(ShiftType.MORNING).department(general).assignedEmployee(nurse).build();
        Shift s5 = Shift.builder().id(5L).shiftDate(LocalDate.of(2026, 10, 23)).shiftType(ShiftType.MORNING).department(general).assignedEmployee(nurse).build();

        // Sum: 5 * 9 = 45 -> overtime = 45 - 40 = 5 hours
        constraintVerifier.verifyThat(RosterConstraintProvider::maxWeeklyWorkingHours)
                .given(s1, s2, s3, s4, s5)
                .penalizesBy(5);
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

    @Test
    @DisplayName("Should penalize 3 or more consecutive high-stress shifts (ICU)")
    void testExceededConsecutiveHighStressShifts() {
        Department icu = Department.builder().id(1L).name("ICU").type(DepartmentType.ICU).build();

        Employee nurse = Employee.builder()
                .id(106L)
                .employeeCode("RN-STRESS")
                .fullName("ICU Nurse")
                .skills(Set.of(Skill.REGISTERED_NURSE, Skill.ICU_CERTIFIED))
                .build();

        // 3 consecutive ICU shifts on Day 1, Day 2, Day 3
        Shift day1 = Shift.builder().id(1L).shiftDate(LocalDate.of(2026, 10, 20)).shiftType(ShiftType.MORNING).department(icu).assignedEmployee(nurse).build();
        Shift day2 = Shift.builder().id(2L).shiftDate(LocalDate.of(2026, 10, 21)).shiftType(ShiftType.MORNING).department(icu).assignedEmployee(nurse).build();
        Shift day3 = Shift.builder().id(3L).shiftDate(LocalDate.of(2026, 10, 22)).shiftType(ShiftType.MORNING).department(icu).assignedEmployee(nurse).build();

        // 3 consecutive days triggers the medium penalty
        constraintVerifier.verifyThat(RosterConstraintProvider::exceededConsecutiveHighStressShifts)
                .given(day1, day2, day3)
                .penalizesBy(1);

        // 2 consecutive days (safe limit) incurs zero penalty
        constraintVerifier.verifyThat(RosterConstraintProvider::exceededConsecutiveHighStressShifts)
                .given(day1, day2)
                .penalizesBy(0);
    }

    @Test
    @DisplayName("Should apply quadratic penalty to night shifts to encourage fair distribution")
    void testFairNightShiftDistribution() {
        Department general = Department.builder().id(1L).name("General").type(DepartmentType.GENERAL_WARD).build();

        Employee nurse = Employee.builder()
                .id(107L)
                .employeeCode("RN-NIGHT")
                .fullName("Night Nurse")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .build();

        Shift n1 = Shift.builder().id(1L).shiftDate(LocalDate.of(2026, 10, 20)).shiftType(ShiftType.NIGHT).department(general).assignedEmployee(nurse).build();
        Shift n2 = Shift.builder().id(2L).shiftDate(LocalDate.of(2026, 10, 22)).shiftType(ShiftType.NIGHT).department(general).assignedEmployee(nurse).build();
        Shift n3 = Shift.builder().id(3L).shiftDate(LocalDate.of(2026, 10, 24)).shiftType(ShiftType.NIGHT).department(general).assignedEmployee(nurse).build();

        // 3 night shifts = 3^2 = 9 soft penalty points
        constraintVerifier.verifyThat(RosterConstraintProvider::fairNightShiftDistribution)
                .given(n1, n2, n3)
                .penalizesBy(9);
    }

    @Test
    @DisplayName("Should penalize assigning fatigued staff to high-stress wards")
    void testProtectFatiguedStaffFromHighStress() {
        Department icu = Department.builder().id(1L).name("ICU").type(DepartmentType.ICU).build(); // stressMultiplier = 3.0

        Employee fatiguedNurse = Employee.builder()
                .id(108L)
                .employeeCode("RN-FATIGUE")
                .fullName("Fatigued Staff")
                .currentFatigueFactor(1.8) // High fatigue factor
                .skills(Set.of(Skill.REGISTERED_NURSE, Skill.ICU_CERTIFIED))
                .build();

        Employee restedNurse = Employee.builder()
                .id(109L)
                .employeeCode("RN-RESTED")
                .fullName("Rested Staff")
                .currentFatigueFactor(1.0) // Normal rested factor
                .skills(Set.of(Skill.REGISTERED_NURSE, Skill.ICU_CERTIFIED))
                .build();

        Shift intensiveShiftFatigued = Shift.builder()
                .id(1L)
                .shiftDate(LocalDate.of(2026, 10, 20))
                .shiftType(ShiftType.MORNING)
                .department(icu)
                .assignedEmployee(fatiguedNurse)
                .build();

        Shift intensiveShiftRested = Shift.builder()
                .id(2L)
                .shiftDate(LocalDate.of(2026, 10, 20))
                .shiftType(ShiftType.MORNING)
                .department(icu)
                .assignedEmployee(restedNurse)
                .build();

        // Fatigued nurse penalty: (1.8 - 1.0) * 10 * 3.0 = 24 points
        constraintVerifier.verifyThat(RosterConstraintProvider::protectFatiguedStaffFromHighStress)
                .given(intensiveShiftFatigued)
                .penalizesBy(24);

        // Rested nurse penalty: 0 points
        constraintVerifier.verifyThat(RosterConstraintProvider::protectFatiguedStaffFromHighStress)
                .given(intensiveShiftRested)
                .penalizesBy(0);
    }
}
