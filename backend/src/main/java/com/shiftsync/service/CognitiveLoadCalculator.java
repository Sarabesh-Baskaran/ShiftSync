package com.shiftsync.service;

import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.enums.BurnoutRiskLevel;
import com.shiftsync.dto.analytics.CognitiveLoadProfileDTO;
import com.shiftsync.dto.analytics.RestGapViolation;
import com.shiftsync.dto.analytics.TeamScheduleAnalyticsDTO;
import com.shiftsync.repository.DepartmentRepository;
import com.shiftsync.repository.EmployeeRepository;
import com.shiftsync.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

/**
 * Core mathematical engine analyzing staff cognitive strain, rest deficits,
 * and roster fairness across clinical departments.
 */
@Service
@RequiredArgsConstructor
public class CognitiveLoadCalculator {

    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Evaluates the comprehensive cognitive load and fatigue profile of a single staff member.
     */
    public CognitiveLoadProfileDTO calculateProfile(Employee employee, List<Shift> shifts) {
        if (employee == null) {
            return null;
        }

        List<Shift> sortedShifts = (shifts != null) ? new ArrayList<>(shifts) : new ArrayList<>();
        sortedShifts.sort(Comparator.comparing(Shift::getStartDateTime));

        int totalShifts = sortedShifts.size();
        double totalHoursWorked = 0.0;
        double totalCognitiveLoadScore = 0.0;

        for (Shift shift : sortedShifts) {
            totalHoursWorked += shift.getDurationHours();
            totalCognitiveLoadScore += shift.calculateCognitiveLoadScore();
        }

        double averageShiftLoad = (totalShifts > 0) ? (totalCognitiveLoadScore / totalShifts) : 0.0;

        // Evaluate consecutive high-stress shifts
        int currentStreak = 0;
        int maxConsecutiveHighStressShifts = 0;
        for (Shift shift : sortedShifts) {
            if (shift.isHighStress()) {
                currentStreak++;
                maxConsecutiveHighStressShifts = Math.max(maxConsecutiveHighStressShifts, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        // Evaluate rest gaps between successive shifts
        double shortestRestGapHours = Double.MAX_VALUE;
        List<RestGapViolation> restGapViolations = new ArrayList<>();
        List<String> riskFactors = new ArrayList<>();

        for (int i = 0; i < sortedShifts.size() - 1; i++) {
            Shift current = sortedShifts.get(i);
            Shift next = sortedShifts.get(i + 1);

            if (current.getEndDateTime() != null && next.getStartDateTime() != null) {
                Duration gap = Duration.between(current.getEndDateTime(), next.getStartDateTime());
                double gapHours = gap.toMinutes() / 60.0;
                shortestRestGapHours = Math.min(shortestRestGapHours, gapHours);

                if (gapHours < 12.0) {
                    restGapViolations.add(RestGapViolation.builder()
                            .precedingShiftId(current.getId())
                            .precedingShiftEnd(current.getEndDateTime())
                            .precedingDepartment(current.getDepartment().getName())
                            .followingShiftId(next.getId())
                            .followingShiftStart(next.getStartDateTime())
                            .followingDepartment(next.getDepartment().getName())
                            .gapHours(round(gapHours, 1))
                            .severe(true)
                            .build());

                    riskFactors.add(String.format("Critical rest deficit: only %.1f hours rest between %s and %s (legal min: 12h)",
                            gapHours, current.getDepartment().getName(), next.getDepartment().getName()));
                } else if (gapHours < 14.0) {
                    restGapViolations.add(RestGapViolation.builder()
                            .precedingShiftId(current.getId())
                            .precedingShiftEnd(current.getEndDateTime())
                            .precedingDepartment(current.getDepartment().getName())
                            .followingShiftId(next.getId())
                            .followingShiftStart(next.getStartDateTime())
                            .followingDepartment(next.getDepartment().getName())
                            .gapHours(round(gapHours, 1))
                            .severe(false)
                            .build());

                    riskFactors.add(String.format("Tight recovery gap: %.1f hours rest between %s and %s",
                            gapHours, current.getDepartment().getName(), next.getDepartment().getName()));
                }
            }
        }

        if (shortestRestGapHours == Double.MAX_VALUE) {
            shortestRestGapHours = 0.0;
        }

        // Check high-stress consecutive streak limits
        if (maxConsecutiveHighStressShifts > employee.getMaxConsecutiveHighStressShifts()) {
            riskFactors.add(String.format("Exceeded high-stress shift threshold: %d consecutive intensive shifts (safe limit: %d)",
                    maxConsecutiveHighStressShifts, employee.getMaxConsecutiveHighStressShifts()));
        }

        // Check weekly hours overtime hazard
        if (totalHoursWorked > employee.getMaxWeeklyHours()) {
            riskFactors.add(String.format("Overtime hazard: %.1f hours scheduled against %.d contract limit",
                    totalHoursWorked, employee.getMaxWeeklyHours()));
        }

        // Check self-reported baseline fatigue
        if (employee.getCurrentFatigueFactor() >= 1.8) {
            riskFactors.add(String.format("Elevated self-reported fatigue factor (%.2fx) from daily check-in",
                    employee.getCurrentFatigueFactor()));
        }

        // Determine Burnout Risk Level
        BurnoutRiskLevel riskLevel;
        boolean hasSevereGap = restGapViolations.stream().anyMatch(RestGapViolation::isSevere);

        if (hasSevereGap || maxConsecutiveHighStressShifts > 2 || totalCognitiveLoadScore > 120.0) {
            riskLevel = BurnoutRiskLevel.CRITICAL;
        } else if (!restGapViolations.isEmpty() || totalCognitiveLoadScore > 75.0 || employee.getCurrentFatigueFactor() >= 1.6) {
            riskLevel = BurnoutRiskLevel.HIGH;
        } else if (totalCognitiveLoadScore > 40.0 || totalHoursWorked > 35.0) {
            riskLevel = BurnoutRiskLevel.MODERATE;
        } else {
            riskLevel = BurnoutRiskLevel.LOW;
        }

        return CognitiveLoadProfileDTO.builder()
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .currentFatigueFactor(employee.getCurrentFatigueFactor())
                .totalShifts(totalShifts)
                .totalHoursWorked(round(totalHoursWorked, 1))
                .totalCognitiveLoadScore(round(totalCognitiveLoadScore, 1))
                .averageShiftLoad(round(averageShiftLoad, 1))
                .maxConsecutiveHighStressShifts(maxConsecutiveHighStressShifts)
                .shortestRestGapHours(round(shortestRestGapHours, 1))
                .burnoutRiskLevel(riskLevel)
                .riskColorHex(riskLevel.getColorHex())
                .riskFactors(riskFactors)
                .restGapViolations(restGapViolations)
                .build();
    }

    /**
     * Evaluates a single employee by querying their assigned shifts within a calendar window.
     */
    @Transactional(readOnly = true)
    public CognitiveLoadProfileDTO getEmployeeProfile(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));

        List<Shift> shifts = shiftRepository.findByAssignedEmployeeAndShiftDateBetweenOrderByShiftDateAsc(
                employee, startDate, endDate);

        return calculateProfile(employee, shifts);
    }

    /**
     * Evaluates team-wide workload distribution, calculating mathematical variance
     * and a normalized fairness score for hospital administrators.
     */
    @Transactional(readOnly = true)
    public TeamScheduleAnalyticsDTO calculateTeamAnalytics(LocalDate startDate, LocalDate endDate, Long departmentId) {
        List<Employee> allEmployees = employeeRepository.findByActiveTrue();
        List<Shift> shifts;

        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + departmentId));
            shifts = shiftRepository.findByDepartmentAndShiftDateBetween(department, startDate, endDate);
        } else {
            shifts = shiftRepository.findByShiftDateBetweenOrderByShiftDateAsc(startDate, endDate);
        }

        // Group shifts by assigned employee ID
        Map<Long, List<Shift>> shiftsByEmployee = new HashMap<>();
        for (Shift shift : shifts) {
            if (shift.getAssignedEmployee() != null) {
                shiftsByEmployee.computeIfAbsent(shift.getAssignedEmployee().getId(), k -> new ArrayList<>())
                        .add(shift);
            }
        }

        List<CognitiveLoadProfileDTO> profiles = new ArrayList<>();
        double totalTeamLoad = 0.0;
        int lowCount = 0, modCount = 0, highCount = 0, critCount = 0;

        for (Employee emp : allEmployees) {
            List<Shift> empShifts = shiftsByEmployee.getOrDefault(emp.getId(), Collections.emptyList());
            CognitiveLoadProfileDTO profile = calculateProfile(emp, empShifts);
            profiles.add(profile);

            totalTeamLoad += profile.getTotalCognitiveLoadScore();

            switch (profile.getBurnoutRiskLevel()) {
                case LOW -> lowCount++;
                case MODERATE -> modCount++;
                case HIGH -> highCount++;
                case CRITICAL -> critCount++;
            }
        }

        int staffCount = allEmployees.size();
        double meanLoad = (staffCount > 0) ? (totalTeamLoad / staffCount) : 0.0;

        // Calculate Standard Deviation
        double sumSquaredDiff = 0.0;
        for (CognitiveLoadProfileDTO p : profiles) {
            double diff = p.getTotalCognitiveLoadScore() - meanLoad;
            sumSquaredDiff += diff * diff;
        }

        double stdDev = (staffCount > 0) ? Math.sqrt(sumSquaredDiff / staffCount) : 0.0;

        // Calculate Fairness Score (0 to 100)
        double fairnessScore = 100.0;
        if (meanLoad > 0.0) {
            double coefficientOfVariation = stdDev / meanLoad;
            fairnessScore = Math.max(0.0, 100.0 * (1.0 - Math.min(1.0, coefficientOfVariation)));
        }

        return TeamScheduleAnalyticsDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalStaffEvaluated(staffCount)
                .totalShiftsScheduled(shifts.size())
                .totalTeamCognitiveLoad(round(totalTeamLoad, 1))
                .averageLoadPerStaff(round(meanLoad, 1))
                .standardDeviation(round(stdDev, 1))
                .fairnessScore(round(fairnessScore, 1))
                .lowRiskStaffCount(lowCount)
                .moderateRiskStaffCount(modCount)
                .highRiskStaffCount(highCount)
                .criticalRiskStaffCount(critCount)
                .staffProfiles(profiles)
                .build();
    }

    private double round(double val, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(val * scale) / scale;
    }
}
