package com.example.training.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.training.domain.DatasetFile;
import com.example.training.domain.DatasetFileRepository;
import com.example.training.domain.DatasetId;
import com.example.training.domain.TenantId;
import com.example.training.infra.entity.DatasetFileEntity;
import com.example.training.infra.mapper.DatasetFileMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据集文件仓储实现。
 */
public class DatasetFileRepositoryImpl implements DatasetFileRepository {
    private final DatasetFileMapper mapper;

    public DatasetFileRepositoryImpl(DatasetFileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public DatasetFile save(DatasetFile file) {
        DatasetFileEntity entity = toEntity(file);
        if (mapper.selectById(entity.getId()) == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return file;
    }

    @Override
    public List<DatasetFile> findByDataset(TenantId tenantId, DatasetId datasetId) {
        QueryWrapper<DatasetFileEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId.value())
                .eq("dataset_id", datasetId.value())
                .orderByDesc("uploaded_at");
        return mapper.selectList(wrapper).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByDataset(TenantId tenantId, DatasetId datasetId) {
        QueryWrapper<DatasetFileEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId.value())
                .eq("dataset_id", datasetId.value());
        return mapper.selectCount(wrapper);
    }

    private DatasetFileEntity toEntity(DatasetFile file) {
        DatasetFileEntity entity = new DatasetFileEntity();
        entity.setId(file.id());
        entity.setTenantId(file.tenantId().value());
        entity.setDatasetId(file.datasetId().value());
        entity.setFilename(file.filename());
        entity.setObjectKey(file.objectKey());
        entity.setSize(file.size());
        entity.setContentType(file.contentType());
        entity.setUploadedAt(file.uploadedAt());
        return entity;
    }

    private DatasetFile toDomain(DatasetFileEntity entity) {
        return new DatasetFile(
                entity.getId(),
                DatasetId.of(entity.getDatasetId()),
                TenantId.of(entity.getTenantId()),
                entity.getFilename(),
                entity.getObjectKey(),
                entity.getSize(),
                entity.getContentType(),
                entity.getUploadedAt()
        );
    }
}
