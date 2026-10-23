package com.fraudsentinel.scoring.alert;

public record RuleStatsView(String rule, long confirmedFraud, long falsePositive, Double precision) {}