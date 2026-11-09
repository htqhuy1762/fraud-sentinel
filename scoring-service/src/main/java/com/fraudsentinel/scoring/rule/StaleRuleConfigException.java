package com.fraudsentinel.scoring.rule;

import com.fraudsentinel.scoring.common.ConflictException;

public class StaleRuleConfigException extends ConflictException {
    public StaleRuleConfigException(String code, long expected, long actual) {
        super("Rule " + code + " was changed by someone else (you have version " + expected
                + ", current is " + actual + "). Reload and try again.");
    }
}