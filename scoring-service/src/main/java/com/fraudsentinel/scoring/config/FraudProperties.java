package com.fraudsentinel.scoring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fraud")
public record FraudProperties(DecisionThresholds decision) {

    public record DecisionThresholds(int reviewThreshold, int blockThreshold) {}
}