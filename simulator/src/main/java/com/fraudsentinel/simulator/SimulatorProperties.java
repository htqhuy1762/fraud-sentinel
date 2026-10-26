package com.fraudsentinel.simulator;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.net.URI;

@ConfigurationProperties(prefix = "simulator")
public record SimulatorProperties(String topic, URI scoringUrl, BigDecimal fallbackMaxAmount,
                                  int accounts, int tps, double fraudRate) {}