# payment-service

Simulates payment processing for completed rides. Supports WALLET, UPI, and CARD methods. Publishes a `payment-completed` Kafka event after successful payment.

- **Port:** 8085
- **Database:** MySQL (`ridex`) + Kafka producer
- **Swagger:** http://localhost:8085/swagger-ui.html

## Endpoints

| Method | Endpoint                      | Description              |
|--------|-------------------------------|--------------------------|
| POST   | /api/payment/pay              | Process a payment        |
| GET    | /api/payment/history/{userId} | Get user payment history |

## Payment Methods

| Method | Description             |
|--------|-------------------------|
| WALLET | In-app wallet balance   |
| UPI    | UPI payment simulation  |
| CARD   | Credit/debit card sim   |

## Kafka Events Published

| Action            | Topic             | Consumed By          |
|-------------------|-------------------|----------------------|
| Payment processed | payment-completed | notification-service |

## Process Payment Example

```bash
curl -X POST http://localhost:8085/api/payment/pay \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 1,
    "userId": 1,
    "amount": 152.0,
    "paymentMethod": "UPI"
  }'
```
