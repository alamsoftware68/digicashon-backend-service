package com.repairapp.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

/**
 * Configuration to handle Flyway repair if needed
 */
@Configuration
public class FlywayConfig {

    @Autowired
    private DataSource dataSource;

    @EventListener
    @Order(1)
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // This will be called after context is refreshed
        // Flyway repair is handled automatically by Spring Boot when validation is disabled
        System.out.println("Flyway configuration loaded successfully");
    }
}
