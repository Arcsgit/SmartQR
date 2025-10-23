package com.example.smartqr.dto.auth;

import java.util.UUID;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private UUID userId;
    private String email;
    private String name;

    public AuthResponse() {
    }

    public AuthResponse(String token, String type, UUID userId, String email, String name) {
        this.token = token;
        this.type = type;
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public AuthResponse(String token, UUID userId, String email, String name) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "token='" + token + '\'' +
                ", type='" + type + '\'' +
                ", userId=" + userId +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
