package com.develop.ws;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class IdempotentProducerIntegrationTest {
    @Autowired
    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    @MockBean
    KafkaAdmin kafkaAdmin;

    @Test
    void idempotenceEnabledTest() {
        ProducerFactory<String, ProductCreatedEvent> producerFactory = kafkaTemplate.getProducerFactory();

        Map<String, Object> config = producerFactory.getConfigurationProperties();

        assertTrue((Boolean) config.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));
        assertTrue("all".equalsIgnoreCase((String) config.get(ProducerConfig.ACKS_CONFIG)));

        if (config.containsKey(ProducerConfig.RETRIES_CONFIG)) {
            assertTrue(Integer.parseInt(config.get(ProducerConfig.RETRIES_CONFIG).toString()) > 0);
        }
    }
}