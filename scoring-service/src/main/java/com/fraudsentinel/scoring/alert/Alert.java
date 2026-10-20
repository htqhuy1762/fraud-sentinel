package com.fraudsentinel.scoring.alert;

import com.fraudsentinel.scoring.domain.Decision;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fraud_alert")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String txnId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private int score;

    @Enumerated(EnumType.STRING)
    private Decision decision;

    private String reasons;

    @Enumerated(EnumType.STRING)
    private AlertStatus status;

    private Instant txnTime;
    private Instant createdAt;

    protected Alert() {}   // JPA bắt buộc có constructor rỗng

    public static Alert from(RuleAlertEvent event) {
        Alert a = new Alert();
        a.txnId = event.transaction().txnId();
        a.accountId = event.transaction().accountId();
        a.amount = event.transaction().amount();
        a.currency = event.transaction().currency();
        a.score = event.result().score();
        a.decision = event.result().decision();
        a.reasons = String.join(",", event.result().reasons());
        a.status = AlertStatus.OPEN;
        a.txnTime = event.transaction().timestamp();
        a.createdAt = event.createdAt();
        return a;
    }

    public Long getId() { return id; }
    public String getTxnId() { return txnId; }
    public String getAccountId() { return accountId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public int getScore() { return score; }
    public Decision getDecision() { return decision; }
    public String getReasons() { return reasons; }
    public AlertStatus getStatus() { return status; }
    public Instant getTxnTime() { return txnTime; }
    public Instant getCreatedAt() { return createdAt; }
}