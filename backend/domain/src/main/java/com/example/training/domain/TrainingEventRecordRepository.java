package com.example.training.domain;

import java.util.List;

/**
 * 训练事件记录仓储。
 */
public interface TrainingEventRecordRepository {
    TrainingEventRecord save(TrainingEventRecord record);

    List<TrainingEventRecord> findByTenant(TenantId tenantId);
}
