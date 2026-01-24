package com.example.training.domain;

import java.util.Optional;

/**
 * 训练结果仓储。
 */
public interface TrainingResultRepository {
    TrainingResult save(TrainingResult result);

    Optional<TrainingResult> findByDataset(TenantId tenantId, DatasetId datasetId);
}
