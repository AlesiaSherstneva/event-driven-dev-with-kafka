package com.develop.core.dto;


import com.develop.core.types.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private UUID orderId;
    private UUID customerId;
    private UUID productId;
    private Integer productQuantity;
    private OrderStatus status;

    public Order(UUID customerId, UUID productId, Integer productQuantity, OrderStatus status) {
        this.customerId = customerId;
        this.productId = productId;
        this.productQuantity = productQuantity;
        this.status = status;
    }
}