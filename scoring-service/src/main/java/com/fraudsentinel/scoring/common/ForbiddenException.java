package com.fraudsentinel.scoring.common;

/** Người gọi không có quyền thực hiện thao tác này trên tài nguyên → HTTP 403. */
public abstract class ForbiddenException extends RuntimeException {
    protected ForbiddenException(String message) {
        super(message);
    }
}
