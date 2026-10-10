package com.shiftsync.controller;

import com.shiftsync.dto.analytics.CognitiveLoadProfileDTO;
import com.shiftsync.dto.analytics.TeamScheduleAnalyticsDTO;
import com.shiftsync.service.CognitiveLoadCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final CognitiveLoadCalculator cognitiveLoadCalculator;

    @GetMapping("/staff/{employeeId}")
    public ResponseEntity<CognitiveLoadProfileDTO> getStaffCognitiveProfile(
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(7);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now().plusDays(14);

        return ResponseEntity.ok(cognitiveLoadCalculator.getEmployeeProfile(employeeId, start, end));
    }

    @GetMapping("/team")
    public ResponseEntity<TeamScheduleAnalyticsDTO> getTeamAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long departmentId) {

        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(7);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now().plusDays(14);

        return ResponseEntity.ok(cognitiveLoadCalculator.calculateTeamAnalytics(start, end, departmentId));
    }
}
