package com.fraudsentinel.scoring.rule.impl;

import com.fraudsentinel.scoring.config.FraudProperties;
import com.fraudsentinel.scoring.domain.Transaction;
import com.fraudsentinel.scoring.rule.*;
import org.springframework.stereotype.Component;

@Component
public class HighAmountRule implements Rule {

    private final FraudProperties.HighAmount cfg;

    public HighAmountRule(FraudProperties props) {
        this.cfg = props.rules().highAmount();
    }

    @Override public String code() { return "AMT_HIGH"; }
    @Override public SubjectType subjectType() { return SubjectType.CUSTOMER; }
    @Override public boolean enabled() { return cfg.enabled(); }

    @Override
    public RuleResult evaluate(Transaction txn) {
        if (txn.amount().compareTo(cfg.threshold()) > 0) {
            return RuleResult.hit(code(), cfg.score(),
                    "Amount %s exceeds %s".formatted(txn.amount(), cfg.threshold()));
        }
        return RuleResult.pass(code());
    }
}