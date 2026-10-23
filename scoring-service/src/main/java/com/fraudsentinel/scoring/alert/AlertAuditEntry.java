package com.fraudsentinel.scoring.alert;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "fraud_alert_audit")
public class AlertAuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long alertId;
    private String action;

    @Enumerated(EnumType.STRING)
    private AlertStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private AlertStatus toStatus;

    private String actor;
    private String note;
    private Instant createdAt;

    protected AlertAuditEntry() {}

    public static AlertAuditEntry of(Alert alert, String action, AlertStatus from,
                                     String actor, String note, Instant at) {
        AlertAuditEntry e = new AlertAuditEntry();
        e.alertId = alert.getId();
        e.action = action;
        e.fromStatus = from;
        e.toStatus = alert.getStatus();
        e.actor = actor;
        e.note = note;
        e.createdAt = at;
        return e;
    }

    public Long getId() { return id; }
    public Long getAlertId() { return alertId; }
    public String getAction() { return action; }
    public AlertStatus getFromStatus() { return fromStatus; }
    public AlertStatus getToStatus() { return toStatus; }
    public String getActor() { return actor; }
    public String getNote() { return note; }
    public Instant getCreatedAt() { return createdAt; }
}