package com.fraudsentinel.scoring.rule;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "rule_config")
public class RuleConfigEntity {

    @Id
    private String code;
    private boolean enabled;
    private int score;
    private String params;

    @Version
    private Long version;

    private String updatedBy;
    private Instant updatedAt;

    protected RuleConfigEntity() {}

    public void update(boolean enabled, int score, String params, String actor, Instant now) {
        this.enabled = enabled;
        this.score = score;
        this.params = params;
        this.updatedBy = actor;
        this.updatedAt = now;
    }

    public String getCode() { return code; }
    public boolean isEnabled() { return enabled; }
    public int getScore() { return score; }
    public String getParams() { return params; }
    public Long getVersion() { return version; }
    public String getUpdatedBy() { return updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }
}