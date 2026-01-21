package com.example.smartqr.repository;

import com.example.smartqr.model.QRAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QRAnalyticsRepository extends JpaRepository<QRAnalytics, Long> {
    List<QRAnalytics> findByQrIdOrderByTimestampDesc(UUID qrId);

    long countByQrId(UUID qrId);

    @Query("SELECT COUNT(DISTINCT a.ipAddress) FROM QRAnalytics a WHERE a.qrId = :qrId")
    long countDistinctIpsByQrId(@Param("qrId") UUID qrId);

    @Query("SELECT a FROM QRAnalytics a WHERE a.qrId = :qrId ORDER BY a.timestamp DESC")
    List<QRAnalytics> findRecentScansByQrId(@Param("qrId") UUID qrId, org.springframework.data.domain.Pageable pageable);
}
