package com.example.smartqr.service;

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

    @Value("${app.base-url}")
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

    private User getCurrentUser() {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            return userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } catch (Exception e) {
            log.error("Failed to get current user", e);
            throw new RuntimeException("Authentication error: " + e.getMessage());
        }
    }

    @Transactional
    public QRGenerateResponse generateQR(QRGenerateRequest request) {
        try {
            User currentUser = getCurrentUser();
            UUID qrId = UUID.randomUUID();
            String scanUrl = baseUrl + "/api/qr/scan/" + qrId;
            log.info("Generating QR code for user: {}", currentUser.getEmail());
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    scanUrl,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_SIZE,
                    QR_CODE_SIZE
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            String fileName = qrId + ".png";
            String imageUrl = s3Service.uploadFile(fileName, outputStream);

            QRCode qrCode = new QRCode();
            qrCode.setId(qrId);
            qrCode.setData(request.getData());
            qrCode.setImageUrl(imageUrl);
            qrCode.setUser(currentUser);
            qrCode.setCreatedAt(LocalDateTime.now());

            if (request.getName() != null && !request.getName().trim().isEmpty())
                qrCode.setName(request.getName().trim());
            qrCodeRepository.save(qrCode);

            log.info("QR Code generated: {}", qrId);
            return new QRGenerateResponse(qrId, imageUrl, scanUrl);
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code", e);
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void updateQR(UUID id, QRUpdateRequest request) {
        User currentUser = getCurrentUser();
        QRCode qrCode = qrCodeRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found or access denied"));
        qrCode.setData(request.getData());
        if (request.getName() != null)
            qrCode.setName(request.getName().trim());
        qrCodeRepository.save(qrCode);
        log.info("QR Code updated: {}", id);
    }

    @Transactional
    public String handleScan(UUID id, HttpServletRequest request) {
        log.info("=== Handling scan for QR ID: {} ===", id);
        try {
            QRCode qrCode = qrCodeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("QR Code not found"));

            String ipAddress = getClientIp(request);
            String deviceInfo = request.getHeader("User-Agent");
            String region = determineRegionFromIP(ipAddress);

            log.info("Scan from IP: {}, Region: {}", ipAddress, region);

            QRAnalytics analytics = new QRAnalytics();
            analytics.setQrId(id);
            analytics.setTimestamp(LocalDateTime.now());
            analytics.setDeviceInfo(deviceInfo);
            analytics.setIpAddress(ipAddress);
            analytics.setRegion(region);

            analyticsRepository.save(analytics);

            String targetUrl = qrCode.getData();
            if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://"))
                targetUrl = "https://" + targetUrl;
            log.info("Redirecting to: {}", targetUrl);
            return targetUrl;
        } catch (Exception e) {
            log.error("Error handling scan", e);
            throw new RuntimeException("Failed to process QR scan: " + e.getMessage(), e);
        }
    }

    public AnalyticsResponse getAnalytics(UUID qrId) {
        User currentUser = getCurrentUser();

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

    public List<QRCode> getAllQRCodes() {
        User currentUser = getCurrentUser();
        List<QRCode> qrCodes = qrCodeRepository.findByUserOrderByCreatedAtDesc(currentUser);
        log.info("User {} has {} QR codes", currentUser.getEmail(), qrCodes.size());
        return qrCodes;
    }

    public QRCode getQRCode(UUID id) {
        User currentUser = getCurrentUser();
        return qrCodeRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found or access denied"));
    }

    @Transactional
    public void deleteQRCode(UUID id) {
        log.info("Attempting to delete QR code: {}", id);
        try {
            User currentUser = getCurrentUser();
            log.info("Current user: {} ({})", currentUser.getEmail(), currentUser.getId());

            QRCode qrCode = qrCodeRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("QR Code not found: {}", id);
                        return new ResourceNotFoundException("QR Code not found with ID: " + id);
                    });

            log.info("QR Code found. Owner: {} ({})",
                    qrCode.getUser().getEmail(),
                    qrCode.getUser().getId());

            if (!qrCode.getUser().getId().equals(currentUser.getId())) {
                log.error("Access denied. QR {} belongs to user {}, but current user is {}",
                        id, qrCode.getUser().getEmail(), currentUser.getEmail());
                throw new ResourceNotFoundException("Access denied - QR Code belongs to another user");
            }

            try {
                String fileName = id + ".png";
                s3Service.deleteFile(fileName);
                log.info("S3 file deleted: {}", fileName);
            } catch (Exception e) {
                log.warn("Failed to delete S3 file for QR: {}. Continuing with database deletion.", id, e);
            }

            qrCodeRepository.delete(qrCode);
            log.info("QR Code deleted successfully: {}", id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting QR code: {}", id, e);
            throw new RuntimeException("Failed to delete QR code: " + e.getMessage());
        }
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

    private String determineRegionFromIP(String ipAddress) {
        if (isPrivateIP(ipAddress))
            return "Local/Private Network";
        if (ipinfoToken == null || ipinfoToken.isEmpty())
            return "Unknown (No API Key)";

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
                if (json.has("error"))
                    return "Unknown (API Error)";

                String city = json.has("city") ? json.get("city").asText() : "";
                String region = json.has("region") ? json.get("region").asText() : "";
                String country = json.has("country") ? json.get("country").asText() : "";

                if (country.isEmpty())
                    country = json.has("country_code") ? json.get("country_code").asText() : "";

                List<String> parts = new ArrayList<>();
                if (!city.isEmpty()) parts.add(city);
                if (!region.isEmpty() && !region.equals(city)) parts.add(region);
                if (!country.isEmpty()) parts.add(country);

                String result = parts.isEmpty() ? "Unknown Location" : String.join(", ", parts);
                log.info("IP {} → {}", ipAddress, result);
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
        if (userAgent == null || userAgent.isEmpty())
            return "Unknown";

        String ua = userAgent.toLowerCase();
        if (ua.contains("bot") || ua.contains("crawler")) return "Bot";
        else if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) return "Mobile";
        else if (ua.contains("tablet") || ua.contains("ipad")) return "Tablet";
        else return "Desktop";
    }

    public String testRegionDetection(String ipAddress) {
        return determineRegionFromIP(ipAddress);
    }

    public boolean isIpinfoConfigured() {
        return ipinfoToken != null && !ipinfoToken.isEmpty();
    }
}
