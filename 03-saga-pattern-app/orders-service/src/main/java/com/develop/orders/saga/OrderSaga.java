package com.develop.orders.saga;

import com.develop.core.dto.commands.ProcessPaymentCommand;
import com.develop.core.dto.commands.ReserveProductCommand;
import com.develop.core.dto.events.OrderCreatedEvent;
import com.develop.core.dto.events.ProductReservedEvent;
import com.develop.core.types.OrderStatus;
import com.develop.orders.service.OrderHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {
        "${orders.events.topic.name}",
        "${products.events.topic.name}"
})
@RequiredArgsConstructor
public class OrderSaga {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderHistoryService orderHistoryService;

    @Value("${products.commands.topic.name}")
    private String productsCommandsTopicName;

    @Value("${payments.commands.topic.name}")
    private String paymentsCommandsTopicName;

    @KafkaHandler
    public void handleEvent(@Payload OrderCreatedEvent event) {
        ReserveProductCommand command = ReserveProductCommand.builder()
                .productId(event.getProductId())
                .productQuantity(event.getProductQuantity())
                .orderId(event.getOrderId())
                .build();

        kafkaTemplate.send(productsCommandsTopicName, command);

        orderHistoryService.add(event.getOrderId(), OrderStatus.CREATED);
    }

    @KafkaHandler
    public void handleEvent(@Payload ProductReservedEvent event) {
        ProcessPaymentCommand command = ProcessPaymentCommand.builder()
                .orderId(event.getOrderId())
                .productId(event.getProductId())
                .productPrice(event.getProductPrice())
                .productQuantity(event.getProductQuantity())
                .build();

        kafkaTemplate.send(paymentsCommandsTopicName, command);
    }
}