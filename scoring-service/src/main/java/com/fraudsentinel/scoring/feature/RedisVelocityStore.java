package com.fraudsentinel.scoring.feature;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class RedisVelocityStore implements VelocityStore {

    private static final RedisScript<Long> SCRIPT =
            RedisScript.of(new ClassPathResource("scripts/velocity.lua"), Long.class);

    private final StringRedisTemplate redis;

    public RedisVelocityStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public long recordAndCount(String key, String eventId, Instant eventTime, Duration window) {
        long now = eventTime.toEpochMilli();
        long windowStart = now - window.toMillis();
        long ttl = window.toMillis() * 2;   // giữ dư 1 chút so với cửa sổ

        Long count = redis.execute(SCRIPT, List.of(key),
                String.valueOf(now),
                String.valueOf(windowStart),
                eventId,
                String.valueOf(ttl));

        return count == null ? 0 : count;
    }
}