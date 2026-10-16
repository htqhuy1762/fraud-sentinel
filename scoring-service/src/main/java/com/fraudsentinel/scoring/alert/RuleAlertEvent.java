package com.fraudsentinel.scoring.alert;

import com.fraudsentinel.scoring.domain.ScoreResult;
import com.fraudsentinel.scoring.domain.Transaction;

import java.time.Instant;

public record RuleAlertEvent(Transaction transaction, ScoreResult result, Instant createdAt) {}