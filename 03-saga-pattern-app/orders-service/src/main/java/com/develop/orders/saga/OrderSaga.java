package com.develop.orders.saga;

import com.develop.core.dto.events.OrderCreatedEvent;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "${order.events.topic.name}")
public class OrderSaga {
    @KafkaHandler
    public void handleEvent(@Payload OrderCreatedEvent event) {

    }
}