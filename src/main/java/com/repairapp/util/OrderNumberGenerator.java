package com.repairapp.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Utility class for generating unique order and invoice numbers
 */
public class OrderNumberGenerator {

    private static final String ORDER_PREFIX = "ORD";
    private static final String INVOICE_PREFIX = "INV";
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private static final Random RANDOM = new Random();

    /**
     * Generate unique order number
     * Format: ORD-YYYY-NNNN (e.g., ORD-2024-0001)
     */
    public static String generateOrderNumber() {
        String year = LocalDateTime.now().format(YEAR_FORMATTER);
        int randomNumber = RANDOM.nextInt(9999) + 1;
        return String.format("%s-%s-%04d", ORDER_PREFIX, year, randomNumber);
    }

    /**
     * Generate unique invoice number
     * Format: INV-YYYY-NNNN (e.g., INV-2024-0001)
     */
    public static String generateInvoiceNumber() {
        String year = LocalDateTime.now().format(YEAR_FORMATTER);
        int randomNumber = RANDOM.nextInt(9999) + 1;
        return String.format("%s-%s-%04d", INVOICE_PREFIX, year, randomNumber);
    }

    /**
     * Generate tracking number for orders
     * Format: TRK-YYYYMMDD-NNNN (e.g., TRK-20240115-0001)
     */
    public static String generateTrackingNumber() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNumber = RANDOM.nextInt(9999) + 1;
        return String.format("TRK-%s-%04d", dateStr, randomNumber);
    }

    /**
     * Generate payment reference
     * Format: PAY-TIMESTAMP-RANDOM (e.g., PAY-1642248123-7832)
     */
    public static String generatePaymentReference() {
        long timestamp = System.currentTimeMillis() / 1000;
        int randomSuffix = RANDOM.nextInt(9999) + 1000;
        return String.format("PAY-%d-%d", timestamp, randomSuffix);
    }
}
