package com.example.training.domain;

import java.time.Instant;
import java.util.Map;

/**
 * 训练事件。
 * <p>
 * 通过 Kafka 发布，训练平台订阅后执行异步训练。
 */
public record TrainingEvent(
        TrainingEventType type,
        TenantId tenantId,
        DatasetId datasetId,
        Instant occurredAt,
        Map<String, Object> payload
) {
}
