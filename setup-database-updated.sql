-- DigiCashOn Repair Service - Updated Database Schema
-- MySQL Database Setup Script

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS laptoprepair;
USE laptoprepair;

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS service_features;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS repair_orders;
DROP TABLE IF EXISTS payment_methods;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS repair_services;
DROP TABLE IF EXISTS service_categories;
DROP TABLE IF EXISTS users;

-- Create Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('CUSTOMER', 'TECHNICIAN', 'ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create Service Categories table
CREATE TABLE service_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    icon VARCHAR(10),
    display_order INT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    device_type ENUM('LAPTOP', 'MOBILE', 'TABLET', 'WATCH'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create Repair Services table
CREATE TABLE repair_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    device_type ENUM('LAPTOP', 'MOBILE', 'TABLET', 'WATCH') NOT NULL,
    base_price DECIMAL(12, 2) NOT NULL,
    estimated_duration_hours INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    image_url VARCHAR(255),
    badge VARCHAR(50),
    category_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (category_id) REFERENCES service_categories(id)
);

-- Create Service Features table (for service features list)
CREATE TABLE service_features (
    service_id BIGINT NOT NULL,
    feature VARCHAR(255) NOT NULL,
    FOREIGN KEY (service_id) REFERENCES repair_services(id) ON DELETE CASCADE
);

-- Create Addresses table
CREATE TABLE addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(6) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create Payment Methods table
CREATE TABLE payment_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type ENUM('CREDIT_CARD', 'DEBIT_CARD', 'DIGITAL_WALLET', 'BANK_TRANSFER') NOT NULL,
    last_four VARCHAR(4) NOT NULL,
    brand VARCHAR(50),
    expiry_month VARCHAR(2),
    expiry_year VARCHAR(4),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    cardholder_name VARCHAR(100),
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create Repair Orders table
CREATE TABLE repair_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(20) NOT NULL UNIQUE,
    status ENUM('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    total_price DECIMAL(12, 2) NOT NULL,
    problem_description TEXT,
    solution_description TEXT,
    scheduled_date TIMESTAMP NULL,
    completed_date TIMESTAMP NULL,
    pickup_address VARCHAR(500) NOT NULL,
    customer_notes VARCHAR(500),
    technician_notes VARCHAR(500),
    user_id BIGINT NOT NULL,
    service_id BIGINT NULL, -- For backward compatibility
    technician_id BIGINT NULL,
    address_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (service_id) REFERENCES repair_services(id),
    FOREIGN KEY (technician_id) REFERENCES users(id),
    FOREIGN KEY (address_id) REFERENCES addresses(id)
);

-- Create Order Items table (for multiple services per order)
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(12, 2) NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL,
    notes VARCHAR(500),
    order_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (order_id) REFERENCES repair_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES repair_services(id)
);

-- Create Invoices table
CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(20) NOT NULL UNIQUE,
    amount DECIMAL(12, 2) NOT NULL,
    tax_amount DECIMAL(12, 2) DEFAULT 0.00,
    discount_amount DECIMAL(12, 2) DEFAULT 0.00,
    status ENUM('PENDING', 'PAID', 'OVERDUE', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    due_date TIMESTAMP NOT NULL,
    paid_date TIMESTAMP NULL,
    notes TEXT,
    payment_reference VARCHAR(100),
    order_id BIGINT NOT NULL,
    payment_method_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (order_id) REFERENCES repair_orders(id),
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
);

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_orders_user_id ON repair_orders(user_id);
CREATE INDEX idx_orders_status ON repair_orders(status);
CREATE INDEX idx_orders_created_at ON repair_orders(created_at);
CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_payment_methods_user_id ON payment_methods(user_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_invoices_order_id ON invoices(order_id);
CREATE INDEX idx_services_device_type ON repair_services(device_type);
CREATE INDEX idx_services_category_id ON repair_services(category_id);
