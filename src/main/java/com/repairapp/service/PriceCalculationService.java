package com.repairapp.service;

import com.repairapp.dto.deviceeval.DeviceEvaluationRequest;
import com.repairapp.dto.deviceeval.DeviceEvaluationResponse;

/**
 * Service for calculating device prices based on specifications and condition
 */
public interface PriceCalculationService {

    /**
     * Calculate price for a device evaluation
     *
     * Formula:
     * FINAL_PRICE = BASE_PRICE
     *               + SPECIFICATION_IMPACT
     *               × CONDITION_MULTIPLIER
     *               × AGE_DEPRECIATION
     *               - ISSUE_DEDUCTIONS
     *               + ACCESSORY_PREMIUMS
     *               - PROCESSING_FEE
     *
     * @param request Device evaluation request with all specifications
     * @return Device evaluation response with calculated price and breakdown
     */
    DeviceEvaluationResponse calculatePrice(DeviceEvaluationRequest request);

    /**
     * Recalculate price for an existing evaluation
     *
     * @param evaluationId Existing evaluation ID
     * @return Updated evaluation response with recalculated price
     */
    DeviceEvaluationResponse recalculatePrice(Long evaluationId);
}
