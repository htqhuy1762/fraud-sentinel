package com.fraudsentinel.scoring.rule;

public class StaleRuleConfigException extends RuntimeException {
    public StaleRuleConfigException(String code, long expected, long actual) {
        super("Rule " + code + " was changed by someone else (you have version " + expected
                + ", current is " + actual + "). Reload and try again.");
    }
}