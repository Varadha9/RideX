package com.ridex.payment.service;

import com.ridex.payment.entity.Payment;
import com.ridex.payment.repository.PaymentRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class PaymentService {

    private static final Logger log = Logger.getLogger(PaymentService.class.getName());
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentService(PaymentRepository paymentRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Payment processPayment(Payment payment) {
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        Payment saved = paymentRepository.save(payment);
        log.info("Payment processed: bookingId=" + saved.getBookingId()
                + ", method=" + saved.getPaymentMethod() + ", amount=" + saved.getAmount());
        kafkaTemplate.send("payment-completed", Map.of(
                "bookingId", saved.getBookingId(),
                "userId", saved.getUserId(),
                "amount", saved.getAmount(),
                "method", saved.getPaymentMethod().name()
        ));
        return saved;
    }

    public List<Payment> getHistory(Long userId) {
        return paymentRepository.findByUserId(userId);
    }
}
