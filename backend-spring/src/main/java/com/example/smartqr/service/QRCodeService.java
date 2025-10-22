package com.example.smartqr.service;

import com.example.smartqr.dto.AnalyticsResponse;
import com.example.smartqr.dto.QRGenerateRequest;
import com.example.smartqr.dto.QRGenerateResponse;
import com.example.smartqr.dto.QRUpdateRequest;
import com.example.smartqr.exception.ResourceNotFoundException;
import com.example.smartqr.model.QRAnalytics;
import com.example.smartqr.model.QRCode;
import com.example.smartqr.repository.QRAnalyticsRepository;
import com.example.smartqr.repository.QRCodeRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QRCodeService {
    private static final Logger log = LoggerFactory.getLogger(QRCodeService.class);
    private final QRCodeRepository qrCodeRepository;
    private final QRAnalyticsRepository analyticsRepository;
    private final S3Service s3Service;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final int QR_CODE_SIZE = 300;

    public QRCodeService(QRCodeRepository qrCodeRepository, QRAnalyticsRepository analyticsRepository, S3Service s3Service) {
        this.qrCodeRepository = qrCodeRepository;
        this.analyticsRepository = analyticsRepository;
        this.s3Service = s3Service;
    }

    @Transactional
    public QRGenerateResponse generateQR(QRGenerateRequest request) {
        try {
            UUID qrId = UUID.randomUUID();
            String scanUrl = baseUrl + "/api/qr/scan/" + qrId;

            // Generate QR Code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    scanUrl,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_SIZE,
                    QR_CODE_SIZE
            );

            // Convert to image
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            // Upload to S3
            String fileName = qrId + ".png";
            String imageUrl = s3Service.uploadFile(fileName, outputStream);

            // Save to database
            QRCode qrCode = new QRCode();
            qrCode.setId(qrId);
            qrCode.setData(request.getData());
            qrCode.setImageUrl(imageUrl);
            qrCodeRepository.save(qrCode);

            log.info("QR Code generated successfully: {}", qrId);
            return new QRGenerateResponse(qrId, imageUrl, scanUrl);

        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code", e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

//@Transactional
//public QRGenerateResponse generateQR(QRGenerateRequest request) {
//    try {
//        // Generate UUID manually
//        UUID qrId = UUID.randomUUID();
//        String scanUrl = baseUrl + "/api/qr/scan/" + qrId;
//
//        // Generate QR Code
//        QRCodeWriter qrCodeWriter = new QRCodeWriter();
//        BitMatrix bitMatrix = qrCodeWriter.encode(
//                scanUrl,
//                BarcodeFormat.QR_CODE,
//                QR_CODE_SIZE,
//                QR_CODE_SIZE
//        );
//
//        // Convert to image
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
//
//        // Upload to S3
//        String fileName = qrId + ".png";
//        String imageUrl = s3Service.uploadFile(fileName, outputStream);
//
//        // Create QR code entity WITHOUT builder
//        QRCode qrCode = new QRCode();
//        qrCode.setId(qrId);
//        qrCode.setData(request.getData());
//        qrCode.setImageUrl(imageUrl);
//        qrCode.setCreatedAt(LocalDateTime.now());
//
//        // Save to database
//        qrCodeRepository.save(qrCode);
//
//        log.info("QR Code generated successfully: {}", qrId);
//        return new QRGenerateResponse(qrId, imageUrl, scanUrl);
//
//    } catch (WriterException | IOException e) {
//        log.error("Failed to generate QR code", e);
//        throw new RuntimeException("Failed to generate QR code", e);
//    }
//}



    @Transactional
    public void updateQR(UUID id, QRUpdateRequest request) {
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found with ID: " + id));

        qrCode.setData(request.getData());
        qrCodeRepository.save(qrCode);

        log.info("QR Code updated successfully: {}", id);
    }

    @Transactional
    public String handleScan(UUID id, HttpServletRequest request) {
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found with ID: " + id));

        // Log analytics
        QRAnalytics analytics = new QRAnalytics();
        analytics.setQrId(id);
        analytics.setDeviceInfo(request.getHeader("User-Agent"));
        analytics.setIpAddress(getClientIp(request));
        analytics.setRegion(determineRegion(getClientIp(request)));
        analyticsRepository.save(analytics);

        log.info("QR Code scanned: {} from IP: {}", id, getClientIp(request));
        return qrCode.getData();
    }

    public AnalyticsResponse getAnalytics(UUID id) {
        // Verify QR code exists
        qrCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found with ID: " + id));

        long scanCount = analyticsRepository.countByQrId(id);
        long uniqueIps = analyticsRepository.countDistinctIpsByQrId(id);
        List<QRAnalytics> recentScans = analyticsRepository.findRecentScansByQrId(id, PageRequest.of(0, 100));

        // Calculate device statistics
        Map<String, Long> deviceStats = calculateDeviceStats(recentScans);

        // Calculate daily scan statistics
        Map<String, Long> dailyScans = calculateDailyScans(recentScans);

        return new AnalyticsResponse(scanCount, uniqueIps, recentScans, deviceStats, dailyScans);
    }

    public List<QRCode> getAllQRCodes() {
        return qrCodeRepository.findAll();
    }

    public QRCode getQRCodeById(UUID id) {
        return qrCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found with ID: " + id));
    }

    @Transactional
    public void deleteQRCode(UUID id) {
        QRCode qrCode = qrCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QR Code not found with ID: " + id));

        // Delete from S3
        try {
            String fileName = id + ".png";
            s3Service.deleteFile(fileName);
        } catch (Exception e) {
            log.warn("Failed to delete QR image from S3: {}", id, e);
        }

        // Delete analytics data
        List<QRAnalytics> analytics = analyticsRepository.findByQrIdOrderByTimestampDesc(id);
        analyticsRepository.deleteAll(analytics);

        // Delete QR code
        qrCodeRepository.delete(qrCode);

        log.info("QR Code deleted successfully: {}", id);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String determineRegion(String ipAddress) {
        // Simple IP-based region detection (you can integrate with a geolocation service)
        if (ipAddress.startsWith("127.") || ipAddress.equals("::1")) {
            return "Local";
        }
        // Add more sophisticated region detection logic here
        return "Unknown";
    }

    private Map<String, Long> calculateDeviceStats(List<QRAnalytics> scans) {
        return scans.stream()
                .collect(Collectors.groupingBy(
                        this::categorizeDevice,
                        Collectors.counting()
                ));
    }

    private String categorizeDevice(QRAnalytics analytics) {
        String userAgent = Optional.ofNullable(analytics.getDeviceInfo()).orElse("").toLowerCase();

        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "Mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "Tablet";
        } else if (userAgent.contains("desktop") || userAgent.contains("windows") ||
                   userAgent.contains("mac") || userAgent.contains("linux")) {
            return "Desktop";
        }
        return "Unknown";
    }

    private Map<String, Long> calculateDailyScans(List<QRAnalytics> scans) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return scans.stream()
                .collect(Collectors.groupingBy(
                        analytics -> analytics.getTimestamp().toLocalDate().format(formatter),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }
}
