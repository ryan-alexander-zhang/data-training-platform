package com.example.training.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.training.domain.DatasetId;
import com.example.training.domain.LabelProject;
import com.example.training.domain.LabelProjectRepository;
import com.example.training.domain.TenantId;
import com.example.training.infra.entity.LabelProjectEntity;
import com.example.training.infra.mapper.LabelProjectMapper;

import java.util.Optional;

public class LabelProjectRepositoryImpl implements LabelProjectRepository {
    private final LabelProjectMapper mapper;

    public LabelProjectRepositoryImpl(LabelProjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public LabelProject save(LabelProject project) {
        LabelProjectEntity entity = toEntity(project);
        if (mapper.selectById(entity.getId()) == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return project;
    }

    @Override
    public Optional<LabelProject> findByDataset(TenantId tenantId, DatasetId datasetId) {
        QueryWrapper<LabelProjectEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId.value())
                .eq("dataset_id", datasetId.value());
        return Optional.ofNullable(mapper.selectOne(wrapper)).map(this::toDomain);
    }

    private LabelProjectEntity toEntity(LabelProject project) {
        LabelProjectEntity entity = new LabelProjectEntity();
        entity.setId(project.id());
        entity.setTenantId(project.tenantId().value());
        entity.setDatasetId(project.datasetId().value());
        entity.setLabelStudioProjectId(project.labelStudioProjectId());
        entity.setCreatedAt(project.createdAt());
        return entity;
    }

    private LabelProject toDomain(LabelProjectEntity entity) {
        return new LabelProject(
                entity.getId(),
                TenantId.of(entity.getTenantId()),
                DatasetId.of(entity.getDatasetId()),
                entity.getLabelStudioProjectId(),
                entity.getCreatedAt()
        );
    }
}
