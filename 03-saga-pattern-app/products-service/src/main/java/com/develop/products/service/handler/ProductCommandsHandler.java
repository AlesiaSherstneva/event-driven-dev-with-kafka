package com.develop.products.service.handler;

import com.develop.core.dto.Product;
import com.develop.core.dto.commands.ReserveProductCommand;
import com.develop.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(topics = "${products.commands.topic.name}")
@RequiredArgsConstructor
public class ProductCommandsHandler {
    private final ProductService productService;

    @KafkaHandler
    public void handleCommand(@Payload ReserveProductCommand command) {
        try {
            Product desiredProduct = Product.builder()
                    .id(command.getProductId())
                    .quantity(command.getProductQuantity())
                    .build();

            productService.reserve(desiredProduct, command.getOrderId());
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage(), ex);
        }
    }
}