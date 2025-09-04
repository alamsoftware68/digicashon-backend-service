package com.repairapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration
 * 
 * This configuration ensures proper setup of JPA repositories and auditing
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.repairapp.repository",
    enableDefaultTransactions = true
)
@EnableJpaAuditing(auditorAwareRef = "auditingHandler")
@EnableTransactionManagement
public class JpaConfig {
    // JPA configuration is handled through application.properties
    // This class ensures proper annotation-based configuration
}
