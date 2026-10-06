package com.shiftsync.service;

import com.shiftsync.common.exception.ResourceNotFoundException;
import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.enums.ShiftStatus;
import com.shiftsync.domain.enums.ShiftType;
import com.shiftsync.dto.shift.CreateShiftRequest;
import com.shiftsync.dto.shift.GenerateRosterSlotsRequest;
import com.shiftsync.dto.shift.ShiftResponse;
import com.shiftsync.repository.DepartmentRepository;
import com.shiftsync.repository.EmployeeRepository;
import com.shiftsync.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public ShiftResponse createShift(CreateShiftRequest request) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        Employee employee = null;
        if (request.getEmployeeId() != null) {
            employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));
        }

        Shift shift = Shift.builder()
                .shiftDate(request.getShiftDate())
                .shiftType(request.getShiftType())
                .department(department)
                .assignedEmployee(employee)
                .requiredSkill(request.getRequiredSkill() != null ? request.getRequiredSkill() : department.getMandatorySkill())
                .status(employee != null ? ShiftStatus.ASSIGNED : ShiftStatus.UNASSIGNED)
                .build();

        Shift saved = shiftRepository.save(shift);
        return ShiftResponse.fromEntity(saved);
    }

    @Transactional
    public List<ShiftResponse> generateRosterSlots(GenerateRosterSlotsRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        List<Department> departments;
        if (request.getDepartmentIds() != null && !request.getDepartmentIds().isEmpty()) {
            departments = departmentRepository.findAllById(request.getDepartmentIds());
        } else {
            departments = departmentRepository.findAll();
        }

        List<Shift> createdShifts = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();

        while (!currentDate.isAfter(request.getEndDate())) {
            for (Department department : departments) {
                for (ShiftType shiftType : request.getShiftTypes()) {
                    for (int i = 0; i < department.getMinStaffPerShift(); i++) {
                        Shift slot = Shift.builder()
                                .shiftDate(currentDate)
                                .shiftType(shiftType)
                                .department(department)
                                .requiredSkill(department.getMandatorySkill())
                                .status(ShiftStatus.UNASSIGNED)
                                .build();
                        createdShifts.add(slot);
                    }
                }
            }
            currentDate = currentDate.plusDays(1);
        }

        List<Shift> savedShifts = shiftRepository.saveAll(createdShifts);
        return savedShifts.stream().map(ShiftResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ShiftResponse getShiftById(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + id));
        return ShiftResponse.fromEntity(shift);
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> getShifts(LocalDate startDate, LocalDate endDate, Long departmentId, Long employeeId) {
        List<Shift> shifts;
        if (departmentId != null) {
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
            shifts = shiftRepository.findByDepartmentAndShiftDateBetween(dept, startDate, endDate);
        } else if (employeeId != null) {
            Employee emp = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
            shifts = shiftRepository.findByAssignedEmployeeAndShiftDateBetweenOrderByShiftDateAsc(emp, startDate, endDate);
        } else {
            shifts = shiftRepository.findByShiftDateBetweenOrderByShiftDateAsc(startDate, endDate);
        }
        return shifts.stream().map(ShiftResponse::fromEntity).toList();
    }

    @Transactional
    public ShiftResponse assignEmployee(Long shiftId, Long employeeId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + shiftId));

        if (employeeId == null) {
            shift.setAssignedEmployee(null);
            shift.setStatus(ShiftStatus.UNASSIGNED);
        } else {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
            shift.setAssignedEmployee(employee);
            shift.setStatus(ShiftStatus.ASSIGNED);
        }

        Shift updated = shiftRepository.save(shift);
        return ShiftResponse.fromEntity(updated);
    }

    @Transactional
    public ShiftResponse markSickCallout(Long shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found with id: " + shiftId));

        shift.setStatus(ShiftStatus.SICK_CALLOUT);
        shift.setAssignedEmployee(null);

        Shift updated = shiftRepository.save(shift);
        return ShiftResponse.fromEntity(updated);
    }
}
