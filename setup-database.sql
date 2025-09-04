-- MySQL Database Setup for RepairApp Backend
-- Execute this script in MySQL to set up the database

-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS laptoprepair 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Switch to the database
USE laptoprepair;

-- Grant privileges to root user (in case needed)
GRANT ALL PRIVILEGES ON laptoprepair.* TO 'root'@'localhost';
FLUSH PRIVILEGES;

-- Display confirmation
SELECT 'Database "laptoprepair" has been created successfully!' AS Status;
SELECT 'You can now start the Spring Boot application.' AS NextStep;
