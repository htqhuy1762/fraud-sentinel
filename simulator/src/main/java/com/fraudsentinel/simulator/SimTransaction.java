package com.fraudsentinel.simulator;

import java.math.BigDecimal;
import java.time.Instant;

public record SimTransaction(
        String txnId,
        String accountId,
        BigDecimal amount,
        String currency,
        String channel,
        String receiverAccount,
        String deviceId,
        String ip,
        Instant timestamp
) {}