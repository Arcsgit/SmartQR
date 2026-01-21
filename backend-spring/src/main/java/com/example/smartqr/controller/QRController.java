package com.example.smartqr.controller;

import com.example.smartqr.dto.*;
import com.example.smartqr.exception.ResourceNotFoundException;
import com.example.smartqr.model.QRCode;
import com.example.smartqr.service.QRCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/qr")
@CrossOrigin(origins = "*",
             allowedHeaders = "*",
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class QRController {
    private static final Logger log = LoggerFactory.getLogger(QRController.class);

    @Autowired
    private QRCodeService qrCodeService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<QRGenerateResponse>> generateQR(@Valid @RequestBody QRGenerateRequest request) {
        try {
            log.info("Generate QR request received for data: {}", request.getData());
            QRGenerateResponse response = qrCodeService.generateQR(request);
            return ResponseEntity.ok(ApiResponse.success("QR Code generated successfully", response));
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate QR code: " + e.getMessage()));
        }
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<String>> updateQR(@PathVariable UUID id, @Valid @RequestBody QRUpdateRequest request) {
        try {
            log.info("Update QR request received for ID: {}", id);
            qrCodeService.updateQR(id, request);
            return ResponseEntity.ok(ApiResponse.success("QR Code updated successfully"));
        } catch (Exception e) {
            log.error("Error updating QR code: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update QR code: " + e.getMessage()));
        }
    }

    @GetMapping("/scan/{id}")
    public void scanQR(@PathVariable("id") UUID id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            log.info("Scan request received for QR ID: {}", id);
            String targetUrl = qrCodeService.handleScan(id, request);
            log.info("Redirecting to URL: {}", targetUrl);
            response.sendRedirect(targetUrl);
        } catch (Exception e) {
            log.error("Error handling QR scan for ID: {}", id, e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "QR Code not found or invalid");
        }
    }

    @GetMapping("/analytics/{id}")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics(@PathVariable UUID id) {
        try {
            log.info("Analytics request received for QR ID: {}", id);
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
            log.info("Get all QR codes request received");
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
            log.info("Get QR code request received for ID: {}", id);
            QRCode qrCode = qrCodeService.getQRCode(id);
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
            log.info("Delete request for QR ID: {}", id);
            qrCodeService.deleteQRCode(id);
            return ResponseEntity.ok(ApiResponse.success("QR Code deleted successfully"));
        } catch (ResourceNotFoundException e) {
            log.error("QR Code not found or access denied: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting QR code: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete QR code: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadQRCode(@PathVariable UUID id) {
        try {
            log.info("Download request for QR ID: {}", id);
            QRCode qrCode = qrCodeService.getQRCode(id);

            URL url = new URL(qrCode.getImageUrl());
            InputStream inputStream = url.openStream();
            byte[] imageBytes = inputStream.readAllBytes();
            inputStream.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDisposition(
                ContentDisposition.attachment()
                    .filename("smartqr-" + id + ".png")
                    .build()
            );
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            log.info("QR Code download successful: {}", id);
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to download QR code: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/test-location")
    public ResponseEntity<Map<String, String>> testLocation(@RequestParam(required = false) String ip, HttpServletRequest request) {
        try {
            String testIp = (ip != null && !ip.isEmpty()) ? ip : getClientIp(request);
            String region = qrCodeService.testRegionDetection(testIp);

            Map<String, String> result = new HashMap<>();
            result.put("ip", testIp);
            result.put("region", region);
            result.put("ipinfoConfigured", qrCodeService.isIpinfoConfigured() ? "Yes" : "No");

            log.info("Test location for IP: {} -> Region: {}", testIp, region);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error testing location", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "SmartQR API");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        health.put("ipinfoConfigured", qrCodeService.isIpinfoConfigured());
        return ResponseEntity.ok(health);
    }


    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress))
            ipAddress = request.getHeader("X-Real-IP");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress))
            ipAddress = request.getRemoteAddr();

        if (ipAddress != null && ipAddress.contains(","))
            ipAddress = ipAddress.split(",")[0].trim();
        return ipAddress;
    }
}
