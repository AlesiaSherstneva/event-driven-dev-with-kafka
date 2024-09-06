package com.develop.core.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardProcessRequest {
    @NotNull
    @Positive
    private BigInteger creditCardNumber;

    @NotNull
    @Positive
    private BigDecimal paymentAmount;
}