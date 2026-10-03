package com.shiftsync.repository;

import com.shiftsync.domain.entity.Employee;
import com.shiftsync.domain.entity.WellbeingCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WellbeingCheckInRepository extends JpaRepository<WellbeingCheckIn, Long> {

    Optional<WellbeingCheckIn> findByEmployeeAndCheckInDate(Employee employee, LocalDate checkInDate);

    List<WellbeingCheckIn> findByEmployeeOrderByRecordedAtDesc(Employee employee);

    List<WellbeingCheckIn> findByCheckInDate(LocalDate checkInDate);
}
