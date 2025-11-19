package com.example.smartqr.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "qr_analytics")
public class QRAnalytics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "qr_id", nullable = false)  // ← REMOVED columnDefinition
    private UUID qrId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "device_info", columnDefinition = "TEXT")  // ← CHANGED back to TEXT
    private String deviceInfo;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column
    private String region;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    public QRAnalytics() {
    }

    public QRAnalytics(Long id, UUID qrId, LocalDateTime timestamp, String deviceInfo, String ipAddress, String region) {
        this.id = id;
        this.qrId = qrId;
        this.timestamp = timestamp;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.region = region;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getQrId() {
        return qrId;
    }

    public void setQrId(UUID qrId) {
        this.qrId = qrId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "QRAnalytics{" +
                "id=" + id +
                ", qrId=" + qrId +
                ", timestamp=" + timestamp +
                ", deviceInfo='" + deviceInfo + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", region='" + region + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        QRAnalytics that = (QRAnalytics) o;
        return Objects.equals(id, that.id) && Objects.equals(qrId, that.qrId) && Objects.equals(timestamp, that.timestamp) && Objects.equals(deviceInfo, that.deviceInfo) && Objects.equals(ipAddress, that.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, qrId, timestamp, deviceInfo, ipAddress);
    }
}
