package com.platform.order.kafka;

import com.platform.order.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventConsumer {

    @KafkaListener(
            topics = "${app.kafka.topics.order}",
            groupId = "order-service-group"
    )
    public void consume(OrderEvent event) {

        log.info(
                "Order event received. orderId={}, orderNumber={}, userId={}, status={}, totalAmount={}",
                event.orderId(),
                event.orderNumber(),
                event.userId(),
                event.status(),
                event.totalAmount()
        );

        if (event.items() != null) {

            event.items().forEach(item -> {

                log.info(
                        "Order item received. productId={}, variantId={}, quantity={}, unitPrice={}, totalPrice={}",
                        item.productId(),
                        item.variantId(),
                        item.quantity(),
                        item.unitPrice(),
                        item.totalPrice()
                );
            });
        }
    }
}