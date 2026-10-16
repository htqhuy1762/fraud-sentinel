package com.fraudsentinel.scoring.rule;

public record RuleResult(String code, boolean hit, int score, String reason) {

    public static RuleResult hit(String code, int score, String reason) {
        return new RuleResult(code, true, score, reason);
    }

    public static RuleResult pass(String code) {
        return new RuleResult(code, false, 0, null);
    }
}