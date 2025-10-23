package com.example.smartqr.service;
//
//import com.example.smartqr.dto.*;
//import com.example.smartqr.exception.ResourceNotFoundException;
//import com.example.smartqr.model.QRAnalytics;
//import com.example.smartqr.model.QRCode;
//import com.example.smartqr.repository.QRAnalyticsRepository;
//import com.example.smartqr.repository.QRCodeRepository;
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.WriterException;
//import com.google.zxing.client.j2se.MatrixToImageWriter;
//import com.google.zxing.common.BitMatrix;
//import com.google.zxing.qrcode.QRCodeWriter;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.http.HttpServletRequest;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class QRCodeService {
//    private static final Logger log = LoggerFactory.getLogger(QRCodeService.class);
//    private final QRCodeRepository qrCodeRepository;
//    private final QRAnalyticsRepository analyticsRepository;
//    private final S3Service s3Service;
//
//    @Value("${app.base-url:http://localhost:8080}")
//    private String baseUrl;
//
//    @Value("${ipinfo.token:}")
//    private String ipinfoToken;
//
//    private static final int QR_CODE_SIZE = 300;
//    private final HttpClient httpClient = HttpClient.newHttpClient();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//     public QRCodeService(QRCodeRepository qrCodeRepository, QRAnalyticsRepository analyticsRepository, S3Service s3Service) {
//        this.qrCodeRepository = qrCodeRepository;
//        this.analyticsRepository = analyticsRepository;
//        this.s3Service = s3Service;
//    }
//
//    /**
//     * Generate a new QR code
//     */
//    @Transactional
//    public QRGenerateResponse generateQR(QRGenerateRequest request) {
//        try {
//            UUID qrId = UUID.randomUUID();
//            String scanUrl = baseUrl + "/api/qr/scan/" + qrId;
//
//            log.info("Generating QR code with ID: {}", qrId);
//            log.info("Scan URL: {}", scanUrl);
//
//            // Generate QR code image
//            QRCodeWriter qrCodeWriter = new QRCodeWriter();
//            BitMatrix bitMatrix = qrCodeWriter.encode(
//                    scanUrl,
//                    BarcodeFormat.QR_CODE,
//                    QR_CODE_SIZE,
//                    QR_CODE_SIZE
//            );
//
//            // Convert to PNG
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
//
//            // Upload to S3
//            String fileName = qrId + ".png";
//            String imageUrl = s3Service.uploadFile(fileName, outputStream);
//
//            // Save to database
//            QRCode qrCode = new QRCode();
//            qrCode.setId(qrId);
//            qrCode.setData(request.getData());
//            qrCode.setImageUrl(imageUrl);
//            qrCode.setCreatedAt(LocalDateTime.now());
//
//            qrCodeRepository.save(qrCode);
//
//            log.info("QR Code generated successfully: {}", qrId);
//            return new QRGenerateResponse(qrId, imageUrl, scanUrl);
//
//        } catch (WriterException | IOException e) {
//            log.error("Failed to generate QR code", e);
//            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Update QR code destination
//     */
//    @Transactional
//    public void updateQR(UUID id, QRUpdateRequest request) {
//        log.info("Updating QR code: {}", id);
//
//        QRCode qrCode = qrCodeRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found with ID: " + id));
//
//        qrCode.setData(request.getData());
//        qrCodeRepository.save(qrCode);
//
//        log.info("QR Code updated successfully: {} -> New destination: {}", id, request.getData());
//    }
//
//    /**
//     * Handle QR code scan - track analytics and return redirect URL
//     */
//    @Transactional
//    public String handleScan(UUID id, HttpServletRequest request) {
//        log.info("=== Handling scan for QR ID: {} ===", id);
//
//        try {
//            // Find QR code
//            QRCode qrCode = qrCodeRepository.findById(id)
//                    .orElseThrow(() -> new ResourceNotFoundException("QR Code not found with ID: " + id));
//
//            log.info("QR Code found. Destination: {}", qrCode.getData());
//
//            // Get client information
//            String ipAddress = getClientIp(request);
//            String deviceInfo = request.getHeader("User-Agent");
//            String region = determineRegionFromIP(ipAddress);
//
//            log.info("Scan details - IP: {}, Device: {}, Region: {}",
//                    ipAddress, parseDevice(deviceInfo), region);
//
//            // Save analytics
//            QRAnalytics analytics = new QRAnalytics();
//            analytics.setQrId(id);
//            analytics.setTimestamp(LocalDateTime.now());
//            analytics.setDeviceInfo(deviceInfo);
//            analytics.setIpAddress(ipAddress);
//            analytics.setRegion(region);
//
//            analyticsRepository.save(analytics);
//            log.info("Analytics saved successfully");
//
//            // Prepare redirect URL
//            String targetUrl = qrCode.getData();
//            if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
//                targetUrl = "https://" + targetUrl;
//            }
//
//            log.info("Redirecting to: {}", targetUrl);
//            return targetUrl;
//
//        } catch (Exception e) {
//            log.error("Error handling scan for QR ID: {}", id, e);
//            throw new RuntimeException("Failed to process QR scan: " + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Get analytics for a QR code
//     */
//    public AnalyticsResponse getAnalytics(UUID qrId) {
//        log.info("Fetching analytics for QR ID: {}", qrId);
//
//        List<QRAnalytics> scans = analyticsRepository.findByQrIdOrderByTimestampDesc(qrId);
//
//        // Calculate stats
//        int scanCount = scans.size();
//        long uniqueIps = scans.stream()
//                .map(QRAnalytics::getIpAddress)
//                .distinct()
//                .count();
//
//        // Device statistics
//        Map<String, Long> deviceStats = scans.stream()
//                .collect(Collectors.groupingBy(
//                        scan -> parseDevice(scan.getDeviceInfo()),
//                        Collectors.counting()
//                ));
//
//        // Daily scan statistics
//        Map<String, Long> dailyScans = scans.stream()
//                .collect(Collectors.groupingBy(
//                        scan -> scan.getTimestamp().toLocalDate().toString(),
//                        Collectors.counting()
//                ));
//
//        log.info("Analytics retrieved: {} total scans, {} unique IPs", scanCount, uniqueIps);
//
//        return new AnalyticsResponse(scanCount, (int) uniqueIps, scans, deviceStats, dailyScans);
//    }
//
//    /**
//     * Get all QR codes (ordered by creation date)
//     */
//    public List<QRCode> getAllQRCodes() {
//        log.info("Fetching all QR codes");
//        List<QRCode> qrCodes = qrCodeRepository.findAllByOrderByCreatedAtDesc();
//        log.info("Found {} QR codes", qrCodes.size());
//        return qrCodes;
//    }
//
//    /**
//     * Get single QR code by ID
//     */
//    public QRCode getQRCode(UUID id) {
//        log.info("Fetching QR code: {}", id);
//        return qrCodeRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found with ID: " + id));
//    }
//
//    /**
//     * Delete QR code (and its S3 image)
//     */
//    @Transactional
//    public void deleteQRCode(UUID id) {
//        log.info("Deleting QR code: {}", id);
//
//        QRCode qrCode = qrCodeRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found with ID: " + id));
//
//        // Try to delete S3 file
//        try {
//            String fileName = id + ".png";
//            s3Service.deleteFile(fileName);
//            log.info("S3 file deleted: {}", fileName);
//        } catch (Exception e) {
//            log.warn("Failed to delete S3 file for QR: {}. Continuing with database deletion.", id, e);
//        }
//
//        // Delete from database
//        qrCodeRepository.delete(qrCode);
//        log.info("QR Code deleted successfully: {}", id);
//    }
//
//    // ============= HELPER METHODS =============
//
//    /**
//     * Extract client IP address from request
//     */
//    private String getClientIp(HttpServletRequest request) {
//        String ipAddress = request.getHeader("X-Forwarded-For");
//
//        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getHeader("X-Real-IP");
//        }
//
//        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getHeader("Proxy-Client-IP");
//        }
//
//        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getHeader("WL-Proxy-Client-IP");
//        }
//
//        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getHeader("HTTP_CLIENT_IP");
//        }
//
//        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
//        }
//
//        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getRemoteAddr();
//        }
//
//        // Handle multiple IPs (take the first one)
//        if (ipAddress != null && ipAddress.contains(",")) {
//            ipAddress = ipAddress.split(",")[0].trim();
//        }
//
//        log.debug("Detected IP address: {}", ipAddress);
//        return ipAddress;
//    }
//
//    /**
//     * Determine geographic region from IP address using ipinfo.io
//     */
//    private String determineRegionFromIP(String ipAddress) {
//        // Check for localhost and private IPs
//        if (isPrivateIP(ipAddress)) {
//            log.debug("Local/Private IP detected: {}", ipAddress);
//            return "Local/Private Network";
//        }
//
//        // Check if ipinfo token is configured
//        if (ipinfoToken == null || ipinfoToken.isEmpty()) {
//            log.warn("ipinfo.io token not configured. Cannot determine region for IP: {}", ipAddress);
//            return "Unknown (No API Key)";
//        }
//
//        try {
//            // Call ipinfo.io API
//            String apiUrl = "https://ipinfo.io/" + ipAddress + "?token=" + ipinfoToken;
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(apiUrl))
//                    .timeout(java.time.Duration.ofSeconds(5))
//                    .GET()
//                    .build();
//
//            log.debug("Calling ipinfo.io API for IP: {}", ipAddress);
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//            if (response.statusCode() == 200) {
//                JsonNode json = objectMapper.readTree(response.body());
//
//                log.debug("ipinfo.io response: {}", response.body());
//
//                // Check for error in response
//                if (json.has("error")) {
//                    String error = json.get("error").asText();
//                    log.error("ipinfo.io API error: {}", error);
//                    return "Unknown (API Error)";
//                }
//
//                // Parse location data (handling different response formats)
//                String city = json.has("city") ? json.get("city").asText() : "";
//                String region = json.has("region") ? json.get("region").asText() : "";
//                String country = json.has("country") ? json.get("country").asText() : "";
//                String countryCode = json.has("country_code") ? json.get("country_code").asText() : "";
//                String continentCode = json.has("continent_code") ? json.get("continent_code").asText() : "";
//                String continent = json.has("continent") ? json.get("continent").asText() : "";
//
//                // Build region string based on available data
//                List<String> parts = new ArrayList<>();
//
//                // Priority: City > Region > Country/Continent
//                if (!city.isEmpty()) {
//                    parts.add(city);
//                }
//
//                if (!region.isEmpty() && !region.equals(city)) {
//                    parts.add(region);
//                }
//
//                // Use full country name if available, otherwise country code
//                if (!country.isEmpty()) {
//                    parts.add(country);
//                } else if (!countryCode.isEmpty()) {
//                    parts.add(countryCode);
//                } else if (!continent.isEmpty()) {
//                    // Fallback to continent if no country info
//                    parts.add(continent);
//                } else if (!continentCode.isEmpty()) {
//                    parts.add(continentCode);
//                }
//
//                String result = parts.isEmpty() ? "Unknown Location" : String.join(", ", parts);
//
//                log.info("✅ Resolved IP {} to region: {}", ipAddress, result);
//                return result;
//
//            } else if (response.statusCode() == 429) {
//                log.error("ipinfo.io rate limit exceeded");
//                return "Unknown (Rate Limit)";
//            } else {
//                log.error("ipinfo.io API returned status code: {}", response.statusCode());
//                return "Unknown (API Error)";
//            }
//
//        } catch (Exception e) {
//            log.error("Error determining region for IP: {}", ipAddress, e);
//            return "Unknown (Error)";
//        }
//    }
//
//    /**
//     * Check if IP is private/local
//     */
//    private boolean isPrivateIP(String ipAddress) {
//        if (ipAddress == null) return true;
//
//        return ipAddress.equals("127.0.0.1") ||
//               ipAddress.equals("0:0:0:0:0:0:0:1") ||
//               ipAddress.equals("::1") ||
//               ipAddress.startsWith("192.168.") ||
//               ipAddress.startsWith("10.") ||
//               ipAddress.startsWith("172.16.") ||
//               ipAddress.startsWith("172.17.") ||
//               ipAddress.startsWith("172.18.") ||
//               ipAddress.startsWith("172.19.") ||
//               ipAddress.startsWith("172.20.") ||
//               ipAddress.startsWith("172.21.") ||
//               ipAddress.startsWith("172.22.") ||
//               ipAddress.startsWith("172.23.") ||
//               ipAddress.startsWith("172.24.") ||
//               ipAddress.startsWith("172.25.") ||
//               ipAddress.startsWith("172.26.") ||
//               ipAddress.startsWith("172.27.") ||
//               ipAddress.startsWith("172.28.") ||
//               ipAddress.startsWith("172.29.") ||
//               ipAddress.startsWith("172.30.") ||
//               ipAddress.startsWith("172.31.");
//    }
//
//    /**
//     * Parse device type from User-Agent string
//     */
//    private String parseDevice(String userAgent) {
//        if (userAgent == null || userAgent.isEmpty()) {
//            return "Unknown";
//        }
//
//        String ua = userAgent.toLowerCase();
//
//        if (ua.contains("bot") || ua.contains("crawler") || ua.contains("spider")) {
//            return "Bot";
//        } else if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
//            return "Mobile";
//        } else if (ua.contains("tablet") || ua.contains("ipad")) {
//            return "Tablet";
//        } else {
//            return "Desktop";
//        }
//    }
//
//    // ============= PUBLIC TEST METHODS =============
//
//    /**
//     * Test region detection (for debugging)
//     */
//    public String testRegionDetection(String ipAddress) {
//        log.info("Testing region detection for IP: {}", ipAddress);
//        return determineRegionFromIP(ipAddress);
//    }
//
//    /**
//     * Check if ipinfo.io is configured
//     */
//    public boolean isIpinfoConfigured() {
//        boolean configured = ipinfoToken != null && !ipinfoToken.isEmpty();
//        log.debug("ipinfo.io configured: {}", configured);
//        return configured;
//    }
//}

import com.example.smartqr.dto.*;
import com.example.smartqr.exception.ResourceNotFoundException;
import com.example.smartqr.model.QRAnalytics;
import com.example.smartqr.model.QRCode;
import com.example.smartqr.model.User;
import com.example.smartqr.repository.QRAnalyticsRepository;
import com.example.smartqr.repository.QRCodeRepository;
import com.example.smartqr.repository.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QRCodeService {
    private static final Logger log = LoggerFactory.getLogger(QRCodeService.class);

    private final QRCodeRepository qrCodeRepository;
    private final QRAnalyticsRepository analyticsRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${ipinfo.token:}")
    private String ipinfoToken;

    private static final int QR_CODE_SIZE = 300;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QRCodeService(QRCodeRepository qrCodeRepository, QRAnalyticsRepository analyticsRepository, UserRepository userRepository, S3Service s3Service) {
        this.qrCodeRepository = qrCodeRepository;
        this.analyticsRepository = analyticsRepository;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
    }

    /**
     * Get currently authenticated user
     */
    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Generate QR code (linked to current user)
     */
    @Transactional
    public QRGenerateResponse generateQR(QRGenerateRequest request) {
        try {
            User currentUser = getCurrentUser();

            UUID qrId = UUID.randomUUID();
            String scanUrl = baseUrl + "/api/qr/scan/" + qrId;

            log.info("Generating QR code for user: {} (ID: {})", currentUser.getEmail(), qrId);

            // Generate QR code image
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    scanUrl,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_SIZE,
                    QR_CODE_SIZE
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            // Upload to S3
            String fileName = qrId + ".png";
            String imageUrl = s3Service.uploadFile(fileName, outputStream);

            // Save to database with user reference
            QRCode qrCode = new QRCode();
            qrCode.setId(qrId);
            qrCode.setData(request.getData());
            qrCode.setImageUrl(imageUrl);
            qrCode.setUser(currentUser);  // 🔥 Link to user
            qrCode.setCreatedAt(LocalDateTime.now());

            qrCodeRepository.save(qrCode);

            log.info("QR Code generated successfully for user: {}", currentUser.getEmail());
            return new QRGenerateResponse(qrId, imageUrl, scanUrl);

        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code", e);
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }

    /**
     * Update QR code (only if belongs to current user)
     */
    @Transactional
    public void updateQR(UUID id, QRUpdateRequest request) {
        User currentUser = getCurrentUser();

        QRCode qrCode = qrCodeRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found or access denied"));

        qrCode.setData(request.getData());
        qrCodeRepository.save(qrCode);

        log.info("QR Code updated by user: {} (QR ID: {})", currentUser.getEmail(), id);
    }

    /**
     * Handle QR scan (public - no auth required)
     */
    @Transactional
    public String handleScan(UUID id, HttpServletRequest request) {
        log.info("=== Handling scan for QR ID: {} ===", id);

        try {
            QRCode qrCode = qrCodeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("QR Code not found"));

            // Get client information
            String ipAddress = getClientIp(request);
            String deviceInfo = request.getHeader("User-Agent");
            String region = determineRegionFromIP(ipAddress);

            log.info("Scan from IP: {}, Region: {}", ipAddress, region);

            // Save analytics
            QRAnalytics analytics = new QRAnalytics();
            analytics.setQrId(id);
            analytics.setTimestamp(LocalDateTime.now());
            analytics.setDeviceInfo(deviceInfo);
            analytics.setIpAddress(ipAddress);
            analytics.setRegion(region);

            analyticsRepository.save(analytics);

            // Prepare redirect URL
            String targetUrl = qrCode.getData();
            if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                targetUrl = "https://" + targetUrl;
            }

            log.info("Redirecting to: {}", targetUrl);
            return targetUrl;

        } catch (Exception e) {
            log.error("Error handling scan", e);
            throw new RuntimeException("Failed to process QR scan: " + e.getMessage(), e);
        }
    }

    /**
     * Get analytics (only for user's own QR codes)
     */
    public AnalyticsResponse getAnalytics(UUID qrId) {
        User currentUser = getCurrentUser();

        // Verify user owns this QR code
        qrCodeRepository.findByIdAndUser(qrId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found or access denied"));

        List<QRAnalytics> scans = analyticsRepository.findByQrIdOrderByTimestampDesc(qrId);

        int scanCount = scans.size();
        long uniqueIps = scans.stream()
                .map(QRAnalytics::getIpAddress)
                .distinct()
                .count();

        Map<String, Long> deviceStats = scans.stream()
                .collect(Collectors.groupingBy(
                        scan -> parseDevice(scan.getDeviceInfo()),
                        Collectors.counting()
                ));

        Map<String, Long> dailyScans = scans.stream()
                .collect(Collectors.groupingBy(
                        scan -> scan.getTimestamp().toLocalDate().toString(),
                        Collectors.counting()
                ));

        log.info("Analytics fetched by user: {} for QR: {}", currentUser.getEmail(), qrId);

        return new AnalyticsResponse(scanCount, (int) uniqueIps, scans, deviceStats, dailyScans);
    }

    /**
     * Get all QR codes for current user
     */
    public List<QRCode> getAllQRCodes() {
        User currentUser = getCurrentUser();
        List<QRCode> qrCodes = qrCodeRepository.findByUserOrderByCreatedAtDesc(currentUser);
        log.info("User {} has {} QR codes", currentUser.getEmail(), qrCodes.size());
        return qrCodes;
    }

    /**
     * Get single QR code (only if belongs to current user)
     */
    public QRCode getQRCode(UUID id) {
        User currentUser = getCurrentUser();
        return qrCodeRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found or access denied"));
    }

    /**
     * Delete QR code (only if belongs to current user)
     */
    @Transactional
    public void deleteQRCode(UUID id) {
        User currentUser = getCurrentUser();

        QRCode qrCode = qrCodeRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found or access denied"));

        // Delete from S3
        try {
            String fileName = id + ".png";
            s3Service.deleteFile(fileName);
        } catch (Exception e) {
            log.warn("Failed to delete S3 file for QR: {}", id, e);
        }

        qrCodeRepository.delete(qrCode);
        log.info("QR Code deleted by user: {} (QR ID: {})", currentUser.getEmail(), id);
    }

    // ============= HELPER METHODS (unchanged) =============

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }

    private String determineRegionFromIP(String ipAddress) {
        if (isPrivateIP(ipAddress)) {
            return "Local/Private Network";
        }

        if (ipinfoToken == null || ipinfoToken.isEmpty()) {
            return "Unknown (No API Key)";
        }

        try {
            String apiUrl = "https://ipinfo.io/" + ipAddress + "?token=" + ipinfoToken;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = objectMapper.readTree(response.body());

                if (json.has("error")) {
                    return "Unknown (API Error)";
                }

                String city = json.has("city") ? json.get("city").asText() : "";
                String region = json.has("region") ? json.get("region").asText() : "";
                String country = json.has("country") ? json.get("country").asText() : "";

                if (country.isEmpty()) {
                    country = json.has("country_code") ? json.get("country_code").asText() : "";
                }

                List<String> parts = new ArrayList<>();
                if (!city.isEmpty()) parts.add(city);
                if (!region.isEmpty() && !region.equals(city)) parts.add(region);
                if (!country.isEmpty()) parts.add(country);

                String result = parts.isEmpty() ? "Unknown Location" : String.join(", ", parts);
                log.info("✅ IP {} → {}", ipAddress, result);
                return result;
            }

            return "Unknown (API Error)";

        } catch (Exception e) {
            log.error("Error resolving IP: {}", ipAddress, e);
            return "Unknown (Error)";
        }
    }

    private boolean isPrivateIP(String ipAddress) {
        if (ipAddress == null) return true;
        return ipAddress.equals("127.0.0.1") ||
               ipAddress.equals("0:0:0:0:0:0:0:1") ||
               ipAddress.equals("::1") ||
               ipAddress.startsWith("192.168.") ||
               ipAddress.startsWith("10.");
    }

    private String parseDevice(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        String ua = userAgent.toLowerCase();

        if (ua.contains("bot") || ua.contains("crawler")) {
            return "Bot";
        } else if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "Mobile";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    public String testRegionDetection(String ipAddress) {
        return determineRegionFromIP(ipAddress);
    }

    public boolean isIpinfoConfigured() {
        return ipinfoToken != null && !ipinfoToken.isEmpty();
    }
}
