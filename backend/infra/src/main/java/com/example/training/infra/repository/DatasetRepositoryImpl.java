package com.example.training.infra.repository;

import com.example.training.domain.Dataset;
import com.example.training.domain.DatasetId;
import com.example.training.domain.DatasetRepository;
import com.example.training.domain.DatasetStatus;
import com.example.training.domain.TenantId;
import com.example.training.infra.entity.DatasetEntity;
import com.example.training.infra.mapper.DatasetMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        QueryWrapper<DatasetEntity> existing = new QueryWrapper<>();
        existing.eq("id", entity.getId());
        if (mapper.selectOne(existing) == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return dataset;
    }

    @Override
    public Optional<Dataset> findById(TenantId tenantId, DatasetId datasetId) {
        QueryWrapper<DatasetEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("id", datasetId.value())
                .eq("tenant_id", tenantId.value());
        DatasetEntity entity = mapper.selectOne(wrapper);
        if (entity == null || !entity.getTenantId().equals(tenantId.value())) {
            return Optional.empty();
        }
        return Optional.of(toDomain(entity));
    }

    @Override
    public List<Dataset> findByTenant(TenantId tenantId) {
        QueryWrapper<DatasetEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId.value())
                .orderByDesc("updated_at");
        return mapper.selectList(wrapper).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
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
