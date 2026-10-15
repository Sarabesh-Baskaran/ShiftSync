package com.shiftsync.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should seed hospital data via POST /api/demo/seed")
    void testSeedDataEndpoint() throws Exception {
        mockMvc.perform(post("/api/demo/seed?forceReset=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.departmentsCount").value(5))
                .andExpect(jsonPath("$.staffCount").value(24));
    }

    @Test
    @DisplayName("Should return database status metrics via GET /api/demo/status")
    void testGetStatusEndpoint() throws Exception {
        mockMvc.perform(get("/api/demo/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentsCount").isNumber())
                .andExpect(jsonPath("$.staffCount").isNumber());
    }

    @Test
    @DisplayName("Should reset database via DELETE /api/demo/reset")
    void testResetEndpoint() throws Exception {
        mockMvc.perform(delete("/api/demo/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
