# common-library

Shared Java library used by all microservices. Contains Kafka event POJOs and shared enums.

## Contents

### Enums

**BookingStatus**
```java
REQUESTED, DRIVER_ASSIGNED, DRIVER_ARRIVED, STARTED, COMPLETED, CANCELLED
```

**PaymentMethod**
```java
WALLET, UPI, CARD
```

### Kafka Event Classes

**RideEvent** — published by booking-service and trip-service
```java
Long bookingId
Long userId
Long driverId
String eventType       // ride-requested, driver-accepted, ride-completed
String pickupLocation
String dropLocation
Double fare
```

**NotificationEvent** — used internally by notification-service
```java
String type
String message
Long bookingId
```

## How to Use

This library is installed into the local Maven repository before building any service:

```bash
mvn -f common-library/pom.xml clean install -DskipTests
```

Each service declares it as a dependency in its `pom.xml`:

```xml
<dependency>
    <groupId>com.ridex</groupId>
    <artifactId>common-library</artifactId>
    <version>1.0.0</version>
</dependency>
```
