package com.example.training.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.training.domain.DatasetId;
import com.example.training.domain.TenantId;
import com.example.training.domain.UploadSession;
import com.example.training.domain.UploadSessionRepository;
import com.example.training.infra.entity.UploadSessionEntity;
import com.example.training.infra.mapper.UploadSessionMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class UploadSessionRepositoryImpl implements UploadSessionRepository {
    private final UploadSessionMapper mapper;

    public UploadSessionRepositoryImpl(UploadSessionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UploadSession save(UploadSession session) {
        UploadSessionEntity entity = toEntity(session);
        if (mapper.selectById(entity.getId()) == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return session;
    }

    @Override
    public Optional<UploadSession> findByUploadId(TenantId tenantId, UUID datasetId, String uploadId) {
        QueryWrapper<UploadSessionEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId.value())
                .eq("dataset_id", datasetId)
                .eq("upload_id", uploadId);
        return Optional.ofNullable(mapper.selectOne(wrapper)).map(this::toDomain);
    }

    @Override
    public List<UploadSession> findByDataset(TenantId tenantId, DatasetId datasetId) {
        QueryWrapper<UploadSessionEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId.value())
                .eq("dataset_id", datasetId.value())
                .orderByDesc("created_at");
        return mapper.selectList(wrapper).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private UploadSessionEntity toEntity(UploadSession session) {
        UploadSessionEntity entity = new UploadSessionEntity();
        entity.setId(session.id());
        entity.setTenantId(session.tenantId().value());
        entity.setDatasetId(session.datasetId().value());
        entity.setFilename(session.filename());
        entity.setObjectKey(session.objectKey());
        entity.setContentType(session.contentType());
        entity.setUploadId(session.uploadId());
        entity.setPartSize(session.partSize());
        entity.setTotalSize(session.totalSize());
        entity.setStatus(session.status());
        entity.setCreatedAt(session.createdAt());
        entity.setCompletedAt(session.completedAt());
        return entity;
    }

    private UploadSession toDomain(UploadSessionEntity entity) {
        return new UploadSession(
                entity.getId(),
                TenantId.of(entity.getTenantId()),
                DatasetId.of(entity.getDatasetId()),
                entity.getFilename(),
                entity.getObjectKey(),
                entity.getContentType(),
                entity.getUploadId(),
                entity.getPartSize(),
                entity.getTotalSize(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getCompletedAt()
        );
    }
}
