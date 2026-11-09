package com.fraudsentinel.scoring.deadletter;

import java.time.Instant;

public record DeadLetterView(Long id, String dltTopic, String originalTopic, Integer originalPartition,
                             Long originalOffset, String messageKey, String payload,
                             String exceptionClass, String exceptionMessage, DeadLetterStatus status,
                             Instant receivedAt, String resolvedBy, Instant resolvedAt, String resolutionNote) {

    static DeadLetterView of(DeadLetter d) {
        return new DeadLetterView(d.getId(), d.getDltTopic(), d.getOriginalTopic(), d.getOriginalPartition(),
                d.getOriginalOffset(), d.getMessageKey(), d.getPayload(),
                d.getExceptionClass(), d.getExceptionMessage(), d.getStatus(),
                d.getReceivedAt(), d.getResolvedBy(), d.getResolvedAt(), d.getResolutionNote());
    }
}
