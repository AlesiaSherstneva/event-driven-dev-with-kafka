package com.develop.estore.service;

import com.develop.estore.error.TransferServiceException;
import com.develop.estore.io.TransferEntity;
import com.develop.estore.io.TransferRepository;
import com.develop.estore.model.TransferRestModel;
import com.develop.payments.events.DepositRequestedEvent;
import com.develop.payments.events.WithdrawalRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.Uuid;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Environment environment;
    private final RestTemplate restTemplate;
    private final TransferRepository transferRepository;

    @Override
    // if we need specific behaviour
    /* @Transactional(value = "kafkaTransactionManager",
            rollbackFor = {TransferServiceException.class, ConnectException.class},
            noRollbackFor = {SpecificException.class}) */
    // if we have one default behaviour
    @Transactional("transactionManager")
    public boolean transfer(TransferRestModel transferRestModel) {
        WithdrawalRequestedEvent withdrawalEvent = new WithdrawalRequestedEvent(transferRestModel.getSenderId(),
                transferRestModel.getRecipientId(), transferRestModel.getAmount());

        DepositRequestedEvent depositEvent = new DepositRequestedEvent(transferRestModel.getSenderId(),
                transferRestModel.getRecipientId(), transferRestModel.getAmount());

        TransferEntity transferEntity = new TransferEntity();
        BeanUtils.copyProperties(transferRestModel, transferEntity);
        transferEntity.setTransferId(Uuid.randomUuid().toString());

        try {
            // save record to a database table
            transferRepository.save(transferEntity);

            kafkaTemplate.send(environment.getProperty("withdraw-money-topic", "withdraw-money-topic"), withdrawalEvent);
            log.info("Sent event to withdrawal topic");

            // Business logic that causes and error
            callRemoteService();

            kafkaTemplate.send(environment.getProperty("deposit-money-topic", "deposit-money-topic"), depositEvent);
            log.info("Sent event to deposit topic");

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new TransferServiceException(ex);
        }

        return true;
    }

    private void callRemoteService() throws Exception {
        String requestUrl = "http://localhost:8082/response/200";
        ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, null, String.class);

        if (response.getStatusCode().value() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            throw new Exception("Destination Microservice not available");
        }

        if (response.getStatusCode().value() == HttpStatus.OK.value()) {
            log.info("Received response from mock service: " + response.getBody());
        }
    }
}
