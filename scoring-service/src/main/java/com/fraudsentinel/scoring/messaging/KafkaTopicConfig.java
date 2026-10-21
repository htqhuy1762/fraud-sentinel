package com.fraudsentinel.scoring.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic transactionsTopic(@Value("${app.topics.transactions}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic decisionsTopic(@Value("${app.topics.decisions}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic alertsTopic(@Value("${app.topics.alerts}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic transactionsDlt(@Value("${app.topics.transactions}") String name) {
        return TopicBuilder.name(name + "-dlt").partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic alertsDlt(@Value("${app.topics.alerts}") String name) {
        return TopicBuilder.name(name + "-dlt").partitions(3).replicas(1).build();
    }
}