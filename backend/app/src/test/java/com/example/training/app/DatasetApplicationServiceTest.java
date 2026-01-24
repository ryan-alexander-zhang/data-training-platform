package com.example.training.app;

import com.example.training.domain.Dataset;
import com.example.training.domain.DatasetFile;
import com.example.training.domain.DatasetFileRepository;
import com.example.training.domain.DatasetId;
import com.example.training.domain.DatasetRepository;
import com.example.training.domain.DatasetStatus;
import com.example.training.domain.LabelProject;
import com.example.training.domain.LabelProjectRepository;
import com.example.training.domain.LabelStudioProjectSummary;
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
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatasetApplicationServiceTest {

    @Test
    void completesUploadAndAnnotationFlow() {
        InMemoryDatasetRepository datasetRepository = new InMemoryDatasetRepository();
        InMemoryTrainingEventPublisher eventPublisher = new InMemoryTrainingEventPublisher();
        InMemoryDatasetFileRepository fileRepository = new InMemoryDatasetFileRepository();
        InMemoryTrainingEventRecordRepository eventRecordRepository = new InMemoryTrainingEventRecordRepository();
        InMemoryTrainingResultRepository resultRepository = new InMemoryTrainingResultRepository();
        InMemoryObjectStorageService storageService = new InMemoryObjectStorageService();
        InMemoryUploadSessionRepository uploadSessionRepository = new InMemoryUploadSessionRepository();
        InMemoryUploadPartRepository uploadPartRepository = new InMemoryUploadPartRepository();
        InMemoryLabelProjectRepository labelProjectRepository = new InMemoryLabelProjectRepository();
        InMemoryLabelStudioService labelStudioService = new InMemoryLabelStudioService();

        DatasetApplicationService service = new DatasetApplicationService(
                datasetRepository,
                eventPublisher,
                fileRepository,
                eventRecordRepository,
                resultRepository,
                storageService,
                uploadSessionRepository,
                uploadPartRepository,
                labelProjectRepository,
                labelStudioService,
                "http://localhost:8081",
                "http://localhost:8081",
                "test-token"
        );

        UUID tenantId = UUID.randomUUID();
        Dataset dataset = service.createDataset(tenantId, "test dataset");
        assertEquals(DatasetStatus.CREATED, dataset.status());

        DatasetFile file = service.uploadFile(
                tenantId,
                dataset.id().value(),
                "sample.png",
                "image/png",
                10L,
                new ByteArrayInputStream(new byte[]{1, 2, 3})
        );
        assertNotNull(file);
        assertTrue(storageService.objects.containsKey(file.objectKey()));

        service.completeUpload(tenantId, dataset.id().value());
        Dataset updated = datasetRepository.findById(TenantId.of(tenantId), dataset.id()).orElseThrow();
        assertEquals(DatasetStatus.READY_FOR_LABELING, updated.status());
        assertEquals(1, labelProjectRepository.projects.size());

        service.completeAnnotation(tenantId, dataset.id().value());
        Dataset afterAnnotation = datasetRepository.findById(TenantId.of(tenantId), dataset.id()).orElseThrow();
        assertEquals(DatasetStatus.TRAINING_REQUESTED, afterAnnotation.status());
        assertEquals(1, eventPublisher.events.size());
        assertEquals(1, eventRecordRepository.records.size());
        assertTrue(storageService.objects.keySet().stream()
                .anyMatch(key -> key.contains("/annotations/label-studio.json")));
        TrainingEvent published = eventPublisher.events.get(0);
        assertEquals(TrainingEventType.TRAINING_REQUESTED, published.type());
        assertTrue(published.payload().containsKey("annotationKey"));
        assertTrue(published.payload().get("annotationKey").toString().contains("/annotations/"));
    }

    private static class InMemoryDatasetRepository implements DatasetRepository {
        private final List<Dataset> datasets = new ArrayList<>();

        @Override
        public Dataset save(Dataset dataset) {
            datasets.removeIf(existing -> existing.id().value().equals(dataset.id().value()));
            datasets.add(dataset);
            return dataset;
        }

        @Override
        public Optional<Dataset> findById(TenantId tenantId, DatasetId datasetId) {
            return datasets.stream()
                    .filter(dataset -> dataset.id().value().equals(datasetId.value()))
                    .filter(dataset -> dataset.tenantId().value().equals(tenantId.value()))
                    .findFirst();
        }

        @Override
        public List<Dataset> findByTenant(TenantId tenantId) {
            return datasets.stream()
                    .filter(dataset -> dataset.tenantId().value().equals(tenantId.value()))
                    .toList();
        }
    }

    private static class InMemoryDatasetFileRepository implements DatasetFileRepository {
        private final List<DatasetFile> files = new ArrayList<>();

        @Override
        public DatasetFile save(DatasetFile file) {
            files.add(file);
            return file;
        }

        @Override
        public List<DatasetFile> findByDataset(TenantId tenantId, DatasetId datasetId) {
            return files.stream()
                    .filter(file -> file.datasetId().value().equals(datasetId.value()))
                    .filter(file -> file.tenantId().value().equals(tenantId.value()))
                    .toList();
        }

        @Override
        public long countByDataset(TenantId tenantId, DatasetId datasetId) {
            return findByDataset(tenantId, datasetId).size();
        }
    }

    private static class InMemoryTrainingEventRecordRepository implements TrainingEventRecordRepository {
        private final List<TrainingEventRecord> records = new ArrayList<>();

        @Override
        public TrainingEventRecord save(TrainingEventRecord record) {
            records.add(record);
            return record;
        }

        @Override
        public List<TrainingEventRecord> findByTenant(TenantId tenantId) {
            return records.stream()
                    .filter(record -> record.tenantId().value().equals(tenantId.value()))
                    .toList();
        }
    }

    private static class InMemoryTrainingResultRepository implements TrainingResultRepository {
        private final List<TrainingResult> results = new ArrayList<>();

        @Override
        public TrainingResult save(TrainingResult result) {
            results.add(result);
            return result;
        }

        @Override
        public Optional<TrainingResult> findByDataset(TenantId tenantId, DatasetId datasetId) {
            return results.stream()
                    .filter(result -> result.datasetId().value().equals(datasetId.value()))
                    .filter(result -> result.tenantId().value().equals(tenantId.value()))
                    .findFirst();
        }
    }

    private static class InMemoryTrainingEventPublisher implements TrainingEventPublisher {
        private final List<TrainingEvent> events = new ArrayList<>();

        @Override
        public void publish(TrainingEvent event) {
            events.add(event);
        }
    }

    private static class InMemoryObjectStorageService implements ObjectStorageService {
        private final Map<String, byte[]> objects = new java.util.HashMap<>();

        @Override
        public void upload(String objectKey, java.io.InputStream inputStream, long size, String contentType) {
            try {
                objects.put(objectKey, inputStream.readAllBytes());
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }

        @Override
        public StoredObject download(String objectKey) {
            byte[] data = objects.get(objectKey);
            return new StoredObject(new ByteArrayInputStream(data), data.length, "application/octet-stream");
        }

        @Override
        public MultipartUpload initMultipartUpload(String objectKey, String contentType) {
            return new MultipartUpload("upload-1", 1024L);
        }

        @Override
        public MultipartUploadedPart uploadPart(String objectKey, String uploadId, int partNumber, java.io.InputStream inputStream, long size) {
            return new MultipartUploadedPart(partNumber, "etag-" + partNumber, size);
        }

        @Override
        public void completeMultipartUpload(String objectKey, String uploadId, List<MultipartUploadPart> parts) {
        }
    }

    private static class InMemoryUploadSessionRepository implements UploadSessionRepository {
        private final List<UploadSession> sessions = new ArrayList<>();

        @Override
        public UploadSession save(UploadSession session) {
            sessions.removeIf(existing -> existing.id().equals(session.id()));
            sessions.add(session);
            return session;
        }

        @Override
        public Optional<UploadSession> findByUploadId(TenantId tenantId, UUID datasetId, String uploadId) {
            return sessions.stream()
                    .filter(session -> session.tenantId().value().equals(tenantId.value()))
                    .filter(session -> session.datasetId().value().equals(datasetId))
                    .filter(session -> session.uploadId().equals(uploadId))
                    .findFirst();
        }

        @Override
        public List<UploadSession> findByDataset(TenantId tenantId, DatasetId datasetId) {
            return sessions.stream()
                    .filter(session -> session.tenantId().value().equals(tenantId.value()))
                    .filter(session -> session.datasetId().value().equals(datasetId.value()))
                    .toList();
        }
    }

    private static class InMemoryUploadPartRepository implements UploadPartRepository {
        private final List<UploadPart> parts = new ArrayList<>();

        @Override
        public UploadPart save(UploadPart part) {
            parts.removeIf(existing -> existing.id().equals(part.id()));
            parts.add(part);
            return part;
        }

        @Override
        public Optional<UploadPart> findBySessionAndPartNumber(UUID sessionId, int partNumber) {
            return parts.stream()
                    .filter(part -> part.sessionId().equals(sessionId))
                    .filter(part -> part.partNumber() == partNumber)
                    .findFirst();
        }

        @Override
        public List<UploadPart> findBySession(UUID sessionId) {
            return parts.stream()
                    .filter(part -> part.sessionId().equals(sessionId))
                    .toList();
        }
    }

    private static class InMemoryLabelProjectRepository implements LabelProjectRepository {
        private final List<LabelProject> projects = new ArrayList<>();

        @Override
        public LabelProject save(LabelProject project) {
            projects.removeIf(existing -> existing.id().equals(project.id()));
            projects.add(project);
            return project;
        }

        @Override
        public Optional<LabelProject> findByDataset(TenantId tenantId, DatasetId datasetId) {
            return projects.stream()
                    .filter(project -> project.tenantId().value().equals(tenantId.value()))
                    .filter(project -> project.datasetId().value().equals(datasetId.value()))
                    .findFirst();
        }

        @Override
        public Optional<LabelProject> findByLabelStudioProjectId(long labelStudioProjectId) {
            return projects.stream()
                    .filter(project -> project.labelStudioProjectId() == labelStudioProjectId)
                    .findFirst();
        }
    }

    private static class InMemoryLabelStudioService implements LabelStudioService {
        @Override
        public long createProject(String title, String description, String labelConfig) {
            return 100L;
        }

        @Override
        public void importTasks(long projectId, List<LabelStudioTask> tasks) {
        }

        @Override
        public String exportAnnotations(long projectId) {
            return "{}";
        }

        @Override
        public LabelStudioProjectSummary getProjectSummary(long projectId) {
            return new LabelStudioProjectSummary(1L, 0L);
        }

        @Override
        public void ensureWebhook(long projectId, String url) {
        }
    }
}

