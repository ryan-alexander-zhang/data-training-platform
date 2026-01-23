package com.example.training.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 训练事件记录，用于展示与审计。
 */
public class TrainingEventRecord {
    private final UUID id;
    private final TenantId tenantId;
    private final DatasetId datasetId;
    private final String datasetName;
    private final TrainingEventType type;
    private final Instant occurredAt;

    public TrainingEventRecord(UUID id,
                               TenantId tenantId,
                               DatasetId datasetId,
                               String datasetName,
                               TrainingEventType type,
                               Instant occurredAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        this.datasetId = Objects.requireNonNull(datasetId, "datasetId is required");
        this.datasetName = Objects.requireNonNull(datasetName, "datasetName is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt is required");
    }

    public UUID id() {
        return id;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public DatasetId datasetId() {
        return datasetId;
    }

    public String datasetName() {
        return datasetName;
    }

    public TrainingEventType type() {
        return type;
    }

    public Instant occurredAt() {
        return occurredAt;
    }
}
