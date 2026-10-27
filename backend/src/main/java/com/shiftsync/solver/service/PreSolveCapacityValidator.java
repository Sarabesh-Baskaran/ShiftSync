package com.shiftsync.solver.service;

import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.enums.Skill;
import com.shiftsync.solver.dto.CapacityAnalysisDTO;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Validates hospital staffing supply against shift requirements prior to solver invocation.
 * Detects skill bottlenecks, overall capacity shortfalls, and physical impossibilities early.
 */
@Component
public class PreSolveCapacityValidator {

    public CapacityAnalysisDTO validateCapacity(List<Employee> staff, List<Shift> shifts) {
        List<String> warnings = new ArrayList<>();
        List<String> criticalDeficits = new ArrayList<>();

        int totalStaff = (staff != null) ? staff.size() : 0;
        int totalShifts = (shifts != null) ? shifts.size() : 0;

        if (totalStaff == 0) {
            criticalDeficits.add("No active healthcare staff found in the system.");
            return CapacityAnalysisDTO.builder()
                    .theoreticallyFeasible(false)
                    .totalActiveStaff(0)
                    .totalShiftsRequired(totalShifts)
                    .criticalDeficits(criticalDeficits)
                    .warnings(warnings)
                    .build();
        }

        if (totalShifts == 0) {
            return CapacityAnalysisDTO.builder()
                    .theoreticallyFeasible(true)
                    .totalActiveStaff(totalStaff)
                    .totalShiftsRequired(0)
                    .totalHoursDemand(0.0)
                    .totalStaffCapacityHours(0.0)
                    .warnings(List.of("No shift requirements found for this date range."))
                    .criticalDeficits(Collections.emptyList())
                    .build();
        }

        // Determine days span in the schedule
        List<Shift> sortedShifts = new ArrayList<>(shifts);
        sortedShifts.sort(Comparator.comparing(Shift::getShiftDate));
        long daysSpan = ChronoUnit.DAYS.between(sortedShifts.get(0).getShiftDate(), sortedShifts.get(sortedShifts.size() - 1).getShiftDate()) + 1;
        double weeksFraction = Math.max(1.0 / 7.0, daysSpan / 7.0);

        // Calculate hours demand
        double totalDemandHours = shifts.stream()
                .mapToDouble(Shift::getDurationHours)
                .sum();

        // Calculate staff hours capacity (prorated by weeks fraction)
        double totalCapacityHours = staff.stream()
                .mapToDouble(emp -> emp.getMaxWeeklyHours() * weeksFraction)
                .sum();

        double capacityBuffer = totalCapacityHours - totalDemandHours;
        double staffingRatio = (totalDemandHours > 0) ? (totalCapacityHours / totalDemandHours) : 1.0;

        // Skill supply vs demand analysis
        Map<String, Integer> staffBySkill = new HashMap<>();
        for (Employee emp : staff) {
            if (emp.getSkills() != null) {
                for (Skill skill : emp.getSkills()) {
                    staffBySkill.merge(skill.name(), 1, Integer::sum);
                }
            }
        }

        Map<String, Integer> shiftsBySkill = new HashMap<>();
        for (Shift shift : shifts) {
            Skill needed = shift.getRequiredSkill();
            if (needed == null && shift.getDepartment() != null) {
                needed = shift.getDepartment().getMandatorySkill();
            }
            if (needed != null) {
                shiftsBySkill.merge(needed.name(), 1, Integer::sum);
            }
        }

        // Verify each required skill has available certified staff
        for (Map.Entry<String, Integer> entry : shiftsBySkill.entrySet()) {
            String skillName = entry.getKey();
            int shiftsNeeded = entry.getValue();
            int staffAvailable = staffBySkill.getOrDefault(skillName, 0);

            if (staffAvailable == 0) {
                criticalDeficits.add(String.format("Critical Skill Bottleneck: %d shifts require %s, but 0 active staff hold this certification.",
                        shiftsNeeded, skillName));
            } else if (shiftsNeeded > staffAvailable * 5) {
                warnings.add(String.format("High Skill Utilization: %d shifts require %s with only %d certified staff available.",
                        shiftsNeeded, skillName, staffAvailable));
            }
        }

        // Check overall capacity deficits
        if (capacityBuffer < 0) {
            warnings.add(String.format("Capacity Deficit: Shift demand (%.1f hours) exceeds total staff capacity (%.1f hours) by %.1f hours.",
                    totalDemandHours, totalCapacityHours, Math.abs(capacityBuffer)));
        } else if (staffingRatio < 1.15) {
            warnings.add(String.format("Tight Staffing Buffer: Total capacity is only %.1f%% of required demand. Absences or sick callouts may create unfillable slots.",
                    staffingRatio * 100.0));
        }

        boolean theoreticallyFeasible = criticalDeficits.isEmpty() && capacityBuffer >= 0;

        return CapacityAnalysisDTO.builder()
                .theoreticallyFeasible(theoreticallyFeasible)
                .totalActiveStaff(totalStaff)
                .totalShiftsRequired(totalShifts)
                .totalHoursDemand(round(totalDemandHours, 1))
                .totalStaffCapacityHours(round(totalCapacityHours, 1))
                .capacityBufferHours(round(capacityBuffer, 1))
                .staffingRatio(round(staffingRatio, 2))
                .warnings(warnings)
                .criticalDeficits(criticalDeficits)
                .staffCountBySkill(staffBySkill)
                .shiftCountByRequiredSkill(shiftsBySkill)
                .build();
    }

    private double round(double val, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(val * scale) / scale;
    }
}
