package com.shiftsync.service;

import com.shiftsync.repository.DepartmentRepository;
import com.shiftsync.repository.EmployeeRepository;
import com.shiftsync.repository.ShiftRepository;
import com.shiftsync.repository.WellbeingCheckInRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DataSeederServiceTest {

    @Autowired
    private DataSeederService dataSeederService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private WellbeingCheckInRepository wellbeingCheckInRepository;

    @Test
    @DisplayName("Should seed departments, medical staff, shifts, and wellbeing check-ins")
    void testSeedHospitalData() {
        Map<String, Object> summary = dataSeederService.seedHospitalData(true);

        assertThat(summary.get("status")).isEqualTo("SUCCESS");
        assertThat((Integer) summary.get("departmentsCount")).isEqualTo(5);
        assertThat((Integer) summary.get("staffCount")).isEqualTo(24);
        assertThat((Integer) summary.get("shiftsScheduledCount")).isGreaterThan(100);
        assertThat((Integer) summary.get("wellbeingCheckInsCount")).isEqualTo(24);

        // Verify database counts directly
        assertThat(departmentRepository.count()).isEqualTo(5);
        assertThat(employeeRepository.count()).isEqualTo(24);
        assertThat(shiftRepository.count()).isGreaterThan(100);
        assertThat(wellbeingCheckInRepository.count()).isEqualTo(24);
    }

    @Test
    @DisplayName("Should wipe all hospital data on reset")
    void testResetAllData() {
        dataSeederService.seedHospitalData(true);
        assertThat(employeeRepository.count()).isGreaterThan(0);

        dataSeederService.resetAllData();

        assertThat(departmentRepository.count()).isZero();
        assertThat(employeeRepository.count()).isZero();
        assertThat(shiftRepository.count()).isZero();
        assertThat(wellbeingCheckInRepository.count()).isZero();
    }
}
