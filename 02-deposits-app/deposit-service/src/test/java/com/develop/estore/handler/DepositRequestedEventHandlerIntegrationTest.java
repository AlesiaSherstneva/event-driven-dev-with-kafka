package com.develop.estore.handler;

import com.develop.payments.events.DepositRequestedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@EmbeddedKafka
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
public class DepositRequestedEventHandlerIntegrationTest {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private Environment env;

    @Test
    void handleDepositRequestedEventPositiveTest(CapturedOutput output) throws ExecutionException, InterruptedException {
        DepositRequestedEvent depositEvent = DepositRequestedEvent.builder()
                .amount(BigDecimal.valueOf(new Random().nextDouble() * 100_000)
                        .setScale(2, RoundingMode.HALF_UP))
                .build();

        ProducerRecord<String, Object> record =
                new ProducerRecord<>(env.getRequiredProperty("deposit-topic-name"), depositEvent);

        kafkaTemplate.send(record).get();

        String logMessage = String.format("Received a new deposit event: %s", depositEvent.getAmount().toString());

        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> output.getOut().contains(logMessage));
    }
}