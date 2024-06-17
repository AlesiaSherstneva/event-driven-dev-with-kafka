package com.develop.ws.rest;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class CreateProductRestModel {
    private String title;
    private BigDecimal price;
    private Integer quantity;
}