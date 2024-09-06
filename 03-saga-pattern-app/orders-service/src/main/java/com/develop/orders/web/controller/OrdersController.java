package com.develop.orders.web.controller;

import com.develop.core.dto.Order;
import com.develop.orders.dto.CreateOrderRequest;
import com.develop.orders.dto.CreateOrderResponse;
import com.develop.orders.dto.OrderHistoryResponse;
import com.develop.orders.service.OrderHistoryService;
import com.develop.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {
    private final OrderService orderService;
    private final OrderHistoryService orderHistoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CreateOrderResponse placeOrder(@RequestBody @Valid CreateOrderRequest request) {
        Order order = new Order();
        BeanUtils.copyProperties(request, order);
        Order createdOrder = orderService.placeOrder(order);

        CreateOrderResponse response = new CreateOrderResponse();
        BeanUtils.copyProperties(createdOrder, response);
        return response;
    }

    @GetMapping("/{orderId}/history")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderHistoryResponse> getOrderHistory(@PathVariable UUID orderId) {
        return orderHistoryService.findByOrderId(orderId).stream().map(orderHistory -> {
            OrderHistoryResponse orderHistoryResponse = new OrderHistoryResponse();
            BeanUtils.copyProperties(orderHistory, orderHistoryResponse);
            return orderHistoryResponse;
        }).toList();
    }
}