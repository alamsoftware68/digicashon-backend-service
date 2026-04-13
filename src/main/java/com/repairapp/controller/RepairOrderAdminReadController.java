package com.repairapp.controller;

import com.repairapp.dto.order.RepairOrderAdminDto;
import com.repairapp.dto.response.ApiResponse;
import com.repairapp.entity.RepairOrder;
import com.repairapp.entity.User;
import com.repairapp.service.OrderService;
import com.repairapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read-only endpoint for admin portal to consume repair orders and user info.
 * Intentionally public (no auth) - consumed only by the internal admin backend service.
 */
@Tag(name = "Admin Read - Repair Orders", description = "Public read endpoints for admin portal integration")
@RestController
@RequestMapping("/admin-read")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RepairOrderAdminReadController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Get all repair orders", description = "Returns all repair orders for admin portal consumption")
    @GetMapping("/repair-orders")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<RepairOrderAdminDto>>> getAllRepairOrders() {
        List<RepairOrder> orders = orderService.getRecentOrders();
        List<RepairOrderAdminDto> dtos = orders.stream()
                .map(RepairOrderAdminDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Repair orders retrieved successfully", dtos));
    }

    @Operation(summary = "Get basic user info by ID", description = "Returns name, email and phone for a user (admin portal use)")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getUserBasicInfo(@PathVariable Long userId) {
        try {
            User user = userService.getUserDetails(userId);
            Map<String, String> info = new HashMap<>();
            info.put("id", String.valueOf(user.getId()));
            info.put("name", user.getName() != null ? user.getName() : "");
            info.put("email", user.getEmail() != null ? user.getEmail() : "");
            info.put("phone", user.getPhone() != null ? user.getPhone() : "");
            return ResponseEntity.ok(ApiResponse.success("User info retrieved", info));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success("User not found", new HashMap<>()));
        }
    }
}
