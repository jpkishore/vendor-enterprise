package com.platform.payment.kafka;

import com.platform.payment.dto.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    private final String paymentTopic;

    public PaymentEventProducer(
            KafkaTemplate<String, PaymentEvent> kafkaTemplate,
            @Value("${app.kafka.topics.payment}")
            String paymentTopic
    ) {

        this.kafkaTemplate =
                kafkaTemplate;

        this.paymentTopic =
                paymentTopic;
    }

    public void publish(
            PaymentEvent event
    ) {

        kafkaTemplate.send(
                paymentTopic,
                event.orderNumber(),
                event
        );

        log.info(
                "Payment event published. paymentNumber={}, orderNumber={}, status={}, topic={}",
                event.paymentNumber(),
                event.orderNumber(),
                event.status(),
                paymentTopic
        );
    }
}