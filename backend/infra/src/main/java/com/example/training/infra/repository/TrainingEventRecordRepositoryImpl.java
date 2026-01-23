package com.example.training.infra.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.training.domain.DatasetId;
import com.example.training.domain.TenantId;
import com.example.training.domain.TrainingEventRecord;
import com.example.training.domain.TrainingEventRecordRepository;
import com.example.training.domain.TrainingEventType;
import com.example.training.infra.entity.TrainingEventEntity;
import com.example.training.infra.mapper.TrainingEventMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 训练事件记录仓储实现。
 */
public class TrainingEventRecordRepositoryImpl implements TrainingEventRecordRepository {
    private final TrainingEventMapper mapper;

    public TrainingEventRecordRepositoryImpl(TrainingEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public TrainingEventRecord save(TrainingEventRecord record) {
        TrainingEventEntity entity = toEntity(record);
        if (mapper.selectById(entity.getId()) == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return record;
    }

    @Override
    public List<TrainingEventRecord> findByTenant(TenantId tenantId) {
        QueryWrapper<TrainingEventEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("tenant_id", tenantId.value())
                .orderByDesc("occurred_at");
        return mapper.selectList(wrapper).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private TrainingEventEntity toEntity(TrainingEventRecord record) {
        TrainingEventEntity entity = new TrainingEventEntity();
        entity.setId(record.id());
        entity.setTenantId(record.tenantId().value());
        entity.setDatasetId(record.datasetId().value());
        entity.setDatasetName(record.datasetName());
        entity.setEventType(record.type().name());
        entity.setOccurredAt(record.occurredAt());
        return entity;
    }

    private TrainingEventRecord toDomain(TrainingEventEntity entity) {
        return new TrainingEventRecord(
                entity.getId(),
                TenantId.of(entity.getTenantId()),
                DatasetId.of(entity.getDatasetId()),
                entity.getDatasetName(),
                TrainingEventType.valueOf(entity.getEventType()),
                entity.getOccurredAt()
        );
    }
}
