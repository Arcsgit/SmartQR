package com.example.smartqr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class QRGenerateRequest {
    @NotBlank(message = "Data cannot be empty")
    @Size(max = 2000, message = "Data cannot exceed 2000 characters")
    private String data;

    public QRGenerateRequest() {
    }

    public QRGenerateRequest(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "QRGenerateRequest{" +
                "data='" + data + '\'' +
                '}';
    }
}
