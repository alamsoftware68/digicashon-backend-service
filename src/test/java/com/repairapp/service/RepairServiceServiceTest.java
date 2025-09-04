package com.repairapp.service;

import com.repairapp.entity.RepairService;
import com.repairapp.repository.RepairServiceRepository;
import com.repairapp.service.RepairServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RepairServiceServiceTest {

    @Mock
    private RepairServiceRepository repairServiceRepository;

    @InjectMocks
    private RepairServiceService repairServiceService;

    private RepairService laptopService;
    private RepairService mobileService;
    private RepairService expensiveService;

    @BeforeEach
    void setUp() {
        laptopService = new RepairService();
        laptopService.setId(1L);
        laptopService.setName("Laptop Screen Repair");
        laptopService.setDescription("Professional laptop screen replacement service");
        laptopService.setDeviceType(RepairService.DeviceType.LAPTOP);
        laptopService.setBasePrice(new BigDecimal("75.00"));
        laptopService.setEstimatedDurationHours(2);
        laptopService.setIsActive(true);
        laptopService.setIsFeatured(true);

        mobileService = new RepairService();
        mobileService.setId(2L);
        mobileService.setName("Mobile Battery Replacement");
        mobileService.setDescription("Quick mobile battery replacement");
        mobileService.setDeviceType(RepairService.DeviceType.MOBILE);
        mobileService.setBasePrice(new BigDecimal("35.00"));
        mobileService.setEstimatedDurationHours(1);
        mobileService.setIsActive(true);
        mobileService.setIsFeatured(false);

        expensiveService = new RepairService();
        expensiveService.setId(3L);
        expensiveService.setName("Premium Motherboard Repair");
        expensiveService.setDescription("High-end motherboard repair service");
        expensiveService.setDeviceType(RepairService.DeviceType.LAPTOP);
        expensiveService.setBasePrice(new BigDecimal("150.00"));
        expensiveService.setEstimatedDurationHours(4);
        expensiveService.setIsActive(true);
        expensiveService.setIsFeatured(false);
    }

    @Test
    void getAllServices_ShouldReturnAllActiveServices() {
        // Given
        List<RepairService> expectedServices = Arrays.asList(laptopService, mobileService, expensiveService);
        when(repairServiceRepository.findByIsActiveOrderByDeviceTypeAsc(true)).thenReturn(expectedServices);

        // When
        List<RepairService> actualServices = repairServiceService.getAllServices();

        // Then
        assertThat(actualServices).hasSize(3);
        assertThat(actualServices).containsExactlyElementsOf(expectedServices);
    }

    @Test
    void getServicesByDeviceType_ShouldReturnServicesForSpecificDevice() {
        // Given
        List<RepairService> laptopServices = Arrays.asList(laptopService, expensiveService);
        when(repairServiceRepository.findByDeviceTypeAndIsActive(RepairService.DeviceType.LAPTOP, true))
                .thenReturn(laptopServices);

        // When
        List<RepairService> actualServices = repairServiceService.getServicesByDeviceType(RepairService.DeviceType.LAPTOP);

        // Then
        assertThat(actualServices).hasSize(2);
        assertThat(actualServices).containsExactly(laptopService, expensiveService);
    }

    @Test
    void searchWithFilters_ShouldFilterByPriceRange() {
        // Given
        List<RepairService> affordableServices = Arrays.asList(laptopService, mobileService);
        when(repairServiceRepository.findWithFilters(
                isNull(), isNull(), eq(new BigDecimal("0")), eq(new BigDecimal("100")), isNull(), eq("price_asc")))
                .thenReturn(affordableServices);

        // When
        List<RepairService> actualServices = repairServiceService.searchWithFilters(
                null, null, new BigDecimal("0"), new BigDecimal("100"), null, "price_asc");

        // Then
        assertThat(actualServices).hasSize(2);
        assertThat(actualServices).containsExactly(laptopService, mobileService);
    }

    @Test
    void searchWithFilters_ShouldFilterByDeviceTypeAndDuration() {
        // Given
        List<RepairService> quickMobileServices = Arrays.asList(mobileService);
        when(repairServiceRepository.findWithFilters(
                isNull(), eq(RepairService.DeviceType.MOBILE), isNull(), isNull(), eq(2), eq("duration_asc")))
                .thenReturn(quickMobileServices);

        // When
        List<RepairService> actualServices = repairServiceService.searchWithFilters(
                null, RepairService.DeviceType.MOBILE, null, null, 2, "duration_asc");

        // Then
        assertThat(actualServices).hasSize(1);
        assertThat(actualServices.get(0).getDeviceType()).isEqualTo(RepairService.DeviceType.MOBILE);
        assertThat(actualServices.get(0).getEstimatedDurationHours()).isLessThanOrEqualTo(2);
    }

    @Test
    void getFeaturedServices_ShouldReturnOnlyFeaturedServices() {
        // Given
        List<RepairService> featuredServices = Arrays.asList(laptopService);
        when(repairServiceRepository.findFeaturedServices()).thenReturn(featuredServices);

        // When
        List<RepairService> actualServices = repairServiceService.getFeaturedServices();

        // Then
        assertThat(actualServices).hasSize(1);
        assertThat(actualServices.get(0).getIsFeatured()).isTrue();
    }

    @Test
    void getServiceById_ShouldReturnCorrectService() {
        // Given
        when(repairServiceRepository.findById(1L)).thenReturn(Optional.of(laptopService));

        // When
        RepairService actualService = repairServiceService.getServiceById(1L);

        // Then
        assertThat(actualService).isNotNull();
        assertThat(actualService.getId()).isEqualTo(1L);
        assertThat(actualService.getName()).isEqualTo("Laptop Screen Repair");
    }

    @Test
    void searchServices_ShouldReturnMatchingServices() {
        // Given
        List<RepairService> searchResults = Arrays.asList(laptopService);
        when(repairServiceRepository.findByNameContainingIgnoreCaseAndIsActive("screen"))
                .thenReturn(searchResults);

        // When
        List<RepairService> actualServices = repairServiceService.searchServices("screen");

        // Then
        assertThat(actualServices).hasSize(1);
        assertThat(actualServices.get(0).getName()).containsIgnoringCase("screen");
    }

    @Test
    void getServicesByPriceRange_ShouldReturnServicesInRange() {
        // Given
        List<RepairService> midRangeServices = Arrays.asList(laptopService);
        when(repairServiceRepository.findByPriceRange(new BigDecimal("50"), new BigDecimal("100")))
                .thenReturn(midRangeServices);

        // When
        List<RepairService> actualServices = repairServiceService.getServicesByPriceRange(
                new BigDecimal("50"), new BigDecimal("100"));

        // Then
        assertThat(actualServices).hasSize(1);
        assertThat(actualServices.get(0).getBasePrice())
                .isBetween(new BigDecimal("50"), new BigDecimal("100"));
    }

    @Test
    void getServicesByMaxDuration_ShouldReturnQuickServices() {
        // Given
        List<RepairService> quickServices = Arrays.asList(mobileService, laptopService);
        when(repairServiceRepository.findByMaxDuration(2)).thenReturn(quickServices);

        // When
        List<RepairService> actualServices = repairServiceService.getServicesByMaxDuration(2);

        // Then
        assertThat(actualServices).hasSize(2);
        actualServices.forEach(service -> 
            assertThat(service.getEstimatedDurationHours()).isLessThanOrEqualTo(2));
    }

    @Test
    void getServiceStatistics_ShouldReturnStatisticsObject() {
        // Given
        when(repairServiceRepository.count()).thenReturn(3L);
        when(repairServiceRepository.countByIsActive(true)).thenReturn(3L);
        when(repairServiceRepository.countByDeviceTypeAndIsActive(RepairService.DeviceType.LAPTOP, true)).thenReturn(2L);
        when(repairServiceRepository.countByDeviceTypeAndIsActive(RepairService.DeviceType.MOBILE, true)).thenReturn(1L);
        when(repairServiceRepository.countByDeviceTypeAndIsActive(RepairService.DeviceType.TABLET, true)).thenReturn(0L);
        when(repairServiceRepository.countByDeviceTypeAndIsActive(RepairService.DeviceType.WATCH, true)).thenReturn(0L);

        // Mock price range counts
        when(repairServiceRepository.countByPriceRangeAndIsActive(any(), any(), eq(true))).thenReturn(1L);
        when(repairServiceRepository.countByPriceGreaterThanAndIsActive(any(), eq(true))).thenReturn(1L);

        // When
        Object statistics = repairServiceService.getServiceStatistics();

        // Then
        assertThat(statistics).isNotNull();
        // Additional assertions can be added to verify the statistics structure
    }
}
