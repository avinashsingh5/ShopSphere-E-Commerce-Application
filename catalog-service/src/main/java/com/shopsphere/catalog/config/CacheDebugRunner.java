package com.shopsphere.catalog.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheDebugRunner {

    @Bean
    CommandLineRunner cacheDebug(CacheManager cacheManager) {
        return args -> {
            System.out.println("CACHE MANAGER = " + cacheManager.getClass().getName());
            System.out.println("CACHE NAMES = " + cacheManager.getCacheNames());
        };
    }
}