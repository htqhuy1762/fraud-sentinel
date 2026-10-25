package com.fraudsentinel.scoring.rule.impl;

import com.fraudsentinel.scoring.domain.Transaction;
import com.fraudsentinel.scoring.rule.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighAmountRule implements Rule<HighAmountRule.Params> {

    public record Params(BigDecimal threshold) {
        public Params {
            if (threshold == null || threshold.signum() <= 0) {
                throw new IllegalArgumentException("threshold must be > 0");
            }
        }
    }

    @Override public String code() { return "AMT_HIGH"; }
    @Override public SubjectType subjectType() { return SubjectType.CUSTOMER; }
    @Override public Class<Params> paramsType() { return Params.class; }

    @Override
    public RuleResult evaluate(Transaction txn, Params p, int score) {
        if (txn.amount().compareTo(p.threshold()) > 0) {
            return RuleResult.hit(code(), score,
                    "Amount %s exceeds %s".formatted(txn.amount(), p.threshold()));
        }
        return RuleResult.pass(code());
    }
}