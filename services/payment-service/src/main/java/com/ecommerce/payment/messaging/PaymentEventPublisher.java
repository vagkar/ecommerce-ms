package com.ecommerce.payment.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String succeededTopic;
    private final String failedTopic;

    public PaymentEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.payment-succeeded}") String succeededTopic,
            @Value("${app.kafka.topics.payment-failed}") String failedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.succeededTopic = succeededTopic;
        this.failedTopic = failedTopic;
    }

    public void publishSucceeded(PaymentSucceededEvent event) {
        kafkaTemplate.send(succeededTopic, event.orderId().toString(), event);
    }

    public void publishFailed(PaymentFailedEvent event) {
        kafkaTemplate.send(failedTopic, event.orderId().toString(), event);
    }
}
