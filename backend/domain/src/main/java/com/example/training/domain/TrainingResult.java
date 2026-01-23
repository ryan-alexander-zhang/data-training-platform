package com.example.training.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 训练结果。
 */
public class TrainingResult {
    private final UUID id;
    private final TenantId tenantId;
    private final DatasetId datasetId;
    private final String artifactKey;
    private final String metricsKey;
    private final Instant createdAt;

    public TrainingResult(UUID id,
                          TenantId tenantId,
                          DatasetId datasetId,
                          String artifactKey,
                          String metricsKey,
                          Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        this.datasetId = Objects.requireNonNull(datasetId, "datasetId is required");
        this.artifactKey = Objects.requireNonNull(artifactKey, "artifactKey is required");
        this.metricsKey = Objects.requireNonNull(metricsKey, "metricsKey is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
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

    public String artifactKey() {
        return artifactKey;
    }

    public String metricsKey() {
        return metricsKey;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
