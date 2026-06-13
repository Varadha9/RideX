package com.ridex.notification.consumer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.logging.Logger;

@Component
public class KafkaToRabbitBridge {

    private static final Logger log = Logger.getLogger(KafkaToRabbitBridge.class.getName());
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}") private String exchange;
    @Value("${rabbitmq.routing-key}") private String routingKey;

    public KafkaToRabbitBridge(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @KafkaListener(topics = {"ride-requested", "driver-accepted", "ride-completed", "payment-completed"},
                   groupId = "notification-service-group")
    public void onRideEvent(Map<String, Object> event) {
        Map<String, Object> notification = Map.of(
                "message", "Notification for booking #" + event.getOrDefault("bookingId", "N/A"),
                "event", event
        );
        log.info("Forwarding to RabbitMQ: " + notification);
        rabbitTemplate.convertAndSend(exchange, routingKey, notification);
    }
}
