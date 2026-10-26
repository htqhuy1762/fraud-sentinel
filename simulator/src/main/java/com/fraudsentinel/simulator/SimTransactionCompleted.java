package com.fraudsentinel.simulator;

/** inlineDecision = null khi không gọi được hệ thống fraud. */
public record SimTransactionCompleted(SimTransaction transaction, String inlineDecision, String status) {}