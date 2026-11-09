package com.fraudsentinel.scoring.deadletter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Listener đọc topic DLT KHÔNG được dùng error handler chung: error handler chung sẽ đẩy
 * message lỗi sang "<topic>-dlt", tức "transactions.completed-dlt-dlt", một topic không ai đọc.
 * Ở đây là chốt chặn cuối cùng, nên khi lỗi (ví dụ Postgres sập) chỉ có một lựa chọn: thử lại
 * mãi mãi, chờ lâu dần tới tối đa 1 phút. Partition DLT bị dừng trong lúc đó, nhưng không mất gì.
 */
@Configuration
class DeadLetterListenerConfig {

    static final String FACTORY = "deadLetterListenerFactory";

    @Bean(FACTORY)
    ConcurrentKafkaListenerContainerFactory<String, String> deadLetterListenerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ExponentialBackOff backOff = new ExponentialBackOff(1_000L, 2.0);
        backOff.setMaxInterval(60_000L);
        backOff.setMaxElapsedTime(Long.MAX_VALUE);   // không bao giờ bỏ cuộc

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(new DefaultErrorHandler(backOff));   // không có recoverer → không có DLT của DLT
        return factory;
    }
}
