package com.fraudsentinel.scoring.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import tools.jackson.core.JacksonException;

@Configuration
public class KafkaErrorHandlingConfig {

    /**
     * Lỗi tạm thời: thử lại với khoảng chờ tăng dần (mặc định 0,5s → 1s → 2s → 4s, tổng tối đa ~10s),
     * hết thời gian thì đẩy sang <topic>-dlt.
     * Retry ngắn là có chủ đích: trong lúc retry, cả partition đứng chờ. Sự cố kéo dài (DB sập lâu)
     * nên để message sang DLT, nơi nó được lưu bền và replay được, thay vì chặn mọi message phía sau.
     */
    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> template,
                                          @Value("${app.kafka.retry.initial-interval-ms}") long initialIntervalMs,
                                          @Value("${app.kafka.retry.multiplier}") double multiplier,
                                          @Value("${app.kafka.retry.max-elapsed-ms}") long maxElapsedMs) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);

        ExponentialBackOff backOff = new ExponentialBackOff(initialIntervalMs, multiplier);
        backOff.setMaxElapsedTime(maxElapsedMs);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // Lỗi "vĩnh viễn": retry bao nhiêu lần cũng vậy → đẩy thẳng sang DLT
        handler.addNotRetryableExceptions(JacksonException.class, InvalidMessageException.class);
        return handler;
    }
}
