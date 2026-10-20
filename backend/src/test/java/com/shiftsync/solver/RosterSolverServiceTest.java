package com.shiftsync.solver;

import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.enums.DepartmentType;
import com.shiftsync.domain.enums.ShiftType;
import com.shiftsync.domain.enums.Skill;
import com.shiftsync.repository.DepartmentRepository;
import com.shiftsync.repository.EmployeeRepository;
import com.shiftsync.repository.ShiftRepository;
import com.shiftsync.solver.dto.SolverJobResponse;
import com.shiftsync.solver.service.RosterSolverService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RosterSolverServiceTest {

    @Autowired
    private RosterSolverService rosterSolverService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Test
    @DisplayName("Should initiate solver job and return active job status")
    void testStartSolvingAndGetStatus() {
        Department ward = departmentRepository.save(Department.builder()
                .name("Oncology Unit")
                .type(DepartmentType.GENERAL_WARD)
                .minStaffPerShift(1)
                .build());

        Employee nurse = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-SOLV-01")
                .fullName("Solver Test Nurse")
                .email("solver.test@hospital.org")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .build());

        LocalDate today = LocalDate.of(2026, 11, 1);

        shiftRepository.save(Shift.builder()
                .shiftDate(today)
                .shiftType(ShiftType.MORNING)
                .department(ward)
                .build());

        SolverJobResponse job = rosterSolverService.startSolving(today, today);

        assertThat(job).isNotNull();
        assertThat(job.getJobId()).isNotEmpty();
        assertThat(job.getTotalShifts()).isEqualTo(1);

        // Fetch status
        SolverJobResponse status = rosterSolverService.getJobStatus(job.getJobId());
        assertThat(status.getJobId()).isEqualTo(job.getJobId());

        // Terminate solver cleanly
        rosterSolverService.terminateEarly(job.getJobId());
    }
}
