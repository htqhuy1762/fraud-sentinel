CREATE TABLE fraud_alert (
                             id          BIGSERIAL      PRIMARY KEY,
                             txn_id      VARCHAR(64)    NOT NULL UNIQUE,
                             account_id  VARCHAR(64)    NOT NULL,
                             amount      NUMERIC(19, 2) NOT NULL,
                             currency    VARCHAR(3)     NOT NULL,
                             score       INT            NOT NULL,
                             decision    VARCHAR(16)    NOT NULL,
                             reasons     VARCHAR(512)   NOT NULL,
                             status      VARCHAR(16)    NOT NULL,
                             txn_time    TIMESTAMPTZ    NOT NULL,
                             created_at  TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_fraud_alert_status_created ON fraud_alert (status, created_at DESC);
CREATE INDEX idx_fraud_alert_account        ON fraud_alert (account_id);