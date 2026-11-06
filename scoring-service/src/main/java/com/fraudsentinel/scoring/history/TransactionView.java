package com.fraudsentinel.scoring.history;

import com.fraudsentinel.scoring.domain.Decision;
import com.fraudsentinel.scoring.domain.TxnStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionView(String txnId, String accountId, String receiverAccount, BigDecimal amount,
                              String currency, String channel, String deviceId, String ip, Instant txnTime,
                              Decision inlineDecision, TxnStatus status) {

    static TransactionView of(TransactionRecord r) {
        return new TransactionView(r.getTxnId(), r.getAccountId(), r.getReceiverAccount(), r.getAmount(),
                r.getCurrency(), r.getChannel(), r.getDeviceId(), r.getIp(), r.getTxnTime(),
                r.getInlineDecision(), r.getStatus());
    }
}