package com.example.smartqr.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "qr_codes")  // Keep explicit table name
public class QRCode {
    @Id
    private UUID id;  // ← REMOVED @GeneratedValue (manual generation)

    @Column(nullable = false, columnDefinition = "TEXT")  // ← CHANGED back to TEXT
    private String data;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")  // ← CHANGED back to TEXT
    private String imageUrl;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        // Auto-generate name if not provided
        if (name == null || name.isEmpty()) {
            name = generateNameFromData();
        }
    }

    private String generateNameFromData() {
        try {
            java.net.URL url = new java.net.URL(data);
            String host = url.getHost().replace("www.", "");
            return host.length() > 30 ? host.substring(0, 30) : host;
        } catch (Exception e) {
            return data.length() > 30 ? data.substring(0, 30) + "..." : data;
        }
    }

    public QRCode() {
    }

    public QRCode(UUID id, String data, String imageUrl, String name, LocalDateTime createdAt, User user) {
        this.id = id;
        this.data = data;
        this.imageUrl = imageUrl;
        this.name = name;
        this.createdAt = createdAt;
        this.user = user;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "QRCode{" +
                "id=" + id +
                ", data='" + data + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", user=" + user +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        QRCode qrCode = (QRCode) o;
        return Objects.equals(id, qrCode.id) && Objects.equals(data, qrCode.data) && Objects.equals(imageUrl, qrCode.imageUrl) && Objects.equals(name, qrCode.name) && Objects.equals(createdAt, qrCode.createdAt) && Objects.equals(user, qrCode.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data, imageUrl, name, createdAt, user);
    }
}
