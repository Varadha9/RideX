package com.ridex.trip.kafka;

import com.ridex.trip.entity.Trip;
import com.ridex.trip.repository.TripRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class TripEventConsumer {

    private static final Logger log = Logger.getLogger(TripEventConsumer.class.getName());
    private final TripRepository tripRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TripEventConsumer(TripRepository tripRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.tripRepository = tripRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "ride-requested", groupId = "trip-service-group")
    public void onRideRequested(Map<String, Object> event) {
        log.info("Trip service received ride-requested: " + event);
        Trip trip = new Trip();
        trip.setBookingId(getLong(event, "bookingId"));
        trip.setUserId(getLong(event, "userId"));
        trip.setFare(getDouble(event, "fare"));
        trip.setStatus(Trip.TripStatus.REQUESTED);
        tripRepository.save(trip);
    }

    @KafkaListener(topics = "driver-accepted", groupId = "trip-service-group")
    public void onDriverAccepted(Map<String, Object> event) {
        Long bookingId = getLong(event, "bookingId");
        tripRepository.findByBookingId(bookingId).ifPresent(trip -> {
            trip.setDriverId(getLong(event, "driverId"));
            trip.setStatus(Trip.TripStatus.DRIVER_ASSIGNED);
            tripRepository.save(trip);
        });
    }

    @KafkaListener(topics = "ride-completed", groupId = "trip-service-group")
    public void onRideCompleted(Map<String, Object> event) {
        Long bookingId = getLong(event, "bookingId");
        tripRepository.findByBookingId(bookingId).ifPresent(trip -> {
            trip.setStatus(Trip.TripStatus.COMPLETED);
            trip.setCompletedAt(LocalDateTime.now());
            tripRepository.save(trip);
            kafkaTemplate.send("analytics-event", event);
        });
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? ((Number) val).longValue() : null;
    }

    private Double getDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? ((Number) val).doubleValue() : null;
    }
}
