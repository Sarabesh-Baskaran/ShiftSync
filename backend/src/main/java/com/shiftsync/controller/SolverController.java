package com.shiftsync.controller;

import com.shiftsync.solver.dto.CapacityAnalysisDTO;
import com.shiftsync.solver.dto.SolveRosterRequest;
import com.shiftsync.solver.dto.SolverJobResponse;
import com.shiftsync.solver.service.RosterSolverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/roster")
@RequiredArgsConstructor
public class SolverController {

    private final RosterSolverService rosterSolverService;

    @PostMapping("/solve")
    public ResponseEntity<SolverJobResponse> solveRoster(
            @RequestBody(required = false) @Valid SolveRosterRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (request != null) {
            return ResponseEntity.ok(rosterSolverService.startSolving(request));
        }

        LocalDate start = (startDate != null) ? startDate : LocalDate.now();
        LocalDate end = (endDate != null) ? endDate : start.plusDays(6);

        SolveRosterRequest fallbackRequest = SolveRosterRequest.builder()
                .startDate(start)
                .endDate(end)
                .timeoutSeconds(30)
                .validateCapacityFirst(true)
                .build();

        return ResponseEntity.ok(rosterSolverService.startSolving(fallbackRequest));
    }

    @GetMapping("/validate-capacity")
    public ResponseEntity<CapacityAnalysisDTO> validateCapacity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<Long> departmentIds) {

        LocalDate start = (startDate != null) ? startDate : LocalDate.now();
        LocalDate end = (endDate != null) ? endDate : start.plusDays(6);

        return ResponseEntity.ok(rosterSolverService.validateCapacityOnly(start, end, departmentIds));
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
