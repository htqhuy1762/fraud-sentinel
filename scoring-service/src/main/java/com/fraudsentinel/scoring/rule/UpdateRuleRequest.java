package com.fraudsentinel.scoring.rule;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public record UpdateRuleRequest(
        @NotNull Boolean enabled,
        @NotNull @Min(0) @Max(100) Integer score,
        @NotNull JsonNode params,
        @NotNull Long version     // version client đang thấy, để chống ghi đè
) {}