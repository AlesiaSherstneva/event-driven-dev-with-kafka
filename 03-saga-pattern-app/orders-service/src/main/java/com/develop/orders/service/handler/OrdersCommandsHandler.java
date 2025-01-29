package com.develop.orders.service.handler;

import com.develop.core.dto.commands.ApproveOrderCommand;
import com.develop.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "${orders.commands.topic.name}")
@RequiredArgsConstructor
public class OrdersCommandsHandler {
    private final OrderService orderService;

    @KafkaHandler
    public void handleCommand(@Payload ApproveOrderCommand command) {
        orderService.approveOrder(command.getOrderId());
    }
}