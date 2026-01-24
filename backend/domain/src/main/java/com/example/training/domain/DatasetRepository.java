package com.example.training.domain;

import java.util.Optional;
import java.util.List;

/**
 * 数据集仓储接口。
 * <p>
 * 由 infra 层实现，提供持久化能力。
 */
public interface DatasetRepository {
    Dataset save(Dataset dataset);

    Optional<Dataset> findById(TenantId tenantId, DatasetId datasetId);

    List<Dataset> findByTenant(TenantId tenantId);
}
