package com.fraudsentinel.scoring.rule.impl;

import com.fraudsentinel.scoring.domain.Transaction;
import com.fraudsentinel.scoring.feature.VelocityStore;
import com.fraudsentinel.scoring.rule.*;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class VelocityRule implements Rule<VelocityRule.Params> {

    public record Params(int windowSeconds, int maxCount) {
        public Params {
            if (windowSeconds <= 0 || maxCount <= 0) {
                throw new IllegalArgumentException("windowSeconds and maxCount must be > 0");
            }
        }
    }

    private final VelocityStore store;

    public VelocityRule(VelocityStore store) {
        this.store = store;
    }

    @Override public String code() { return "VEL_BURST"; }
    @Override public SubjectType subjectType() { return SubjectType.CUSTOMER; }
    @Override public Class<Params> paramsType() { return Params.class; }

    @Override
    public RuleResult evaluate(Transaction txn, Params p, int score) {
        String key = "vel:acc:" + txn.accountId();
        long count = store.recordAndCount(key, txn.txnId(), txn.timestamp(), Duration.ofSeconds(p.windowSeconds()));
        if (count > p.maxCount()) {
            return RuleResult.hit(code(), score,
                    "%d transactions in last %ds (max %d)".formatted(count, p.windowSeconds(), p.maxCount()));
        }
        return RuleResult.pass(code());
    }
}