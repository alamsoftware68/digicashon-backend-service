-- Sample Data for DigiCashOn Repair Service - Updated Schema
USE laptoprepair;

-- Insert sample users
INSERT INTO users (username, name, email, password, phone, role, is_active, email_verified, phone_verified) VALUES
('john_customer', 'John Doe', 'john.doe@example.com', '$2a$10$TKh8H1.PfQx37YO8YSgOOuvpYdE7s.bVNjpgOON2vZXF/FS9AZoSu', '+91-9876543210', 'CUSTOMER', TRUE, TRUE, FALSE),
('jane_customer', 'Jane Smith', 'jane.smith@example.com', '$2a$10$TKh8H1.PfQx37YO8YSgOOuvpYdE7s.bVNjpgOON2vZXF/FS9AZoSu', '+91-9876543211', 'CUSTOMER', TRUE, TRUE, TRUE),
('tech_user', 'Tech Support', 'tech.support@digicashon.com', '$2a$10$TKh8H1.PfQx37YO8YSgOOuvpYdE7s.bVNjpgOON2vZXF/FS9AZoSu', '+91-9876543212', 'TECHNICIAN', TRUE, TRUE, TRUE),
('admin_user', 'Admin User', 'admin@digicashon.com', '$2a$10$TKh8H1.PfQx37YO8YSgOOuvpYdE7s.bVNjpgOON2vZXF/FS9AZoSu', '+91-9876543213', 'ADMIN', TRUE, TRUE, TRUE),
('test_user', 'Test Customer', 'test@example.com', '$2a$10$TKh8H1.PfQx37YO8YSgOOuvpYdE7s.bVNjpgOON2vZXF/FS9AZoSu', '+91-9876543214', 'CUSTOMER', TRUE, FALSE, FALSE);

-- Insert service categories
INSERT INTO service_categories (name, description, icon, display_order, is_active, device_type) VALUES
('Home Visit', 'On-site repair services at your location', '🏠', 1, TRUE, 'LAPTOP'),
('Hardware Repair', 'Comprehensive hardware repair services', '🔧', 2, TRUE, 'LAPTOP'),
('Software Services', 'Software installation and troubleshooting', '💻', 3, TRUE, 'LAPTOP'),
('Data Services', 'Data recovery and backup solutions', '💾', 4, TRUE, 'LAPTOP'),
('Mobile Repair', 'Screen and hardware repair for mobile devices', '📱', 1, TRUE, 'MOBILE'),
('Mobile Services', 'Software and data services for mobile devices', '📱', 2, TRUE, 'MOBILE'),
('Add-On Services', 'Additional services and accessories', '➕', 5, TRUE, 'LAPTOP');

-- Insert repair services
INSERT INTO repair_services (name, description, device_type, base_price, estimated_duration_hours, is_active, image_url, badge, category_id) VALUES
-- Laptop Home Visit Services
('Home Visit Diagnosis', 'Professional technician visit for laptop diagnosis', 'LAPTOP', 299.00, 2, TRUE, '/images/home-visit.jpg', 'Popular', 1),
('Home Visit Screen Repair', 'On-site laptop screen replacement service', 'LAPTOP', 4999.00, 3, TRUE, '/images/screen-repair.jpg', 'Fast', 1),
('Home Visit Keyboard Repair', 'On-site keyboard replacement and repair', 'LAPTOP', 1999.00, 2, TRUE, '/images/keyboard-repair.jpg', NULL, 1),

-- Laptop Hardware Repair
('Screen Replacement', 'Complete laptop screen replacement with warranty', 'LAPTOP', 4500.00, 4, TRUE, '/images/screen-replacement.jpg', 'Popular', 2),
('Battery Replacement', 'Original laptop battery replacement service', 'LAPTOP', 2999.00, 2, TRUE, '/images/battery-replacement.jpg', 'Fast', 2),
('Keyboard Replacement', 'Full keyboard assembly replacement', 'LAPTOP', 1899.00, 3, TRUE, '/images/keyboard-replacement.jpg', NULL, 2),
('Hard Drive Upgrade', 'SSD/HDD upgrade and data migration', 'LAPTOP', 3999.00, 5, TRUE, '/images/hdd-upgrade.jpg', 'Upgrade', 2),
('RAM Upgrade', 'Memory upgrade for better performance', 'LAPTOP', 2499.00, 2, TRUE, '/images/ram-upgrade.jpg', 'Upgrade', 2),
('Motherboard Repair', 'Motherboard diagnosis and repair service', 'LAPTOP', 7999.00, 8, TRUE, '/images/motherboard-repair.jpg', NULL, 2),
('Port Repair', 'USB, HDMI, charging port repair', 'LAPTOP', 1999.00, 3, TRUE, '/images/port-repair.jpg', NULL, 2),

-- Laptop Software Services
('OS Installation', 'Fresh operating system installation and setup', 'LAPTOP', 999.00, 3, TRUE, '/images/os-install.jpg', 'Fast', 3),
('Virus Removal', 'Complete malware and virus removal service', 'LAPTOP', 799.00, 2, TRUE, '/images/virus-removal.jpg', 'Popular', 3),
('Software Troubleshooting', 'Diagnosis and fixing software issues', 'LAPTOP', 699.00, 2, TRUE, '/images/software-troubleshoot.jpg', NULL, 3),
('Driver Installation', 'Hardware driver update and installation', 'LAPTOP', 499.00, 1, TRUE, '/images/driver-install.jpg', NULL, 3),

-- Laptop Data Services
('Data Recovery', 'Recover lost or corrupted data from storage', 'LAPTOP', 2999.00, 6, TRUE, '/images/data-recovery.jpg', 'Critical', 4),
('Data Backup', 'Complete system backup and migration', 'LAPTOP', 899.00, 3, TRUE, '/images/data-backup.jpg', NULL, 4),
('Data Transfer', 'Transfer data between old and new devices', 'LAPTOP', 699.00, 2, TRUE, '/images/data-transfer.jpg', 'Fast', 4),

-- Mobile Repair Services
('Mobile Screen Repair', 'Mobile phone screen replacement service', 'MOBILE', 2999.00, 2, TRUE, '/images/mobile-screen.jpg', 'Popular', 5),
('Mobile Battery Replace', 'Mobile phone battery replacement', 'MOBILE', 1499.00, 1, TRUE, '/images/mobile-battery.jpg', 'Fast', 5),
('Mobile Charging Port', 'Charging port repair for mobile devices', 'MOBILE', 1299.00, 2, TRUE, '/images/mobile-port.jpg', NULL, 5),
('Mobile Speaker Repair', 'Speaker and microphone repair service', 'MOBILE', 999.00, 1, TRUE, '/images/mobile-speaker.jpg', NULL, 5),

-- Mobile Software Services
('Mobile OS Repair', 'Mobile operating system repair and reset', 'MOBILE', 799.00, 2, TRUE, '/images/mobile-os.jpg', NULL, 6),
('Mobile Data Recovery', 'Recover lost data from mobile devices', 'MOBILE', 1999.00, 4, TRUE, '/images/mobile-data.jpg', 'Critical', 6),

-- Add-on Services
('Express Service', 'Priority service with faster turnaround', 'LAPTOP', 999.00, 0, TRUE, '/images/express.jpg', 'Priority', 7),
('Pickup & Drop', 'Free pickup and drop service within city limits', 'LAPTOP', 299.00, 0, TRUE, '/images/pickup.jpg', 'Convenient', 7),
('Extended Warranty', '1-year extended warranty on repair work', 'LAPTOP', 1499.00, 0, TRUE, '/images/warranty.jpg', 'Protection', 7),
('Device Cleaning', 'Professional laptop cleaning and maintenance', 'LAPTOP', 599.00, 1, TRUE, '/images/cleaning.jpg', NULL, 7);

-- Insert service features
INSERT INTO service_features (service_id, feature) VALUES
-- Home Visit Diagnosis features
(1, 'Professional technician visit'),
(1, 'Complete system diagnosis'),
(1, 'Detailed problem report'),
(1, 'No transportation required'),
(1, 'Same-day service available'),

-- Screen Replacement features
(4, 'Original quality display'),
(4, 'Professional installation'),
(4, '6 months warranty'),
(4, 'Color accuracy testing'),
(4, 'Dead pixel guarantee'),

-- Battery Replacement features
(5, 'Original manufacturer battery'),
(5, 'Capacity testing included'),
(5, '1 year warranty'),
(5, 'Quick 2-hour service'),
(5, 'Battery health optimization'),

-- OS Installation features
(11, 'Latest OS version'),
(11, 'Driver installation included'),
(11, 'Basic software setup'),
(11, 'System optimization'),
(11, 'Security updates applied'),

-- Data Recovery features
(15, 'Advanced recovery techniques'),
(15, 'No data, no charge policy'),
(15, 'Secure handling guarantee'),
(15, 'Up to 95% success rate'),
(15, 'Confidentiality assured');

-- Insert sample addresses
INSERT INTO addresses (type, address, city, state, zip_code, is_default, user_id) VALUES
('Home', '123 Main Street, Apartment 4B', 'Mumbai', 'Maharashtra', '400001', TRUE, 1),
('Office', '456 Business Park, Tower A, Floor 10', 'Mumbai', 'Maharashtra', '400051', FALSE, 1),
('Home', '789 Garden Lane, Villa 12', 'Pune', 'Maharashtra', '411001', TRUE, 2),
('Home', '321 Tech Hub, Block C', 'Bangalore', 'Karnataka', '560001', TRUE, 5),
('Office', '654 Innovation Drive, Building 7', 'Bangalore', 'Karnataka', '560025', FALSE, 5);

-- Insert sample payment methods
INSERT INTO payment_methods (type, last_four, brand, expiry_month, expiry_year, is_default, is_active, cardholder_name, user_id) VALUES
('CREDIT_CARD', '1234', 'VISA', '12', '2025', TRUE, TRUE, 'John Doe', 1),
('DEBIT_CARD', '5678', 'MASTERCARD', '08', '2026', FALSE, TRUE, 'John Doe', 1),
('CREDIT_CARD', '9012', 'VISA', '06', '2024', TRUE, TRUE, 'Jane Smith', 2),
('DIGITAL_WALLET', '3456', 'PAYTM', NULL, NULL, FALSE, TRUE, 'Jane Smith', 2),
('CREDIT_CARD', '7890', 'AMEX', '10', '2025', TRUE, TRUE, 'Test Customer', 5);

-- Insert sample repair orders
INSERT INTO repair_orders (order_number, status, total_price, problem_description, solution_description, scheduled_date, pickup_address, customer_notes, user_id, service_id, technician_id, address_id) VALUES
('ORD-2024-001', 'COMPLETED', 4999.00, 'Laptop screen is cracked and display is distorted', 'Replaced LCD screen with original part. Tested display quality and color accuracy.', '2024-01-15 10:00:00', '123 Main Street, Apartment 4B, Mumbai, Maharashtra, 400001', 'Please handle with care', 1, 4, 3, 1),
('ORD-2024-002', 'IN_PROGRESS', 3298.00, 'Laptop battery drains very quickly and sometimes shuts down', NULL, '2024-01-20 14:00:00', '789 Garden Lane, Villa 12, Pune, Maharashtra, 411001', 'Battery purchased 2 years ago', 2, 5, 3, 3),
('ORD-2024-003', 'PENDING', 2998.00, 'Mobile phone screen is completely black', NULL, '2024-01-25 11:00:00', '321 Tech Hub, Block C, Bangalore, Karnataka, 560001', 'Phone was dropped yesterday', 5, 18, NULL, 4),
('ORD-2024-004', 'CANCELLED', 1999.00, 'Keyboard keys are sticky and some keys not working', 'Customer cancelled before service', NULL, '456 Business Park, Tower A, Floor 10, Mumbai, Maharashtra, 400051', 'Spilled coffee on keyboard', 1, 6, NULL, 2);

-- Insert sample order items (for multi-service orders)
INSERT INTO order_items (quantity, unit_price, total_price, notes, order_id, service_id) VALUES
-- Order 1 (Screen replacement)
(1, 4500.00, 4500.00, 'Standard screen replacement', 1, 4),
(1, 499.00, 499.00, 'Driver update for new screen', 1, 14),

-- Order 2 (Battery + cleaning)
(1, 2999.00, 2999.00, 'Original battery replacement', 2, 5),
(1, 299.00, 299.00, 'Express service requested', 2, 23),

-- Order 3 (Mobile screen repair only)
(1, 2999.00, 2999.00, 'Emergency mobile repair', 3, 18),

-- Order 4 (Cancelled - Keyboard repair)
(1, 1899.00, 1899.00, 'Keyboard replacement', 4, 6),
(1, 100.00, 100.00, 'Service cancelled', 4, 24);

-- Insert sample invoices
INSERT INTO invoices (invoice_number, amount, tax_amount, discount_amount, status, due_date, paid_date, notes, payment_reference, order_id, payment_method_id) VALUES
('INV-2024-001', 4999.00, 899.82, 0.00, 'PAID', '2024-01-15 23:59:59', '2024-01-15 16:30:00', 'Payment received via credit card', 'TXN-CC-20240115001', 1, 1),
('INV-2024-002', 3298.00, 593.64, 200.00, 'PENDING', '2024-01-25 23:59:59', NULL, 'Battery replacement service - discount applied for repeat customer', NULL, 2, 3),
('INV-2024-003', 2998.00, 539.64, 0.00, 'PENDING', '2024-01-30 23:59:59', NULL, 'Mobile screen repair service', NULL, 3, 5),
('INV-2024-004', 1999.00, 359.82, 0.00, 'CANCELLED', '2024-01-20 23:59:59', NULL, 'Order cancelled by customer', NULL, 4, 1);

-- Show table contents for verification
SELECT 'Users' as Table_Name, COUNT(*) as Row_Count FROM users
UNION ALL
SELECT 'Service Categories', COUNT(*) FROM service_categories
UNION ALL  
SELECT 'Repair Services', COUNT(*) FROM repair_services
UNION ALL
SELECT 'Service Features', COUNT(*) FROM service_features
UNION ALL
SELECT 'Addresses', COUNT(*) FROM addresses
UNION ALL
SELECT 'Payment Methods', COUNT(*) FROM payment_methods
UNION ALL
SELECT 'Repair Orders', COUNT(*) FROM repair_orders
UNION ALL
SELECT 'Order Items', COUNT(*) FROM order_items
UNION ALL
SELECT 'Invoices', COUNT(*) FROM invoices;

-- Verify the data
SELECT 
    ro.order_number,
    u.name as customer_name,
    ro.status,
    ro.total_price,
    COUNT(oi.id) as service_count,
    GROUP_CONCAT(rs.name SEPARATOR ', ') as services
FROM repair_orders ro
JOIN users u ON ro.user_id = u.id
LEFT JOIN order_items oi ON ro.id = oi.order_id
LEFT JOIN repair_services rs ON oi.service_id = rs.id
GROUP BY ro.id, ro.order_number, u.name, ro.status, ro.total_price
ORDER BY ro.created_at DESC;
