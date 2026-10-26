package com.fraudsentinel.scoring.alert;

import com.fraudsentinel.scoring.domain.ScoreResult;
import com.fraudsentinel.scoring.domain.Transaction;
import com.fraudsentinel.scoring.rule.Stage;

import java.time.Instant;

/**
 * @param fundsMoved true = tiền đã đi, false = giao dịch bị từ chối,
 *                   null = chưa biết (alert INLINE được tạo trước khi core thực hiện)
 */
public record RuleAlertEvent(Transaction transaction, ScoreResult result, Stage stage,
                             Boolean fundsMoved, Instant createdAt) {}