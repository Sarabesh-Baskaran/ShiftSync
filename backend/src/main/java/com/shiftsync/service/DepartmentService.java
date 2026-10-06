package com.shiftsync.service;

import com.shiftsync.common.exception.ResourceNotFoundException;
import com.shiftsync.domain.entity.Department;
import com.shiftsync.dto.department.CreateDepartmentRequest;
import com.shiftsync.dto.department.DepartmentResponse;
import com.shiftsync.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Department with name '" + request.getName() + "' already exists");
        }

        Department department = Department.builder()
                .name(request.getName())
                .type(request.getType())
                .minStaffPerShift(request.getMinStaffPerShift())
                .mandatorySkill(request.getMandatorySkill())
                .build();

        Department saved = departmentRepository.save(department);
        return DepartmentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return DepartmentResponse.fromEntity(department);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
    }
}
