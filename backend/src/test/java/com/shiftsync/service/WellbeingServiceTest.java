package com.shiftsync.service;

import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.enums.Skill;
import com.shiftsync.dto.wellbeing.CheckInRequest;
import com.shiftsync.dto.wellbeing.WellbeingCheckInResponse;
import com.shiftsync.repository.EmployeeRepository;
import com.shiftsync.repository.WellbeingCheckInRepository;
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
class WellbeingServiceTest {

    @Autowired
    private WellbeingService wellbeingService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WellbeingCheckInRepository wellbeingCheckInRepository;

    @Test
    @DisplayName("Should record check-in and dynamically recalculate employee fatigue factor")
    void testRecordCheckInUpdatesEmployeeFatigue() {
        Employee employee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-W01")
                .fullName("Jordan Lee")
                .email("jordan.lee@hospital.org")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .currentFatigueFactor(1.0)
                .build());

        CheckInRequest request = CheckInRequest.builder()
                .employeeId(employee.getId())
                .checkInDate(LocalDate.now())
                .fatigueScore(5)
                .stressLevel(4)
                .sleepHours(4.0)
                .notes("Emergency trauma shift last night was overwhelming.")
                .build();

        WellbeingCheckInResponse response = wellbeingService.recordCheckIn(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getEmployeeId()).isEqualTo(employee.getId());
        assertThat(response.getComputedFatigueFactor()).isGreaterThan(1.8);

        // Verify the employee entity was updated in the DB
        Employee updatedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(updatedEmployee.getCurrentFatigueFactor()).isEqualTo(response.getComputedFatigueFactor());
    }
}
