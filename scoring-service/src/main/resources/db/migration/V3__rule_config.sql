CREATE TABLE rule_config (
                             code        VARCHAR(32)  PRIMARY KEY,
                             enabled     BOOLEAN      NOT NULL,
                             score       INT          NOT NULL CHECK (score BETWEEN 0 AND 100),
                             params      TEXT         NOT NULL,
                             version     BIGINT       NOT NULL DEFAULT 0,
                             updated_by  VARCHAR(64)  NOT NULL,
                             updated_at  TIMESTAMPTZ  NOT NULL
);

CREATE TABLE rule_config_audit (
                                   id          BIGSERIAL    PRIMARY KEY,
                                   code        VARCHAR(32)  NOT NULL REFERENCES rule_config (code),
                                   old_value   TEXT,
                                   new_value   TEXT         NOT NULL,
                                   changed_by  VARCHAR(64)  NOT NULL,
                                   changed_at  TIMESTAMPTZ  NOT NULL
);

-- Giá trị ban đầu = đúng những gì đang có trong application.yaml
INSERT INTO rule_config (code, enabled, score, params, updated_by, updated_at) VALUES
                                                                                   ('AMT_HIGH',   true, 60, '{"threshold": 50000000}',                                  'migration', now()),
                                                                                   ('TIME_NIGHT', true, 20, '{"startHour": 1, "endHour": 4, "zone": "Asia/Ho_Chi_Minh"}', 'migration', now()),
                                                                                   ('VEL_BURST',  true, 50, '{"windowSeconds": 60, "maxCount": 5}',                      'migration', now());