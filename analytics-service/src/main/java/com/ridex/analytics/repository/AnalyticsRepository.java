package com.ridex.analytics.repository;

import com.ridex.analytics.entity.RideAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsRepository extends JpaRepository<RideAnalytics, Long> {

    @Query("SELECT COUNT(r) FROM RideAnalytics r WHERE r.timestamp >= :start AND r.timestamp < :end")
    Long countRidesInRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(r.fare) FROM RideAnalytics r WHERE r.timestamp >= :start AND r.timestamp < :end")
    Double sumRevenueInRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(r.fare) FROM RideAnalytics r WHERE r.driverId = :driverId")
    Double driverEarnings(@Param("driverId") Long driverId);

    List<RideAnalytics> findTop10ByOrderByTimestampDesc();
}
