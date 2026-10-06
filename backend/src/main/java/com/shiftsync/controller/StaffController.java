package com.shiftsync.controller;

import com.shiftsync.dto.employee.CreateEmployeeRequest;
import com.shiftsync.dto.employee.EmployeeResponse;
import com.shiftsync.dto.employee.UpdateEmployeeRequest;
import com.shiftsync.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = staffService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllActiveEmployees() {
        return ResponseEntity.ok(staffService.getAllActiveEmployees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.getEmployeeById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(staffService.updateEmployee(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateEmployee(@PathVariable Long id) {
        staffService.deactivateEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
