package com.example.training.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.training.domain.UploadPart;
import com.example.training.domain.UploadPartRepository;
import com.example.training.infra.entity.UploadPartEntity;
import com.example.training.infra.mapper.UploadPartMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class UploadPartRepositoryImpl implements UploadPartRepository {
    private final UploadPartMapper mapper;

    public UploadPartRepositoryImpl(UploadPartMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UploadPart save(UploadPart part) {
        UploadPartEntity entity = toEntity(part);
        QueryWrapper<UploadPartEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", entity.getSessionId())
                .eq("part_number", entity.getPartNumber());
        UploadPartEntity existing = mapper.selectOne(wrapper);
        if (existing != null) {
            return toDomain(existing);
        }
        mapper.insert(entity);
        return part;
    }

    @Override
    public Optional<UploadPart> findBySessionAndPartNumber(UUID sessionId, int partNumber) {
        QueryWrapper<UploadPartEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId)
                .eq("part_number", partNumber);
        return Optional.ofNullable(mapper.selectOne(wrapper)).map(this::toDomain);
    }

    @Override
    public List<UploadPart> findBySession(UUID sessionId) {
        QueryWrapper<UploadPartEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId)
                .orderByAsc("part_number");
        return mapper.selectList(wrapper).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private UploadPartEntity toEntity(UploadPart part) {
        UploadPartEntity entity = new UploadPartEntity();
        entity.setId(part.id());
        entity.setSessionId(part.sessionId());
        entity.setPartNumber(part.partNumber());
        entity.setEtag(part.etag());
        entity.setSize(part.size());
        entity.setCreatedAt(part.createdAt());
        return entity;
    }

    private UploadPart toDomain(UploadPartEntity entity) {
        return new UploadPart(
                entity.getId(),
                entity.getSessionId(),
                entity.getPartNumber(),
                entity.getEtag(),
                entity.getSize(),
                entity.getCreatedAt()
        );
    }
}
