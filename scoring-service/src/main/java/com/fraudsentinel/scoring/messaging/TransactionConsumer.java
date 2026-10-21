package com.fraudsentinel.scoring.messaging;

import com.fraudsentinel.scoring.domain.ScoreResult;
import com.fraudsentinel.scoring.domain.Transaction;
import com.fraudsentinel.scoring.scoring.ScoringService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.Set;

@Component
public class TransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionConsumer.class);

    private final ScoringService scoringService;
    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper json;
    private final Validator validator;
    private final String decisionsTopic;

    public TransactionConsumer(ScoringService scoringService, KafkaTemplate<String, String> kafka,
                               JsonMapper json, Validator validator,
                               @Value("${app.topics.decisions}") String decisionsTopic) {
        this.scoringService = scoringService;
        this.kafka = kafka;
        this.json = json;
        this.validator = validator;
        this.decisionsTopic = decisionsTopic;
    }

    @KafkaListener(topics = "${app.topics.transactions}")
    public void onMessage(ConsumerRecord<String, String> record) {
        Transaction txn;
        try {
            txn = json.readValue(record.value(), Transaction.class);
        } catch (JacksonException e) {
            // JSON hỏng: retry bao nhiêu lần cũng hỏng → log rồi bỏ qua (sau này: đẩy vào dead-letter topic)
            log.error("Bad JSON at partition={} offset={}: {}", record.partition(), record.offset(), e.getMessage());
            return;
        }

        Set<ConstraintViolation<Transaction>> violations = validator.validate(txn);
        if (!violations.isEmpty()) {
            log.error("Invalid txn {}: {}", txn.txnId(), violations);
            return;
        }

        ScoreResult result = scoringService.score(txn);
        kafka.send(decisionsTopic, txn.accountId(), json.writeValueAsString(result));
    }
}