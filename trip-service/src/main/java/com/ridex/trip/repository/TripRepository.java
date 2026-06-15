package com.ridex.trip.repository;

import com.ridex.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findByBookingId(Long bookingId);
    List<Trip> findByUserId(Long userId);
    List<Trip> findByDriverId(Long driverId);
}
