package com.repairapp.controller;

import com.repairapp.dto.response.ApiResponse;
import com.repairapp.entity.TempLead;
import com.repairapp.repository.TempLeadRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "Temp Leads", description = "Temp lead capture for non-logged-in users checking device price")
@RestController
@RequestMapping("/devices/temp-leads")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class TempLeadController {

    private final TempLeadRepository tempLeadRepository;

    @Operation(summary = "Save temp lead", description = "Called when non-logged-in user gets device price")
    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<TempLead>> saveTempLead(@RequestBody Map<String, Object> body) {
        TempLead lead = new TempLead();
        lead.setMobile(getString(body, "mobile"));
        lead.setSuperCategory(getString(body, "superCategory"));
        lead.setBrandName(getString(body, "brandName"));
        lead.setBrandModelName(getString(body, "brandModelName"));
        lead.setProcessorName(getString(body, "processorName"));
        lead.setRamLabel(getString(body, "ramLabel"));
        lead.setStorageLabel(getString(body, "storageLabel"));
        lead.setGraphicsName(getString(body, "graphicsName"));
        lead.setScreenSizeLabel(getString(body, "screenSizeLabel"));
        lead.setConditionCode(getString(body, "conditionCode"));
        lead.setAgeCode(getString(body, "ageCode"));
        if (body.get("finalPrice") != null) {
            lead.setFinalPrice(new BigDecimal(body.get("finalPrice").toString()));
        }
        if (body.get("evaluationId") != null) {
            lead.setEvaluationId(Long.parseLong(body.get("evaluationId").toString()));
        }

        // Remove any previous temp lead for this mobile (upsert behaviour)
        try { tempLeadRepository.deleteByMobile(lead.getMobile()); } catch (Exception ignored) {}

        TempLead saved = tempLeadRepository.save(lead);
        log.info("Temp lead saved for mobile: {}**, evalId: {}", lead.getMobile().substring(0, 4), lead.getEvaluationId());
        return ResponseEntity.ok(ApiResponse.success("Temp lead saved", saved));
    }

    @Operation(summary = "Get all temp leads", description = "Used by admin backend to fetch all temp leads")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<TempLead>>> getAllTempLeads() {
        List<TempLead> leads = tempLeadRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(ApiResponse.success("Temp leads retrieved", leads));
    }

    @Operation(summary = "Delete temp lead by mobile", description = "Called when user places an actual order")
    @DeleteMapping("/mobile/{mobile}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteByMobile(@PathVariable String mobile) {
        tempLeadRepository.deleteByMobile(mobile);
        log.info("Temp lead deleted for mobile: {}**", mobile.length() > 4 ? mobile.substring(0, 4) : mobile);
        return ResponseEntity.ok(ApiResponse.success("Temp lead removed", null));
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}
