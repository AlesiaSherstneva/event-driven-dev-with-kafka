package com.develop.estore.service;

import com.develop.estore.error.TransferServiceException;
import com.develop.estore.model.TransferRestModel;
import com.develop.payments.events.DepositRequestedEvent;
import com.develop.payments.events.WithdrawalRequestedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@EmbeddedKafka(partitions = 3, count = 3, controlledShutdown = true,
        topics = {"${deposit-topic-name}", "${withdraw-topic-name}"})
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
public class TransferServiceIntegrationTest {
    @Autowired(required = false)
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private Environment env;

    @Autowired
    private TransferService transferService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private TransferRestModel transferRestModel;
    private MockRestServiceServer mockServer;
    private Consumer<String, Object> depositConsumer;
    private Consumer<String, Object> withdrawConsumer;

    @BeforeEach
    public void setUp() {
        transferRestModel = TransferRestModel.builder()
                .senderId("testSenderId")
                .recipientId("testRecipientId")
                .amount(new BigDecimal(500))
                .build();

        mockServer = MockRestServiceServer.createServer(restTemplate);

        depositConsumer = createConsumer(
                env.getRequiredProperty("spring.kafka.consumer.deposit-group-id"),
                env.getRequiredProperty("deposit-topic-name")
        );
        withdrawConsumer = createConsumer(
                env.getRequiredProperty("spring.kafka.consumer.withdraw-group-id"),
                env.getRequiredProperty("withdraw-topic-name")
        );

        skipAllExistingMessages(depositConsumer, withdrawConsumer);
    }

    private Consumer<String, Object> createConsumer(String groupId, String topic) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(groupId, "true", embeddedKafkaBroker);

        Consumer<String, Object> consumer = new DefaultKafkaConsumerFactory<String, Object>(consumerProps).createConsumer();

        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, topic);
        return consumer;
    }

    @SafeVarargs
    private void skipAllExistingMessages(Consumer<String, Object>... consumers) {
        for (Consumer<String, Object> consumer : consumers) {
            await().atMost(5, TimeUnit.SECONDS)
                    .until(() -> !consumer.assignment().isEmpty());

            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(consumer.assignment());
            endOffsets.forEach(consumer::seek);

            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(100));
            assertThat(records.isEmpty()).isTrue();
        }
    }

    @Test
    public void createPaymentSuccessfulTest() {
        mockServer.expect(requestTo("http://localhost:8082/response/200"))
                .andRespond(withSuccess());

        transferService.transfer(transferRestModel);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, Object> depositRecords = KafkaTestUtils.getRecords(depositConsumer);
            DepositRequestedEvent depositEvent = objectMapper.readValue(
                    depositRecords.iterator().next().value().toString(),
                    DepositRequestedEvent.class
            );

            assertAll(
                    () -> assertThat(depositRecords.count()).isEqualTo(1),
                    () -> assertThat(depositEvent.getSenderId()).isEqualTo(transferRestModel.getSenderId()),
                    () -> assertThat(depositEvent.getRecipientId()).isEqualTo(transferRestModel.getRecipientId()),
                    () -> assertThat(depositEvent.getAmount()).isEqualTo(transferRestModel.getAmount())
            );

            ConsumerRecords<String, Object> withdrawRecords = KafkaTestUtils.getRecords(withdrawConsumer);
            WithdrawalRequestedEvent withdrawEvent = objectMapper.readValue(
                    withdrawRecords.iterator().next().value().toString(),
                    WithdrawalRequestedEvent.class
            );

            assertAll(
                    () -> assertThat(withdrawRecords.count()).isEqualTo(1),
                    () -> assertThat(withdrawEvent.getSenderId()).isEqualTo(transferRestModel.getSenderId()),
                    () -> assertThat(withdrawEvent.getRecipientId()).isEqualTo(transferRestModel.getRecipientId()),
                    () -> assertThat(withdrawEvent.getAmount()).isEqualTo(transferRestModel.getAmount())
            );
        });
    }

    @Test
    public void createPaymentFailTest() {
        mockServer.expect(requestTo("http://localhost:8082/response/200"))
                .andRespond(withServiceUnavailable());

        assertThatThrownBy(() -> transferService.transfer(transferRestModel))
                .isInstanceOf(TransferServiceException.class);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, Object> depositRecords = depositConsumer.poll(Duration.ofMillis(1000));
            assertThat(depositRecords.count()).isEqualTo(0);

            ConsumerRecords<String, Object> withdrawRecords = withdrawConsumer.poll(Duration.ofMillis(1000));
            assertThat(withdrawRecords.count()).isEqualTo(0);
        });
    }

    @AfterEach
    public void tearDown() {
        depositConsumer.close();
        withdrawConsumer.close();
    }
}