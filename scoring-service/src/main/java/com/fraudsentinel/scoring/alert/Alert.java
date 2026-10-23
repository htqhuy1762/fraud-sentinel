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

    @Version
    private Long version;

    private String assignee;
    private Instant assignedAt;
    private Instant resolvedAt;
    private String resolutionNote;

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

    // ─── Workflow ───────────────────────────────────────────────

    public void assign(String analyst, Instant now) {
        requireStatus(AlertStatus.OPEN, "assign");
        this.assignee = analyst;
        this.assignedAt = now;
        this.status = AlertStatus.IN_REVIEW;
    }

    public void release(String analyst) {
        requireStatus(AlertStatus.IN_REVIEW, "release");
        requireAssignee(analyst);
        this.assignee = null;
        this.assignedAt = null;
        this.status = AlertStatus.OPEN;
    }

    public void resolve(String analyst, AlertStatus outcome, String note, Instant now) {
        if (outcome != AlertStatus.CONFIRMED_FRAUD && outcome != AlertStatus.FALSE_POSITIVE) {
            throw new IllegalArgumentException("Outcome must be CONFIRMED_FRAUD or FALSE_POSITIVE");
        }
        requireStatus(AlertStatus.IN_REVIEW, "resolve");
        requireAssignee(analyst);
        this.status = outcome;
        this.resolvedAt = now;
        this.resolutionNote = note;
    }

    private void requireStatus(AlertStatus expected, String action) {
        if (this.status != expected) {
            throw new AlertStateException("Cannot " + action + " alert " + id + " in status " + status);
        }
    }

    private void requireAssignee(String analyst) {
        if (!analyst.equals(this.assignee)) {
            throw new NotAssigneeException(analyst, assignee);
        }
    }

    public Long getVersion() { return version; }
    public String getAssignee() { return assignee; }
    public Instant getAssignedAt() { return assignedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public String getResolutionNote() { return resolutionNote; }
}