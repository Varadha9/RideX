package com.ridex.trip.service;

import com.ridex.trip.entity.Trip;
import com.ridex.trip.repository.TripRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TripService(TripRepository tripRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.tripRepository = tripRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Trip startTrip(Long bookingId) {
        Trip trip = tripRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Trip not found for booking: " + bookingId));
        trip.setStatus(Trip.TripStatus.STARTED);
        trip.setStartedAt(LocalDateTime.now());
        Trip saved = tripRepository.save(trip);
        kafkaTemplate.send("ride-started", Map.of("bookingId", bookingId, "driverId", trip.getDriverId()));
        return saved;
    }

    public Trip endTrip(Long bookingId) {
        Trip trip = tripRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Trip not found for booking: " + bookingId));
        trip.setStatus(Trip.TripStatus.COMPLETED);
        trip.setCompletedAt(LocalDateTime.now());
        Trip saved = tripRepository.save(trip);
        kafkaTemplate.send("ride-completed", Map.of(
                "bookingId", bookingId,
                "driverId", trip.getDriverId(),
                "userId", trip.getUserId(),
                "fare", trip.getFare()
        ));
        return saved;
    }

    public List<Trip> getUserHistory(Long userId) {
        return tripRepository.findByUserId(userId);
    }

    public List<Trip> getDriverHistory(Long driverId) {
        return tripRepository.findByDriverId(driverId);
    }
}
