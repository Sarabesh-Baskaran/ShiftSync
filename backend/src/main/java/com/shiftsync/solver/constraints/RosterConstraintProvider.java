package com.shiftsync.solver.constraints;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import com.shiftsync.domain.entity.Shift;

/**
 * Defines the mathematical scoring rules (Hard, Medium, Soft) for hospital nurse rostering.
 * Hard constraints guarantee clinical safety and legality.
 * Medium constraints guarantee full staffing coverage.
 * Soft constraints optimize for staff well-being, cognitive load fairness, and rest.
 */
public class RosterConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                // Hard Constraints (Safety & Legality)
                requiredClinicalQualification(factory),
                noSimultaneousDoubleBooking(factory),

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
     * Hard Constraint: A medical staff member cannot work two shifts at the exact same time
     * (e.g., both Morning ER and Morning ICU on the same calendar date).
     */
    public Constraint noSimultaneousDoubleBooking(ConstraintFactory factory) {
        return factory.forEachUniquePair(Shift.class,
                        Joiners.equal(Shift::getAssignedEmployee),
                        Joiners.equal(Shift::getShiftDate),
                        Joiners.equal(Shift::getShiftType))
                .filter((s1, s2) -> s1.getAssignedEmployee() != null)
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Double booked for simultaneous shift slots");
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
}
