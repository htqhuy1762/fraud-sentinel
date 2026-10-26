package com.fraudsentinel.scoring.messaging;

import com.fraudsentinel.scoring.domain.Transaction;
import com.fraudsentinel.scoring.domain.TransactionCompletedEvent;
import com.fraudsentinel.scoring.scoring.ScoringService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;

@Component
public class TransactionCompletedConsumer {

    private final ScoringService scoringService;
    private final JsonMapper json;
    private final Validator validator;

    public TransactionCompletedConsumer(ScoringService scoringService, JsonMapper json, Validator validator) {
        this.scoringService = scoringService;
        this.json = json;
        this.validator = validator;
    }

    @KafkaListener(topics = "${app.topics.transactions}")
    public void onMessage(ConsumerRecord<String, String> record) {
        TransactionCompletedEvent event = json.readValue(record.value(), TransactionCompletedEvent.class);

        if (event.transaction() == null || event.status() == null) {
            throw new InvalidMessageException("Missing transaction or status at offset " + record.offset());
        }
        Set<ConstraintViolation<Transaction>> violations = validator.validate(event.transaction());
        if (!violations.isEmpty()) {
            throw new InvalidMessageException("Invalid txn " + event.transaction().txnId() + ": " + violations);
        }

        scoringService.monitor(event);
    }
}