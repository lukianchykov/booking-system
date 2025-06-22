package com.lukianchykov.bookingsystem.service;

import com.lukianchykov.bookingsystem.utils.AvailableUnitsChangedEvent;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CacheService {

    @Cacheable(value = "availableUnitsCount")
    public Long getAvailableUnitsCount() {
        log.info("Calculating available units count");
        throw new UnsupportedOperationException("Use CountAvailableUnitsService to calculate directly.");
    }

    @CacheEvict(value = "availableUnitsCount", allEntries = true)
    public void evictAvailableUnitsCache() {
        log.info("Evicting available units cache");
    }

    @EventListener
    public void handleAvailableUnitsChanged(AvailableUnitsChangedEvent event) {
        evictAvailableUnitsCache();
    }
}
