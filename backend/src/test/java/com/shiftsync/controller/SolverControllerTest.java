package com.shiftsync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.enums.DepartmentType;
import com.shiftsync.domain.enums.ShiftType;
import com.shiftsync.domain.enums.Skill;
import com.shiftsync.repository.DepartmentRepository;
import com.shiftsync.repository.EmployeeRepository;
import com.shiftsync.repository.ShiftRepository;
import com.shiftsync.solver.dto.SolveRosterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SolverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Test
    @DisplayName("Should execute dry-run capacity check via GET /api/roster/validate-capacity")
    void testValidateCapacityEndpoint() throws Exception {
        mockMvc.perform(get("/api/roster/validate-capacity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theoreticallyFeasible").isBoolean())
                .andExpect(jsonPath("$.warnings").isArray());
    }

    @Test
    @DisplayName("Should initiate roster solve via POST /api/roster/solve with request body")
    void testSolveRosterEndpoint() throws Exception {
        Department ward = departmentRepository.save(Department.builder()
                .name("Cardiology Step-down")
                .type(DepartmentType.GENERAL_WARD)
                .build());

        employeeRepository.save(Employee.builder()
                .employeeCode("RN-SOLV-API")
                .fullName("Test Nurse")
                .email("solv.api@hospital.org")
                .skills(Set.of(Skill.REGISTERED_NURSE))
                .build());

        LocalDate today = LocalDate.of(2026, 11, 10);

        shiftRepository.save(Shift.builder()
                .shiftDate(today)
                .shiftType(ShiftType.MORNING)
                .department(ward)
                .build());

        SolveRosterRequest request = SolveRosterRequest.builder()
                .startDate(today)
                .endDate(today)
                .timeoutSeconds(10)
                .validateCapacityFirst(true)
                .build();

        mockMvc.perform(post("/api/roster/solve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("SOLVING"))
                .andExpect(jsonPath("$.capacityAnalysis").isNotEmpty());
    }
}
