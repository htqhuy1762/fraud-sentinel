package com.fraudsentinel.scoring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "fraud")
public record FraudProperties(Rules rules, DecisionThresholds decision) {

    public record Rules(HighAmount highAmount, NightTime nightTime) {}

    public record HighAmount(boolean enabled, BigDecimal threshold, int score) {}

    public record NightTime(boolean enabled, int startHour, int endHour, String zone, int score) {}

    public record DecisionThresholds(int reviewThreshold, int blockThreshold) {}
}