package com.fraudsentinel.scoring.rule;

import tools.jackson.databind.JsonNode;

import java.time.Instant;

public record RuleConfigView(String code, boolean enabled, int score, JsonNode params,
                             long version, String updatedBy, Instant updatedAt) {}