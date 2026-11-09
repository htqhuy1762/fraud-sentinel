package com.fraudsentinel.scoring.common;

/** Yêu cầu hợp lệ nhưng xung đột với trạng thái hiện tại của tài nguyên → HTTP 409. */
public abstract class ConflictException extends RuntimeException {
    protected ConflictException(String message) {
        super(message);
    }
}
