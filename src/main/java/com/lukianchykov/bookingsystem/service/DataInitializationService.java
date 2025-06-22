package com.lukianchykov.bookingsystem.service;

import java.math.BigDecimal;
import java.util.Random;

import com.lukianchykov.bookingsystem.domain.AccommodationType;
import com.lukianchykov.bookingsystem.domain.Unit;
import com.lukianchykov.bookingsystem.domain.User;
import com.lukianchykov.bookingsystem.repository.UnitRepository;
import com.lukianchykov.bookingsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {

    private final UnitRepository unitRepository;

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (unitRepository.count() == 0) {
            log.info("Initializing data with 90 random units...");
            initializeRandomUnits();
        }
    }

    private void initializeRandomUnits() {
        Random random = new Random();
        AccommodationType[] types = AccommodationType.values();

        User defaultUser = userRepository.findByEmail("system@booking.com")
            .orElseGet(() -> {
                User user = User.builder()
                    .email("system@booking.com")
                    .name("System User")
                    .build();
                return userRepository.save(user);
            });

        for (int i = 0; i < 90; i++) {
            BigDecimal baseCost = BigDecimal.valueOf(50 + random.nextInt(950));

            Unit unit = Unit.builder()
                .numberOfRooms(1 + random.nextInt(5))
                .accommodationType(types[random.nextInt(types.length)])
                .floor(1 + random.nextInt(20))
                .baseCost(baseCost)
                .description("Randomly generated unit #" + (i + 1))
                .owner(defaultUser)
                .build();

            unitRepository.save(unit);
        }

        log.info("Successfully created 90 random units");
    }
}