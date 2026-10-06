package com.shiftsync.controller;

import com.shiftsync.domain.enums.ShiftStatus;
import com.shiftsync.dto.shift.CreateShiftRequest;
import com.shiftsync.dto.shift.GenerateRosterSlotsRequest;
import com.shiftsync.dto.shift.ShiftResponse;
import com.shiftsync.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    public ResponseEntity<ShiftResponse> createShift(@Valid @RequestBody CreateShiftRequest request) {
        ShiftResponse response = shiftService.createShift(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/generate-slots")
    public ResponseEntity<List<ShiftResponse>> generateRosterSlots(@Valid @RequestBody GenerateRosterSlotsRequest request) {
        List<ShiftResponse> response = shiftService.generateRosterSlots(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ShiftResponse>> getShifts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long employeeId) {

        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(7);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now().plusDays(14);

        return ResponseEntity.ok(shiftService.getShifts(start, end, departmentId, employeeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftResponse> getShiftById(@PathVariable Long id) {
        return ResponseEntity.ok(shiftService.getShiftById(id));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<ShiftResponse> assignEmployee(
            @PathVariable Long id,
            @RequestParam(required = false) Long employeeId) {
        return ResponseEntity.ok(shiftService.assignEmployee(id, employeeId));
    }

    @PostMapping("/{id}/call-sick")
    public ResponseEntity<ShiftResponse> markSickCallout(@PathVariable Long id) {
        return ResponseEntity.ok(shiftService.markSickCallout(id));
    }
}
