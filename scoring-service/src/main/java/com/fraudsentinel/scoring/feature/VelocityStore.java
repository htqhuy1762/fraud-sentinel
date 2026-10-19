package com.fraudsentinel.scoring.feature;

import java.time.Duration;
import java.time.Instant;

public interface VelocityStore {

    /**
     * Ghi nhận 1 sự kiện vào cửa sổ trượt và trả về số sự kiện trong cửa sổ (bao gồm sự kiện này).
     */
    long recordAndCount(String key, String eventId, Instant eventTime, Duration window);
}