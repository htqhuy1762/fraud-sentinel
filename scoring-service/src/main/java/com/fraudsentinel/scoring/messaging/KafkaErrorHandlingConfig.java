package com.fraudsentinel.scoring.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.core.JacksonException;

@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);

        // Lỗi tạm thời (DB chập chờn, timeout...): thử lại 2 lần, cách nhau 1 giây, rồi mới đẩy sang DLT
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2));

        // Lỗi "vĩnh viễn": retry bao nhiêu lần cũng vậy → đẩy thẳng sang DLT
        handler.addNotRetryableExceptions(JacksonException.class, InvalidMessageException.class);
        return handler;
    }
}