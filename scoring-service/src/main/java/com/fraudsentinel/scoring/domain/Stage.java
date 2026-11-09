package com.fraudsentinel.scoring.domain;

public enum Stage {
    /** Chạy trên đường duyệt giao dịch: phải nhanh. */
    INLINE,
    /** Chạy sau khi giao dịch hoàn tất: được phép nặng hơn, nhìn rộng hơn. */
    MONITORING
}