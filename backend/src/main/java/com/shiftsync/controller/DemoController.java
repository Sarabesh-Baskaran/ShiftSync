package com.shiftsync.controller;

import com.shiftsync.service.DataSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DataSeederService dataSeederService;

    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seedData(
            @RequestParam(defaultValue = "false") boolean forceReset) {
        return ResponseEntity.ok(dataSeederService.seedHospitalData(forceReset));
    }

    @DeleteMapping("/reset")
    public ResponseEntity<Map<String, String>> resetData() {
        dataSeederService.resetAllData();
        return ResponseEntity.ok(Map.of("message", "All ShiftSync database tables have been successfully cleared."));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(dataSeederService.getDatabaseStatus());
    }
}
