package com.ridex.booking.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.logging.Logger;

@Component
public class RideEventProducer {

    private static final Logger log = Logger.getLogger(RideEventProducer.class.getName());
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RideEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishRideRequested(Map<String, Object> event) {
        log.info("Publishing ride-requested: " + event);
        kafkaTemplate.send("ride-requested", event);
    }

    public void publishDriverAccepted(Map<String, Object> event) {
        log.info("Publishing driver-accepted: " + event);
        kafkaTemplate.send("driver-accepted", event);
    }
}
