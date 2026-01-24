package com.example.training.app;

import com.example.training.domain.Dataset;
import com.example.training.domain.DatasetFile;
import com.example.training.domain.DatasetFileRepository;
import com.example.training.domain.DatasetId;
import com.example.training.domain.DatasetRepository;
import com.example.training.domain.ObjectStorageService;
import com.example.training.domain.TenantId;
import com.example.training.domain.TrainingEvent;
import com.example.training.domain.TrainingEventRecord;
import com.example.training.domain.TrainingEventRecordRepository;
import com.example.training.domain.TrainingEventPublisher;
import com.example.training.domain.TrainingEventType;
import com.example.training.domain.TrainingResult;
import com.example.training.domain.TrainingResultRepository;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 数据集应用服务。
 * <p>
 * 负责：
 * 1. 创建数据集
 * 2. 上传文件与状态流转
 * 3. 标注完成后触发训练事件
 */
public class DatasetApplicationService {
    private final DatasetRepository repository;
    private final TrainingEventPublisher eventPublisher;
    private final DatasetFileRepository fileRepository;
    private final TrainingEventRecordRepository eventRecordRepository;
    private final TrainingResultRepository resultRepository;
    private final ObjectStorageService storageService;

    public DatasetApplicationService(DatasetRepository repository,
                                     TrainingEventPublisher eventPublisher,
                                     DatasetFileRepository fileRepository,
                                     TrainingEventRecordRepository eventRecordRepository,
                                     TrainingResultRepository resultRepository,
                                     ObjectStorageService storageService) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.fileRepository = fileRepository;
        this.eventRecordRepository = eventRecordRepository;
        this.resultRepository = resultRepository;
        this.storageService = storageService;
    }

    /**
     * 创建数据集，并返回生成的 UUIDv7。
     */
    public Dataset createDataset(UUID tenantId, String name) {
        Dataset dataset = new Dataset(DatasetId.of(UUIDv7Generator.generate()), TenantId.of(tenantId), name);
        return repository.save(dataset);
    }

    public List<Dataset> listDatasets(UUID tenantId) {
        return repository.findByTenant(TenantId.of(tenantId));
    }

    public Dataset getDataset(UUID tenantId, UUID datasetId) {
        return repository.findById(TenantId.of(tenantId), DatasetId.of(datasetId))
                .orElseThrow(() -> new IllegalArgumentException("dataset not found"));
    }

    public List<DatasetFile> listFiles(UUID tenantId, UUID datasetId) {
        return fileRepository.findByDataset(TenantId.of(tenantId), DatasetId.of(datasetId));
    }

    public long countFiles(UUID tenantId, UUID datasetId) {
        return fileRepository.countByDataset(TenantId.of(tenantId), DatasetId.of(datasetId));
    }

    public DatasetFile uploadFile(UUID tenantId,
                                  UUID datasetId,
                                  String filename,
                                  String contentType,
                                  long size,
                                  InputStream inputStream) {
        Dataset dataset = getDataset(tenantId, datasetId);
        if (dataset.status() == com.example.training.domain.DatasetStatus.CREATED) {
            dataset.markUploading();
            repository.save(dataset);
        }

        UUID fileId = UUIDv7Generator.generate();
        String objectKey = "datasets/" + tenantId + "/" + datasetId + "/" + fileId + "-" + filename;
        storageService.upload(objectKey, inputStream, size, contentType);

        DatasetFile file = new DatasetFile(
                fileId,
                DatasetId.of(datasetId),
                TenantId.of(tenantId),
                filename,
                objectKey,
                size,
                contentType,
                Instant.now()
        );
        return fileRepository.save(file);
    }

    public void completeUpload(UUID tenantId, UUID datasetId) {
        Dataset dataset = getDataset(tenantId, datasetId);
        dataset.markReadyForLabeling();
        repository.save(dataset);
    }

    /**
     * 标注完成后触发训练请求事件。
     */
    public void completeAnnotation(UUID tenantId, UUID datasetId) {
        Dataset dataset = getDataset(tenantId, datasetId);
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
        eventRecordRepository.save(new TrainingEventRecord(
                UUIDv7Generator.generate(),
                dataset.tenantId(),
                dataset.id(),
                dataset.name(),
                event.type(),
                event.occurredAt()
        ));
        eventPublisher.publish(event);
    }

    public List<TrainingEventRecord> listTrainingEvents(UUID tenantId) {
        return eventRecordRepository.findByTenant(TenantId.of(tenantId));
    }

    public TrainingResult getTrainingResult(UUID tenantId, UUID datasetId) {
        return resultRepository.findByDataset(TenantId.of(tenantId), DatasetId.of(datasetId))
                .orElseThrow(() -> new IllegalArgumentException("training result not found"));
    }
}
