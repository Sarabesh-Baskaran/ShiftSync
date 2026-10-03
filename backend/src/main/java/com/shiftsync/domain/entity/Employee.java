package com.shiftsync.domain.entity;

import com.shiftsync.domain.enums.Skill;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Healthcare worker entity (Nurses, Doctors, Medical Staff).
 */
@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    private String employeeCode;

    @NotBlank
    @Column(nullable = false)
    private String fullName;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;

    @ElementCollection(targetClass = Skill.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_skills", joinColumns = @JoinColumn(name = "employee_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "skill", nullable = false)
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();

    public void setSkills(Set<Skill> skills) {
        this.skills = (skills != null) ? new HashSet<>(skills) : new HashSet<>();
    }

    public static class EmployeeBuilder {
        public EmployeeBuilder skills(Set<Skill> skills) {
            this.skills$value = (skills != null) ? new HashSet<>(skills) : new HashSet<>();
            this.skills$set = true;
            return this;
        }
    }

    /**
     * Safety limit: Maximum number of consecutive high-stress (ICU/ER/Surgery) shifts
     * allowed before mandatory 48-hour decompression rest.
     */
    @Builder.Default
    @Column(nullable = false)
    private int maxConsecutiveHighStressShifts = 2;

    /**
     * Contractual or safety limit on weekly working hours (e.g. 40h).
     */
    @Builder.Default
    @Column(nullable = false)
    private int maxWeeklyHours = 40;

    /**
     * Current fatigue factor (updated dynamically from wellbeing check-ins).
     * 1.0 = baseline / fully rested, >1.5 = elevated fatigue, >2.0 = high burnout risk.
     */
    @Builder.Default
    @Column(nullable = false)
    private double currentFatigueFactor = 1.0;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Helper to verify if the employee holds a specific clinical certification.
     */
    public boolean hasSkill(Skill skill) {
        return skill == null || (skills != null && skills.contains(skill));
    }

    /**
     * Validates if the employee is clinically certified to work in the specified department.
     */
    public boolean isQualifiedFor(Department department) {
        if (department == null || department.getMandatorySkill() == null) {
            return true;
        }
        return hasSkill(department.getMandatorySkill());
    }
}
