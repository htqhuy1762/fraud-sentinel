package com.fraudsentinel.scoring.rule;

import com.fraudsentinel.scoring.domain.Transaction;

import java.util.Set;

public interface Rule<P> {
    String code();
    SubjectType subjectType();
    Class<P> paramsType();
    RuleResult evaluate(Transaction txn, P params, int score);

    /** Mặc định rule chạy inline. Rule nặng sẽ override thành MONITORING. */
    default Set<Stage> stages() {
        return Set.of(Stage.INLINE);
    }
}