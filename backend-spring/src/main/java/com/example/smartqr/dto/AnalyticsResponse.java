package com.example.smartqr.dto;

import com.example.smartqr.model.QRAnalytics;

import java.util.List;
import java.util.Map;

public class AnalyticsResponse {
    private long scanCount;
    private long uniqueIps;
    private List<QRAnalytics> scans;
    private Map<String, Long> deviceStats;
    private Map<String, Long> dailyScans;

    public AnalyticsResponse() {
    }

    public AnalyticsResponse(long scanCount, long uniqueIps, List<QRAnalytics> scans, Map<String, Long> deviceStats, Map<String, Long> dailyScans) {
        this.scanCount = scanCount;
        this.uniqueIps = uniqueIps;
        this.scans = scans;
        this.deviceStats = deviceStats;
        this.dailyScans = dailyScans;
    }

    public long getScanCount() {
        return scanCount;
    }

    public void setScanCount(long scanCount) {
        this.scanCount = scanCount;
    }

    public long getUniqueIps() {
        return uniqueIps;
    }

    public void setUniqueIps(long uniqueIps) {
        this.uniqueIps = uniqueIps;
    }

    public List<QRAnalytics> getScans() {
        return scans;
    }

    public void setScans(List<QRAnalytics> scans) {
        this.scans = scans;
    }

    public Map<String, Long> getDeviceStats() {
        return deviceStats;
    }

    public void setDeviceStats(Map<String, Long> deviceStats) {
        this.deviceStats = deviceStats;
    }

    public Map<String, Long> getDailyScans() {
        return dailyScans;
    }

    public void setDailyScans(Map<String, Long> dailyScans) {
        this.dailyScans = dailyScans;
    }

    @Override
    public String toString() {
        return "AnalyticsResponse{" +
                "scanCount=" + scanCount +
                ", uniqueIps=" + uniqueIps +
                ", scans=" + scans +
                ", deviceStats=" + deviceStats +
                ", dailyScans=" + dailyScans +
                '}';
    }
}
