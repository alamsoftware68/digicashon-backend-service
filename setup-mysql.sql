-- MySQL Database Setup Script
-- Run this script before starting the Spring Boot application

-- Create the database
CREATE DATABASE IF NOT EXISTS repairapp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE repairapp_db;

-- Show current database
SELECT DATABASE() AS current_database;

-- Show tables (will be empty initially, Spring Boot will create them)
SHOW TABLES;

-- Verify the database is ready
SELECT 'MySQL database repairapp_db is ready for Spring Boot application' AS status;
