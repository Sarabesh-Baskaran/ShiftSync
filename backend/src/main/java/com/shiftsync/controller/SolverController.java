package com.shiftsync.controller;

import com.shiftsync.solver.dto.SolverJobResponse;
import com.shiftsync.solver.service.RosterSolverService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/roster")
@RequiredArgsConstructor
public class SolverController {

    private final RosterSolverService rosterSolverService;

    @PostMapping("/solve")
    public ResponseEntity<SolverJobResponse> solveRoster(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate start = (startDate != null) ? startDate : LocalDate.now();
        LocalDate end = (endDate != null) ? endDate : start.plusDays(6);

        return ResponseEntity.ok(rosterSolverService.startSolving(start, end));
    }

    @GetMapping("/status/{jobId}")
    public ResponseEntity<SolverJobResponse> getSolverStatus(@PathVariable String jobId) {
        return ResponseEntity.ok(rosterSolverService.getJobStatus(jobId));
    }

    @PostMapping("/stop/{jobId}")
    public ResponseEntity<Void> stopSolving(@PathVariable String jobId) {
        rosterSolverService.terminateEarly(jobId);
        return ResponseEntity.noContent().build();
    }
}
