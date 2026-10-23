ALTER TABLE fraud_alert
    ADD COLUMN version         BIGINT        NOT NULL DEFAULT 0,
    ADD COLUMN assignee        VARCHAR(64),
    ADD COLUMN assigned_at     TIMESTAMPTZ,
    ADD COLUMN resolved_at     TIMESTAMPTZ,
    ADD COLUMN resolution_note VARCHAR(1000);

CREATE INDEX idx_fraud_alert_assignee ON fraud_alert (assignee, status);

CREATE TABLE fraud_alert_audit (
                                   id          BIGSERIAL     PRIMARY KEY,
                                   alert_id    BIGINT        NOT NULL REFERENCES fraud_alert (id),
                                   action      VARCHAR(32)   NOT NULL,
                                   from_status VARCHAR(16),
                                   to_status   VARCHAR(16)   NOT NULL,
                                   actor       VARCHAR(64)   NOT NULL,
                                   note        VARCHAR(1000),
                                   created_at  TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_fraud_alert_audit_alert ON fraud_alert_audit (alert_id, created_at);