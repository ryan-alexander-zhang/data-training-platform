package com.example.training.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.training.domain.DatasetId;
import com.example.training.domain.TenantId;
import com.example.training.domain.TrainingResult;
import com.example.training.domain.TrainingResultRepository;
import com.example.training.infra.entity.TrainingResultEntity;
import com.example.training.infra.mapper.TrainingResultMapper;

import java.util.Optional;

/**
 * 训练结果仓储实现。
 */
public class TrainingResultRepositoryImpl implements TrainingResultRepository {
    private final TrainingResultMapper mapper;

    public TrainingResultRepositoryImpl(TrainingResultMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public TrainingResult save(TrainingResult result) {
        TrainingResultEntity entity = toEntity(result);
        if (mapper.selectById(entity.getId()) == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return result;
    }

    @Override
    public Optional<TrainingResult> findByDataset(TenantId tenantId, DatasetId datasetId) {
        QueryWrapper<TrainingResultEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId.value())
                .eq("dataset_id", datasetId.value());
        TrainingResultEntity entity = mapper.selectOne(wrapper);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(entity));
    }

    private TrainingResultEntity toEntity(TrainingResult result) {
        TrainingResultEntity entity = new TrainingResultEntity();
        entity.setId(result.id());
        entity.setTenantId(result.tenantId().value());
        entity.setDatasetId(result.datasetId().value());
        entity.setArtifactKey(result.artifactKey());
        entity.setMetricsKey(result.metricsKey());
        entity.setCreatedAt(result.createdAt());
        return entity;
    }

    private TrainingResult toDomain(TrainingResultEntity entity) {
        return new TrainingResult(
                entity.getId(),
                TenantId.of(entity.getTenantId()),
                DatasetId.of(entity.getDatasetId()),
                entity.getArtifactKey(),
                entity.getMetricsKey(),
                entity.getCreatedAt()
        );
    }
}
