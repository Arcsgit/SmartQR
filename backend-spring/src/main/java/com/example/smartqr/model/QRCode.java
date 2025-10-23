package com.example.smartqr.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table
public class QRCode {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String data;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    public QRCode() {
    }

    public QRCode(UUID id, String data, String imageUrl, LocalDateTime createdAt, User user) {
        this.id = id;
        this.data = data;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (id == null) {
            id = UUID.randomUUID();
        }
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
                ", createdAt=" + createdAt +
                ", user=" + user +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        QRCode qrCode = (QRCode) o;
        return Objects.equals(id, qrCode.id) && Objects.equals(data, qrCode.data) && Objects.equals(imageUrl, qrCode.imageUrl) && Objects.equals(createdAt, qrCode.createdAt) && Objects.equals(user, qrCode.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, data, imageUrl, createdAt, user);
    }
}
