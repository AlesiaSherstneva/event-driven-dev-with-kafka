package com.develop.orders.service;

import com.develop.core.dto.Order;
import com.develop.core.dto.events.OrderCreatedEvent;
import com.develop.core.types.OrderStatus;
import com.develop.orders.dao.jpa.entity.OrderEntity;
import com.develop.orders.dao.jpa.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${order.events.topic.name}")
    private String orderEventsTopicName;

    @Override
    public Order placeOrder(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setCustomerId(order.getCustomerId());
        entity.setProductId(order.getProductId());
        entity.setProductQuantity(order.getProductQuantity());
        entity.setStatus(OrderStatus.CREATED);
        orderRepository.save(entity);

        OrderCreatedEvent placeOrder = OrderCreatedEvent.builder()
                .orderId(entity.getId())
                .customerId(entity.getCustomerId())
                .productId(entity.getProductId())
                .productQuantity(entity.getProductQuantity())
                .build();
        kafkaTemplate.send(orderEventsTopicName, placeOrder);

        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getProductQuantity(),
                entity.getStatus());
    }
}