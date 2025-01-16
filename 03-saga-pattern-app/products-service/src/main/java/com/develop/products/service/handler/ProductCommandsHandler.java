package com.develop.products.service.handler;

import com.develop.core.dto.Product;
import com.develop.core.dto.commands.ReserveProductCommand;
import com.develop.core.dto.events.ProductReservationFailedEvent;
import com.develop.core.dto.events.ProductReservedEvent;
import com.develop.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(topics = "${products.commands.topic.name}")
@RequiredArgsConstructor
public class ProductCommandsHandler {
    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${products.events.topic.name}")
    private String productsEventsTopicName;

    @KafkaHandler
    public void handleCommand(@Payload ReserveProductCommand command) {
        try {
            Product desiredProduct = Product.builder()
                    .id(command.getProductId())
                    .quantity(command.getProductQuantity())
                    .build();
            Product reservedProduct = productService.reserve(desiredProduct, command.getOrderId());

            ProductReservedEvent productReservedEvent = ProductReservedEvent.builder()
                    .orderId(command.getOrderId())
                    .productId(command.getProductId())
                    .productPrice(reservedProduct.getPrice())
                    .productQuantity(command.getProductQuantity())
                    .build();
            kafkaTemplate.send(productsEventsTopicName, productReservedEvent);
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage(), ex);

            ProductReservationFailedEvent failedEvent = ProductReservationFailedEvent.builder()
                    .productId(command.getProductId())
                    .orderId(command.getOrderId())
                    .productQuantity(command.getProductQuantity())
                    .build();
            kafkaTemplate.send(productsEventsTopicName, failedEvent);
        }
    }
}