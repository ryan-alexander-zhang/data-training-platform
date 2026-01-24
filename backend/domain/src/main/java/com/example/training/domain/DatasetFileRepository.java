package com.example.training.domain;

import java.util.List;

/**
 * 数据集文件仓储。
 */
public interface DatasetFileRepository {
    DatasetFile save(DatasetFile file);

    List<DatasetFile> findByDataset(TenantId tenantId, DatasetId datasetId);

    long countByDataset(TenantId tenantId, DatasetId datasetId);
}
