package com.shiftsync.domain;

import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.entity.WellbeingCheckIn;
import com.shiftsync.domain.enums.DepartmentType;
import com.shiftsync.domain.enums.ShiftStatus;
import com.shiftsync.domain.enums.ShiftType;
import com.shiftsync.domain.enums.Skill;
import com.shiftsync.repository.DepartmentRepository;
import com.shiftsync.repository.EmployeeRepository;
import com.shiftsync.repository.ShiftRepository;
import com.shiftsync.repository.WellbeingCheckInRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DomainEntityMappingTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private WellbeingCheckInRepository wellbeingCheckInRepository;

    @Test
    @DisplayName("Should persist and retrieve Department with stress metrics")
    void testDepartmentPersistence() {
        Department icu = Department.builder()
                .name("Cardiothoracic ICU")
                .type(DepartmentType.ICU)
                .minStaffPerShift(3)
                .mandatorySkill(Skill.ICU_CERTIFIED)
                .build();

        Department saved = departmentRepository.save(icu);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStressMultiplier()).isEqualTo(3.0);
        assertThat(saved.isHighStress()).isTrue();
    }

    @Test
    @DisplayName("Should persist Employee with clinical skills and validate qualification")
    void testEmployeeSkillsAndQualification() {
        Department icu = departmentRepository.save(Department.builder()
                .name("Neuro ICU")
                .type(DepartmentType.ICU)
                .mandatorySkill(Skill.ICU_CERTIFIED)
                .build());

        Department generalWard = departmentRepository.save(Department.builder()
                .name("Ward 4A")
                .type(DepartmentType.GENERAL_WARD)
                .build());

        Employee nurseAlice = employeeRepository.save(Employee.builder()
                .employeeCode("RN-101")
                .fullName("Alice Walker, RN")
                .email("alice.walker@hospital.org")
                .skills(Set.of(Skill.REGISTERED_NURSE, Skill.ICU_CERTIFIED))
                .maxWeeklyHours(40)
                .build());

        Employee nurseBob = employeeRepository.save(Employee.builder()
                .employeeCode("RN-102")
                .fullName("Bob Miller, RN")
                .email("bob.miller@hospital.org")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .maxWeeklyHours(40)
                .build());

        assertThat(nurseAlice.isQualifiedFor(icu)).isTrue();
        assertThat(nurseBob.isQualifiedFor(icu)).isFalse();
        assertThat(nurseBob.isQualifiedFor(generalWard)).isTrue();
    }

    @Test
    @DisplayName("Should calculate shift end timestamp across midnight and compute cognitive load")
    void testShiftMidnightTransitionAndCognitiveLoad() {
        Department er = departmentRepository.save(Department.builder()
                .name("Emergency Triage")
                .type(DepartmentType.EMERGENCY)
                .mandatorySkill(Skill.EMERGENCY_TRAINED)
                .build());

        Employee nurse = employeeRepository.save(Employee.builder()
                .employeeCode("RN-201")
                .fullName("Sarah Connor")
                .email("sarah.c@hospital.org")
                .skills(Set.of(Skill.EMERGENCY_TRAINED))
                .currentFatigueFactor(1.2)
                .build());

        LocalDate today = LocalDate.of(2026, 10, 10);

        Shift nightShift = shiftRepository.save(Shift.builder()
                .shiftDate(today)
                .shiftType(ShiftType.NIGHT)
                .department(er)
                .assignedEmployee(nurse)
                .status(ShiftStatus.ASSIGNED)
                .build());

        // Test start and end dates (crossing midnight: 23:00 today to 07:30 tomorrow)
        assertThat(nightShift.getStartDateTime()).isEqualTo(LocalDateTime.of(2026, 10, 10, 23, 0));
        assertThat(nightShift.getEndDateTime()).isEqualTo(LocalDateTime.of(2026, 10, 11, 7, 30));
        assertThat(nightShift.getDurationHours()).isEqualTo(8.5);

        // Cognitive load formula: 8.5 (duration) * 2.5 (ER stress) * 2.0 (night circadian) * 1.2 (fatigue)
        double expectedCognitiveLoad = 8.5 * 2.5 * 2.0 * 1.2; // 51.0
        assertThat(nightShift.calculateCognitiveLoadScore()).isEqualTo(expectedCognitiveLoad);
    }

    @Test
    @DisplayName("Should record WellbeingCheckIn and compute dynamic fatigue factor")
    void testWellbeingCheckInDynamicFatigue() {
        Employee nurse = employeeRepository.save(Employee.builder()
                .employeeCode("RN-301")
                .fullName("David Kim")
                .email("david.kim@hospital.org")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .build());

        // Check-in with high fatigue (4/5), high stress (4/5), and sleep deprivation (4.5 hours)
        WellbeingCheckIn checkIn = wellbeingCheckInRepository.save(WellbeingCheckIn.builder()
                .employee(nurse)
                .checkInDate(LocalDate.now())
                .fatigueScore(4)
                .stressLevel(4)
                .sleepHours(4.5)
                .notes("Tough day with critical trauma cases.")
                .build());

        double dynamicFactor = checkIn.computeDynamicFatigueFactor();

        // Baseline: 1.0 + (3 * 0.15) + (3 * 0.10) + (2.5 * 0.12) = 1.0 + 0.45 + 0.30 + 0.30 = 2.05
        assertThat(dynamicFactor).isGreaterThan(1.8);

        // Update employee's active fatigue
        nurse.setCurrentFatigueFactor(dynamicFactor);
        employeeRepository.save(nurse);

        Employee updated = employeeRepository.findById(nurse.getId()).orElseThrow();
        assertThat(updated.getCurrentFatigueFactor()).isEqualTo(dynamicFactor);
    }
}
