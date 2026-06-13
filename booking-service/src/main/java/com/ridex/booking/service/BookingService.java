package com.ridex.booking.service;

import com.ridex.booking.entity.Booking;
import com.ridex.booking.kafka.RideEventProducer;
import com.ridex.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RideEventProducer rideEventProducer;

    public BookingService(BookingRepository bookingRepository, RideEventProducer rideEventProducer) {
        this.bookingRepository = bookingRepository;
        this.rideEventProducer = rideEventProducer;
    }

    public Booking createBooking(Booking booking) {
        booking.setStatus(Booking.BookingStatus.REQUESTED);
        Booking saved = bookingRepository.save(booking);
        rideEventProducer.publishRideRequested(Map.of(
                "bookingId", saved.getId(),
                "userId", saved.getUserId(),
                "pickup", saved.getPickupLocation(),
                "drop", saved.getDropLocation(),
                "fare", saved.getEstimatedFare() != null ? saved.getEstimatedFare() : 0.0
        ));
        return saved;
    }

    public Booking getStatus(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));
    }

    public Booking assignDriver(Long bookingId, Long driverId) {
        Booking booking = getStatus(bookingId);
        booking.setDriverId(driverId);
        booking.setStatus(Booking.BookingStatus.DRIVER_ASSIGNED);
        Booking updated = bookingRepository.save(booking);
        rideEventProducer.publishDriverAccepted(Map.of("bookingId", bookingId, "driverId", driverId));
        return updated;
    }

    public Booking cancelBooking(Long id) {
        Booking booking = getStatus(id);
        if (booking.getStatus() == Booking.BookingStatus.STARTED ||
            booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel booking in status: " + booking.getStatus());
        }
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }
}
