package com.lukianchykov.bookingsystem.service;

import com.lukianchykov.bookingsystem.utils.AvailableUnitsChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {
    
    private final UnitService unitService;

    @Cacheable(value = "availableUnitsCount", key = "'available'")
    public Long getAvailableUnitsCount() {
        log.info("Cache miss - calculating available units count from database");
        return unitService.countAvailableUnitsFromDatabase();
    }

    @CacheEvict(value = "availableUnitsCount", allEntries = true)
    public void evictAvailableUnitsCache() {
        log.info("Evicting available units cache");
    }

    @EventListener
    public void handleAvailableUnitsChanged(AvailableUnitsChangedEvent event) {
        log.info("Available units changed event received from: {}", event.getSource().getClass().getSimpleName());
        evictAvailableUnitsCache();
    }

    public Long refreshAvailableUnitsCount() {
        log.info("Refreshing available units cache");
        evictAvailableUnitsCache();
        return getAvailableUnitsCount();
    }

    public boolean isCacheHealthy() {
        try {
            getAvailableUnitsCount();
            return true;
        } catch (Exception e) {
            log.error("Cache health check failed", e);
            return false;
        }
    }
}