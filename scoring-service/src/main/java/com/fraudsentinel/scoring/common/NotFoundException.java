package com.fraudsentinel.scoring.common;

/** Tài nguyên được yêu cầu không tồn tại → HTTP 404. */
public abstract class NotFoundException extends RuntimeException {
    protected NotFoundException(String message) {
        super(message);
    }
}
