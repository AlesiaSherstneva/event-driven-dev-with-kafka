package com.develop.orders.service;

import com.develop.core.dto.Order;
import com.develop.core.dto.events.OrderApprovedEvent;
import com.develop.core.dto.events.OrderCreatedEvent;
import com.develop.core.types.OrderStatus;
import com.develop.orders.dao.jpa.entity.OrderEntity;
import com.develop.orders.dao.jpa.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${orders.events.topic.name}")
    private String ordersEventsTopicName;

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
        kafkaTemplate.send(ordersEventsTopicName, placeOrder);

        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getProductQuantity(),
                entity.getStatus());
    }

    @Override
    public void approveOrder(UUID orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElse(null);

        Assert.notNull(orderEntity,
                String.format("No order was found with id %s in the database", orderId));

        orderEntity.setStatus(OrderStatus.APPROVED);
        orderRepository.save(orderEntity);

        OrderApprovedEvent orderApprovedEvent = OrderApprovedEvent.builder()
                .orderId(orderId)
                .build();
        kafkaTemplate.send(ordersEventsTopicName, orderApprovedEvent);
    }
}