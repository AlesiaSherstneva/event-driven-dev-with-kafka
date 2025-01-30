package com.develop.orders.service;

import com.develop.core.dto.Order;

import java.util.UUID;

public interface OrderService {
    Order placeOrder(Order order);

    void approveOrder(UUID orderId);
}