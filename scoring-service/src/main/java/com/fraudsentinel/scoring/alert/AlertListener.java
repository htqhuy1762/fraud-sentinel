package com.fraudsentinel.scoring.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AlertListener {

    private static final Logger log = LoggerFactory.getLogger(AlertListener.class);

    private final AlertRepository repository;

    public AlertListener(AlertRepository repository) {
        this.repository = repository;
    }

    @Async
    @EventListener
    public void onAlert(RuleAlertEvent event) {
        String txnId = event.transaction().txnId();

        if (repository.existsByTxnId(txnId)) {
            log.info("Alert for txn={} already exists, skip", txnId);
            return;
        }
        try {
            Alert saved = repository.save(Alert.from(event));
            log.warn("ALERT saved id={} txn={} decision={} reasons={}",
                    saved.getId(), txnId, saved.getDecision(), saved.getReasons());
        } catch (DataIntegrityViolationException e) {
            // 2 request cùng txnId đến gần như đồng thời: cả 2 qua được existsByTxnId,
            // nhưng UNIQUE constraint chặn bản thứ 2 → coi như đã có, bỏ qua.
            log.info("Duplicate alert for txn={} (race), skip", txnId);
        }
    }
}