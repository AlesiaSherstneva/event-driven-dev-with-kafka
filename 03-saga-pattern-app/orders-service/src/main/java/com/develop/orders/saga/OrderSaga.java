package com.develop.orders.saga;

import com.develop.core.dto.commands.ReserveProductCommand;
import com.develop.core.dto.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "${order.events.topic.name}")
@RequiredArgsConstructor
public class OrderSaga {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${products.commands.topic.name}")
    private final String productCommandTopicName;

    @KafkaHandler
    public void handleEvent(@Payload OrderCreatedEvent event) {
        ReserveProductCommand command = ReserveProductCommand.builder()
                .productId(event.getProductId())
                .productQuantity(event.getProductQuantity())
                .orderId(event.getOrderId())
                .build();
        kafkaTemplate.send(productCommandTopicName, command);
    }
}