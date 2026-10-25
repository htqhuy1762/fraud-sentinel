package com.fraudsentinel.scoring.rule;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "rule_config_audit")
public class RuleConfigAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String oldValue;
    private String newValue;
    private String changedBy;
    private Instant changedAt;

    protected RuleConfigAudit() {}

    public RuleConfigAudit(String code, String oldValue, String newValue, String changedBy, Instant changedAt) {
        this.code = code;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }
}