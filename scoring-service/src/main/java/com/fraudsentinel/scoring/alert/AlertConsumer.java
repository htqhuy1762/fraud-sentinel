package com.fraudsentinel.scoring.alert;

import com.fraudsentinel.scoring.domain.RuleAlertEvent;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AlertConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertConsumer.class);

    /** Ràng buộc UNIQUE (txn_id, stage) tạo ở migration V4. */
    static final String UNIQUE_TXN_STAGE = "uq_fraud_alert_txn_stage";
    /** Mã SQLSTATE chuẩn cho vi phạm unique: dùng khi driver không trả về tên ràng buộc. */
    private static final String UNIQUE_VIOLATION_SQLSTATE = "23505";

    private final AlertRepository repository;
    private final JsonMapper json;
    private final AlertCaseService caseService;

    public AlertConsumer(AlertRepository repository, JsonMapper json, AlertCaseService caseService) {
        this.repository = repository;
        this.json = json;
        this.caseService = caseService;
    }

    @KafkaListener(topics = "${app.topics.alerts}", groupId = "alert-writer")
    public void onMessage(String payload) {
        RuleAlertEvent event = json.readValue(payload, RuleAlertEvent.class);
        String txnId = event.transaction().txnId();

        if (repository.existsByTxnIdAndStage(txnId, event.stage())) {
            log.info("Alert for txn={} stage={} already exists, skip", txnId, event.stage());
            return;
        }
        try {
            Alert saved = caseService.open(event);
            log.info("ALERT saved id={} txn={} stage={} fundsMoved={} decision={} reasons={}",
                    saved.getId(), txnId, saved.getStage(), saved.getFundsMoved(),
                    saved.getDecision(), saved.getReasons());
        } catch (DataIntegrityViolationException e) {
            if (!isDuplicateAlert(e)) {
                // Vi phạm ràng buộc KHÁC (cột quá dài, NULL...): không phải trùng, không được bỏ qua.
                // Ném lại để error handler retry rồi đẩy sang rule-alerts-dlt, alert không bị mất âm thầm.
                throw e;
            }
            // Hai lần xử lý cùng alert chạy song song: cả hai qua được existsBy..., ràng buộc chặn bản thứ hai
            log.info("Duplicate alert for txn={} stage={} (race), skip", txnId, event.stage());
        }
    }

    /** Chỉ coi là "trùng" khi đúng là vi phạm ràng buộc UNIQUE (txn_id, stage). */
    static boolean isDuplicateAlert(DataIntegrityViolationException e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof ConstraintViolationException cve) {
                String constraint = cve.getConstraintName();
                if (constraint != null) {
                    return UNIQUE_TXN_STAGE.equalsIgnoreCase(constraint);
                }
                return UNIQUE_VIOLATION_SQLSTATE.equals(cve.getSQLState());
            }
        }
        return false;
    }
}
