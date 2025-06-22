package com.lukianchykov.bookingsystem.config;

import com.lukianchykov.bookingsystem.service.CacheService;
import org.mockito.Mockito;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class BookingSystemTestConfiguration {

    @Bean(name = "testCacheManager")
    @Primary
    public CacheService mockCacheService() {
        return Mockito.mock(CacheService.class);
    }
}