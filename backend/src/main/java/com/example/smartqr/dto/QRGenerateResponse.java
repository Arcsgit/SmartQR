package com.example.smartqr.dto;

import java.util.UUID;

public class QRGenerateResponse {
    private UUID qrId;
    private String downloadUrl;
    private String scanUrl;

    public QRGenerateResponse() {
    }

    public QRGenerateResponse(UUID qrId, String downloadUrl, String scanUrl) {
        this.qrId = qrId;
        this.downloadUrl = downloadUrl;
        this.scanUrl = scanUrl;
    }

    public UUID getQrId() {
        return qrId;
    }

    public void setQrId(UUID qrId) {
        this.qrId = qrId;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getScanUrl() {
        return scanUrl;
    }

    public void setScanUrl(String scanUrl) {
        this.scanUrl = scanUrl;
    }

    @Override
    public String toString() {
        return "QRGenerateResponse{" +
                "qrId=" + qrId +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", scanUrl='" + scanUrl + '\'' +
                '}';
    }
}
