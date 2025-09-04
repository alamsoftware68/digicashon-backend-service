package com.repairapp.controller;

import com.repairapp.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Frontend-compatible REST controller
 * This controller provides APIs that match the frontend's data structure expectations
 */
@Tag(name = "Frontend APIs", description = "Frontend-compatible APIs")
@RestController
@RequestMapping("/frontend")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FrontendController {

    /**
     * Get laptop service categories for the frontend
     */
    @Operation(summary = "Get laptop service categories", description = "Get service categories structured for the laptop repair page")
    @GetMapping("/laptop-services")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLaptopServices() {
        List<Map<String, Object>> serviceCategories = new ArrayList<>();

        // Home Visit Category
        Map<String, Object> homeVisit = new HashMap<>();
        homeVisit.put("id", "home-visit");
        homeVisit.put("name", "Home Visit");
        homeVisit.put("icon", "🏠");
        
        List<Map<String, Object>> homeVisitServices = Arrays.asList(
            createService("Windows Laptop", 499, "Complete diagnosis and service at your home with certified technicians", 
                "/images/laptop-repair-1.jpg", "Popular", Arrays.asList("Free Diagnosis", "Same Day Service", "90 Days Warranty", "Certified Technicians")),
            createService("Desktop", 399, "Professional desktop service and maintenance at your location",
                "/images/laptop-repair-2.jpg", null, Arrays.asList("Hardware Check", "Cleaning Service", "60 Days Warranty", "Data Protection")),
            createService("MacBook", 999, "Specialized MacBook service and repair by Apple certified experts",
                "/images/laptop-repair-3.jpg", "Premium", Arrays.asList("Apple Certified", "Original Parts", "180 Days Warranty", "Data Recovery"))
        );
        homeVisit.put("services", homeVisitServices);
        serviceCategories.add(homeVisit);

        // Repair Category
        Map<String, Object> repair = new HashMap<>();
        repair.put("id", "repair");
        repair.put("name", "Repair");
        repair.put("icon", "🔧");
        
        List<Map<String, Object>> repairServices = Arrays.asList(
            createService("Software Issue", 799, "OS problems, virus removal, software conflicts and system optimization",
                "/images/laptop-repair-4.jpg", null, Arrays.asList("Virus Removal", "OS Repair", "Software Fix", "Performance Boost")),
            createService("Charging Issue", 899, "Battery replacement, charging port repair and power management",
                "/images/Laptop-service.jpg", "Quick Fix", Arrays.asList("Battery Test", "Port Repair", "Power Optimization", "Genuine Parts")),
            createService("Display Issue", 1499, "Screen replacement, display problems and LCD/LED repair",
                "/images/laptop-repair-1.jpg", null, Arrays.asList("Screen Replacement", "Color Calibration", "Brightness Fix", "HD Quality")),
            createService("Keyboard Issue", 699, "Key replacement, keyboard repair and functionality restoration",
                "/images/laptop-repair-2.jpg", null, Arrays.asList("Key Replacement", "Backlight Fix", "Deep Cleaning", "Full Testing")),
            createService("Overheating Issue", 899, "Fan cleaning, thermal management and cooling system optimization",
                "/images/laptop-repair-3.jpg", null, Arrays.asList("Fan Cleaning", "Thermal Paste", "Vent Cleaning", "Temperature Monitor")),
            createService("Physical Damage", 1999, "Body damage repair, structural fixes and cosmetic restoration",
                "/images/laptop-repair-4.jpg", "Expert Care", Arrays.asList("Body Repair", "Hinge Fix", "Cosmetic Restore", "Structural Support")),
            createService("Port Issue", 699, "USB, HDMI, audio and other port repairs with genuine components",
                "/images/Laptop-service.jpg", null, Arrays.asList("Port Replacement", "Connection Test", "Genuine Parts", "Full Functionality")),
            createService("Slow Performance", 799, "System optimization, speed improvement and performance enhancement",
                "/images/laptop-repair-1.jpg", "Speed Boost", Arrays.asList("System Cleanup", "Registry Fix", "Startup Optimization", "RAM Upgrade")),
            createService("Speaker Issue", 699, "Audio problems, speaker replacement and sound system repair",
                "/images/laptop-repair-2.jpg", null, Arrays.asList("Speaker Fix", "Audio Drivers", "Sound Quality", "Volume Control")),
            createService("Touchpad Issue", 599, "Trackpad repair, sensitivity adjustment and functionality restoration",
                "/images/laptop-repair-3.jpg", null, Arrays.asList("Touchpad Repair", "Sensitivity Fix", "Gesture Control", "Driver Update")),
            createService("Unknown Issue", 1299, "Complete diagnostic service to identify and resolve any laptop problem",
                "/images/laptop-repair-4.jpg", "Full Diagnostic", Arrays.asList("Complete Diagnosis", "Problem Identification", "Expert Solution", "Detailed Report"))
        );
        repair.put("services", repairServices);
        serviceCategories.add(repair);

        // Laptop/Desktop Service Category
        Map<String, Object> laptopDesktopService = new HashMap<>();
        laptopDesktopService.put("id", "laptop-desktop-service");
        laptopDesktopService.put("name", "Laptop/Desktop Service");
        laptopDesktopService.put("icon", "💻");
        
        List<Map<String, Object>> laptopDesktopServices = Arrays.asList(
            createService("Basic Service", 699, "Essential cleaning and basic maintenance for optimal performance",
                "/images/Laptop-service.jpg", null, Arrays.asList("External Cleaning", "Basic Maintenance", "Software Update", "30 Days Support")),
            createService("Advanced Service", 1299, "Deep cleaning, performance optimization and comprehensive maintenance",
                "/images/laptop-repair-1.jpg", "Recommended", Arrays.asList("Deep Cleaning", "Performance Tuning", "System Optimization", "90 Days Warranty")),
            createService("Gaming Laptop Service", 1599, "Specialized maintenance for gaming laptops with performance enhancement",
                "/images/laptop-repair-2.jpg", "Gaming Pro", Arrays.asList("GPU Optimization", "Cooling Enhancement", "Performance Mode", "Gaming Support")),
            createService("Business Laptop Service", 999, "Professional laptop service package for business and enterprise needs",
                "/images/laptop-repair-3.jpg", null, Arrays.asList("Security Setup", "Data Protection", "Professional Tools", "Business Support"))
        );
        laptopDesktopService.put("services", laptopDesktopServices);
        serviceCategories.add(laptopDesktopService);

        // Add-On Service Category
        Map<String, Object> addOnService = new HashMap<>();
        addOnService.put("id", "add-on-service");
        addOnService.put("name", "Add-On Service");
        addOnService.put("icon", "➕");
        
        List<Map<String, Object>> addOnServices = Arrays.asList(
            createService("Data Recovery", 1999, "Professional data recovery from damaged or corrupted storage devices",
                "/images/laptop-repair-4.jpg", "Data Safe", Arrays.asList("File Recovery", "Drive Repair", "99% Success Rate", "Secure Process")),
            createService("Software Installation", 299, "Operating system installation, software setup and driver configuration",
                "/images/Laptop-service.jpg", null, Arrays.asList("OS Installation", "Driver Setup", "Software Config", "License Activation")),
            createService("Hardware Upgrade", 799, "RAM, SSD, and other component upgrades to boost performance",
                "/images/laptop-repair-1.jpg", "Performance+", Arrays.asList("RAM Upgrade", "SSD Installation", "Component Replacement", "Compatibility Check")),
            createService("Antivirus Setup", 399, "Security software installation, configuration and system protection",
                "/images/laptop-repair-2.jpg", null, Arrays.asList("Antivirus Install", "Security Config", "Threat Removal", "Protection Setup")),
            createService("System Backup", 499, "Complete system backup and restore setup for data protection",
                "/images/laptop-repair-3.jpg", null, Arrays.asList("Full Backup", "Restore Setup", "Cloud Integration", "Automated Schedule"))
        );
        addOnService.put("services", addOnServices);
        serviceCategories.add(addOnService);

        return ResponseEntity.ok(ApiResponse.success("Laptop services retrieved successfully", serviceCategories));
    }

    /**
     * Get user profile data compatible with frontend
     */
    @Operation(summary = "Get user profile", description = "Get user profile data structured for the frontend")
    @GetMapping("/user-profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserProfile() {
        Map<String, Object> userProfile = new HashMap<>();
        
        userProfile.put("id", "1");
        userProfile.put("name", "Alam Khan");
        userProfile.put("email", "alam@example.com");
        userProfile.put("phone", "+91 7026666835");
        
        // Addresses
        List<Map<String, Object>> addresses = Arrays.asList(
            createAddress("1", "Home", "123 Main Street, Apt 4B", "New York", "NY", "10001", true),
            createAddress("2", "Work", "456 Business Ave, Suite 200", "New York", "NY", "10002", false)
        );
        userProfile.put("addresses", addresses);
        
        // Orders
        List<Map<String, Object>> orders = Arrays.asList(
            createOrder("ORD-2024-001", "Laptop Screen Repair", "Completed", "2024-01-15", 299.99, "Mike Johnson", "123 Main Street, Apt 4B"),
            createOrder("ORD-2024-002", "Phone Battery Replacement", "In Progress", "2024-01-20", 89.99, "Sarah Wilson", "123 Main Street, Apt 4B"),
            createOrder("ORD-2024-003", "Tablet Screen Repair", "Pending", "2024-01-25", 199.99, "Not Assigned", "456 Business Ave, Suite 200")
        );
        userProfile.put("orders", orders);
        
        // Payment Methods
        List<Map<String, Object>> paymentMethods = Arrays.asList(
            createPaymentMethod("1", "Credit Card", "4532", "Visa", "12", "2025", true),
            createPaymentMethod("2", "Credit Card", "8765", "MasterCard", "06", "2026", false)
        );
        userProfile.put("paymentMethods", paymentMethods);
        
        // Invoices
        List<Map<String, Object>> invoices = Arrays.asList(
            createInvoice("INV-2024-001", "ORD-2024-001", "2024-01-15", 299.99, "Paid", "Laptop Screen Repair"),
            createInvoice("INV-2024-002", "ORD-2024-002", "2024-01-20", 89.99, "Pending", "Phone Battery Replacement")
        );
        userProfile.put("invoices", invoices);
        
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", userProfile));
    }

    private Map<String, Object> createService(String name, int price, String description, String image, String badge, List<String> features) {
        Map<String, Object> service = new HashMap<>();
        service.put("name", name);
        service.put("price", price);
        service.put("description", description);
        service.put("image", image);
        if (badge != null) service.put("badge", badge);
        service.put("features", features);
        return service;
    }

    private Map<String, Object> createAddress(String id, String type, String address, String city, String state, String zipCode, boolean isDefault) {
        Map<String, Object> addr = new HashMap<>();
        addr.put("id", id);
        addr.put("type", type);
        addr.put("address", address);
        addr.put("city", city);
        addr.put("state", state);
        addr.put("zipCode", zipCode);
        addr.put("isDefault", isDefault);
        return addr;
    }

    private Map<String, Object> createOrder(String id, String service, String status, String date, double price, String technician, String address) {
        Map<String, Object> order = new HashMap<>();
        order.put("id", id);
        order.put("service", service);
        order.put("status", status);
        order.put("date", date);
        order.put("price", price);
        order.put("technician", technician);
        order.put("address", address);
        return order;
    }

    private Map<String, Object> createPaymentMethod(String id, String type, String last4, String brand, String expiryMonth, String expiryYear, boolean isDefault) {
        Map<String, Object> pm = new HashMap<>();
        pm.put("id", id);
        pm.put("type", type);
        pm.put("last4", last4);
        pm.put("brand", brand);
        pm.put("expiryMonth", expiryMonth);
        pm.put("expiryYear", expiryYear);
        pm.put("isDefault", isDefault);
        return pm;
    }

    private Map<String, Object> createInvoice(String id, String orderId, String date, double amount, String status, String service) {
        Map<String, Object> invoice = new HashMap<>();
        invoice.put("id", id);
        invoice.put("orderId", orderId);
        invoice.put("date", date);
        invoice.put("amount", amount);
        invoice.put("status", status);
        invoice.put("service", service);
        return invoice;
    }
}
