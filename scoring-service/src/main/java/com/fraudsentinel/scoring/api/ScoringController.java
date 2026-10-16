package com.fraudsentinel.scoring.api;

import com.fraudsentinel.scoring.domain.ScoreResult;
import com.fraudsentinel.scoring.domain.Transaction;
import com.fraudsentinel.scoring.scoring.ScoringService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ScoringController {

    private final ScoringService scoringService;

    public ScoringController(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    @PostMapping("/score")
    public ScoreResult score(@Valid @RequestBody Transaction txn) {
        return scoringService.score(txn);
    }
}