package com.develop.products.config;

import com.develop.core.constants.KafkaConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {
    @Value("${products.events.topic.name}")
    private String productsEventsTopicName;

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(
            @Autowired(required = false) ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    NewTopic createProductsEventTopic() {
        return TopicBuilder.name(productsEventsTopicName)
                .partitions(KafkaConstants.TOPIC_PARTITIONS)
                .replicas(KafkaConstants.TOPIC_REPLICATION_FACTOR)
                .build();
    }
}