package com.fraudsentinel.scoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Nguồn thời gian duy nhất của ứng dụng. Code nghiệp vụ dùng Instant.now(clock)
 * thay vì Instant.now(), để test có thể thay bằng Clock.fixed(...) và kiểm tra
 * chính xác các logic phụ thuộc thời gian (cửa sổ 24 giờ, cut-off...).
 */
@Configuration
public class ClockConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
