package com.shiftsync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiftsync.domain.enums.Skill;
import com.shiftsync.dto.employee.CreateEmployeeRequest;
import com.shiftsync.service.StaffService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StaffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StaffService staffService;

    @Test
    @DisplayName("Should create employee via REST API and return 201 Created")
    void testCreateEmployeeEndpoint() throws Exception {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .employeeCode("EMP-API-01")
                .fullName("Marcus Aurelius, RN")
                .email("marcus.a@hospital.org")
                .phoneNumber("555-0199")
                .skills(Set.of(Skill.REGISTERED_NURSE, Skill.EMERGENCY_TRAINED))
                .maxConsecutiveHighStressShifts(2)
                .maxWeeklyHours(40)
                .build();

        mockMvc.perform(post("/api/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.employeeCode").value("EMP-API-01"))
                .andExpect(jsonPath("$.fullName").value("Marcus Aurelius, RN"))
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request body has validation errors")
    void testCreateEmployeeValidationFailure() throws Exception {
        CreateEmployeeRequest invalidRequest = CreateEmployeeRequest.builder()
                .employeeCode("") // Invalid: blank
                .fullName("")     // Invalid: blank
                .email("not-an-email") // Invalid email format
                .build();

        mockMvc.perform(post("/api/staff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors.employeeCode").isNotEmpty())
                .andExpect(jsonPath("$.validationErrors.email").isNotEmpty());
    }

    @Test
    @DisplayName("Should list all active employees")
    void testGetAllActiveEmployeesEndpoint() throws Exception {
        staffService.createEmployee(CreateEmployeeRequest.builder()
                .employeeCode("EMP-API-02")
                .fullName("Florence Nightingale")
                .email("florence@hospital.org")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .build());

        mockMvc.perform(get("/api/staff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
