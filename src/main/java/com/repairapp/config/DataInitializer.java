package com.repairapp.config;

import com.repairapp.entity.RepairService;
import com.repairapp.repository.RepairServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Initialize sample data for development and testing
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local"}) // Only run in development profiles
public class DataInitializer implements CommandLineRunner {
    
    private final RepairServiceRepository repairServiceRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (repairServiceRepository.count() == 0) {
            initializeRepairServices();
            log.info("Sample repair services initialized successfully");
        } else {
            log.info("Sample data already exists, skipping initialization");
        }
    }
    
    private void initializeRepairServices() {
        List<RepairService> sampleServices = Arrays.asList(
            // Mobile Services
            createService("Screen Replacement", "Professional mobile screen replacement with warranty", 
                         RepairService.DeviceType.MOBILE, "45.00", 2, true, true),
            createService("Battery Replacement", "Quick mobile battery replacement service", 
                         RepairService.DeviceType.MOBILE, "35.00", 1, true, false),
            createService("Charging Port Repair", "Fix charging port issues and connectivity problems", 
                         RepairService.DeviceType.MOBILE, "40.00", 1, true, false),
            createService("Water Damage Recovery", "Professional water damage assessment and repair", 
                         RepairService.DeviceType.MOBILE, "85.00", 4, true, true),
            createService("Camera Repair", "Front and rear camera repair services", 
                         RepairService.DeviceType.MOBILE, "55.00", 2, true, false),
            
            // Laptop Services
            createService("Screen Replacement", "High-quality laptop screen replacement", 
                         RepairService.DeviceType.LAPTOP, "120.00", 3, true, true),
            createService("Keyboard Replacement", "Complete keyboard replacement service", 
                         RepairService.DeviceType.LAPTOP, "75.00", 2, true, false),
            createService("Hard Drive Upgrade", "SSD/HDD upgrade and data migration", 
                         RepairService.DeviceType.LAPTOP, "95.00", 2, true, true),
            createService("RAM Upgrade", "Memory upgrade for better performance", 
                         RepairService.DeviceType.LAPTOP, "65.00", 1, true, false),
            createService("Motherboard Repair", "Complex motherboard diagnostics and repair", 
                         RepairService.DeviceType.LAPTOP, "180.00", 6, true, false),
            createService("Cooling System Cleaning", "Professional cooling system maintenance", 
                         RepairService.DeviceType.LAPTOP, "45.00", 1, true, false),
            
            // Tablet Services
            createService("Screen Replacement", "Tablet screen replacement with touch functionality", 
                         RepairService.DeviceType.TABLET, "85.00", 2, true, true),
            createService("Battery Replacement", "Tablet battery replacement service", 
                         RepairService.DeviceType.TABLET, "60.00", 2, true, false),
            createService("Charging Port Repair", "Tablet charging port repair and replacement", 
                         RepairService.DeviceType.TABLET, "50.00", 1, true, false),
            
            // Watch Services
            createService("Screen Replacement", "Smart watch screen replacement", 
                         RepairService.DeviceType.WATCH, "65.00", 1, true, true),
            createService("Battery Replacement", "Smart watch battery replacement", 
                         RepairService.DeviceType.WATCH, "45.00", 1, true, false),
            createService("Strap Replacement", "Watch strap replacement and fitting", 
                         RepairService.DeviceType.WATCH, "25.00", 1, true, false)
        );
        
        repairServiceRepository.saveAll(sampleServices);
    }
    
    private RepairService createService(String name, String description, RepairService.DeviceType deviceType,
                                       String price, int durationHours, boolean isActive, boolean isFeatured) {
        RepairService service = new RepairService();
        service.setName(name);
        service.setDescription(description);
        service.setDeviceType(deviceType);
        service.setBasePrice(new BigDecimal(price));
        service.setEstimatedDurationHours(durationHours);
        service.setIsActive(isActive);
        service.setIsFeatured(isFeatured);
        
        // Add some sample features
        if (name.contains("Screen")) {
            service.setFeatures(Arrays.asList("Warranty Included", "Original Parts", "Quick Service"));
            service.setBadge("Popular");
        } else if (name.contains("Battery")) {
            service.setFeatures(Arrays.asList("High Capacity", "Fast Service", "Warranty"));
            service.setBadge("Quick Fix");
        } else if (isFeatured) {
            service.setFeatures(Arrays.asList("Premium Service", "Expert Technician", "Extended Warranty"));
            service.setBadge("Premium");
        } else {
            service.setFeatures(Arrays.asList("Professional Service", "Quality Parts"));
        }
        
        return service;
    }
}
