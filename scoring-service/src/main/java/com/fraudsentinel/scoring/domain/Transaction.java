package com.fraudsentinel.scoring.domain;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Ràng buộc ở đây phải khớp với độ dài cột trong DB (fraud_alert, transaction_history).
 * Dữ liệu sai bị chặn ngay ở cửa vào (400 / DLT), thay vì nổ ra ở lúc ghi DB.
 */
public record Transaction(
        @NotBlank @Size(max = 64) String txnId,
        @NotBlank @Size(max = 64) String accountId,
        @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal amount,   // NUMERIC(19, 2)
        @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "must be an ISO 4217 code such as VND") String currency,
        @Size(max = 16) String channel,
        @Size(max = 64) String receiverAccount,
        @Size(max = 64) String deviceId,
        @Size(max = 45) String ip,
        @NotNull Instant timestamp
) {}
