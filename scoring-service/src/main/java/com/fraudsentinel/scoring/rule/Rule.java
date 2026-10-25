package com.fraudsentinel.scoring.rule;

import com.fraudsentinel.scoring.domain.Transaction;

/**
 * @param <P> kiểu params riêng của rule (đọc từ cột rule_config.params)
 */
public interface Rule<P> {
    String code();
    SubjectType subjectType();
    Class<P> paramsType();
    RuleResult evaluate(Transaction txn, P params, int score);
}