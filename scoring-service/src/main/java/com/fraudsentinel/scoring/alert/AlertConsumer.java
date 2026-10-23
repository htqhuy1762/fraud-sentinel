package com.fraudsentinel.scoring.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AlertConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertConsumer.class);

    private final AlertRepository repository;
    private final JsonMapper json;
    private final AlertCaseService caseService;

    public AlertConsumer(AlertRepository repository, JsonMapper json,  AlertCaseService caseService) {
        this.repository = repository;
        this.json = json;
        this.caseService = caseService;
    }

    @KafkaListener(topics = "${app.topics.alerts}", groupId = "alert-writer")
    public void onMessage(String payload) {
        RuleAlertEvent event = json.readValue(payload, RuleAlertEvent.class);
        String txnId = event.transaction().txnId();

        if (repository.existsByTxnId(txnId)) {
            log.info("Alert for txn={} already exists, skip", txnId);
            return;
        }
        try {
            Alert saved = caseService.open(event);
            log.warn("ALERT saved id={} txn={} decision={} reasons={}",
                    saved.getId(), txnId, saved.getDecision(), saved.getReasons());
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate alert for txn={} (race), skip", txnId);
        }
    }
}