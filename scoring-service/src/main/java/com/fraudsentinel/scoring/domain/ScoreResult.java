package com.fraudsentinel.scoring.domain;

import java.util.List;

public record ScoreResult(
        String txnId,
        int score,
        Decision decision,
        List<String> reasons
) {}