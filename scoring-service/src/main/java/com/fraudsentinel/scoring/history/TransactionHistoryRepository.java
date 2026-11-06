package com.fraudsentinel.scoring.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface TransactionHistoryRepository extends JpaRepository<TransactionRecord, String> {

    /** @return 1 nếu đã ghi, 0 nếu giao dịch đã tồn tại (xử lý trùng) */
    @Modifying
    @Query(value = """
            INSERT INTO transaction_history
                (txn_id, account_id, receiver_account, amount, currency, channel, device_id, ip,
                 txn_time, inline_decision, status, recorded_at)
            VALUES
                (:txnId, :accountId, :receiverAccount, :amount, :currency, :channel, :deviceId, :ip,
                 :txnTime, :inlineDecision, :status, :recordedAt)
            ON CONFLICT (txn_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("txnId") String txnId,
                       @Param("accountId") String accountId,
                       @Param("receiverAccount") String receiverAccount,
                       @Param("amount") BigDecimal amount,
                       @Param("currency") String currency,
                       @Param("channel") String channel,
                       @Param("deviceId") String deviceId,
                       @Param("ip") String ip,
                       @Param("txnTime") Instant txnTime,
                       @Param("inlineDecision") String inlineDecision,
                       @Param("status") String status,
                       @Param("recordedAt") Instant recordedAt);

    /** Trang đầu: mới nhất trước. */
    @Query(value = """
            SELECT * FROM transaction_history
            WHERE account_id = :accountId
            ORDER BY txn_time DESC, txn_id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TransactionRecord> firstPage(@Param("accountId") String accountId, @Param("limit") int limit);

    /** Các trang sau: cũ hơn con trỏ (txn_time, txn_id) của dòng cuối trang trước. */
    @Query(value = """
            SELECT * FROM transaction_history
            WHERE account_id = :accountId
              AND (txn_time, txn_id) < (:beforeTime, :beforeTxnId)
            ORDER BY txn_time DESC, txn_id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TransactionRecord> nextPage(@Param("accountId") String accountId,
                                     @Param("beforeTime") Instant beforeTime,
                                     @Param("beforeTxnId") String beforeTxnId,
                                     @Param("limit") int limit);

    /** Các giao dịch của tài khoản trong một khoảng thời gian (ngữ cảnh quanh một alert). */
    List<TransactionRecord> findByAccountIdAndTxnTimeBetweenOrderByTxnTimeAsc(
            String accountId, Instant from, Instant to);
}