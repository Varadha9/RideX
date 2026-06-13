package com.ridex.analytics.service;

import com.ridex.analytics.entity.RideAnalytics;
import com.ridex.analytics.repository.AnalyticsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.logging.Logger;

@Service
public class AnalyticsEventConsumer {

    private static final Logger log = Logger.getLogger(AnalyticsEventConsumer.class.getName());
    private final AnalyticsRepository analyticsRepository;

    public AnalyticsEventConsumer(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @KafkaListener(topics = "analytics-event", groupId = "analytics-service-group")
    public void onRideCompleted(Map<String, Object> event) {
        log.info("Analytics received: " + event);
        RideAnalytics record = new RideAnalytics();
        record.setRideId(getLong(event, "bookingId"));
        record.setUserId(getLong(event, "userId"));
        record.setDriverId(getLong(event, "driverId"));
        record.setFare(getDouble(event, "fare"));
        analyticsRepository.save(record);
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
