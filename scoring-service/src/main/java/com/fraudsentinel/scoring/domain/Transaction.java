package com.fraudsentinel.scoring.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

public record Transaction(
        @NotBlank String txnId,
        @NotBlank String accountId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency,
        String channel,
        String receiverAccount,
        String deviceId,
        String ip,
        @NotNull Instant timestamp
) {}