package com.example.training.domain;

/**
 * 训练事件发布器。
 * <p>
 * 基于 Kafka 或其他消息中间件实现。
 */
public interface TrainingEventPublisher {
    void publish(TrainingEvent event);
}
