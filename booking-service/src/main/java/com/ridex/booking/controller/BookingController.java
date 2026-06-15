package com.ridex.booking.controller;

import com.ridex.booking.entity.Booking;
import com.ridex.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Booking Service", description = "Core ride booking management")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new ride booking")
    public ResponseEntity<Booking> create(@RequestBody Booking booking) {
        return ResponseEntity.ok(bookingService.createBooking(booking));
    }

    @GetMapping("/status/{id}")
    @Operation(summary = "Get booking status")
    public ResponseEntity<Booking> getStatus(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getStatus(id));
    }

    @PostMapping("/cancel/{id}")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<Booking> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    @PutMapping("/assign-driver/{bookingId}")
    @Operation(summary = "Assign a driver to a booking")
    public ResponseEntity<Booking> assignDriver(@PathVariable Long bookingId, @RequestParam Long driverId) {
        return ResponseEntity.ok(bookingService.assignDriver(bookingId, driverId));
    }
}
