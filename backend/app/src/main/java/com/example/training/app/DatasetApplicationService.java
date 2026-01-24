package com.example.training.app;

import com.example.training.domain.Dataset;
import com.example.training.domain.DatasetFile;
import com.example.training.domain.DatasetFileRepository;
import com.example.training.domain.DatasetId;
import com.example.training.domain.DatasetRepository;
import com.example.training.domain.LabelProject;
import com.example.training.domain.LabelProjectRepository;
import com.example.training.domain.LabelStudioService;
import com.example.training.domain.LabelStudioTask;
import com.example.training.domain.MultipartUpload;
import com.example.training.domain.MultipartUploadPart;
import com.example.training.domain.MultipartUploadedPart;
import com.example.training.domain.ObjectStorageService;
import com.example.training.domain.TenantId;
import com.example.training.domain.TrainingEvent;
import com.example.training.domain.TrainingEventRecord;
import com.example.training.domain.TrainingEventRecordRepository;
import com.example.training.domain.TrainingEventPublisher;
import com.example.training.domain.TrainingEventType;
import com.example.training.domain.TrainingResult;
import com.example.training.domain.TrainingResultRepository;
import com.example.training.domain.UploadPart;
import com.example.training.domain.UploadPartRepository;
import com.example.training.domain.UploadSession;
import com.example.training.domain.UploadSessionRepository;
import com.example.training.domain.UploadSessionStatus;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * 数据集应用服务。
 * <p>
 * 负责：
 * 1. 创建数据集
 * 2. 上传文件与状态流转
 * 3. 标注完成后触发训练事件
 */
@Slf4j
public class DatasetApplicationService {
    private final DatasetRepository repository;
    private final TrainingEventPublisher eventPublisher;
    private final DatasetFileRepository fileRepository;
    private final TrainingEventRecordRepository eventRecordRepository;
    private final TrainingResultRepository resultRepository;
    private final ObjectStorageService storageService;
    private final UploadSessionRepository uploadSessionRepository;
    private final UploadPartRepository uploadPartRepository;
    private final LabelProjectRepository labelProjectRepository;
    private final LabelStudioService labelStudioService;
    private final String publicBaseUrl;
    private final String labelStudioWebhookBaseUrl;
    private final String labelStudioWebhookToken;

    public DatasetApplicationService(DatasetRepository repository,
                                     TrainingEventPublisher eventPublisher,
                                     DatasetFileRepository fileRepository,
                                     TrainingEventRecordRepository eventRecordRepository,
                                     TrainingResultRepository resultRepository,
                                     ObjectStorageService storageService,
                                     UploadSessionRepository uploadSessionRepository,
                                     UploadPartRepository uploadPartRepository,
                                     LabelProjectRepository labelProjectRepository,
                                     LabelStudioService labelStudioService,
                                     String publicBaseUrl,
                                     String labelStudioWebhookBaseUrl,
                                     String labelStudioWebhookToken) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.fileRepository = fileRepository;
        this.eventRecordRepository = eventRecordRepository;
        this.resultRepository = resultRepository;
        this.storageService = storageService;
        this.uploadSessionRepository = uploadSessionRepository;
        this.uploadPartRepository = uploadPartRepository;
        this.labelProjectRepository = labelProjectRepository;
        this.labelStudioService = labelStudioService;
        this.publicBaseUrl = publicBaseUrl;
        this.labelStudioWebhookBaseUrl = labelStudioWebhookBaseUrl;
        this.labelStudioWebhookToken = labelStudioWebhookToken;
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

    public DatasetFile getFile(UUID tenantId, UUID datasetId, UUID fileId) {
        return fileRepository.findByDataset(TenantId.of(tenantId), DatasetId.of(datasetId)).stream()
                .filter(file -> file.id().equals(fileId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("file not found"));
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
        String objectKey = buildRawObjectKey(tenantId, datasetId, fileId, filename);
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

    public UploadSession createUploadSession(UUID tenantId,
                                             UUID datasetId,
                                             String filename,
                                             String contentType,
                                             long size) {
        Dataset dataset = getDataset(tenantId, datasetId);
        if (dataset.status() == com.example.training.domain.DatasetStatus.CREATED) {
            dataset.markUploading();
            repository.save(dataset);
        }

        UUID sessionId = UUIDv7Generator.generate();
        String objectKey = buildRawObjectKey(tenantId, datasetId, sessionId, filename);
        MultipartUpload upload = storageService.initMultipartUpload(objectKey, contentType);

        UploadSession session = new UploadSession(
                sessionId,
                TenantId.of(tenantId),
                DatasetId.of(datasetId),
                filename,
                objectKey,
                contentType,
                upload.uploadId(),
                upload.partSize(),
                size,
                UploadSessionStatus.IN_PROGRESS.name(),
                Instant.now(),
                null
        );
        return uploadSessionRepository.save(session);
    }

    public UploadSession getUploadSession(UUID tenantId, UUID datasetId, String uploadId) {
        return uploadSessionRepository.findByUploadId(TenantId.of(tenantId), datasetId, uploadId)
                .orElseThrow(() -> new IllegalArgumentException("upload session not found"));
    }

    public List<UploadPart> listUploadParts(UUID sessionId) {
        return uploadPartRepository.findBySession(sessionId);
    }

    public UploadPart uploadPart(UUID tenantId,
                                 UUID datasetId,
                                 String uploadId,
                                 int partNumber,
                                 InputStream inputStream,
                                 long size) {
        UploadSession session = getUploadSession(tenantId, datasetId, uploadId);
        if (session.status().equals(UploadSessionStatus.COMPLETED.name())) {
            throw new IllegalStateException("upload session already completed");
        }
        UploadPart existing = uploadPartRepository.findBySessionAndPartNumber(session.id(), partNumber)
                .orElse(null);
        if (existing != null) {
            return existing;
        }
        MultipartUploadedPart uploaded = storageService.uploadPart(
                session.objectKey(),
                session.uploadId(),
                partNumber,
                inputStream,
                size
        );
        UploadPart part = new UploadPart(
                UUIDv7Generator.generate(),
                session.id(),
                uploaded.partNumber(),
                uploaded.etag(),
                uploaded.size(),
                Instant.now()
        );
        return uploadPartRepository.save(part);
    }

    public DatasetFile completeMultipartUpload(UUID tenantId,
                                               UUID datasetId,
                                               String uploadId,
                                               List<MultipartUploadPart> parts) {
        UploadSession session = getUploadSession(tenantId, datasetId, uploadId);
        if (session.status().equals(UploadSessionStatus.COMPLETED.name())) {
            throw new IllegalStateException("upload session already completed");
        }
        storageService.completeMultipartUpload(session.objectKey(), session.uploadId(), parts);

        UploadSession completed = new UploadSession(
                session.id(),
                session.tenantId(),
                session.datasetId(),
                session.filename(),
                session.objectKey(),
                session.contentType(),
                session.uploadId(),
                session.partSize(),
                session.totalSize(),
                UploadSessionStatus.COMPLETED.name(),
                session.createdAt(),
                Instant.now()
        );
        uploadSessionRepository.save(completed);

        DatasetFile file = new DatasetFile(
                UUIDv7Generator.generate(),
                DatasetId.of(datasetId),
                TenantId.of(tenantId),
                session.filename(),
                session.objectKey(),
                session.totalSize(),
                session.contentType(),
                Instant.now()
        );
        return fileRepository.save(file);
    }

    public void completeUpload(UUID tenantId, UUID datasetId) {
        Dataset dataset = getDataset(tenantId, datasetId);
        dataset.markReadyForLabeling();
        repository.save(dataset);

        LabelProject existing = labelProjectRepository.findByDataset(TenantId.of(tenantId), DatasetId.of(datasetId))
                .orElse(null);
        long projectId;
        if (existing == null) {
            String projectTitle = dataset.name().isBlank() ? dataset.id().value().toString() : dataset.name();
            String description = "Auto created for dataset " + dataset.name();
            projectId = labelStudioService.createProject(projectTitle, description, defaultLabelConfig());
            LabelProject project = new LabelProject(
                    UUIDv7Generator.generate(),
                    TenantId.of(tenantId),
                    DatasetId.of(datasetId),
                    projectId,
                    Instant.now()
            );
            labelProjectRepository.save(project);
        } else {
            projectId = existing.labelStudioProjectId();
        }

        String webhookUrl = buildLabelStudioWebhookUrl();
        if (webhookUrl != null) {
            try {
                labelStudioService.ensureWebhook(projectId, webhookUrl);
            } catch (Exception exception) {
                throw new IllegalStateException("label studio webhook ensure failed", exception);
            }
        }

        List<DatasetFile> files = listFiles(tenantId, datasetId);
        List<LabelStudioTask> tasks = files.stream()
                .map(file -> new LabelStudioTask(buildFileUrl(tenantId, datasetId, file.id()), file.filename()))
                .toList();
        if (!tasks.isEmpty()) {
            labelStudioService.importTasks(projectId, tasks);
        } else {
            throw new IllegalStateException("no files available for labeling");
        }
    }

    /**
     * 标注完成后触发训练请求事件。
     */
    public void completeAnnotation(UUID tenantId, UUID datasetId) {
        Dataset dataset = getDataset(tenantId, datasetId);
        if (dataset.status() == com.example.training.domain.DatasetStatus.TRAINING_REQUESTED
                || dataset.status() == com.example.training.domain.DatasetStatus.TRAINING_COMPLETED) {
            log.info("skip completeAnnotation since training already started: datasetId={}, status={}",
                    dataset.id().value(),
                    dataset.status());
            return;
        }
        String annotationPayload = exportAnnotations(tenantId, datasetId);
        String annotationKey = buildAnnotationObjectKey(tenantId, datasetId);
        byte[] annotationBytes = annotationPayload.getBytes(StandardCharsets.UTF_8);
        storageService.upload(
                annotationKey,
                new ByteArrayInputStream(annotationBytes),
                annotationBytes.length,
                "application/json"
        );
        log.info(
                "stored annotation export: datasetId={}, annotationKey={}, bytes={}",
                dataset.id().value(),
                annotationKey,
                annotationBytes.length
        );

        dataset.markAnnotationCompleted();
        dataset.markTrainingRequested();
        repository.save(dataset);

        TrainingEvent event = new TrainingEvent(
                TrainingEventType.TRAINING_REQUESTED,
                dataset.tenantId(),
                dataset.id(),
                Instant.now(),
                Map.of(
                        "datasetName", dataset.name(),
                        "annotationKey", annotationKey
                )
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
        log.info("training requested event published: datasetId={}, annotationKey={}",
                dataset.id().value(),
                annotationKey);
    }

    public List<TrainingEventRecord> listTrainingEvents(UUID tenantId) {
        return eventRecordRepository.findByTenant(TenantId.of(tenantId));
    }

    public TrainingResult getTrainingResult(UUID tenantId, UUID datasetId) {
        return resultRepository.findByDataset(TenantId.of(tenantId), DatasetId.of(datasetId))
                .orElseThrow(() -> new IllegalArgumentException("training result not found"));
    }

    public LabelProject getLabelProject(UUID tenantId, UUID datasetId) {
        return labelProjectRepository.findByDataset(TenantId.of(tenantId), DatasetId.of(datasetId))
                .orElseThrow(() -> new IllegalArgumentException("label project not found"));
    }

    public String exportAnnotations(UUID tenantId, UUID datasetId) {
        LabelProject project = getLabelProject(tenantId, datasetId);
        return labelStudioService.exportAnnotations(project.labelStudioProjectId());
    }

    private String buildFileUrl(UUID tenantId, UUID datasetId, UUID fileId) {
        return publicBaseUrl + "/api/datasets/" + datasetId + "/files/" + fileId + "/preview?tenantId=" + tenantId;
    }

    private String buildLabelStudioWebhookUrl() {
        if (labelStudioWebhookBaseUrl == null || labelStudioWebhookBaseUrl.isBlank()) {
            return null;
        }
        String base = labelStudioWebhookBaseUrl.endsWith("/")
                ? labelStudioWebhookBaseUrl.substring(0, labelStudioWebhookBaseUrl.length() - 1)
                : labelStudioWebhookBaseUrl;
        String url = base + "/api/webhooks/label-studio";
        if (labelStudioWebhookToken != null && !labelStudioWebhookToken.isBlank()) {
            url = url + "?token=" + labelStudioWebhookToken;
        }
        return url;
    }

    private String buildRawObjectKey(UUID tenantId, UUID datasetId, UUID fileId, String filename) {
        return "datasets/" + tenantId + "/" + datasetId + "/raw/" + fileId + "-" + filename;
    }

    private String buildAnnotationObjectKey(UUID tenantId, UUID datasetId) {
        return "datasets/" + tenantId + "/" + datasetId + "/annotations/label-studio.json";
    }

    private String defaultLabelConfig() {
        return "<View>" +
                "<Image name=\"image\" value=\"$image\"/>" +
                "<RectangleLabels name=\"label\" toName=\"image\">" +
                "<Label value=\"defect\" background=\"#E53E3E\"/>" +
                "</RectangleLabels>" +
                "</View>";
    }
}
