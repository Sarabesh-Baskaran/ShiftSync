package com.shiftsync.solver.constraints;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.*;
import com.shiftsync.domain.entity.Shift;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Defines the mathematical scoring rules (Hard, Medium, Soft) for hospital nurse rostering.
 * Hard constraints guarantee clinical safety and legality:
 * 1. Required clinical qualifications (e.g. ICU certified for ICU shifts).
 * 2. Minimum 12-hour rest gap between any two shifts (bans "clopening").
 * 3. At most one shift per calendar day.
 * 4. Maximum weekly working hours limit.
 */
public class RosterConstraintProvider implements ConstraintProvider {

    public static final double MINIMUM_REST_HOURS = 12.0;

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                // Hard Constraints (Clinical Safety & Legal Compliance)
                requiredClinicalQualification(factory),
                atMostOneShiftPerDay(factory),
                minimumRestGapBetweenShifts(factory),
                maxWeeklyWorkingHours(factory),

                // Medium Constraints (Operational Coverage)
                minimizeUnassignedShifts(factory)
        };
    }

    /**
     * Hard Constraint: Every assigned healthcare worker MUST possess the clinical skills
     * required by the ward (e.g. ICU certification for ICU shifts, trauma training for ER).
     */
    public Constraint requiredClinicalQualification(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(shift -> shift.getAssignedEmployee() != null)
                .filter(shift -> !isQualifiedForShift(shift))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Mandatory clinical qualification missing");
    }

    private boolean isQualifiedForShift(Shift shift) {
        if (shift.getRequiredSkill() != null && !shift.getAssignedEmployee().hasSkill(shift.getRequiredSkill())) {
            return false;
        }
        return shift.getAssignedEmployee().isQualifiedFor(shift.getDepartment());
    }

    /**
     * Hard Constraint: A medical staff member must not be assigned more than one shift
     * on the same calendar day.
     */
    public Constraint atMostOneShiftPerDay(ConstraintFactory factory) {
        return factory.forEachUniquePair(Shift.class,
                        Joiners.equal(Shift::getAssignedEmployee),
                        Joiners.equal(Shift::getShiftDate))
                .filter((s1, s2) -> s1.getAssignedEmployee() != null)
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("At most one shift per calendar day");
    }

    /**
     * Hard Constraint: Medical staff MUST have at least 12 hours of uninterrupted rest
     * between the end of one shift and the start of their next shift.
     * Prevents sleep deprivation and the dangerous "clopening" quick-return shift pattern.
     */
    public Constraint minimumRestGapBetweenShifts(ConstraintFactory factory) {
        return factory.forEachUniquePair(Shift.class,
                        Joiners.equal(Shift::getAssignedEmployee))
                .filter((s1, s2) -> s1.getAssignedEmployee() != null)
                .filter((s1, s2) -> hasRestGapViolation(s1, s2, MINIMUM_REST_HOURS))
                .penalize(HardMediumSoftScore.ONE_HARD, (s1, s2) -> getRestDeficitHours(s1, s2, MINIMUM_REST_HOURS))
                .asConstraint("Minimum 12 hours rest gap between shifts");
    }

    /**
     * Hard Constraint: Protect staff from exceeding their contractual or safety weekly hours limit (e.g. 40h).
     */
    public Constraint maxWeeklyWorkingHours(ConstraintFactory factory) {
        return factory.forEach(Shift.class)
                .filter(shift -> shift.getAssignedEmployee() != null)
                .groupBy(Shift::getAssignedEmployee,
                        ConstraintCollectors.sum(shift -> (int) Math.round(shift.getDurationHours())))
                .filter((employee, totalHours) -> totalHours > employee.getMaxWeeklyHours())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (employee, totalHours) -> totalHours - employee.getMaxWeeklyHours())
                .asConstraint("Exceeded maximum weekly working hours");
    }

    /**
     * Medium Constraint: Penalize unassigned shifts so the AI actively fills all hospital staffing quotas.
     */
    public Constraint minimizeUnassignedShifts(ConstraintFactory factory) {
        return factory.forEachIncludingNullVars(Shift.class)
                .filter(shift -> shift.getAssignedEmployee() == null)
                .penalize(HardMediumSoftScore.ONE_MEDIUM)
                .asConstraint("Unassigned shift slot");
    }

    // --- Static Utility Helpers for Rest Gap Calculation ---

    public static boolean hasRestGapViolation(Shift s1, Shift s2, double minRestHours) {
        LocalDateTime start1 = s1.getStartDateTime();
        LocalDateTime start2 = s2.getStartDateTime();
        LocalDateTime end1 = s1.getEndDateTime();
        LocalDateTime end2 = s2.getEndDateTime();

        if (start1 == null || start2 == null || end1 == null || end2 == null) {
            return false;
        }

        Shift first, second;
        if (start1.isBefore(start2)) {
            first = s1;
            second = s2;
        } else if (start2.isBefore(start1)) {
            first = s2;
            second = s1;
        } else {
            // Same start time - overlapping!
            return true;
        }

        Duration gap = Duration.between(first.getEndDateTime(), second.getStartDateTime());
        double gapHours = gap.toMinutes() / 60.0;
        return gapHours < minRestHours;
    }

    public static int getRestDeficitHours(Shift s1, Shift s2, double minRestHours) {
        LocalDateTime start1 = s1.getStartDateTime();
        LocalDateTime start2 = s2.getStartDateTime();

        Shift first, second;
        if (start1.isBefore(start2)) {
            first = s1;
            second = s2;
        } else if (start2.isBefore(start1)) {
            first = s2;
            second = s1;
        } else {
            return (int) Math.ceil(minRestHours);
        }

        Duration gap = Duration.between(first.getEndDateTime(), second.getStartDateTime());
        double gapHours = gap.toMinutes() / 60.0;
        if (gapHours < 0) {
            return (int) Math.ceil(minRestHours + Math.abs(gapHours));
        }
        return (int) Math.max(1, Math.ceil(minRestHours - gapHours));
    }
}
