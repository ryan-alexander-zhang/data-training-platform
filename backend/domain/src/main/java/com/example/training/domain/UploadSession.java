package com.example.training.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class UploadSession {
    private final UUID id;
    private final TenantId tenantId;
    private final DatasetId datasetId;
    private final String filename;
    private final String objectKey;
    private final String contentType;
    private final String uploadId;
    private final long partSize;
    private final long totalSize;
    private final String status;
    private final Instant createdAt;
    private final Instant completedAt;

    public UploadSession(UUID id,
                         TenantId tenantId,
                         DatasetId datasetId,
                         String filename,
                         String objectKey,
                         String contentType,
                         String uploadId,
                         long partSize,
                         long totalSize,
                         String status,
                         Instant createdAt,
                         Instant completedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        this.datasetId = Objects.requireNonNull(datasetId, "datasetId is required");
        this.filename = Objects.requireNonNull(filename, "filename is required");
        this.objectKey = Objects.requireNonNull(objectKey, "objectKey is required");
        this.contentType = Objects.requireNonNull(contentType, "contentType is required");
        this.uploadId = Objects.requireNonNull(uploadId, "uploadId is required");
        this.partSize = partSize;
        this.totalSize = totalSize;
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.completedAt = completedAt;
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

    public String filename() {
        return filename;
    }

    public String objectKey() {
        return objectKey;
    }

    public String contentType() {
        return contentType;
    }

    public String uploadId() {
        return uploadId;
    }

    public long partSize() {
        return partSize;
    }

    public long totalSize() {
        return totalSize;
    }

    public String status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant completedAt() {
        return completedAt;
    }
}
