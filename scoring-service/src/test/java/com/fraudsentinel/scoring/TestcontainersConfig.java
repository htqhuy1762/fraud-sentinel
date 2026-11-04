package com.fraudsentinel.scoring;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.TimeZone;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    static {
        // Test không đi qua main(), nên phải đặt lại múi giờ ở đây
        // (giống ScoringServiceApplication.main). Khối này chạy khi class được nạp,
        // tức là trước khi Flyway và Hikari mở kết nối tới Postgres.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:16");
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafka() {
        return new KafkaContainer("apache/kafka:4.1.0");
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redis() {
        return new GenericContainer<>("redis:7").withExposedPorts(6379);
    }
}