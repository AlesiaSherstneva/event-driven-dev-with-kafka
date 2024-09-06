package com.develop.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
    private UUID id;
    private UUID orderId;
    private UUID paymentId;

    public Shipment(UUID orderId, UUID paymentId) {
        this.orderId = orderId;
        this.paymentId = paymentId;
    }
}