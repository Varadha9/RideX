package com.ridex.analytics.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ride_analytics")
public class RideAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ride_id")
    private Long rideId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "driver_id")
    private Long driverId;

    private String city;
    private Double distance;
    private Double fare;
    private Long duration;

    private LocalDateTime timestamp = LocalDateTime.now();

    public Long getId() { return id; }
    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    public Double getFare() { return fare; }
    public void setFare(Double fare) { this.fare = fare; }
    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
