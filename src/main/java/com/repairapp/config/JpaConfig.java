package com.repairapp.config;

import org.springframework.context.annotation.Configuration;

/**
 * JPA Configuration
 *
 * JPA and transaction management is now handled by:
 * - PrimaryDataSourceConfig: Primary database (repairapp_db)
 * - AdminDataSourceConfig: Admin database (digicashon_admin_db) - read-only
 */
@Configuration
public class JpaConfig {
    // Multi-datasource configuration is handled in separate config classes
}
