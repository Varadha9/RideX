package com.ridex.common.events;

public class RideEvent {
    private Long bookingId;
    private Long userId;
    private Long driverId;
    private String eventType;
    private String pickupLocation;
    private String dropLocation;
    private Double fare;

    public RideEvent() {}

    public RideEvent(Long bookingId, Long userId, Long driverId, String eventType,
                     String pickupLocation, String dropLocation, Double fare) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.driverId = driverId;
        this.eventType = eventType;
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.fare = fare;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public String getDropLocation() { return dropLocation; }
    public void setDropLocation(String dropLocation) { this.dropLocation = dropLocation; }
    public Double getFare() { return fare; }
    public void setFare(Double fare) { this.fare = fare; }
}
