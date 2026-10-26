ALTER TABLE fraud_alert
    ADD COLUMN stage       VARCHAR(16) NOT NULL DEFAULT 'INLINE',
    ADD COLUMN funds_moved BOOLEAN;

-- Cùng một giao dịch giờ có thể có 1 alert INLINE và 1 alert MONITORING
ALTER TABLE fraud_alert DROP CONSTRAINT fraud_alert_txn_id_key;
ALTER TABLE fraud_alert ADD CONSTRAINT uq_fraud_alert_txn_stage UNIQUE (txn_id, stage);