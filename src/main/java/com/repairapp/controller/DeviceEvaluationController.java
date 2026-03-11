package com.repairapp.controller;

import com.repairapp.dto.deviceeval.DeviceEvaluationRequest;
import com.repairapp.dto.deviceeval.DeviceEvaluationResponse;
import com.repairapp.dto.response.ApiResponse;
import com.repairapp.entity.DeviceEvaluation;
import com.repairapp.repository.DeviceEvaluationRepository;
import com.repairapp.service.PriceCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for device evaluation and price calculation
 */
@Tag(name = "Device Evaluation", description = "Device evaluation and price calculation APIs")
@RestController
@RequestMapping("/devices/evaluations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DeviceEvaluationController {

    @Autowired
    private PriceCalculationService priceCalculationService;

    @Autowired
    private DeviceEvaluationRepository deviceEvaluationRepository;

    @Operation(summary = "Calculate device price", description = "Calculate price for a device based on specifications and condition")
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<DeviceEvaluationResponse>> calculatePrice(
            @Valid @RequestBody DeviceEvaluationRequest request) {
        DeviceEvaluationResponse response = priceCalculationService.calculatePrice(request);
        return ResponseEntity.ok(ApiResponse.success("Price calculated successfully", response));
    }

    @Operation(summary = "Get evaluation by ID", description = "Retrieve a specific device evaluation by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeviceEvaluationResponse>> getEvaluationById(@PathVariable Long id) {
        DeviceEvaluation evaluation = deviceEvaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));
        DeviceEvaluationResponse response = DeviceEvaluationResponse.fromEntity(evaluation);
        return ResponseEntity.ok(ApiResponse.success("Evaluation retrieved successfully", response));
    }

    @Operation(summary = "Get user evaluations", description = "Get all evaluations for a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<DeviceEvaluationResponse>>> getUserEvaluations(
            @PathVariable Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DeviceEvaluation> evaluationPage;

        if (status != null && !status.isEmpty()) {
            DeviceEvaluation.EvaluationStatus evalStatus = DeviceEvaluation.EvaluationStatus.valueOf(status);
            List<DeviceEvaluation> evaluations = deviceEvaluationRepository
                    .findByUser_IdAndStatusOrderByCreatedAtDesc(userId, evalStatus);
            List<DeviceEvaluationResponse> responses = evaluations.stream()
                    .map(DeviceEvaluationResponse::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success("User evaluations retrieved successfully", responses));
        } else {
            evaluationPage = deviceEvaluationRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
            List<DeviceEvaluationResponse> responses = evaluationPage.getContent().stream()
                    .map(DeviceEvaluationResponse::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success("User evaluations retrieved successfully", responses));
        }
    }

    @Operation(summary = "Approve evaluation", description = "Approve a device evaluation (user accepts the price)")
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<DeviceEvaluationResponse>> approveEvaluation(@PathVariable Long id) {
        DeviceEvaluation evaluation = deviceEvaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));
        evaluation.setStatus(DeviceEvaluation.EvaluationStatus.APPROVED);
        DeviceEvaluation updated = deviceEvaluationRepository.save(evaluation);
        DeviceEvaluationResponse response = DeviceEvaluationResponse.fromEntity(updated);
        return ResponseEntity.ok(ApiResponse.success("Evaluation approved successfully", response));
    }

    @Operation(summary = "Reject evaluation", description = "Reject a device evaluation (user declines the price)")
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<DeviceEvaluationResponse>> rejectEvaluation(@PathVariable Long id) {
        DeviceEvaluation evaluation = deviceEvaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));
        evaluation.setStatus(DeviceEvaluation.EvaluationStatus.REJECTED);
        DeviceEvaluation updated = deviceEvaluationRepository.save(evaluation);
        DeviceEvaluationResponse response = DeviceEvaluationResponse.fromEntity(updated);
        return ResponseEntity.ok(ApiResponse.success("Evaluation rejected successfully", response));
    }

    @Operation(summary = "Recalculate evaluation price", description = "Recalculate price for an existing evaluation")
    @PostMapping("/{id}/recalculate")
    public ResponseEntity<ApiResponse<DeviceEvaluationResponse>> recalculatePrice(@PathVariable Long id) {
        DeviceEvaluationResponse response = priceCalculationService.recalculatePrice(id);
        return ResponseEntity.ok(ApiResponse.success("Price recalculated successfully", response));
    }

    @Operation(summary = "Get all evaluations", description = "Get all device evaluations (admin use)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceEvaluationResponse>>> getAllEvaluations(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (status != null && !status.isEmpty()) {
            DeviceEvaluation.EvaluationStatus evalStatus = DeviceEvaluation.EvaluationStatus.valueOf(status);
            List<DeviceEvaluation> evaluations = deviceEvaluationRepository.findByStatusOrderByCreatedAtDesc(evalStatus);
            List<DeviceEvaluationResponse> responses = evaluations.stream()
                    .map(DeviceEvaluationResponse::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success("Evaluations retrieved successfully", responses));
        } else {
            Pageable pageable = PageRequest.of(page, size);
            Page<DeviceEvaluation> evaluationPage = deviceEvaluationRepository.findAll(pageable);
            List<DeviceEvaluationResponse> responses = evaluationPage.getContent().stream()
                    .map(DeviceEvaluationResponse::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success("Evaluations retrieved successfully", responses));
        }
    }
}
