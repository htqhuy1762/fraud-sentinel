package com.fraudsentinel.scoring.rule.impl;

import com.fraudsentinel.scoring.config.FraudProperties;
import com.fraudsentinel.scoring.domain.Transaction;
import com.fraudsentinel.scoring.feature.VelocityStore;
import com.fraudsentinel.scoring.rule.*;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class VelocityRule implements Rule {

    private final FraudProperties.Velocity cfg;
    private final VelocityStore store;
    private final Duration window;

    public VelocityRule(FraudProperties props, VelocityStore store) {
        this.cfg = props.rules().velocity();
        this.store = store;
        this.window = Duration.ofSeconds(cfg.windowSeconds());
    }

    @Override public String code() { return "VEL_BURST"; }
    @Override public SubjectType subjectType() { return SubjectType.CUSTOMER; }
    @Override public boolean enabled() { return cfg.enabled(); }

    @Override
    public RuleResult evaluate(Transaction txn) {
        String key = "vel:acc:" + txn.accountId();
        long count = store.recordAndCount(key, txn.txnId(), txn.timestamp(), window);

        if (count > cfg.maxCount()) {
            return RuleResult.hit(code(), cfg.score(),
                    "%d transactions in last %ds (max %d)".formatted(count, cfg.windowSeconds(), cfg.maxCount()));
        }
        return RuleResult.pass(code());
    }
}