package com.fraudsentinel.scoring.rule;

public record RuleConfig(String code, boolean enabled, int score, Object params, long version) {}