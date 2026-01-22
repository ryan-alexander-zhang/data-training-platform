package com.example.training.app;

import com.example.training.domain.Dataset;
import com.example.training.domain.DatasetId;
import com.example.training.domain.DatasetRepository;
import com.example.training.domain.TenantId;
import com.example.training.domain.TrainingEvent;
import com.example.training.domain.TrainingEventPublisher;
import com.example.training.domain.TrainingEventType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 数据集应用服务。
 * <p>
 * 负责：
 * 1. 创建数据集
 * 2. 标注完成后触发训练事件
 */
public class DatasetApplicationService {
    private final DatasetRepository repository;
    private final TrainingEventPublisher eventPublisher;

    public DatasetApplicationService(DatasetRepository repository, TrainingEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 创建数据集，并返回生成的 UUIDv7。
     */
    public Dataset createDataset(UUID tenantId, String name) {
        Dataset dataset = new Dataset(DatasetId.of(UUIDv7Generator.generate()), TenantId.of(tenantId), name);
        return repository.save(dataset);
    }

    /**
     * 标注完成后触发训练请求事件。
     */
    public void completeAnnotation(UUID tenantId, UUID datasetId) {
        Dataset dataset = repository.findById(TenantId.of(tenantId), DatasetId.of(datasetId))
                .orElseThrow(() -> new IllegalArgumentException("dataset not found"));
        dataset.markAnnotationCompleted();
        dataset.markTrainingRequested();
        repository.save(dataset);

        TrainingEvent event = new TrainingEvent(
                TrainingEventType.TRAINING_REQUESTED,
                dataset.tenantId(),
                dataset.id(),
                Instant.now(),
                Map.of("datasetName", dataset.name())
        );
        eventPublisher.publish(event);
    }
}
