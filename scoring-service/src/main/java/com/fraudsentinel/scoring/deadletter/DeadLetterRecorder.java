package com.fraudsentinel.scoring.deadletter;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Đọc mọi topic DLT và ghi lại để không mất khi Kafka xóa dữ liệu theo thời gian lưu giữ. */
@Component
class DeadLetterRecorder {

    private final DeadLetterService service;

    DeadLetterRecorder(DeadLetterService service) {
        this.service = service;
    }

    @KafkaListener(
            topics = {"${app.topics.transactions}-dlt", "${app.topics.alerts}-dlt"},
            groupId = "dead-letter-recorder",
            containerFactory = DeadLetterListenerConfig.FACTORY)
    void onDeadLetter(ConsumerRecord<String, String> record) {
        service.record(record);
    }
}
