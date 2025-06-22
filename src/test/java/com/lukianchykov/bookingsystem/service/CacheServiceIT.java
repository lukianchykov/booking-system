package com.lukianchykov.bookingsystem.service;

import com.lukianchykov.bookingsystem.utils.AvailableUnitsChangedEvent;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@SpringJUnitConfig(CacheServiceTest.CacheTestConfig.class)
class CacheServiceIT {

    private final CacheService cacheService = new CacheService();

    @Test
    void cacheAnnotations_WorkCorrectly() throws NoSuchMethodException {
        assertTrue(cacheService.getClass().getMethod("getAvailableUnitsCount")
            .isAnnotationPresent(org.springframework.cache.annotation.Cacheable.class));
        
        assertTrue(cacheService.getClass().getMethod("evictAvailableUnitsCache")
            .isAnnotationPresent(org.springframework.cache.annotation.CacheEvict.class));
        
        assertTrue(cacheService.getClass().getMethod("handleAvailableUnitsChanged", AvailableUnitsChangedEvent.class)
            .isAnnotationPresent(org.springframework.context.event.EventListener.class));
    }
}