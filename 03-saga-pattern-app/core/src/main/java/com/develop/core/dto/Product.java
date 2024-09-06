package com.develop.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private UUID id;
    private String name;
    private BigDecimal price;
    private Integer quantity;

    public Product(UUID productId, Integer quantity) {
        this.id = productId;
        this.quantity = quantity;
    }
}