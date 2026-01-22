package com.example.training.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * 数据集聚合根。
 * <p>
 * - 负责维护数据集生命周期状态。
 * - 保障标注完成后才触发训练请求。
 */
public class Dataset {
    private final DatasetId id;
    private final TenantId tenantId;
    private String name;
    private DatasetStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public Dataset(DatasetId id, TenantId tenantId, String name) {
        this.id = Objects.requireNonNull(id, "datasetId is required");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        this.name = Objects.requireNonNull(name, "name is required");
        this.status = DatasetStatus.CREATED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public DatasetId id() {
        return id;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public String name() {
        return name;
    }

    public DatasetStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public void markUploading() {
        this.status = DatasetStatus.UPLOADING;
        this.updatedAt = Instant.now();
    }

    public void markReadyForLabeling() {
        this.status = DatasetStatus.READY_FOR_LABELING;
        this.updatedAt = Instant.now();
    }

    public void markAnnotationCompleted() {
        this.status = DatasetStatus.ANNOTATION_COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void markTrainingRequested() {
        if (status != DatasetStatus.ANNOTATION_COMPLETED) {
            throw new IllegalStateException("dataset must be annotated before training");
        }
        this.status = DatasetStatus.TRAINING_REQUESTED;
        this.updatedAt = Instant.now();
    }

    public void markTrainingCompleted() {
        this.status = DatasetStatus.TRAINING_COMPLETED;
        this.updatedAt = Instant.now();
    }

    /**
     * 持久化恢复场景使用的状态回放，避免破坏领域规则。
     */
    public void restoreStatus(DatasetStatus status, Instant updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }
}
