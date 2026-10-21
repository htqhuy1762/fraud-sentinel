package com.fraudsentinel.simulator;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "simulator")
public record SimulatorProperties(String topic, int accounts, int tps, double fraudRate) {}