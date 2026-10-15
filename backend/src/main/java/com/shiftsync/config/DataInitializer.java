package com.shiftsync.config;

import com.shiftsync.service.DataSeederService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Automatically seeds the database on startup if it's currently empty,
 * enabling immediate out-of-the-box local development and testing.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DataSeederService dataSeederService;

    @Override
    public void run(String... args) {
        log.info("Checking ShiftSync hospital dataset status...");
        dataSeederService.seedHospitalData(false);
    }
}
