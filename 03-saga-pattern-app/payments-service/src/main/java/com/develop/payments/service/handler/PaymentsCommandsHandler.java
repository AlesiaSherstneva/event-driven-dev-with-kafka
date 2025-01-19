package com.develop.payments.service.handler;

import com.develop.core.dto.Payment;
import com.develop.core.dto.commands.ProcessPaymentCommand;
import com.develop.core.exceptions.CreditCardProcessorUnavailableException;
import com.develop.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(topics = "${payments.commands.topic.name}")
@RequiredArgsConstructor
public class PaymentsCommandsHandler {
    private final PaymentService paymentService;

    @KafkaHandler
    public void handleCommand(@Payload ProcessPaymentCommand command) {
        try {
            Payment payment = Payment.builder()
                    .orderId(command.getOrderId())
                    .productId(command.getProductId())
                    .productPrice(command.getProductPrice())
                    .productQuantity(command.getProductQuantity())
                    .build();

            Payment processPayment = paymentService.process(payment);
        } catch (CreditCardProcessorUnavailableException ex) {
            log.error(ex.getLocalizedMessage(), ex);
        }
    }
}