package com.develop.orders.service;

import com.develop.core.dto.Order;

public interface OrderService {
    Order placeOrder(Order order);
}