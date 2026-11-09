package com.fraudsentinel.scoring.common;

/**
 * Dữ liệu client gửi lên không hợp lệ → HTTP 400.
 * Dùng thay cho IllegalArgumentException, để một IllegalArgumentException
 * do bug trong code không bị trả về như thể lỗi của client.
 */
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}
