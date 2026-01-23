package com.example.training.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 数据集文件。
 */
public class DatasetFile {
    private final UUID id;
    private final DatasetId datasetId;
    private final TenantId tenantId;
    private final String filename;
    private final String objectKey;
    private final long size;
    private final String contentType;
    private final Instant uploadedAt;

    public DatasetFile(UUID id,
                       DatasetId datasetId,
                       TenantId tenantId,
                       String filename,
                       String objectKey,
                       long size,
                       String contentType,
                       Instant uploadedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.datasetId = Objects.requireNonNull(datasetId, "datasetId is required");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        this.filename = Objects.requireNonNull(filename, "filename is required");
        this.objectKey = Objects.requireNonNull(objectKey, "objectKey is required");
        this.size = size;
        this.contentType = contentType;
        this.uploadedAt = Objects.requireNonNull(uploadedAt, "uploadedAt is required");
    }

    public UUID id() {
        return id;
    }

    public DatasetId datasetId() {
        return datasetId;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public String filename() {
        return filename;
    }

    public String objectKey() {
        return objectKey;
    }

    public long size() {
        return size;
    }

    public String contentType() {
        return contentType;
    }

    public Instant uploadedAt() {
        return uploadedAt;
    }
}
