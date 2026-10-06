package com.shiftsync.controller;

import com.shiftsync.dto.wellbeing.CheckInRequest;
import com.shiftsync.dto.wellbeing.WellbeingCheckInResponse;
import com.shiftsync.service.WellbeingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wellbeing")
@RequiredArgsConstructor
public class WellbeingController {

    private final WellbeingService wellbeingService;

    @PostMapping("/check-in")
    public ResponseEntity<WellbeingCheckInResponse> recordCheckIn(@Valid @RequestBody CheckInRequest request) {
        WellbeingCheckInResponse response = wellbeingService.recordCheckIn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<WellbeingCheckInResponse>> getEmployeeCheckIns(@PathVariable Long employeeId) {
        return ResponseEntity.ok(wellbeingService.getCheckInsForEmployee(employeeId));
    }

    @GetMapping("/today")
    public ResponseEntity<List<WellbeingCheckInResponse>> getTodayCheckIns() {
        return ResponseEntity.ok(wellbeingService.getTodayCheckIns());
    }
}
