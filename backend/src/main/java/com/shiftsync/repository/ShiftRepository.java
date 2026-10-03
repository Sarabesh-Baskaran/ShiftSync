package com.shiftsync.repository;

import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.enums.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findByShiftDateBetweenOrderByShiftDateAsc(LocalDate startDate, LocalDate endDate);

    List<Shift> findByDepartmentAndShiftDateBetween(Department department, LocalDate startDate, LocalDate endDate);

    List<Shift> findByAssignedEmployeeAndShiftDateBetweenOrderByShiftDateAsc(Employee employee, LocalDate startDate, LocalDate endDate);

    List<Shift> findByStatus(ShiftStatus status);

    @Query("SELECT s FROM Shift s WHERE s.assignedEmployee = :employee AND s.shiftDate >= :sinceDate ORDER BY s.shiftDate ASC")
    List<Shift> findRecentShiftsForEmployee(@Param("employee") Employee employee, @Param("sinceDate") LocalDate sinceDate);
}
