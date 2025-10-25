package com.example.smartqr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class QRUpdateRequest {
    @NotBlank(message = "Data cannot be empty")
    @Size(max = 2000, message = "Data cannot exceed 2000 characters")
    private String data;

    private String name;

    public QRUpdateRequest() {
    }

    public QRUpdateRequest(String data, String name) {
        this.data = data;
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "QRUpdateRequest{" +
                "data='" + data + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
