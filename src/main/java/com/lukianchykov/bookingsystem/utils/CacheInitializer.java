package com.lukianchykov.bookingsystem.utils;

import com.lukianchykov.bookingsystem.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheInitializer implements ApplicationRunner {
    
    private final CacheService cacheService;
    
    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("Initializing cache on application startup");
            Long count = cacheService.getAvailableUnitsCount();
            log.info("Cache initialization completed. Available units: {}", count);
        } catch (Exception e) {
            log.error("Failed to initialize cache, will be populated on first request", e);
        }
    }
}