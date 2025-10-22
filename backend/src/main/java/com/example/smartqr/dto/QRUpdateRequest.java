package com.example.smartqr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class QRUpdateRequest {
    @NotBlank(message = "Data cannot be empty")
    @Size(max = 2000, message = "Data cannot exceed 2000 characters")
    private String data;

    public QRUpdateRequest() {
    }

    public QRUpdateRequest(String data) {
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
        return "QRUpdateRequest{" +
                "data='" + data + '\'' +
                '}';
    }
}
