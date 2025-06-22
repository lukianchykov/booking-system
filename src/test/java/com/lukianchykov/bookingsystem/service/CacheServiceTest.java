package com.lukianchykov.bookingsystem.service;

import com.lukianchykov.bookingsystem.utils.AvailableUnitsChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @InjectMocks
    private CacheService cacheService;

    @Test
    void getAvailableUnitsCount_ThrowsUnsupportedOperationException() {
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class, 
            () -> cacheService.getAvailableUnitsCount()
        );
        assertEquals("Use CountAvailableUnitsService to calculate directly.", exception.getMessage());
    }

    @Test
    void handleAvailableUnitsChanged_CallsEvictCache() {
        AvailableUnitsChangedEvent event = new AvailableUnitsChangedEvent(this);

        CacheService spyCacheService = spy(cacheService);
        doNothing().when(spyCacheService).evictAvailableUnitsCache();

        spyCacheService.handleAvailableUnitsChanged(event);

        verify(spyCacheService).evictAvailableUnitsCache();
    }

    @Test
    void evictAvailableUnitsCache_ExecutesWithoutException() {
        assertDoesNotThrow(() -> cacheService.evictAvailableUnitsCache());
    }

    @Configuration
    @EnableCaching
    static class CacheTestConfig {
        
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("availableUnitsCount");
        }
    }
}