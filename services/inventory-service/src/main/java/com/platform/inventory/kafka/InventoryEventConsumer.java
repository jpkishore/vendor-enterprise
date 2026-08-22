package com.platform.inventory.kafka;

import com.platform.inventory.dto.InventoryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryEventConsumer {

    @KafkaListener(
            topics = "${app.kafka.topics.inventory}",
            groupId = "${spring.application.name}-consumer"
    )
    public void consume(InventoryEvent event) {

        log.info(
                "Inventory Kafka event received: eventType={}, " +
                        "inventoryId={}, productId={}, variantId={}, " +
                        "quantity={}, reservedQuantity={}, availableQuantity={}, " +
                        "occurredAt={}",
                event.eventType(),
                event.inventoryId(),
                event.productId(),
                event.variantId(),
                event.quantity(),
                event.reservedQuantity(),
                event.availableQuantity(),
                event.occurredAt()
        );
    }
}