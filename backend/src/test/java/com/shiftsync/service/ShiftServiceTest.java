package com.shiftsync.service;

import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.enums.DepartmentType;
import com.shiftsync.domain.enums.ShiftStatus;
import com.shiftsync.domain.enums.ShiftType;
import com.shiftsync.domain.enums.Skill;
import com.shiftsync.dto.shift.CreateShiftRequest;
import com.shiftsync.dto.shift.GenerateRosterSlotsRequest;
import com.shiftsync.dto.shift.ShiftResponse;
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
class ShiftServiceTest {

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Test
    @DisplayName("Should generate unassigned shift slots across date range for all departments")
    void testGenerateRosterSlots() {
        Department icu = departmentRepository.save(Department.builder()
                .name("General ICU")
                .type(DepartmentType.ICU)
                .minStaffPerShift(2)
                .mandatorySkill(Skill.ICU_CERTIFIED)
                .build());

        Department general = departmentRepository.save(Department.builder()
                .name("Ward 2B")
                .type(DepartmentType.GENERAL_WARD)
                .minStaffPerShift(1)
                .build());

        LocalDate start = LocalDate.of(2026, 10, 10);
        LocalDate end = LocalDate.of(2026, 10, 11); // 2 days

        GenerateRosterSlotsRequest request = GenerateRosterSlotsRequest.builder()
                .startDate(start)
                .endDate(end)
                .departmentIds(List.of(icu.getId(), general.getId()))
                .shiftTypes(List.of(ShiftType.MORNING, ShiftType.EVENING, ShiftType.NIGHT))
                .build();

        List<ShiftResponse> generated = shiftService.generateRosterSlots(request);

        // ICU: 2 days * 3 shift types * 2 staff = 12 slots
        // General: 2 days * 3 shift types * 1 staff = 6 slots
        // Total = 18 slots
        assertThat(generated).hasSize(18);
        assertThat(generated).allMatch(s -> s.getStatus() == ShiftStatus.UNASSIGNED);
    }

    @Test
    @DisplayName("Should handle sick callout by unassigning employee and setting status")
    void testMarkSickCallout() {
        Department general = departmentRepository.save(Department.builder()
                .name("Ward 3C")
                .type(DepartmentType.GENERAL_WARD)
                .build());

        Employee employee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-S01")
                .fullName("Elena Rostova")
                .email("elena.r@hospital.org")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .build());

        ShiftResponse shift = shiftService.createShift(CreateShiftRequest.builder()
                .shiftDate(LocalDate.now().plusDays(1))
                .shiftType(ShiftType.MORNING)
                .departmentId(general.getId())
                .employeeId(employee.getId())
                .build());

        assertThat(shift.getStatus()).isEqualTo(ShiftStatus.ASSIGNED);

        ShiftResponse callout = shiftService.markSickCallout(shift.getId());

        assertThat(callout.getStatus()).isEqualTo(ShiftStatus.SICK_CALLOUT);
        assertThat(callout.getAssignedEmployee()).isNull();
    }
}
