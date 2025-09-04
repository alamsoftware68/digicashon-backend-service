-- Sample data for DigiCashOn Repair Service Application
-- This file will be executed on application startup for MySQL

-- Insert sample users (passwords are BCrypt encoded for 'admin123')
-- Using INSERT IGNORE to prevent duplicate key errors on restart
INSERT IGNORE INTO users (id, username, name, email, password, phone, role, is_active, email_verified, phone_verified, created_at, updated_at, version) VALUES
(1, 'admin', 'Alam Khan', 'alam@repairapp.com', '$2a$10$3euPa5H/Xx4zUXTOIB19ReO9I6sbQOYJTjHdMxzLdMzEWo0p4XVdO', '+91 7026666835', 'ADMIN', true, true, true, NOW(), NOW(), 0),
(2, 'mike_tech', 'Mike Johnson', 'mike@repairapp.com', '$2a$10$3euPa5H/Xx4zUXTOIB19ReO9I6sbQOYJTjHdMxzLdMzEWo0p4XVdO', '+91 9876543210', 'TECHNICIAN', true, true, true, NOW(), NOW(), 0),
(3, 'sarah_tech', 'Sarah Wilson', 'sarah@repairapp.com', '$2a$10$3euPa5H/Xx4zUXTOIB19ReO9I6sbQOYJTjHdMxzLdMzEWo0p4XVdO', '+91 9876543211', 'TECHNICIAN', true, true, true, NOW(), NOW(), 0),
(4, 'customer1', 'John Doe', 'john@example.com', '$2a$10$3euPa5H/Xx4zUXTOIB19ReO9I6sbQOYJTjHdMxzLdMzEWo0p4XVdO', '+91 9876543212', 'CUSTOMER', true, true, false, NOW(), NOW(), 0);

-- Insert sample repair services
INSERT IGNORE INTO repair_services (id, name, description, device_type, base_price, estimated_duration_hours, is_active, image_url, created_at, updated_at, version) VALUES
-- Laptop Services
(1, 'Screen Repair', 'Professional laptop screen replacement with original parts', 'LAPTOP', 299.99, 4, true, '/images/laptop-screen-repair.jpg', NOW(), NOW(), 0),
(2, 'Keyboard Replacement', 'Complete keyboard replacement for all laptop models', 'LAPTOP', 149.99, 2, true, '/images/laptop-keyboard-repair.jpg', NOW(), NOW(), 0),
(3, 'Battery Replacement', 'Laptop battery replacement with 1 year warranty', 'LAPTOP', 199.99, 1, true, '/images/laptop-battery-repair.jpg', NOW(), NOW(), 0),
(4, 'Motherboard Repair', 'Complex motherboard diagnosis and repair', 'LAPTOP', 499.99, 8, true, '/images/laptop-motherboard-repair.jpg', NOW(), NOW(), 0),
(5, 'Hard Drive Replacement', 'SSD/HDD replacement with data recovery', 'LAPTOP', 249.99, 3, true, '/images/laptop-hdd-repair.jpg', NOW(), NOW(), 0),

-- Mobile Services
(6, 'Screen Replacement', 'Mobile screen replacement for all brands', 'MOBILE', 129.99, 1, true, '/images/mobile-screen-repair.jpg', NOW(), NOW(), 0),
(7, 'Battery Replacement', 'Mobile battery replacement', 'MOBILE', 89.99, 1, true, '/images/mobile-battery-repair.jpg', NOW(), NOW(), 0),
(8, 'Camera Repair', 'Front and rear camera repair/replacement', 'MOBILE', 119.99, 2, true, '/images/mobile-camera-repair.jpg', NOW(), NOW(), 0),
(9, 'Charging Port Repair', 'Charging port cleaning and replacement', 'MOBILE', 79.99, 1, true, '/images/mobile-port-repair.jpg', NOW(), NOW(), 0),

-- Tablet Services
(10, 'Screen Replacement', 'Tablet screen replacement with touch functionality', 'TABLET', 199.99, 3, true, '/images/tablet-screen-repair.jpg', NOW(), NOW(), 0),
(11, 'Battery Service', 'Tablet battery replacement and optimization', 'TABLET', 139.99, 2, true, '/images/tablet-battery-repair.jpg', NOW(), NOW(), 0),
(12, 'Software Issues', 'Tablet software troubleshooting and repair', 'TABLET', 99.99, 2, true, '/images/tablet-software-repair.jpg', NOW(), NOW(), 0),

-- Watch Services
(13, 'Battery Replacement', 'Smartwatch battery replacement', 'WATCH', 69.99, 1, true, '/images/watch-battery-repair.jpg', NOW(), NOW(), 0),
(14, 'Screen Repair', 'Smartwatch display repair', 'WATCH', 149.99, 2, true, '/images/watch-screen-repair.jpg', NOW(), NOW(), 0),
(15, 'Water Damage Repair', 'Smartwatch water damage restoration', 'WATCH', 179.99, 4, true, '/images/watch-water-repair.jpg', NOW(), NOW(), 0);

-- Insert sample addresses
INSERT IGNORE INTO addresses (id, type, address, city, state, zip_code, is_default, user_id, created_at, updated_at, version) VALUES
(1, 'Home', '123 Main Street, Apt 4B', 'New York', 'NY', '10001', true, 1, NOW(), NOW(), 0),
(2, 'Work', '456 Business Ave, Suite 200', 'New York', 'NY', '10002', false, 1, NOW(), NOW(), 0),
(3, 'Home', '789 Oak Street', 'Los Angeles', 'CA', '90210', true, 4, NOW(), NOW(), 0);

-- Insert sample payment methods
INSERT IGNORE INTO payment_methods (id, type, last_four, brand, expiry_month, expiry_year, is_default, is_active, cardholder_name, user_id, created_at, updated_at, version) VALUES
(1, 'CREDIT_CARD', '4532', 'Visa', '12', '2025', true, true, 'Alam Khan', 1, NOW(), NOW(), 0),
(2, 'CREDIT_CARD', '8765', 'MasterCard', '06', '2026', false, true, 'Alam Khan', 1, NOW(), NOW(), 0),
(3, 'CREDIT_CARD', '1234', 'Visa', '03', '2027', true, true, 'John Doe', 4, NOW(), NOW(), 0);

-- Insert sample orders
INSERT IGNORE INTO repair_orders (id, order_number, status, total_price, problem_description, pickup_address, customer_notes, user_id, service_id, technician_id, created_at, updated_at, version) VALUES
(1, 'ORD-2024-001', 'COMPLETED', 299.99, 'Laptop screen is cracked and not displaying properly', '123 Main Street, Apt 4B, New York, NY 10001', 'Handle with care, laptop is still under warranty', 1, 1, 2, '2024-01-15 10:00:00', NOW(), 0),
(2, 'ORD-2024-002', 'IN_PROGRESS', 89.99, 'Phone battery drains very quickly', '123 Main Street, Apt 4B, New York, NY 10001', 'Phone is 2 years old', 1, 7, 3, '2024-01-20 14:30:00', NOW(), 0),
(3, 'ORD-2024-003', 'PENDING', 199.99, 'Tablet screen has dead pixels', '456 Business Ave, Suite 200, New York, NY 10002', 'Work device, need it back ASAP', 1, 10, NULL, '2024-01-25 09:15:00', NOW(), 0);

-- Update order completion and scheduled dates
UPDATE repair_orders SET completed_date = '2024-01-16 16:00:00', technician_notes = 'Screen replaced successfully. Tested for 2 hours.' WHERE id = 1;
UPDATE repair_orders SET scheduled_date = '2024-01-22 10:00:00', technician_notes = 'Battery replacement in progress. Expected completion today.' WHERE id = 2;
UPDATE repair_orders SET scheduled_date = '2024-01-26 11:00:00' WHERE id = 3;

-- Insert sample invoices
INSERT IGNORE INTO invoices (id, invoice_number, amount, tax_amount, discount_amount, status, due_date, paid_date, notes, payment_reference, order_id, payment_method_id, created_at, updated_at, version) VALUES
(1, 'INV-2024-001', 299.99, 24.00, 0.00, 'PAID', '2024-02-15 23:59:59', '2024-01-16 18:00:00', 'Payment received via credit card', 'TXN_001_SUCCESS', 1, 1, '2024-01-15 10:00:00', NOW(), 0),
(2, 'INV-2024-002', 89.99, 7.20, 0.00, 'PENDING', '2024-02-20 23:59:59', NULL, 'Payment pending', NULL, 2, NULL, '2024-01-20 14:30:00', NOW(), 0);

-- Generate additional order numbers for the current year
-- This ensures order numbers are unique and realistic
INSERT IGNORE INTO repair_orders (order_number, status, total_price, problem_description, pickup_address, user_id, service_id, created_at, updated_at, version) VALUES
('ORD-2024-004', 'PENDING', 149.99, 'Laptop keyboard keys are not working', '789 Oak Street, Los Angeles, CA 90210', 4, 2, NOW(), NOW(), 0),
('ORD-2024-005', 'CONFIRMED', 69.99, 'Smartwatch not charging properly', '789 Oak Street, Los Angeles, CA 90210', 4, 13, NOW(), NOW(), 0);
