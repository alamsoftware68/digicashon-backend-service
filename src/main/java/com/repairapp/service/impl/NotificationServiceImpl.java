package com.repairapp.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.repairapp.entity.DeviceEvaluation;
import com.repairapp.entity.OTPVerification;
import com.repairapp.entity.OrderItem;
import com.repairapp.entity.RepairOrder;
import com.repairapp.entity.User;
import com.repairapp.service.NotificationService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:info@digicashon.com}")
    private String fromEmail;

    @Value("${app.mail.admin:admin@example.com}")
    private String adminEmail;

    @Value("${app.name:DigiCashOn}")
    private String appName;

    // ─────────────────────────────────────────────────────────────
    // NEW: Order Placement Notifications
    // ─────────────────────────────────────────────────────────────

    @Override
    public void sendSellOrderEmails(DeviceEvaluation evaluation,
                                    String customerName, String customerEmail, String customerPhone) {
        try {
            Map<String, String> notes = parseNotes(evaluation.getNotes());
            String pickupAddress  = notes.getOrDefault("pickup address", "—");
            String addressType    = notes.getOrDefault("address type", "");
            String pickupDate     = notes.getOrDefault("pickup date", "—");
            String pickupTime     = notes.getOrDefault("pickup time", "—");
            String refNumber      = "DEV-" + evaluation.getId();

            byte[] pdfBytes = generateSellOrderPdf(evaluation, customerName, customerPhone,
                    pickupAddress, pickupDate, pickupTime, refNumber);

            // Customer email
            if (customerEmail != null && !customerEmail.isBlank()) {
                String html = buildSellOrderHtml(evaluation, customerName, pickupAddress,
                        addressType, pickupDate, pickupTime, refNumber, false);
                sendHtmlEmailWithAttachment(customerEmail,
                        "Your Sell Order Confirmed - DigiCashOn | " + refNumber,
                        html, pdfBytes, "SellOrder_" + refNumber + ".pdf");
                log.info("Sell order customer email sent to: {}", customerEmail);
            }

            // Admin email
            String adminHtml = buildSellOrderHtml(evaluation, customerName, pickupAddress,
                    addressType, pickupDate, pickupTime, refNumber, true);
            adminHtml = adminHtml.replace("<!-- CUSTOMER_CONTACT -->",
                    buildContactRow("Customer Email", customerEmail) +
                    buildContactRow("Customer Phone", customerPhone));
            sendHtmlEmailWithAttachment(adminEmail,
                    "[New Sell Lead] " + refNumber + " — " + customerName,
                    adminHtml, pdfBytes, "SellOrder_" + refNumber + ".pdf");
            log.info("Sell order admin email sent for: {}", refNumber);

        } catch (Exception e) {
            log.error("Failed to send sell order emails for evaluation: {}", evaluation.getId(), e);
        }
    }

    @Override
    public void sendRepairOrderEmails(RepairOrder order,
                                      String customerName, String customerEmail, String customerPhone) {
        try {
            byte[] pdfBytes = generateRepairOrderPdf(order, customerName, customerPhone);

            // Customer email
            if (customerEmail != null && !customerEmail.isBlank()) {
                String html = buildRepairOrderHtml(order, customerName, false);
                sendHtmlEmailWithAttachment(customerEmail,
                        "Repair Order Confirmed - DigiCashOn | " + order.getOrderNumber(),
                        html, pdfBytes, "RepairOrder_" + order.getOrderNumber() + ".pdf");
                log.info("Repair order customer email sent to: {}", customerEmail);
            }

            // Admin email
            String adminHtml = buildRepairOrderHtml(order, customerName, true);
            adminHtml = adminHtml.replace("<!-- CUSTOMER_CONTACT -->",
                    buildContactRow("Customer Email", customerEmail) +
                    buildContactRow("Customer Phone", customerPhone));
            sendHtmlEmailWithAttachment(adminEmail,
                    "[New Repair Lead] " + order.getOrderNumber() + " — " + customerName,
                    adminHtml, pdfBytes, "RepairOrder_" + order.getOrderNumber() + ".pdf");
            log.info("Repair order admin email sent for: {}", order.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to send repair order emails for order: {}", order.getOrderNumber(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HTML Builders
    // ─────────────────────────────────────────────────────────────

    private String buildSellOrderHtml(DeviceEvaluation ev, String customerName,
                                      String pickupAddress, String addressType,
                                      String pickupDate, String pickupTime,
                                      String refNumber, boolean isAdmin) {
        String badge = isAdmin ? "NEW SELL LEAD" : "SELL ORDER";
        String headline = isAdmin
                ? "&#128203; New Sell Order Received"
                : "&#10003; Your Sell Order is Confirmed!";

        StringBuilder specs = new StringBuilder();
        if (notBlank(ev.getProcessorName())) specs.append(detailRow("Processor", ev.getProcessorName()));
        if (notBlank(ev.getRamLabel()))       specs.append(detailRow("RAM", ev.getRamLabel()));
        if (notBlank(ev.getStorageLabel()))   specs.append(detailRow("Storage", ev.getStorageLabel()));
        if (notBlank(ev.getGraphicsName()))   specs.append(detailRow("Graphics", ev.getGraphicsName()));
        if (notBlank(ev.getScreenSizeLabel())) specs.append(detailRow("Screen Size", ev.getScreenSizeLabel()));

        String condition = ev.getConditionCode() != null ? capitalize(ev.getConditionCode().replace("-", " ")) : "—";
        String age = ev.getAgeCode() != null ? formatAge(ev.getAgeCode()) : "—";

        return emailWrapper(badge, headline,
                "<p style='margin:0 0 20px;font-size:14px;color:#374151;'>Dear <strong>" + esc(customerName) + "</strong>,</p>" +
                (isAdmin ? "<p style='margin:0 0 20px;font-size:14px;color:#6b7280;'>A customer has accepted the device evaluation offer. Details below.</p>"
                         : "<p style='margin:0 0 20px;font-size:14px;color:#6b7280;'>We have confirmed your request to sell your device. Our team will reach out to you shortly with next steps.</p>") +

                sectionHeader("DEVICE INFORMATION") +
                detailTable(
                    detailRow("Reference", refNumber) +
                    detailRow("Device", esc(ev.getBrandName()) + " " + esc(ev.getBrandModelName())) +
                    detailRow("Category", esc(ev.getSuperCategory()) + " / " + esc(ev.getCategory())) +
                    specs +
                    detailRow("Condition", condition) +
                    detailRow("Device Age", age) +
                    detailRow("Has Invoice", Boolean.TRUE.equals(ev.getHasInvoice()) ? "Yes" : "No") +
                    detailRow("Has Original Box", Boolean.TRUE.equals(ev.getHasBox()) ? "Yes" : "No")
                ) +

                priceBlock(ev.getFinalPrice() != null ? "₹" + ev.getFinalPrice().toPlainString() : "—") +

                sectionHeader("PICKUP DETAILS") +
                detailTable(
                    detailRow("Address", esc(pickupAddress)) +
                    (notBlank(addressType) ? detailRow("Address Type", capitalize(esc(addressType))) : "") +
                    detailRow("Pickup Date", esc(pickupDate)) +
                    detailRow("Time Slot", esc(pickupTime))
                ) +

                "<!-- CUSTOMER_CONTACT -->" +

                nextStepsBlock(isAdmin
                    ? new String[]{"Review the order in the admin dashboard", "Assign a pickup agent", "Contact customer to confirm pickup"}
                    : new String[]{"Our team will confirm your pickup slot", "A technician will arrive at your doorstep", "Payment released after device verification"})
        );
    }

    private String buildRepairOrderHtml(RepairOrder order, String customerName, boolean isAdmin) {
        String badge = isAdmin ? "NEW REPAIR LEAD" : "REPAIR ORDER";
        String headline = isAdmin
                ? "&#128203; New Repair Order Received"
                : "&#10003; Your Repair Order is Confirmed!";

        StringBuilder itemRows = new StringBuilder();
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                String name = notBlank(item.getServiceTitle()) ? item.getServiceTitle()
                        : notBlank(item.getServiceName()) ? item.getServiceName() : "Service";
                String cat = notBlank(item.getServiceCategory()) ? item.getServiceCategory() : "";
                itemRows.append(
                    "<tr>" +
                    "<td style='padding:9px 14px;border-bottom:1px solid #e5e7eb;font-size:13px;color:#111827;'>" + esc(name) + (notBlank(cat) ? "<br><span style='font-size:11px;color:#6b7280;'>" + esc(cat) + "</span>" : "") + "</td>" +
                    "<td style='padding:9px 14px;border-bottom:1px solid #e5e7eb;font-size:13px;color:#6b7280;text-align:center;'>" + item.getQuantity() + "</td>" +
                    "<td style='padding:9px 14px;border-bottom:1px solid #e5e7eb;font-size:13px;color:#6b7280;text-align:right;'>₹" + item.getUnitPrice() + "</td>" +
                    "<td style='padding:9px 14px;border-bottom:1px solid #e5e7eb;font-size:13px;font-weight:600;color:#16a34a;text-align:right;'>₹" + item.getTotalPrice() + "</td>" +
                    "</tr>"
                );
            }
        }

        String servicesTable = "<table width='100%' cellpadding='0' cellspacing='0' style='border:1px solid #e5e7eb;border-radius:6px;overflow:hidden;margin-bottom:20px;'>" +
                "<tr style='background:#374151;'>" +
                "<th style='padding:9px 14px;font-size:12px;color:#fff;text-align:left;'>Service</th>" +
                "<th style='padding:9px 14px;font-size:12px;color:#fff;text-align:center;'>Qty</th>" +
                "<th style='padding:9px 14px;font-size:12px;color:#fff;text-align:right;'>Unit Price</th>" +
                "<th style='padding:9px 14px;font-size:12px;color:#fff;text-align:right;'>Total</th>" +
                "</tr>" +
                itemRows +
                "<tr style='background:#f9fafb;'>" +
                "<td colspan='3' style='padding:10px 14px;font-size:13px;font-weight:600;color:#374151;text-align:right;'>TOTAL</td>" +
                "<td style='padding:10px 14px;font-size:15px;font-weight:700;color:#16a34a;text-align:right;'>₹" + order.getTotalPrice() + "</td>" +
                "</tr>" +
                "</table>";

        return emailWrapper(badge, headline,
                "<p style='margin:0 0 20px;font-size:14px;color:#374151;'>Dear <strong>" + esc(customerName) + "</strong>,</p>" +
                (isAdmin ? "<p style='margin:0 0 20px;font-size:14px;color:#6b7280;'>A new repair order has been placed. Details below.</p>"
                         : "<p style='margin:0 0 20px;font-size:14px;color:#6b7280;'>Your repair order has been received and is being processed. Here's your order summary.</p>") +

                sectionHeader("ORDER INFORMATION") +
                detailTable(
                    detailRow("Order Number", order.getOrderNumber()) +
                    detailRow("Status", "Pending — Awaiting Confirmation") +
                    detailRow("Problem", esc(order.getProblemDescription())) +
                    (notBlank(order.getCustomerNotes()) ? detailRow("Special Instructions", esc(order.getCustomerNotes())) : "")
                ) +

                sectionHeader("SERVICES BOOKED") +
                servicesTable +

                sectionHeader("PICKUP ADDRESS") +
                detailTable(detailRow("Address", esc(order.getPickupAddress()))) +

                "<!-- CUSTOMER_CONTACT -->" +

                nextStepsBlock(isAdmin
                    ? new String[]{"Review order in admin dashboard", "Assign a technician", "Contact customer to confirm appointment"}
                    : new String[]{"Our team will confirm your appointment", "A technician will arrive at your doorstep", "Device repaired with quality parts & warranty"})
        );
    }

    // ─────────────────────────────────────────────────────────────
    // PDF Generators
    // ─────────────────────────────────────────────────────────────

    private byte[] generateSellOrderPdf(DeviceEvaluation ev, String customerName, String customerPhone,
                                        String pickupAddress, String pickupDate, String pickupTime,
                                        String refNumber) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            doc.open();

            Color green = new Color(22, 163, 74);
            Color darkGray = new Color(55, 65, 81);
            Color lightGray = new Color(249, 250, 251);

            Font titleFont  = new Font(Font.HELVETICA, 22, Font.BOLD, Color.WHITE);
            Font subFont    = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(187, 247, 208));
            Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
            Font labelFont  = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(107, 114, 128));
            Font valueFont  = new Font(Font.HELVETICA, 10, Font.BOLD, darkGray);
            Font priceFont  = new Font(Font.HELVETICA, 20, Font.BOLD, green);

            // Header banner
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell hCell = new PdfPCell();
            hCell.setBackgroundColor(green);
            hCell.setPadding(18);
            hCell.setBorder(Rectangle.NO_BORDER);
            Paragraph title = new Paragraph("DigiCashOn", titleFont);
            title.add(Chunk.NEWLINE);
            title.add(new Chunk("Your Trusted Tech Partner", subFont));
            hCell.addElement(title);
            header.addCell(hCell);
            doc.add(header);

            doc.add(new Paragraph(" "));

            // Reference + type
            Paragraph ref = new Paragraph("SELL ORDER RECEIPT", new Font(Font.HELVETICA, 13, Font.BOLD, darkGray));
            ref.setSpacingAfter(2);
            doc.add(ref);
            doc.add(new Paragraph("Reference: " + refNumber, labelFont));
            doc.add(new Paragraph(" "));

            // Device details table
            addPdfSection(doc, "DEVICE INFORMATION", headerFont, green);
            PdfPTable details = new PdfPTable(new float[]{2f, 3f});
            details.setWidthPercentage(100);
            details.setSpacingBefore(4);
            addPdfRow(details, "Customer", customerName, labelFont, valueFont, lightGray, Color.WHITE);
            addPdfRow(details, "Phone", customerPhone, labelFont, valueFont, Color.WHITE, lightGray);
            addPdfRow(details, "Device", ev.getBrandName() + " " + ev.getBrandModelName(), labelFont, valueFont, lightGray, Color.WHITE);
            addPdfRow(details, "Category", ev.getSuperCategory() + " / " + ev.getCategory(), labelFont, valueFont, Color.WHITE, lightGray);
            if (notBlank(ev.getProcessorName())) addPdfRow(details, "Processor", ev.getProcessorName(), labelFont, valueFont, lightGray, Color.WHITE);
            if (notBlank(ev.getRamLabel()))       addPdfRow(details, "RAM", ev.getRamLabel(), labelFont, valueFont, Color.WHITE, lightGray);
            if (notBlank(ev.getStorageLabel()))   addPdfRow(details, "Storage", ev.getStorageLabel(), labelFont, valueFont, lightGray, Color.WHITE);
            String cond = ev.getConditionCode() != null ? capitalize(ev.getConditionCode().replace("-", " ")) : "—";
            addPdfRow(details, "Condition", cond, labelFont, valueFont, Color.WHITE, lightGray);
            doc.add(details);

            doc.add(new Paragraph(" "));

            // Offer price
            addPdfSection(doc, "CONFIRMED OFFER PRICE", headerFont, green);
            PdfPTable priceTable = new PdfPTable(1);
            priceTable.setWidthPercentage(100);
            priceTable.setSpacingBefore(4);
            PdfPCell priceCell = new PdfPCell(new Phrase(
                    ev.getFinalPrice() != null ? "\u20B9" + ev.getFinalPrice().toPlainString() : "—", priceFont));
            priceCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            priceCell.setPadding(14);
            priceCell.setBorderColor(green);
            priceTable.addCell(priceCell);
            doc.add(priceTable);

            doc.add(new Paragraph(" "));

            // Pickup details
            addPdfSection(doc, "PICKUP DETAILS", headerFont, green);
            PdfPTable pickup = new PdfPTable(new float[]{2f, 3f});
            pickup.setWidthPercentage(100);
            pickup.setSpacingBefore(4);
            addPdfRow(pickup, "Address", pickupAddress, labelFont, valueFont, lightGray, Color.WHITE);
            addPdfRow(pickup, "Pickup Date", pickupDate, labelFont, valueFont, Color.WHITE, lightGray);
            addPdfRow(pickup, "Time Slot", pickupTime, labelFont, valueFont, lightGray, Color.WHITE);
            doc.add(pickup);

            doc.add(new Paragraph(" "));

            // Footer
            Paragraph footer = new Paragraph("DigiCashOn | info@digicashon.com | digicashon.com",
                    new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(156, 163, 175)));
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate sell order PDF", e);
            return new byte[0];
        }
    }

    private byte[] generateRepairOrderPdf(RepairOrder order, String customerName, String customerPhone) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Color green = new Color(22, 163, 74);
            Color darkGray = new Color(55, 65, 81);
            Color lightGray = new Color(249, 250, 251);

            Font titleFont  = new Font(Font.HELVETICA, 22, Font.BOLD, Color.WHITE);
            Font subFont    = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(187, 247, 208));
            Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
            Font labelFont  = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(107, 114, 128));
            Font valueFont  = new Font(Font.HELVETICA, 10, Font.BOLD, darkGray);
            Font totalFont  = new Font(Font.HELVETICA, 13, Font.BOLD, green);

            // Header banner
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell hCell = new PdfPCell();
            hCell.setBackgroundColor(green);
            hCell.setPadding(18);
            hCell.setBorder(Rectangle.NO_BORDER);
            Paragraph title = new Paragraph("DigiCashOn", titleFont);
            title.add(Chunk.NEWLINE);
            title.add(new Chunk("Your Trusted Tech Partner", subFont));
            hCell.addElement(title);
            header.addCell(hCell);
            doc.add(header);

            doc.add(new Paragraph(" "));

            Paragraph ref = new Paragraph("REPAIR ORDER RECEIPT", new Font(Font.HELVETICA, 13, Font.BOLD, darkGray));
            ref.setSpacingAfter(2);
            doc.add(ref);
            doc.add(new Paragraph("Order Number: " + order.getOrderNumber(), labelFont));
            doc.add(new Paragraph(" "));

            // Customer + order info
            addPdfSection(doc, "ORDER INFORMATION", headerFont, green);
            PdfPTable info = new PdfPTable(new float[]{2f, 3f});
            info.setWidthPercentage(100);
            info.setSpacingBefore(4);
            addPdfRow(info, "Customer", customerName, labelFont, valueFont, lightGray, Color.WHITE);
            addPdfRow(info, "Phone", customerPhone, labelFont, valueFont, Color.WHITE, lightGray);
            addPdfRow(info, "Status", "Pending — Awaiting Confirmation", labelFont, valueFont, lightGray, Color.WHITE);
            if (notBlank(order.getProblemDescription()))
                addPdfRow(info, "Problem", order.getProblemDescription(), labelFont, valueFont, Color.WHITE, lightGray);
            if (notBlank(order.getPickupAddress()))
                addPdfRow(info, "Pickup Address", order.getPickupAddress(), labelFont, valueFont, lightGray, Color.WHITE);
            doc.add(info);

            doc.add(new Paragraph(" "));

            // Services table
            addPdfSection(doc, "SERVICES BOOKED", headerFont, green);
            PdfPTable services = new PdfPTable(new float[]{4f, 1f, 2f, 2f});
            services.setWidthPercentage(100);
            services.setSpacingBefore(4);

            Font thFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            for (String th : new String[]{"Service", "Qty", "Unit Price", "Total"}) {
                PdfPCell thCell = new PdfPCell(new Phrase(th, thFont));
                thCell.setBackgroundColor(darkGray);
                thCell.setPadding(8);
                thCell.setBorder(Rectangle.NO_BORDER);
                services.addCell(thCell);
            }

            boolean alt = false;
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    String name = notBlank(item.getServiceTitle()) ? item.getServiceTitle()
                            : notBlank(item.getServiceName()) ? item.getServiceName() : "Service";
                    Color bg = alt ? lightGray : Color.WHITE;
                    addServicesRow(services, name, String.valueOf(item.getQuantity()),
                            "\u20B9" + item.getUnitPrice(), "\u20B9" + item.getTotalPrice(),
                            labelFont, valueFont, bg);
                    alt = !alt;
                }
            }

            // Total row
            PdfPCell emptyCell = new PdfPCell(new Phrase(""));
            emptyCell.setBorder(Rectangle.NO_BORDER);
            emptyCell.setColspan(3);
            emptyCell.setPadding(8);
            emptyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            emptyCell.addElement(new Phrase("TOTAL", new Font(Font.HELVETICA, 10, Font.BOLD, darkGray)));
            services.addCell(emptyCell);

            PdfPCell totalCell = new PdfPCell(new Phrase("\u20B9" + order.getTotalPrice(), totalFont));
            totalCell.setBorder(Rectangle.NO_BORDER);
            totalCell.setPadding(8);
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            services.addCell(totalCell);

            doc.add(services);

            doc.add(new Paragraph(" "));

            Paragraph footer = new Paragraph("DigiCashOn | info@digicashon.com | digicashon.com",
                    new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(156, 163, 175)));
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate repair order PDF", e);
            return new byte[0];
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Email Helpers
    // ─────────────────────────────────────────────────────────────

    private void sendHtmlEmailWithAttachment(String to, String subject, String html,
                                             byte[] pdfBytes, String filename) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            if (pdfBytes != null && pdfBytes.length > 0) {
                helper.addAttachment(filename,
                        new org.springframework.core.io.ByteArrayResource(pdfBytes));
            }
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send HTML email with attachment to: {}", to, e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HTML Template Helpers
    // ─────────────────────────────────────────────────────────────

    private String emailWrapper(String badge, String headline, String body) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
                "<body style='margin:0;padding:0;background-color:#f0f4f8;font-family:Arial,Helvetica,sans-serif;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f0f4f8;'><tr><td align='center' style='padding:24px 12px;'>" +
                "<table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);'>" +
                // Header
                "<tr><td style='background-color:#16a34a;padding:26px 32px;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0'><tr>" +
                "<td><div style='font-size:24px;font-weight:700;color:#ffffff;'>DigiCashOn</div>" +
                "<div style='font-size:11px;color:#bbf7d0;margin-top:2px;'>Your Trusted Tech Partner</div></td>" +
                "<td align='right'><div style='background:rgba(255,255,255,0.2);color:#ffffff;padding:5px 12px;border-radius:16px;font-size:10px;font-weight:700;'>" + badge + "</div></td>" +
                "</tr></table></td></tr>" +
                // Banner
                "<tr><td style='background-color:#f0fdf4;border-bottom:1px solid #bbf7d0;padding:14px 32px;'>" +
                "<div style='font-size:15px;font-weight:600;color:#16a34a;'>" + headline + "</div>" +
                "</td></tr>" +
                // Body
                "<tr><td style='padding:24px 32px;'>" + body + "</td></tr>" +
                // Footer
                "<tr><td style='background-color:#111827;padding:22px 32px;text-align:center;'>" +
                "<div style='font-size:14px;font-weight:700;color:#ffffff;margin-bottom:4px;'>DigiCashOn</div>" +
                "<div style='font-size:11px;color:#9ca3af;margin-bottom:10px;'>Your Trusted Tech Partner</div>" +
                "<div style='font-size:11px;color:#6b7280;'>" +
                "<a href='mailto:info@digicashon.com' style='color:#4ade80;text-decoration:none;'>info@digicashon.com</a>" +
                " &nbsp;&middot;&nbsp; " +
                "<a href='https://digicashon.com' style='color:#4ade80;text-decoration:none;'>digicashon.com</a>" +
                "</div></td></tr>" +
                "</table></td></tr></table></body></html>";
    }

    private String sectionHeader(String text) {
        return "<div style='background-color:#374151;color:#ffffff;padding:8px 14px;font-size:12px;font-weight:700;" +
                "border-radius:4px 4px 0 0;margin-top:16px;'>" + text + "</div>";
    }

    private String detailTable(String rows) {
        return "<table width='100%' cellpadding='0' cellspacing='0' style='border:1px solid #e5e7eb;border-top:none;" +
                "border-radius:0 0 4px 4px;overflow:hidden;margin-bottom:20px;'>" + rows + "</table>";
    }

    private String detailRow(String label, String value) {
        return "<tr>" +
                "<td style='padding:9px 14px;border-bottom:1px solid #f3f4f6;font-size:12px;color:#6b7280;width:38%;'>" + label + "</td>" +
                "<td style='padding:9px 14px;border-bottom:1px solid #f3f4f6;font-size:13px;color:#111827;font-weight:500;'>" + (value != null ? value : "—") + "</td>" +
                "</tr>";
    }

    private String buildContactRow(String label, String value) {
        return detailRow(label, value != null ? value : "—");
    }

    private String priceBlock(String price) {
        return "<div style='background-color:#f0fdf4;border:2px solid #16a34a;border-radius:6px;padding:14px 24px;" +
                "margin:0 0 20px;text-align:center;'>" +
                "<div style='font-size:11px;color:#6b7280;margin-bottom:4px;letter-spacing:0.5px;'>CONFIRMED OFFER PRICE</div>" +
                "<div style='font-size:30px;font-weight:700;color:#16a34a;'>" + price + "</div>" +
                "</div>";
    }

    private String nextStepsBlock(String[] steps) {
        StringBuilder sb = new StringBuilder(
                "<div style='background-color:#f9fafb;border-radius:6px;padding:16px;margin-top:8px;'>" +
                "<div style='font-size:12px;font-weight:700;color:#374151;margin-bottom:10px;'>WHAT HAPPENS NEXT</div>"
        );
        for (int i = 0; i < steps.length; i++) {
            sb.append("<div style='display:flex;align-items:flex-start;margin-bottom:6px;font-size:13px;color:#374151;'>" +
                    "<span style='background:#16a34a;color:#fff;border-radius:50%;width:18px;height:18px;display:inline-block;" +
                    "text-align:center;line-height:18px;font-size:11px;font-weight:700;margin-right:8px;flex-shrink:0;'>" + (i + 1) + "</span>" +
                    "<span>" + steps[i] + "</span></div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────
    // PDF Helpers
    // ─────────────────────────────────────────────────────────────

    private void addPdfSection(Document doc, String text, Font font, Color bg) throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(8);
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(bg);
        c.setPadding(7);
        c.setBorder(Rectangle.NO_BORDER);
        t.addCell(c);
        doc.add(t);
    }

    private void addPdfRow(PdfPTable table, String label, String value,
                           Font lf, Font vf, Color bg1, Color bg2) {
        PdfPCell lc = new PdfPCell(new Phrase(label, lf));
        lc.setBackgroundColor(bg1);
        lc.setPadding(7);
        lc.setBorderColor(new Color(229, 231, 235));
        table.addCell(lc);
        PdfPCell vc = new PdfPCell(new Phrase(value != null ? value : "—", vf));
        vc.setBackgroundColor(bg2);
        vc.setPadding(7);
        vc.setBorderColor(new Color(229, 231, 235));
        table.addCell(vc);
    }

    private void addServicesRow(PdfPTable table, String name, String qty, String unit, String total,
                                Font lf, Font vf, Color bg) {
        for (String val : new String[]{name, qty, unit, total}) {
            Font f = val.equals(total) ? vf : lf;
            PdfPCell c = new PdfPCell(new Phrase(val, f));
            c.setBackgroundColor(bg);
            c.setPadding(7);
            c.setBorderColor(new Color(229, 231, 235));
            table.addCell(c);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Utility
    // ─────────────────────────────────────────────────────────────

    private Map<String, String> parseNotes(String notes) {
        Map<String, String> map = new HashMap<>();
        if (notes == null) return map;
        for (String line : notes.split("\n")) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                map.put(line.substring(0, colon).trim().toLowerCase(), line.substring(colon + 1).trim());
            }
        }
        return map;
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private String esc(String s) {
        if (s == null) return "—";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    private String formatAge(String ageCode) {
        if (ageCode == null) return "—";
        return switch (ageCode) {
            case "lt-1" -> "Less than 1 year";
            case "1-3"  -> "1–3 years";
            case "gt-3" -> "More than 3 years";
            default     -> ageCode;
        };
    }

    // ─────────────────────────────────────────────────────────────
    // Existing methods (unchanged)
    // ─────────────────────────────────────────────────────────────

    @Override
    public void sendOTPEmail(OTPVerification otp) {
        if (otp.getEmail() == null) {
            log.warn("Cannot send OTP email - email is null for OTP ID: {}", otp.getId());
            return;
        }
        String subject = getOTPEmailSubject(otp.getOtpType());
        String content = buildOTPEmailContent(otp);
        sendEmail(otp.getEmail(), subject, content);
        log.info("OTP email sent to: {} for type: {}", otp.getEmail(), otp.getOtpType());
    }

    @Override
    public void sendOTPSMS(OTPVerification otp) {
        if (otp.getPhone() == null) {
            log.warn("Cannot send OTP SMS - phone is null for OTP ID: {}", otp.getId());
            return;
        }
        sendSMS(otp.getPhone(), buildOTPSMSContent(otp));
        log.info("OTP SMS sent to: {} for type: {}", maskPhone(otp.getPhone()), otp.getOtpType());
    }

    @Override
    public void sendOTPNotification(OTPVerification otp) {
        if (otp.getEmail() != null) sendOTPEmail(otp);
        if (otp.getPhone() != null) sendOTPSMS(otp);
    }

    @Override
    public void sendOrderConfirmationEmail(RepairOrder order) {
        String subject = "Order Confirmation - " + order.getOrderNumber();
        sendEmail(order.getUser().getEmail(), subject, buildOrderConfirmationContent(order));
        log.info("Order confirmation email sent for order: {}", order.getOrderNumber());
    }

    @Override
    public void sendOrderStatusUpdateEmail(RepairOrder order, String previousStatus) {
        sendEmail(order.getUser().getEmail(), "Order Status Update - " + order.getOrderNumber(),
                buildOrderStatusUpdateContent(order, previousStatus));
    }

    @Override
    public void sendOrderCancellationEmail(RepairOrder order) {
        sendEmail(order.getUser().getEmail(), "Order Cancelled - " + order.getOrderNumber(),
                buildOrderCancellationContent(order));
    }

    @Override
    public void sendOrderCompletionEmail(RepairOrder order) {
        sendEmail(order.getUser().getEmail(), "Order Completed - " + order.getOrderNumber(),
                buildOrderCompletionContent(order));
    }

    @Override
    public void sendWelcomeEmail(User user) {
        sendEmail(user.getEmail(), "Welcome to " + appName, buildWelcomeEmailContent(user));
    }

    @Override
    public void sendPasswordResetEmail(User user, String resetToken) {
        sendEmail(user.getEmail(), "Password Reset Request", buildPasswordResetContent(user, resetToken));
    }

    @Override
    public void sendPasswordChangeConfirmationEmail(User user) {
        sendEmail(user.getEmail(), "Password Changed Successfully",
                buildPasswordChangeConfirmationContent(user));
    }

    @Override
    public void sendEmailChangeConfirmationEmail(User user, String newEmail) {
        sendEmail(newEmail, "Email Address Changed",
                buildEmailChangeConfirmationContent(user, newEmail));
    }

    @Override
    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    @Override
    public void sendSMS(String phoneNumber, String message) {
        log.info("SMS would be sent to {}: {}", maskPhone(phoneNumber), message);
    }

    @Override
    public void sendTemplatedEmail(String to, String templateName, Object templateData) {
        log.info("Templated email would be sent to {} using template: {}", to, templateName);
    }

    @Override
    public void sendTemplatedSMS(String phoneNumber, String templateName, Object templateData) {
        log.info("Templated SMS would be sent to {} using template: {}", maskPhone(phoneNumber), templateName);
    }

    @Override
    public void sendBulkEmail(List<String> recipients, String subject, String content) {
        recipients.forEach(r -> sendEmail(r, subject, content));
    }

    @Override
    public void sendBulkSMS(List<String> phoneNumbers, String message) {
        phoneNumbers.forEach(p -> sendSMS(p, message));
    }

    @Override
    public boolean shouldSendEmailNotification(User user, NotificationType type) {
        return user.getEmail() != null && !user.getEmail().isEmpty();
    }

    @Override
    public boolean shouldSendSMSNotification(User user, NotificationType type) {
        return user.getPhone() != null && !user.getPhone().isEmpty();
    }

    // Content builders for legacy plain-text emails
    private String getOTPEmailSubject(OTPVerification.OTPType otpType) {
        return switch (otpType) {
            case EMAIL_VERIFICATION      -> "Verify Your Email Address";
            case PHONE_VERIFICATION      -> "Verify Your Phone Number";
            case PASSWORD_RESET          -> "Password Reset Verification";
            case ORDER_CONFIRMATION      -> "Order Confirmation Code";
            case ACCOUNT_RECOVERY        -> "Account Recovery Verification";
            case TWO_FACTOR_AUTH         -> "Two-Factor Authentication Code";
            case EMAIL_CHANGE_VERIFICATION -> "Verify Your New Email Address";
        };
    }

    private String buildOTPEmailContent(OTPVerification otp) {
        String purpose = switch (otp.getOtpType()) {
            case EMAIL_VERIFICATION      -> "verify your email address";
            case PHONE_VERIFICATION      -> "verify your phone number";
            case PASSWORD_RESET          -> "reset your password";
            case ORDER_CONFIRMATION      -> "confirm your order";
            case ACCOUNT_RECOVERY        -> "recover your account";
            case TWO_FACTOR_AUTH         -> "complete two-factor authentication";
            case EMAIL_CHANGE_VERIFICATION -> "verify your new email address";
        };
        return String.format("Dear Customer,\n\nYour verification code to %s is: %s\n\nThis code will expire in 10 minutes.\n\nBest regards,\n%s Team",
                purpose, otp.getOtpCode(), appName);
    }

    private String buildOTPSMSContent(OTPVerification otp) {
        return String.format("Your %s verification code is: %s. Valid for 10 minutes.", appName, otp.getOtpCode());
    }

    private String buildOrderConfirmationContent(RepairOrder order) {
        return String.format("Dear %s,\n\nOrder confirmed!\nOrder Number: %s\nTotal: \u20B9%.2f\nStatus: %s\n\nBest regards,\n%s Team",
                order.getUser().getName(), order.getOrderNumber(), order.getTotalPrice().doubleValue(), order.getStatus(), appName);
    }

    private String buildOrderStatusUpdateContent(RepairOrder order, String previousStatus) {
        return String.format("Dear %s,\n\nOrder status updated.\nOrder: %s\nPrevious: %s\nCurrent: %s\n\nBest regards,\n%s Team",
                order.getUser().getName(), order.getOrderNumber(), previousStatus, order.getStatus(), appName);
    }

    private String buildOrderCancellationContent(RepairOrder order) {
        return String.format("Dear %s,\n\nOrder %s cancelled.\nReason: %s\n\nBest regards,\n%s Team",
                order.getUser().getName(), order.getOrderNumber(),
                order.getTechnicianNotes() != null ? order.getTechnicianNotes() : "Cancelled by customer", appName);
    }

    private String buildOrderCompletionContent(RepairOrder order) {
        return String.format("Dear %s,\n\nOrder %s completed on %s.\n\nThank you for choosing %s!\n\nBest regards,\n%s Team",
                order.getUser().getName(), order.getOrderNumber(), order.getCompletedDate(), appName, appName);
    }

    private String buildWelcomeEmailContent(User user) {
        return String.format("Dear %s,\n\nWelcome to %s!\n\nThank you for joining us.\n\nBest regards,\n%s Team",
                user.getName(), appName, appName);
    }

    private String buildPasswordResetContent(User user, String resetToken) {
        return String.format("Dear %s,\n\nReset Token: %s\n\nIf you didn't request this, ignore this email.\n\nBest regards,\n%s Team",
                user.getName(), resetToken, appName);
    }

    private String buildPasswordChangeConfirmationContent(User user) {
        return String.format("Dear %s,\n\nYour password has been changed. If this wasn't you, contact support.\n\nBest regards,\n%s Team",
                user.getName(), appName);
    }

    private String buildEmailChangeConfirmationContent(User user, String newEmail) {
        return String.format("Dear %s,\n\nYour email has been updated to: %s\n\nBest regards,\n%s Team",
                user.getName(), newEmail, appName);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() <= 4) return "****";
        return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
    }
}
