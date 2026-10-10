package com.shiftsync.service;

import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.enums.BurnoutRiskLevel;
import com.shiftsync.domain.enums.DepartmentType;
import com.shiftsync.domain.enums.ShiftStatus;
import com.shiftsync.domain.enums.ShiftType;
import com.shiftsync.domain.enums.Skill;
import com.shiftsync.dto.analytics.CognitiveLoadProfileDTO;
import com.shiftsync.dto.analytics.TeamScheduleAnalyticsDTO;
import com.shiftsync.repository.DepartmentRepository;
import com.shiftsync.repository.EmployeeRepository;
import com.shiftsync.repository.ShiftRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CognitiveLoadCalculatorTest {

    @Autowired
    private CognitiveLoadCalculator cognitiveLoadCalculator;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Test
    @DisplayName("Should flag critical risk when rest gap is below 12 hours (e.g., Evening into Morning)")
    void testSevereRestGapViolationDetection() {
        Department general = departmentRepository.save(Department.builder()
                .name("Post-Op Ward")
                .type(DepartmentType.GENERAL_WARD)
                .build());

        Employee nurse = employeeRepository.save(Employee.builder()
                .employeeCode("RN-CALC-01")
                .fullName("Claire Temple, RN")
                .email("claire@hospital.org")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .build());

        LocalDate day1 = LocalDate.of(2026, 10, 12);
        LocalDate day2 = LocalDate.of(2026, 10, 13);

        // Shift 1: Day 1 Evening (15:00 to 23:30)
        Shift eveningShift = shiftRepository.save(Shift.builder()
                .shiftDate(day1)
                .shiftType(ShiftType.EVENING)
                .department(general)
                .assignedEmployee(nurse)
                .status(ShiftStatus.ASSIGNED)
                .build());

        // Shift 2: Day 2 Morning (07:00 to 15:30)
        // Gap = 23:30 to 07:00 = 7.5 hours (severe violation of 12h rule)
        Shift morningShift = shiftRepository.save(Shift.builder()
                .shiftDate(day2)
                .shiftType(ShiftType.MORNING)
                .department(general)
                .assignedEmployee(nurse)
                .status(ShiftStatus.ASSIGNED)
                .build());

        CognitiveLoadProfileDTO profile = cognitiveLoadCalculator.getEmployeeProfile(
                nurse.getId(), day1, day2);

        assertThat(profile.getTotalShifts()).isEqualTo(2);
        assertThat(profile.getShortestRestGapHours()).isEqualTo(7.5);
        assertThat(profile.getRestGapViolations()).hasSize(1);
        assertThat(profile.getRestGapViolations().get(0).isSevere()).isTrue();
        assertThat(profile.getBurnoutRiskLevel()).isEqualTo(BurnoutRiskLevel.CRITICAL);
        assertThat(profile.getRiskFactors()).anyMatch(factor -> factor.contains("Critical rest deficit"));
    }

    @Test
    @DisplayName("Should calculate team analytics and compute fairness score")
    void testTeamAnalyticsAndFairnessCalculation() {
        Department icu = departmentRepository.save(Department.builder()
                .name("Trauma ICU")
                .type(DepartmentType.ICU)
                .build());

        Employee nurseA = employeeRepository.save(Employee.builder()
                .employeeCode("RN-CALC-02")
                .fullName("John Dorian")
                .email("jd@hospital.org")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .build());

        Employee nurseB = employeeRepository.save(Employee.builder()
                .employeeCode("RN-CALC-03")
                .fullName("Christopher Turk")
                .email("turk@hospital.org")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .build());

        LocalDate today = LocalDate.of(2026, 10, 15);

        // Assign one shift to Nurse A and one shift to Nurse B
        shiftRepository.save(Shift.builder()
                .shiftDate(today)
                .shiftType(ShiftType.MORNING)
                .department(icu)
                .assignedEmployee(nurseA)
                .status(ShiftStatus.ASSIGNED)
                .build());

        shiftRepository.save(Shift.builder()
                .shiftDate(today)
                .shiftType(ShiftType.MORNING)
                .department(icu)
                .assignedEmployee(nurseB)
                .status(ShiftStatus.ASSIGNED)
                .build());

        TeamScheduleAnalyticsDTO teamAnalytics = cognitiveLoadCalculator.calculateTeamAnalytics(
                today, today, null);

        assertThat(teamAnalytics.getTotalShiftsScheduled()).isGreaterThanOrEqualTo(2);
        assertThat(teamAnalytics.getStaffProfiles()).isNotEmpty();
        // Since load is balanced between A and B, fairness score should be high
        assertThat(teamAnalytics.getFairnessScore()).isGreaterThan(0.0);
    }
}
