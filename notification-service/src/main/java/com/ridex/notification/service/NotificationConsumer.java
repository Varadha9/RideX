package com.ridex.notification.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.logging.Logger;

@Service
public class NotificationConsumer {

    private static final Logger log = Logger.getLogger(NotificationConsumer.class.getName());

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void processNotification(Map<String, Object> notification) {
        log.info("=== NOTIFICATION ===");
        log.info("Message: " + notification.get("message"));
        log.info("Event data: " + notification.get("event"));
    }
}
