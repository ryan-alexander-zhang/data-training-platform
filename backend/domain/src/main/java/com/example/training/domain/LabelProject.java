package com.example.training.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class LabelProject {
    private final UUID id;
    private final TenantId tenantId;
    private final DatasetId datasetId;
    private final long labelStudioProjectId;
    private final Instant createdAt;

    public LabelProject(UUID id,
                        TenantId tenantId,
                        DatasetId datasetId,
                        long labelStudioProjectId,
                        Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        this.datasetId = Objects.requireNonNull(datasetId, "datasetId is required");
        this.labelStudioProjectId = labelStudioProjectId;
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

    public long labelStudioProjectId() {
        return labelStudioProjectId;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
