package com.fraudsentinel.scoring.api;

import com.fraudsentinel.scoring.domain.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ScoringController {

    private static final BigDecimal HIGH_AMOUNT = new BigDecimal("50000000"); // 50 triệu

    @PostMapping("/score")
    public ScoreResult score(@Valid @RequestBody Transaction txn) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (txn.amount().compareTo(HIGH_AMOUNT) > 0) {
            score += 60;
            reasons.add("AMT_HIGH");
        }

        Decision decision = score >= 80 ? Decision.BLOCK
                : score >= 50 ? Decision.REVIEW
                : Decision.APPROVE;

        return new ScoreResult(txn.txnId(), score, decision, reasons);
    }
}