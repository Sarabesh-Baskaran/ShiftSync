package com.shiftsync.solver.model;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * The Timefold @PlanningSolution representing the hospital scheduling problem and its candidate solutions.
 * Contains:
 * - Problem Facts (Departments, Medical Staff)
 * - Planning Entities (Shift slots whose assignedEmployee variable is iteratively changed)
 * - Planning Score (Hard, Medium, Soft optimization score)
 */
@PlanningSolution
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RosterSchedule {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;

    @ProblemFactCollectionProperty
    private List<Department> departmentList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "employeeRange")
    private List<Employee> employeeList;

    @PlanningEntityCollectionProperty
    private List<Shift> shiftList;

    @PlanningScore
    private HardMediumSoftScore score;
}
