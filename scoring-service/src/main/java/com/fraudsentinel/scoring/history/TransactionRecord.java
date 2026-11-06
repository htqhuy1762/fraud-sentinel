package com.fraudsentinel.scoring.history;

import com.fraudsentinel.scoring.domain.Decision;
import com.fraudsentinel.scoring.domain.TxnStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transaction_history")
public class TransactionRecord {

    @Id
    private String txnId;
    private String accountId;
    private String receiverAccount;
    private BigDecimal amount;
    private String currency;
    private String channel;
    private String deviceId;
    private String ip;
    private Instant txnTime;

    @Enumerated(EnumType.STRING)
    private Decision inlineDecision;

    @Enumerated(EnumType.STRING)
    private TxnStatus status;

    private Instant recordedAt;

    protected TransactionRecord() {}

    public String getTxnId() { return txnId; }
    public String getAccountId() { return accountId; }
    public String getReceiverAccount() { return receiverAccount; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getChannel() { return channel; }
    public String getDeviceId() { return deviceId; }
    public String getIp() { return ip; }
    public Instant getTxnTime() { return txnTime; }
    public Decision getInlineDecision() { return inlineDecision; }
    public TxnStatus getStatus() { return status; }
    public Instant getRecordedAt() { return recordedAt; }
}