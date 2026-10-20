package com.fraudsentinel.scoring.alert;

import com.fraudsentinel.scoring.domain.Decision;

import java.math.BigDecimal;
import java.time.Instant;

public record AlertView(Long id, String txnId, String accountId, BigDecimal amount, String currency,
                        int score, Decision decision, String reasons, AlertStatus status,
                        Instant txnTime, Instant createdAt) {

    static AlertView of(Alert a) {
        return new AlertView(a.getId(), a.getTxnId(), a.getAccountId(), a.getAmount(), a.getCurrency(),
                a.getScore(), a.getDecision(), a.getReasons(), a.getStatus(),
                a.getTxnTime(), a.getCreatedAt());
    }
}