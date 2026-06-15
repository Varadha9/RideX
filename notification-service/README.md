# notification-service

Acts as a bridge between Kafka and RabbitMQ. Listens to all ride lifecycle events on Kafka and forwards them to a RabbitMQ queue for downstream notification delivery (email, SMS, push).

- **Port:** 8087
- **Database:** None (stateless bridge)
- **Kafka consumer + RabbitMQ producer**

## How It Works

```
Kafka Topics ──────────────────► KafkaToRabbitBridge ──► RabbitMQ Exchange
  ride-requested                                           notification_exchange
  driver-accepted                                                │
  ride-completed                                                 ▼
  payment-completed                                      notification_queue
```

## Kafka Topics Consumed

| Topic             | Trigger                     |
|-------------------|-----------------------------|
| ride-requested    | New booking created         |
| driver-accepted   | Driver assigned to booking  |
| ride-completed    | Trip ended                  |
| payment-completed | Payment processed           |

## RabbitMQ Config

| Setting       | Value                   |
|---------------|-------------------------|
| Exchange      | notification_exchange   |
| Queue         | notification_queue      |
| Routing Key   | notification            |
| Host          | rabbitmq                |
| Username      | ridex                   |
| Password      | ridex                   |

## RabbitMQ Management UI

```
http://localhost:15672
Username: ridex
Password: ridex
```

## Notification Payload Format

```json
{
  "message": "Notification for booking #1",
  "event": {
    "bookingId": 1,
    "eventType": "ride-completed",
    "fare": 152.0
  }
}
```
