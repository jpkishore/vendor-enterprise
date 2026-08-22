package com.platform.order.kafka;

import com.platform.order.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private final String orderTopic;

    public OrderEventProducer(
            KafkaTemplate<String, OrderEvent> kafkaTemplate,
            @Value("${app.kafka.topics.order}")
            String orderTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderTopic = orderTopic;
    }

    public void publish(OrderEvent event) {

        kafkaTemplate.send(
                orderTopic,
                event.orderNumber(),
                event
        );

        log.info(
                "Order event published. orderNumber={}, topic={}",
                event.orderNumber(),
                orderTopic
        );
    }
}