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
    List<QRCode> findByUserOrderByCreatedAtDesc(User user);

    Optional<QRCode> findByIdAndUser(UUID id, User user);

    long countByUser(User user);
}
