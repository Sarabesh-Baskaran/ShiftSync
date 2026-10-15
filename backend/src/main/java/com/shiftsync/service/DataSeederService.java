package com.shiftsync.service;

import com.shiftsync.domain.entity.Department;
import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.Shift;
import com.shiftsync.domain.entity.WellbeingCheckIn;
import com.shiftsync.domain.enums.DepartmentType;
import com.shiftsync.domain.enums.ShiftStatus;
import com.shiftsync.domain.enums.ShiftType;
import com.shiftsync.domain.enums.Skill;
import com.shiftsync.dto.shift.GenerateRosterSlotsRequest;
import com.shiftsync.repository.DepartmentRepository;
import com.shiftsync.repository.EmployeeRepository;
import com.shiftsync.repository.ShiftRepository;
import com.shiftsync.repository.WellbeingCheckInRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Hospital Data Seeder & Demo Sandbox Service.
 * Populates realistic hospital wards, medical staff with distinct certifications,
 * a 7-day roster of unassigned shift slots, and recent wellbeing check-ins.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSeederService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final WellbeingCheckInRepository wellbeingCheckInRepository;
    private final ShiftService shiftService;

    @Transactional
    public void resetAllData() {
        log.info("Resetting ShiftSync database...");
        wellbeingCheckInRepository.deleteAll();
        wellbeingCheckInRepository.flush();
        shiftRepository.deleteAll();
        shiftRepository.flush();
        employeeRepository.deleteAll();
        employeeRepository.flush();
        departmentRepository.deleteAll();
        departmentRepository.flush();
        log.info("Database reset complete.");
    }

    @Transactional
    public Map<String, Object> seedHospitalData(boolean forceReset) {
        if (forceReset || departmentRepository.count() == 0) {
            resetAllData();
            log.info("Starting hospital data seeding...");

            // 1. Seed Departments
            List<Department> departments = seedDepartments();

            // 2. Seed Medical Staff (24 Nurses & Specialists)
            List<Employee> staff = seedMedicalStaff();

            // 3. Generate 7 Days of Shift Slots for the Roster
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(6); // 7-day schedule window
            int totalShiftsGenerated = seedShiftSlots(departments, startDate, endDate);

            // 4. Seed Wellbeing Check-ins
            int checkInCount = seedWellbeingCheckIns(staff, startDate);

            log.info("Hospital seeding complete: {} departments, {} staff, {} shifts, {} check-ins.",
                    departments.size(), staff.size(), totalShiftsGenerated, checkInCount);

            Map<String, Object> summary = new HashMap<>();
            summary.put("status", "SUCCESS");
            summary.put("departmentsCount", departments.size());
            summary.put("staffCount", staff.size());
            summary.put("shiftsScheduledCount", totalShiftsGenerated);
            summary.put("wellbeingCheckInsCount", checkInCount);
            summary.put("rosterStartDate", startDate.toString());
            summary.put("rosterEndDate", endDate.toString());
            return summary;
        } else {
            log.info("Database already contains data; skipping seeding.");
            return getDatabaseStatus();
        }
    }

    private List<Department> seedDepartments() {
        List<Department> departments = List.of(
                Department.builder()
                        .name("Intensive Care Unit (ICU)")
                        .type(DepartmentType.ICU)
                        .minStaffPerShift(2)
                        .mandatorySkill(Skill.ICU_CERTIFIED)
                        .build(),
                Department.builder()
                        .name("Emergency Department (ER)")
                        .type(DepartmentType.EMERGENCY)
                        .minStaffPerShift(2)
                        .mandatorySkill(Skill.EMERGENCY_TRAINED)
                        .build(),
                Department.builder()
                        .name("Surgical & Post-Op Unit")
                        .type(DepartmentType.SURGERY)
                        .minStaffPerShift(2)
                        .mandatorySkill(Skill.SURGICAL_SPECIALIST)
                        .build(),
                Department.builder()
                        .name("General Medical Ward (Ward 4)")
                        .type(DepartmentType.GENERAL_WARD)
                        .minStaffPerShift(2)
                        .mandatorySkill(Skill.REGISTERED_NURSE)
                        .build(),
                Department.builder()
                        .name("Pediatrics Care Unit")
                        .type(DepartmentType.PEDIATRICS)
                        .minStaffPerShift(1)
                        .mandatorySkill(Skill.PEDIATRIC_CERTIFIED)
                        .build()
        );
        return departmentRepository.saveAll(departments);
    }

    private List<Employee> seedMedicalStaff() {
        List<Employee> staffList = new ArrayList<>();

        // Group 1: Critical Care & ICU Specialists (6 Staff)
        staffList.add(createStaff("RN-101", "Dr. Sarah Chen, MD", "sarah.chen@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.ICU_CERTIFIED, Skill.CHARGE_NURSE), 2, 40, 1.0));
        staffList.add(createStaff("RN-102", "Elena Rostova, BSN", "elena.rostova@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.ICU_CERTIFIED), 2, 40, 1.1));
        staffList.add(createStaff("RN-103", "Marcus Aurelius, RN", "marcus.a@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.ICU_CERTIFIED), 2, 40, 1.0));
        staffList.add(createStaff("RN-104", "Priya Sharma, CCRN", "priya.s@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.ICU_CERTIFIED, Skill.EMERGENCY_TRAINED), 2, 40, 1.2));
        staffList.add(createStaff("RN-105", "David Kim, RN", "david.kim@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.ICU_CERTIFIED), 2, 40, 1.0));
        staffList.add(createStaff("RN-106", "Amara Okafor, BSN", "amara.o@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.ICU_CERTIFIED), 2, 40, 1.0));

        // Group 2: Emergency & Trauma Specialists (5 Staff)
        staffList.add(createStaff("RN-201", "James 'Logan' Howlett", "james.h@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.EMERGENCY_TRAINED, Skill.CHARGE_NURSE), 2, 40, 1.0));
        staffList.add(createStaff("RN-202", "Natasha Romanoff, RN", "natasha.r@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.EMERGENCY_TRAINED), 2, 40, 1.3));
        staffList.add(createStaff("RN-203", "Carlos Rodriguez, BSN", "carlos.r@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.EMERGENCY_TRAINED), 2, 40, 1.0));
        staffList.add(createStaff("RN-204", "Chloe Bennett, RN", "chloe.b@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.EMERGENCY_TRAINED), 2, 40, 1.1));
        staffList.add(createStaff("RN-205", "Zack Taylor, BSN", "zack.t@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.EMERGENCY_TRAINED), 2, 40, 1.0));

        // Group 3: Surgical & OR Specialists (4 Staff)
        staffList.add(createStaff("RN-301", "Dr. Christopher Turk", "turk@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.SURGICAL_SPECIALIST, Skill.CHARGE_NURSE), 2, 40, 1.0));
        staffList.add(createStaff("RN-302", "Meredith Grey, BSN", "meredith.g@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.SURGICAL_SPECIALIST), 2, 40, 1.2));
        staffList.add(createStaff("RN-303", "Alex Karev, RN", "alex.k@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.SURGICAL_SPECIALIST), 2, 40, 1.0));
        staffList.add(createStaff("RN-304", "Cristina Yang, CCRN", "cristina.y@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.SURGICAL_SPECIALIST, Skill.ICU_CERTIFIED), 2, 40, 1.0));

        // Group 4: Pediatric Specialists (3 Staff)
        staffList.add(createStaff("RN-401", "Arizona Robbins, MD", "arizona.r@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.PEDIATRIC_CERTIFIED, Skill.CHARGE_NURSE), 2, 40, 1.0));
        staffList.add(createStaff("RN-402", "Hannah Abbott, RN", "hannah.a@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.PEDIATRIC_CERTIFIED), 2, 40, 1.0));
        staffList.add(createStaff("RN-403", "Leo Fitz, BSN", "leo.f@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.PEDIATRIC_CERTIFIED), 2, 40, 1.1));

        // Group 5: General Registered Nurses (6 Staff)
        staffList.add(createStaff("RN-501", "Florence Nightingale", "florence.n@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.CHARGE_NURSE), 2, 40, 1.0));
        staffList.add(createStaff("RN-502", "John Dorian, MD", "jd@hospital.org",
                Set.of(Skill.REGISTERED_NURSE), 2, 40, 1.4)); // Currently high fatigue!
        staffList.add(createStaff("RN-503", "Elliot Reid, BSN", "elliot.r@hospital.org",
                Set.of(Skill.REGISTERED_NURSE), 2, 40, 1.0));
        staffList.add(createStaff("RN-504", "Carla Espinosa, RN", "carla.e@hospital.org",
                Set.of(Skill.REGISTERED_NURSE, Skill.CHARGE_NURSE), 2, 40, 1.0));
        staffList.add(createStaff("RN-505", "Samwise Gamgee, BSN", "samwise.g@hospital.org",
                Set.of(Skill.REGISTERED_NURSE), 2, 40, 1.0));
        staffList.add(createStaff("RN-506", "Claire Temple, RN", "claire.t@hospital.org",
                Set.of(Skill.REGISTERED_NURSE), 2, 40, 1.0));

        return employeeRepository.saveAll(staffList);
    }

    private Employee createStaff(String code, String name, String email, Set<Skill> skills,
                                 int maxConsecutiveHighStress, int maxWeeklyHours, double initialFatigue) {
        return Employee.builder()
                .employeeCode(code)
                .fullName(name)
                .email(email)
                .skills(skills)
                .maxConsecutiveHighStressShifts(maxConsecutiveHighStress)
                .maxWeeklyHours(maxWeeklyHours)
                .currentFatigueFactor(initialFatigue)
                .active(true)
                .build();
    }

    private int seedShiftSlots(List<Department> departments, LocalDate startDate, LocalDate endDate) {
        GenerateRosterSlotsRequest request = GenerateRosterSlotsRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .departmentIds(departments.stream().map(Department::getId).toList())
                .shiftTypes(List.of(ShiftType.MORNING, ShiftType.EVENING, ShiftType.NIGHT))
                .build();

        return shiftService.generateRosterSlots(request).size();
    }

    private int seedWellbeingCheckIns(List<Employee> staff, LocalDate today) {
        List<WellbeingCheckIn> checkIns = new ArrayList<>();

        for (Employee emp : staff) {
            // Assign varying fatigue levels for realistic demonstration
            int fatigue = 1;
            int stress = 1;
            double sleep = 7.5;
            String note = "Feeling healthy and rested.";

            if (emp.getEmployeeCode().equals("RN-502")) { // JD: Sleep deprived & high stress
                fatigue = 5;
                stress = 4;
                sleep = 4.0;
                note = "Consecutive double shifts; severe sleep debt.";
            } else if (emp.getEmployeeCode().equals("RN-202")) { // Natasha: Moderate fatigue
                fatigue = 3;
                stress = 3;
                sleep = 5.5;
                note = "Challenging ER trauma day yesterday.";
            } else if (emp.getEmployeeCode().equals("RN-104")) { // Priya
                fatigue = 2;
                stress = 2;
                sleep = 6.5;
                note = "Mild fatigue from night shift transition.";
            }

            WellbeingCheckIn checkIn = WellbeingCheckIn.builder()
                    .employee(emp)
                    .checkInDate(today)
                    .recordedAt(LocalDateTime.now().minusHours(2))
                    .fatigueScore(fatigue)
                    .stressLevel(stress)
                    .sleepHours(sleep)
                    .notes(note)
                    .build();

            // Sync employee fatigue factor
            emp.setCurrentFatigueFactor(checkIn.computeDynamicFatigueFactor());
            checkIns.add(checkIn);
        }

        employeeRepository.saveAll(staff);
        return wellbeingCheckInRepository.saveAll(checkIns).size();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("departmentsCount", departmentRepository.count());
        status.put("staffCount", employeeRepository.count());
        status.put("shiftsScheduledCount", shiftRepository.count());
        status.put("wellbeingCheckInsCount", wellbeingCheckInRepository.count());
        return status;
    }
}
