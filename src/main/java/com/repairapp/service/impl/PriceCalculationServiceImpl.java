package com.repairapp.service.impl;

import com.repairapp.adminread.entity.*;
import com.repairapp.adminread.repository.*;
import com.repairapp.dto.deviceeval.DeviceEvaluationRequest;
import com.repairapp.dto.deviceeval.DeviceEvaluationResponse;
import com.repairapp.entity.DeviceEvaluation;
import com.repairapp.entity.DeviceFunctionalIssueSelection;
import com.repairapp.entity.User;
import com.repairapp.exception.BadRequestException;
import com.repairapp.exception.ResourceNotFoundException;
import com.repairapp.repository.DeviceEvaluationRepository;
import com.repairapp.repository.UserRepository;
import com.repairapp.service.PriceCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of price calculation service
 */
@Service
@Transactional
public class PriceCalculationServiceImpl implements PriceCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(PriceCalculationServiceImpl.class);

    // Constants for pricing
    private static final BigDecimal INVOICE_PREMIUM = new BigDecimal("500.00");
    private static final BigDecimal BOX_PREMIUM = new BigDecimal("200.00");
    private static final BigDecimal PROCESSING_FEE = new BigDecimal("100.00");

    @Autowired
    private DeviceEvaluationRepository deviceEvaluationRepository;

    @Autowired
    private UserRepository userRepository;

    // Admin database repositories (read-only)
    @Autowired
    private AdminDeviceProcessorRepository processorRepository;

    @Autowired
    private AdminDeviceGraphicsRepository graphicsRepository;

    @Autowired
    private AdminDeviceRamOptionRepository ramRepository;

    @Autowired
    private AdminDeviceStorageOptionRepository storageRepository;

    @Autowired
    private AdminDeviceScreenSizeRepository screenSizeRepository;

    @Autowired
    private AdminDeviceConditionMultiplierRepository conditionRepository;

    @Autowired
    private AdminDeviceAgeDepreciationRepository ageRepository;

    @Autowired
    private AdminDeviceFunctionalIssueRepository issueRepository;

    @Autowired
    private AdminBrandRepository brandRepository;

    @Autowired
    private AdminBrandModelRepository brandModelRepository;

    @Override
    public DeviceEvaluationResponse calculatePrice(DeviceEvaluationRequest request) {
        logger.info("Calculating price for device evaluation - Brand Model ID: {}", request.getBrandModelId());

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        // Fetch brand and model to get base price
        AdminBrand brand = brandRepository.findByIdAndEnabledTrue(request.getBrandId())
                .orElseThrow(() -> new BadRequestException("Brand not found or disabled"));

        AdminBrandModel brandModel = brandModelRepository.findByIdAndEnabledTrue(request.getBrandModelId())
                .orElseThrow(() -> new BadRequestException("Brand model not found or disabled"));

        BigDecimal basePrice = brandModel.getBasePrice();
        logger.info("Base price from model: {}", basePrice);

        // Step 1: Calculate specification quality score (average of selected component scores, 0-100)
        BigDecimal specImpact = calculateSpecificationImpact(request);
        logger.info("Specification quality score: {}%", specImpact);

        // Step 2: Apply spec quality score as percentage of base price
        // specScore 100 = full base price, 80 = 80% of base price, etc.
        BigDecimal priceAfterSpecs = basePrice.multiply(specImpact.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        logger.info("Price after spec score (basePrice x specScore%): {}", priceAfterSpecs);

        // Step 3: Apply condition multiplier
        AdminDeviceConditionMultiplier condition = conditionRepository
                .findByConditionCodeAndEnabledTrue(request.getConditionCode())
                .orElseThrow(() -> new BadRequestException("Condition not found or disabled"));

        BigDecimal conditionMultiplier = condition.getMultiplierPercentage().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal priceAfterCondition = priceAfterSpecs.multiply(conditionMultiplier);
        logger.info("Condition multiplier: {}, Price after condition: {}", conditionMultiplier, priceAfterCondition);

        // Step 4: Apply age depreciation
        AdminDeviceAgeDepreciation age = ageRepository
                .findByAgeCodeAndEnabledTrue(request.getAgeCode())
                .orElseThrow(() -> new BadRequestException("Age depreciation not found or disabled"));

        BigDecimal ageDepreciation = age.getDepreciationPercentage().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal priceAfterAge = priceAfterCondition.multiply(ageDepreciation);
        logger.info("Age depreciation: {}, Price after age: {}", ageDepreciation, priceAfterAge);

        // Step 5: Calculate issue deductions
        BigDecimal issueDeductions = BigDecimal.ZERO;
        List<DeviceFunctionalIssueSelection> functionalIssues = new ArrayList<>();

        if (request.getFunctionalIssueIds() != null && !request.getFunctionalIssueIds().isEmpty()) {
            for (Long issueId : request.getFunctionalIssueIds()) {
                AdminDeviceFunctionalIssue issue = issueRepository.findByIdAndEnabledTrue(issueId)
                        .orElseThrow(() -> new BadRequestException("Functional issue not found or disabled: " + issueId));

                issueDeductions = issueDeductions.add(issue.getPriceDeduction());

                // Create functional issue selection
                DeviceFunctionalIssueSelection selection = new DeviceFunctionalIssueSelection(
                        issue.getId(),
                        issue.getIssueCode(),
                        issue.getIssueLabel(),
                        issue.getPriceDeduction(),
                        issue.getSeverity()
                );
                functionalIssues.add(selection);
            }
        }
        logger.info("Total issue deductions: {}", issueDeductions);

        // Step 6: Calculate accessory premiums
        BigDecimal invoicePremium = Boolean.TRUE.equals(request.getHasInvoice()) ? INVOICE_PREMIUM : BigDecimal.ZERO;
        BigDecimal boxPremium = Boolean.TRUE.equals(request.getHasBox()) ? BOX_PREMIUM : BigDecimal.ZERO;
        logger.info("Invoice premium: {}, Box premium: {}", invoicePremium, boxPremium);

        // Step 7: Calculate final price
        BigDecimal finalPrice = priceAfterAge
                .subtract(issueDeductions)
                .add(invoicePremium)
                .add(boxPremium)
                .subtract(PROCESSING_FEE);

        // Ensure final price is not negative
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        // Cap final price at base price — never exceed the "GET PRICE UPTO" amount shown to the user
        if (finalPrice.compareTo(basePrice) > 0) {
            finalPrice = basePrice;
        }

        finalPrice = finalPrice.setScale(2, RoundingMode.HALF_UP);
        logger.info("Final calculated price (capped at basePrice {}): {}", basePrice, finalPrice);

        // Create and save device evaluation
        DeviceEvaluation evaluation = new DeviceEvaluation();
        evaluation.setUser(user);
        evaluation.setSuperCategory(request.getSuperCategory());
        evaluation.setCategory(request.getCategory());
        evaluation.setBrandId(request.getBrandId());
        evaluation.setBrandName(brand.getName());
        evaluation.setBrandModelId(request.getBrandModelId());
        evaluation.setBrandModelName(brandModel.getName());

        // Set system configuration
        if (request.getProcessorId() != null) {
            AdminDeviceProcessor processor = processorRepository.findByIdAndEnabledTrue(request.getProcessorId())
                    .orElseThrow(() -> new BadRequestException("Processor not found or disabled"));
            evaluation.setProcessorId(processor.getId());
            evaluation.setProcessorName(processor.getProcessorName());
        }

        if (request.getGraphicsId() != null) {
            AdminDeviceGraphics graphics = graphicsRepository.findByIdAndEnabledTrue(request.getGraphicsId())
                    .orElse(null);
            if (graphics != null) {
                evaluation.setGraphicsId(graphics.getId());
                evaluation.setGraphicsName(graphics.getGraphicsName());
            }
        }

        if (request.getRamId() != null) {
            AdminDeviceRamOption ram = ramRepository.findByIdAndEnabledTrue(request.getRamId())
                    .orElseThrow(() -> new BadRequestException("RAM option not found or disabled"));
            evaluation.setRamId(ram.getId());
            evaluation.setRamLabel(ram.getCapacityLabel());
        }

        if (request.getStorageId() != null) {
            AdminDeviceStorageOption storage = storageRepository.findByIdAndEnabledTrue(request.getStorageId())
                    .orElseThrow(() -> new BadRequestException("Storage option not found or disabled"));
            evaluation.setStorageId(storage.getId());
            evaluation.setStorageLabel(storage.getStorageLabel());
        }

        if (request.getScreenSizeId() != null) {
            AdminDeviceScreenSize screenSize = screenSizeRepository.findByIdAndEnabledTrue(request.getScreenSizeId())
                    .orElse(null);
            if (screenSize != null) {
                evaluation.setScreenSizeId(screenSize.getId());
                evaluation.setScreenSizeLabel(screenSize.getSizeLabel());
            }
        }

        // Set condition and age
        evaluation.setConditionCode(request.getConditionCode());
        evaluation.setHasInvoice(request.getHasInvoice());
        evaluation.setHasBox(request.getHasBox());
        evaluation.setAgeCode(request.getAgeCode());
        evaluation.setAgeMonths(request.getAgeMonths());

        // Set price breakdown
        evaluation.setBasePrice(basePrice);
        evaluation.setSpecImpact(specImpact);
        evaluation.setConditionMultiplier(conditionMultiplier);
        evaluation.setAgeDepreciation(ageDepreciation);
        evaluation.setIssueDeductions(issueDeductions);
        evaluation.setInvoicePremium(invoicePremium);
        evaluation.setBoxPremium(boxPremium);
        evaluation.setProcessingFee(PROCESSING_FEE);
        evaluation.setFinalPrice(finalPrice);

        evaluation.setStatus(DeviceEvaluation.EvaluationStatus.PENDING);
        evaluation.setNotes(request.getNotes());

        // Add functional issues
        for (DeviceFunctionalIssueSelection issue : functionalIssues) {
            evaluation.addFunctionalIssue(issue);
        }

        // Save evaluation
        DeviceEvaluation savedEvaluation = deviceEvaluationRepository.save(evaluation);
        logger.info("Device evaluation saved with ID: {}", savedEvaluation.getId());

        return DeviceEvaluationResponse.fromEntity(savedEvaluation);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceEvaluationResponse recalculatePrice(Long evaluationId) {
        logger.info("Recalculating price for evaluation ID: {}", evaluationId);

        DeviceEvaluation evaluation = deviceEvaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceEvaluation", "id", evaluationId));

        // Create request from existing evaluation
        DeviceEvaluationRequest request = new DeviceEvaluationRequest();
        request.setUserId(evaluation.getUser().getId());
        request.setSuperCategory(evaluation.getSuperCategory());
        request.setCategory(evaluation.getCategory());
        request.setBrandId(evaluation.getBrandId());
        request.setBrandModelId(evaluation.getBrandModelId());
        request.setProcessorId(evaluation.getProcessorId());
        request.setGraphicsId(evaluation.getGraphicsId());
        request.setRamId(evaluation.getRamId());
        request.setStorageId(evaluation.getStorageId());
        request.setScreenSizeId(evaluation.getScreenSizeId());
        request.setConditionCode(evaluation.getConditionCode());
        request.setHasInvoice(evaluation.getHasInvoice());
        request.setHasBox(evaluation.getHasBox());
        request.setAgeCode(evaluation.getAgeCode());
        request.setAgeMonths(evaluation.getAgeMonths());

        // Extract functional issue IDs
        List<Long> issueIds = new ArrayList<>();
        for (DeviceFunctionalIssueSelection issue : evaluation.getFunctionalIssues()) {
            issueIds.add(issue.getIssueId());
        }
        request.setFunctionalIssueIds(issueIds);
        request.setNotes(evaluation.getNotes());

        return calculatePrice(request);
    }

    /**
     * Calculate average specification quality score (0-100) from selected components.
     * Each component's priceImpact represents its quality score (100 = best, lower = lesser spec).
     * Returns the average across all selected components. If no specs selected, returns 100 (full base price).
     */
    private BigDecimal calculateSpecificationImpact(DeviceEvaluationRequest request) {
        BigDecimal totalScore = BigDecimal.ZERO;
        int count = 0;

        // Processor quality score
        if (request.getProcessorId() != null) {
            AdminDeviceProcessor processor = processorRepository.findByIdAndEnabledTrue(request.getProcessorId())
                    .orElseThrow(() -> new BadRequestException("Processor not found or disabled"));
            totalScore = totalScore.add(processor.getPriceImpact());
            count++;
        }

        // Graphics quality score (optional)
        if (request.getGraphicsId() != null) {
            AdminDeviceGraphics graphics = graphicsRepository.findByIdAndEnabledTrue(request.getGraphicsId())
                    .orElse(null);
            if (graphics != null) {
                totalScore = totalScore.add(graphics.getPriceImpact());
                count++;
            }
        }

        // RAM quality score
        if (request.getRamId() != null) {
            AdminDeviceRamOption ram = ramRepository.findByIdAndEnabledTrue(request.getRamId())
                    .orElseThrow(() -> new BadRequestException("RAM option not found or disabled"));
            totalScore = totalScore.add(ram.getPriceImpact());
            count++;
        }

        // Storage quality score
        if (request.getStorageId() != null) {
            AdminDeviceStorageOption storage = storageRepository.findByIdAndEnabledTrue(request.getStorageId())
                    .orElseThrow(() -> new BadRequestException("Storage option not found or disabled"));
            totalScore = totalScore.add(storage.getPriceImpact());
            count++;
        }

        // Screen size quality score (optional)
        if (request.getScreenSizeId() != null) {
            AdminDeviceScreenSize screenSize = screenSizeRepository.findByIdAndEnabledTrue(request.getScreenSizeId())
                    .orElse(null);
            if (screenSize != null) {
                totalScore = totalScore.add(screenSize.getPriceImpact());
                count++;
            }
        }

        // If no specs selected, return 100 (use full base price)
        if (count == 0) {
            return new BigDecimal("100");
        }

        return totalScore.divide(new BigDecimal(count), 4, RoundingMode.HALF_UP);
    }
}
