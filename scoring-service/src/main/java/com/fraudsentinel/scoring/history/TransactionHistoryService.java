package com.fraudsentinel.scoring.history;

import com.fraudsentinel.scoring.domain.Transaction;
import com.fraudsentinel.scoring.domain.TransactionCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class TransactionHistoryService {

    private static final Logger log = LoggerFactory.getLogger(TransactionHistoryService.class);
    static final int MAX_PAGE_SIZE = 200;

    private final TransactionHistoryRepository repository;
    private final Clock clock;

    public TransactionHistoryService(TransactionHistoryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public void record(TransactionCompletedEvent event) {
        Transaction t = event.transaction();
        int inserted = repository.insertIfAbsent(
                t.txnId(), t.accountId(), t.receiverAccount(), t.amount(), t.currency(), t.channel(),
                t.deviceId(), t.ip(), t.timestamp(),
                event.inlineDecision() == null ? null : event.inlineDecision().name(),
                event.status().name(),
                Instant.now(clock));
        if (inserted == 0) {
            log.info("Transaction {} already recorded, skip", t.txnId());
        }
    }

    @Transactional(readOnly = true)
    public HistoryPage page(String accountId, int limit, Instant beforeTime, String beforeTxnId) {
        int size = Math.clamp(limit, 1, MAX_PAGE_SIZE);
        // Lấy dư 1 dòng: nếu có dòng thứ size+1 thì còn trang sau, khỏi phải COUNT(*)
        List<TransactionRecord> rows = (beforeTime == null)
                ? repository.firstPage(accountId, size + 1)
                : repository.nextPage(accountId, beforeTime, beforeTxnId, size + 1);

        boolean hasMore = rows.size() > size;
        List<TransactionRecord> pageRows = hasMore ? rows.subList(0, size) : rows;
        HistoryPage.Cursor next = null;
        if (hasMore) {
            TransactionRecord last = pageRows.get(pageRows.size() - 1);
            next = new HistoryPage.Cursor(last.getTxnTime(), last.getTxnId());
        }
        return new HistoryPage(pageRows.stream().map(TransactionView::of).toList(), next);
    }

    @Transactional(readOnly = true)
    public List<TransactionView> around(String accountId, Instant at, Duration window) {
        return repository.findByAccountIdAndTxnTimeBetweenOrderByTxnTimeAsc(
                        accountId, at.minus(window), at.plus(window))
                .stream().map(TransactionView::of).toList();
    }
}