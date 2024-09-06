package com.develop.orders.dao.jpa.entity;

import com.develop.core.types.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_quantity")
    private Integer productQuantity;
}