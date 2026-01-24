package com.example.training.app;

import com.example.training.domain.Dataset;
import com.example.training.domain.DatasetId;
import com.example.training.domain.DatasetRepository;
import com.example.training.domain.ObjectStorageService;
import com.example.training.domain.TenantId;
import com.example.training.domain.TrainingEvent;
import com.example.training.domain.TrainingEventPublisher;
import com.example.training.domain.TrainingEventRecord;
import com.example.training.domain.TrainingEventRecordRepository;
import com.example.training.domain.TrainingEventType;
import com.example.training.domain.TrainingResult;
import com.example.training.domain.TrainingResultRepository;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 训练流程编排服务（模拟模型处理）。
 */
public class TrainingWorkflowService {
    private final DatasetRepository repository;
    private final TrainingResultRepository resultRepository;
    private final TrainingEventRecordRepository eventRecordRepository;
    private final TrainingEventPublisher eventPublisher;
    private final ObjectStorageService storageService;

    public TrainingWorkflowService(DatasetRepository repository,
                                   TrainingResultRepository resultRepository,
                                   TrainingEventRecordRepository eventRecordRepository,
                                   TrainingEventPublisher eventPublisher,
                                   ObjectStorageService storageService) {
        this.repository = repository;
        this.resultRepository = resultRepository;
        this.eventRecordRepository = eventRecordRepository;
        this.eventPublisher = eventPublisher;
        this.storageService = storageService;
    }

    public void handleTrainingRequested(TrainingEvent event) {
        Dataset dataset = repository.findById(event.tenantId(), event.datasetId())
                .orElseThrow(() -> new IllegalArgumentException("dataset not found"));

        if (resultRepository.findByDataset(dataset.tenantId(), dataset.id()).isPresent()) {
            return;
        }

        String artifactKey = "results/" + dataset.tenantId().value() + "/" + dataset.id().value() + "/model.bin";
        String metricsKey = "results/" + dataset.tenantId().value() + "/" + dataset.id().value() + "/metrics.json";

        byte[] modelBytes = ("mock model for dataset " + dataset.name()).getBytes(StandardCharsets.UTF_8);
        byte[] metricsBytes = ("{\"accuracy\":0.92,\"dataset\":\"" + dataset.name() + "\"}")
                .getBytes(StandardCharsets.UTF_8);

        storageService.upload(artifactKey, new ByteArrayInputStream(modelBytes), modelBytes.length, "application/octet-stream");
        storageService.upload(metricsKey, new ByteArrayInputStream(metricsBytes), metricsBytes.length, "application/json");

        TrainingResult result = new TrainingResult(
                UUIDv7Generator.generate(),
                dataset.tenantId(),
                dataset.id(),
                artifactKey,
                metricsKey,
                Instant.now()
        );
        resultRepository.save(result);

        dataset.markTrainingCompleted();
        repository.save(dataset);

        TrainingEvent completedEvent = new TrainingEvent(
                TrainingEventType.TRAINING_COMPLETED,
                dataset.tenantId(),
                dataset.id(),
                Instant.now(),
                Map.of("datasetName", dataset.name())
        );
        eventRecordRepository.save(new TrainingEventRecord(
                UUIDv7Generator.generate(),
                dataset.tenantId(),
                dataset.id(),
                dataset.name(),
                completedEvent.type(),
                completedEvent.occurredAt()
        ));
        eventPublisher.publish(completedEvent);
    }

    public void handleTrainingCompleted(TrainingEvent event) {
        // 事件幂等处理：如有外部系统回写，可在此同步状态
    }
}
