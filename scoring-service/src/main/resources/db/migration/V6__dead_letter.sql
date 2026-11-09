-- Message không xử lý được (đã vào topic *-dlt) được ghi lại đây để không bị Kafka xóa sau
-- thời gian lưu giữ, để người vận hành xem, gửi lại (replay) hoặc bỏ qua (discard) có kiểm soát.
CREATE TABLE dead_letter (
    id                 BIGSERIAL     PRIMARY KEY,
    -- Vị trí của message trong topic DLT: dùng để không ghi trùng khi bị đọc lại
    dlt_topic          VARCHAR(128)  NOT NULL,
    dlt_partition      INT           NOT NULL,
    dlt_offset         BIGINT        NOT NULL,
    -- Vị trí gốc, lấy từ header Spring Kafka gắn vào khi đẩy sang DLT
    original_topic     VARCHAR(128),
    original_partition INT,
    original_offset    BIGINT,
    message_key        VARCHAR(256),
    payload            TEXT,
    exception_class    VARCHAR(512),
    exception_message  TEXT,
    status             VARCHAR(16)   NOT NULL,   -- NEW / REPLAYED / DISCARDED
    received_at        TIMESTAMPTZ   NOT NULL,
    resolved_by        VARCHAR(64),
    resolved_at        TIMESTAMPTZ,
    resolution_note    VARCHAR(1000),
    version            BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT uq_dead_letter_position UNIQUE (dlt_topic, dlt_partition, dlt_offset)
);

CREATE INDEX idx_dead_letter_status ON dead_letter (status, received_at DESC);
