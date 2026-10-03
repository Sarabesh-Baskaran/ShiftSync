package com.shiftsync.domain.entity;

import com.shiftsync.domain.enums.DepartmentType;
import com.shiftsync.domain.enums.Skill;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Hospital department / clinical ward entity.
 */
@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepartmentType type;

    @Builder.Default
    @Column(nullable = false)
    private int minStaffPerShift = 1;

    /**
     * Mandatory clinical certification needed to work in this department.
     * If null, any general registered nurse can be assigned.
     */
    @Enumerated(EnumType.STRING)
    private Skill mandatorySkill;

    /**
     * Returns the stress weight multiplier associated with this department's clinical category.
     */
    public double getStressMultiplier() {
        return type != null ? type.getStressMultiplier() : 1.0;
    }

    public boolean isHighStress() {
        return type != null && type.isHighStress();
    }
}
