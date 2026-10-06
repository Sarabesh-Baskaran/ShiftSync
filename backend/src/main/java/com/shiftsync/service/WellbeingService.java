package com.shiftsync.service;

import com.shiftsync.common.exception.ResourceNotFoundException;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.WellbeingCheckIn;
import com.shiftsync.dto.wellbeing.CheckInRequest;
import com.shiftsync.dto.wellbeing.WellbeingCheckInResponse;
import com.shiftsync.repository.EmployeeRepository;
import com.shiftsync.repository.WellbeingCheckInRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WellbeingService {

    private final WellbeingCheckInRepository checkInRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public WellbeingCheckInResponse recordCheckIn(CheckInRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        LocalDate date = request.getCheckInDate() != null ? request.getCheckInDate() : LocalDate.now();

        // Check if employee already checked in today; update if existing, create if new
        WellbeingCheckIn checkIn = checkInRepository.findByEmployeeAndCheckInDate(employee, date)
                .orElseGet(() -> WellbeingCheckIn.builder()
                        .employee(employee)
                        .checkInDate(date)
                        .build());

        checkIn.setRecordedAt(LocalDateTime.now());
        checkIn.setFatigueScore(request.getFatigueScore());
        checkIn.setSleepHours(request.getSleepHours());
        checkIn.setStressLevel(request.getStressLevel());
        checkIn.setNotes(request.getNotes());

        double dynamicFactor = checkIn.computeDynamicFatigueFactor();

        // Dynamically update the employee's active fatigue factor
        employee.setCurrentFatigueFactor(dynamicFactor);
        employeeRepository.save(employee);

        WellbeingCheckIn saved = checkInRepository.save(checkIn);
        return WellbeingCheckInResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<WellbeingCheckInResponse> getCheckInsForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        return checkInRepository.findByEmployeeOrderByRecordedAtDesc(employee).stream()
                .map(WellbeingCheckInResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WellbeingCheckInResponse> getTodayCheckIns() {
        return checkInRepository.findByCheckInDate(LocalDate.now()).stream()
                .map(WellbeingCheckInResponse::fromEntity)
                .toList();
    }
}
