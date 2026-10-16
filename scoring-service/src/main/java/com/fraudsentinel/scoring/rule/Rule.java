package com.fraudsentinel.scoring.rule;

import com.fraudsentinel.scoring.domain.Transaction;

public interface Rule {
    String code();
    SubjectType subjectType();
    boolean enabled();
    RuleResult evaluate(Transaction txn);
}