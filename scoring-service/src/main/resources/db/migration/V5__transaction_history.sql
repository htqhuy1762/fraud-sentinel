CREATE TABLE transaction_history (
                                     txn_id           VARCHAR(64)    PRIMARY KEY,
                                     account_id       VARCHAR(64)    NOT NULL,
                                     receiver_account VARCHAR(64),
                                     amount           NUMERIC(19, 2) NOT NULL,
                                     currency         VARCHAR(3)     NOT NULL,
                                     channel          VARCHAR(16),
                                     device_id        VARCHAR(64),
                                     ip               VARCHAR(45),
                                     txn_time         TIMESTAMPTZ    NOT NULL,
                                     inline_decision  VARCHAR(16),               -- NULL = không được chấm inline
                                     status           VARCHAR(16)    NOT NULL,   -- EXECUTED / DECLINED
                                     recorded_at      TIMESTAMPTZ    NOT NULL
);

-- Lịch sử theo tài khoản gửi, mới nhất trước (phục vụ API và keyset pagination)
CREATE INDEX idx_txn_hist_account_time  ON transaction_history (account_id, txn_time DESC, txn_id DESC);
-- Theo tài khoản nhận: chuẩn bị cho phát hiện mule account ở Phase 2
CREATE INDEX idx_txn_hist_receiver_time ON transaction_history (receiver_account, txn_time DESC);
-- Theo thiết bị: chuẩn bị cho "nhiều tài khoản dùng chung một thiết bị"
CREATE INDEX idx_txn_hist_device_time   ON transaction_history (device_id, txn_time DESC);