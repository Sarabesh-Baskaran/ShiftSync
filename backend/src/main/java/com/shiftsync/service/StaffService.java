package com.shiftsync.service;

import com.shiftsync.common.exception.ResourceNotFoundException;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.dto.employee.CreateEmployeeRequest;
import com.shiftsync.dto.employee.EmployeeResponse;
import com.shiftsync.dto.employee.UpdateEmployeeRequest;
import com.shiftsync.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final EmployeeRepository employeeRepository;

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        if (employeeRepository.findByEmployeeCode(request.getEmployeeCode()).isPresent()) {
            throw new IllegalArgumentException("Employee with code '" + request.getEmployeeCode() + "' already exists");
        }
        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Employee with email '" + request.getEmail() + "' already exists");
        }

        Employee employee = Employee.builder()
                .employeeCode(request.getEmployeeCode())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .skills(request.getSkills() != null ? new HashSet<>(request.getSkills()) : new HashSet<>())
                .maxConsecutiveHighStressShifts(request.getMaxConsecutiveHighStressShifts())
                .maxWeeklyHours(request.getMaxWeeklyHours())
                .currentFatigueFactor(1.0)
                .active(true)
                .build();

        Employee saved = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return EmployeeResponse.fromEntity(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllActiveEmployees() {
        return employeeRepository.findByActiveTrue().stream()
                .map(EmployeeResponse::fromEntity)
                .toList();
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (request.getFullName() != null) employee.setFullName(request.getFullName());
        if (request.getEmail() != null) employee.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null) employee.setPhoneNumber(request.getPhoneNumber());
        if (request.getSkills() != null) employee.setSkills(new HashSet<>(request.getSkills()));
        if (request.getMaxConsecutiveHighStressShifts() != null) {
            employee.setMaxConsecutiveHighStressShifts(request.getMaxConsecutiveHighStressShifts());
        }
        if (request.getMaxWeeklyHours() != null) employee.setMaxWeeklyHours(request.getMaxWeeklyHours());
        if (request.getCurrentFatigueFactor() != null) employee.setCurrentFatigueFactor(request.getCurrentFatigueFactor());
        if (request.getActive() != null) employee.setActive(request.getActive());

        Employee updated = employeeRepository.save(employee);
        return EmployeeResponse.fromEntity(updated);
    }

    @Transactional
    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employee.setActive(false);
        employeeRepository.save(employee);
    }
}
