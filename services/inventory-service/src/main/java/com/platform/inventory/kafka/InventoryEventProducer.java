package com.platform.inventory.kafka;

import com.platform.inventory.dto.InventoryEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventProducer {

    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;

    private final String inventoryTopic;

    public InventoryEventProducer(
            KafkaTemplate<String, InventoryEvent> kafkaTemplate,
            @Value("${app.kafka.topics.inventory}")
            String inventoryTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.inventoryTopic = inventoryTopic;
    }

    public void publish(InventoryEvent event) {

        String key = event.variantId().toString();

        kafkaTemplate.send(
                inventoryTopic,
                key,
                event
        );
    }
}