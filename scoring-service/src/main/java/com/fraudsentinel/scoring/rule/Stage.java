package com.fraudsentinel.scoring.rule;

public enum Stage {
    /** Chạy trên đường duyệt giao dịch: phải nhanh. */
    INLINE,
    /** Chạy sau khi giao dịch hoàn tất: được phép nặng hơn, nhìn rộng hơn. */
    MONITORING
}