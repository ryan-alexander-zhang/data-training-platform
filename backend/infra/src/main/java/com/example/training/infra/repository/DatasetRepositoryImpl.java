package com.example.training.infra.repository;

import com.example.training.domain.Dataset;
import com.example.training.domain.DatasetId;
import com.example.training.domain.DatasetRepository;
import com.example.training.domain.DatasetStatus;
import com.example.training.domain.TenantId;
import com.example.training.infra.entity.DatasetEntity;
import com.example.training.infra.mapper.DatasetMapper;

import java.util.Optional;

/**
 * 数据集仓储实现。
 */
public class DatasetRepositoryImpl implements DatasetRepository {
    private final DatasetMapper mapper;

    public DatasetRepositoryImpl(DatasetMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Dataset save(Dataset dataset) {
        DatasetEntity entity = toEntity(dataset);
        if (mapper.selectById(entity.getId()) == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return dataset;
    }

    @Override
    public Optional<Dataset> findById(TenantId tenantId, DatasetId datasetId) {
        DatasetEntity entity = mapper.selectById(datasetId.value());
        if (entity == null || !entity.getTenantId().equals(tenantId.value())) {
            return Optional.empty();
        }
        return Optional.of(toDomain(entity));
    }

    private DatasetEntity toEntity(Dataset dataset) {
        DatasetEntity entity = new DatasetEntity();
        entity.setId(dataset.id().value());
        entity.setTenantId(dataset.tenantId().value());
        entity.setName(dataset.name());
        entity.setStatus(dataset.status().name());
        entity.setCreatedAt(dataset.createdAt());
        entity.setUpdatedAt(dataset.updatedAt());
        return entity;
    }

    private Dataset toDomain(DatasetEntity entity) {
        Dataset dataset = new Dataset(
                DatasetId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                entity.getName()
        );
        DatasetStatus status = DatasetStatus.valueOf(entity.getStatus());
        dataset.restoreStatus(status, entity.getUpdatedAt());
        return dataset;
    }
}
