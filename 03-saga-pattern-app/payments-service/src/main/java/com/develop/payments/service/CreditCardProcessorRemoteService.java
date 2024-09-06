package com.develop.payments.service;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface CreditCardProcessorRemoteService {
    void process(BigInteger cardNumber, BigDecimal paymentAmount);
}