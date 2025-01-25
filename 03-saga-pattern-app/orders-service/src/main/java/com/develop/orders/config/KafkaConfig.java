package com.develop.orders.config;

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
    @Value("${orders.events.topic.name}")
    private String ordersEventsTopicName;

    @Value("${products.commands.topic.name}")
    private String productsCommandsTopicName;

    @Value("${payments.commands.topic.name}")
    private String paymentsCommandsTopicName;

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(
            @Autowired(required = false) ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    NewTopic createOrdersEventsTopic() {
        return TopicBuilder.name(ordersEventsTopicName)
                .partitions(KafkaConstants.TOPIC_PARTITIONS)
                .replicas(KafkaConstants.TOPIC_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic createProductsCommandsTopic() {
        return TopicBuilder.name(productsCommandsTopicName)
                .partitions(KafkaConstants.TOPIC_PARTITIONS)
                .replicas(KafkaConstants.TOPIC_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    NewTopic createPaymentsCommandTopic() {
        return TopicBuilder.name(paymentsCommandsTopicName)
                .partitions(KafkaConstants.TOPIC_PARTITIONS)
                .replicas(KafkaConstants.TOPIC_REPLICATION_FACTOR)
                .build();
    }
}