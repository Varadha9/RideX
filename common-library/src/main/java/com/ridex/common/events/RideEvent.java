package com.ridex.common.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideEvent {
    private Long bookingId;
    private Long userId;
    private Long driverId;
    private String eventType;   // ride-requested, driver-accepted, ride-started, ride-completed, payment-completed
    private String pickupLocation;
    private String dropLocation;
    private Double fare;
}
