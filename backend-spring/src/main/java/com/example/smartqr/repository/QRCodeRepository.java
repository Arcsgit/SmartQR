package com.example.smartqr.repository;

import com.example.smartqr.model.QRCode;
import com.example.smartqr.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QRCodeRepository extends JpaRepository<QRCode, UUID> {
//    List<QRCode> findAllByOrderByCreatedAtDesc();
    // Find all QR codes for a specific user
    List<QRCode> findByUserOrderByCreatedAtDesc(User user);

    // Find QR code by ID and user (for security)
    Optional<QRCode> findByIdAndUser(UUID id, User user);

    // Count QR codes for a user
    long countByUser(User user);
}
