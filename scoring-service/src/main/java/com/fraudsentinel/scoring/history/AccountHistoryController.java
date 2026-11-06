package com.fraudsentinel.scoring.history;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountHistoryController {

    private final TransactionHistoryService history;

    public AccountHistoryController(TransactionHistoryService history) {
        this.history = history;
    }

    @GetMapping("/{accountId}/transactions")
    public HistoryPage transactions(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant beforeTime,
            @RequestParam(required = false) String beforeTxnId) {
        if ((beforeTime == null) != (beforeTxnId == null)) {
            throw new IllegalArgumentException("beforeTime and beforeTxnId must be given together");
        }
        return history.page(accountId, limit, beforeTime, beforeTxnId);
    }
}