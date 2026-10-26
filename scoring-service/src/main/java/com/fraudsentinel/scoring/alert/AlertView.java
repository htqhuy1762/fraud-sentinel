package com.fraudsentinel.scoring.alert;

import com.fraudsentinel.scoring.domain.Decision;
import com.fraudsentinel.scoring.rule.Stage;

import java.math.BigDecimal;
import java.time.Instant;

public record AlertView(Long id, String txnId, String accountId, BigDecimal amount, String currency,
                        int score, Decision decision, String reasons, AlertStatus status,
                        String assignee, Instant assignedAt, Instant resolvedAt, String resolutionNote,
                        Instant txnTime, Instant createdAt, Stage stage, Boolean fundsMoved) {

    static AlertView of(Alert a) {
        return new AlertView(a.getId(), a.getTxnId(), a.getAccountId(), a.getAmount(), a.getCurrency(),
                a.getScore(), a.getDecision(), a.getReasons(), a.getStatus(),
                a.getAssignee(), a.getAssignedAt(), a.getResolvedAt(), a.getResolutionNote(),
                a.getTxnTime(), a.getCreatedAt(), a.getStage(), a.getFundsMoved());
    }
}