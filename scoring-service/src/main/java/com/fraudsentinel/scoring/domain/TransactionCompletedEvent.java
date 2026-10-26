package com.fraudsentinel.scoring.domain;

/**
 * Sự kiện core phát ra SAU KHI đã thực hiện hoặc từ chối giao dịch.
 * inlineDecision = null nghĩa là giao dịch không được chấm điểm inline
 * (fraud quá tải, timeout, sập → core áp dụng chính sách dự phòng).
 */
public record TransactionCompletedEvent(Transaction transaction, Decision inlineDecision, TxnStatus status) {}