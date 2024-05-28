package com.develop.estore.handler;

import com.develop.payments.events.DepositRequestedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(topics = "deposit-money-topic", containerFactory = "kafkaListenerContainerFactory")
public class DepositRequestedEventHandler {
	@KafkaHandler
	public void handle(@Payload DepositRequestedEvent depositRequestedEvent) {
		log.info("Received a new deposit event: {} ", depositRequestedEvent.getAmount());
	}
}