package com.example.smartqr.controller;

import com.example.smartqr.dto.*;
import com.example.smartqr.model.QRCode;
import com.example.smartqr.service.QRCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/qr")
public class QRController {
    private static final Logger log = LoggerFactory.getLogger(QRController.class);
    private final QRCodeService qrCodeService;

    public QRController(QRCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<QRGenerateResponse>> generateQR(
            @Valid @RequestBody QRGenerateRequest request) {

        try {
            QRGenerateResponse response = qrCodeService.generateQR(request);
            return ResponseEntity.ok(ApiResponse.success("QR Code generated successfully", response));
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate QR code: " + e.getMessage()));
        }
    }

     @GetMapping("/scan/{id}")
    public void scanQR(
            @PathVariable("id") UUID id,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        try {
            log.info("Scan request received for QR ID: {}", id);

            String targetUrl = qrCodeService.handleScan(id, request);

            log.info("Redirecting to URL: {}", targetUrl);

            // Use response.sendRedirect for simpler redirect
            response.sendRedirect(targetUrl);

        } catch (Exception e) {
            log.error("Error handling QR scan for ID: {}", id, e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "QR Code not found");
        }
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<String>> updateQR(
            @PathVariable UUID id,
            @Valid @RequestBody QRUpdateRequest request) {

        try {
            qrCodeService.updateQR(id, request);
            return ResponseEntity.ok(ApiResponse.success("QR Code updated successfully"));
        } catch (Exception e) {
            log.error("Error updating QR code: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update QR code: " + e.getMessage()));
        }
    }

    @GetMapping("/analytics/{id}")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics(@PathVariable UUID id) {
        try {
            AnalyticsResponse analytics = qrCodeService.getAnalytics(id);
            return ResponseEntity.ok(ApiResponse.success(analytics));
        } catch (Exception e) {
            log.error("Error fetching analytics for QR: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch analytics: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<QRCode>>> getAllQRCodes() {
        try {
            List<QRCode> qrCodes = qrCodeService.getAllQRCodes();
            return ResponseEntity.ok(ApiResponse.success(qrCodes));
        } catch (Exception e) {
            log.error("Error fetching QR codes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch QR codes: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QRCode>> getQRCode(@PathVariable UUID id) {
        try {
            QRCode qrCode = qrCodeService.getQRCodeById(id);
            return ResponseEntity.ok(ApiResponse.success(qrCode));
        } catch (Exception e) {
            log.error("Error fetching QR code: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("QR Code not found"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteQRCode(@PathVariable UUID id) {
        try {
            qrCodeService.deleteQRCode(id);
            return ResponseEntity.ok(ApiResponse.success("QR Code deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting QR code: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete QR code: " + e.getMessage()));
        }
    }
}
