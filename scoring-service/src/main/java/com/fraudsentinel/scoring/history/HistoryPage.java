package com.fraudsentinel.scoring.history;

import java.time.Instant;
import java.util.List;

/** next = null nghĩa là đã hết dữ liệu. */
public record HistoryPage(List<TransactionView> items, Cursor next) {

    public record Cursor(Instant beforeTime, String beforeTxnId) {}
}