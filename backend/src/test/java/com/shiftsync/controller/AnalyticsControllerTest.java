package com.shiftsync.controller;

import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.enums.DepartmentType;
import com.shiftsync.domain.enums.Skill;
import com.shiftsync.repository.DepartmentRepository;
import com.shiftsync.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    @DisplayName("Should return staff cognitive profile via REST API")
    void testGetStaffProfileEndpoint() throws Exception {
        Employee employee = employeeRepository.save(Employee.builder()
                .employeeCode("EMP-AN-01")
                .fullName("Gregory House")
                .email("house@hospital.org")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .build());

        mockMvc.perform(get("/api/analytics/staff/" + employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(employee.getId()))
                .andExpect(jsonPath("$.burnoutRiskLevel").isNotEmpty())
                .andExpect(jsonPath("$.riskColorHex").isNotEmpty());
    }

    @Test
    @DisplayName("Should return team schedule analytics via REST API")
    void testGetTeamAnalyticsEndpoint() throws Exception {
        mockMvc.perform(get("/api/analytics/team"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStaffEvaluated").isNumber())
                .andExpect(jsonPath("$.fairnessScore").isNumber())
                .andExpect(jsonPath("$.staffProfiles").isArray());
    }
}
