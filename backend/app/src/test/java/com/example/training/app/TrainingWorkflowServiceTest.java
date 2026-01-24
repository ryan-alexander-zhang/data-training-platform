package com.example.training.app;

import com.example.training.domain.Dataset;
import com.example.training.domain.DatasetId;
import com.example.training.domain.DatasetRepository;
import com.example.training.domain.DatasetStatus;
import com.example.training.domain.ObjectStorageService;
import com.example.training.domain.TenantId;
import com.example.training.domain.TrainingEvent;
import com.example.training.domain.TrainingEventRecord;
import com.example.training.domain.TrainingEventRecordRepository;
import com.example.training.domain.TrainingEventPublisher;
import com.example.training.domain.TrainingEventType;
import com.example.training.domain.TrainingResult;
import com.example.training.domain.TrainingResultRepository;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingWorkflowServiceTest {

    @Test
    void handlesTrainingRequested() {
        InMemoryDatasetRepository datasetRepository = new InMemoryDatasetRepository();
        InMemoryTrainingResultRepository resultRepository = new InMemoryTrainingResultRepository();
        InMemoryTrainingEventRecordRepository eventRecordRepository = new InMemoryTrainingEventRecordRepository();
        InMemoryTrainingEventPublisher eventPublisher = new InMemoryTrainingEventPublisher();
        InMemoryObjectStorageService storageService = new InMemoryObjectStorageService();

        TrainingWorkflowService workflowService = new TrainingWorkflowService(
                datasetRepository,
                resultRepository,
                eventRecordRepository,
                eventPublisher,
                storageService
        );

        UUID tenantId = UUID.randomUUID();
        Dataset dataset = new Dataset(DatasetId.of(UUIDv7Generator.generate()), TenantId.of(tenantId), "flow dataset");
        dataset.markAnnotationCompleted();
        dataset.markTrainingRequested();
        datasetRepository.save(dataset);

        TrainingEvent event = new TrainingEvent(
                TrainingEventType.TRAINING_REQUESTED,
                dataset.tenantId(),
                dataset.id(),
                Instant.now(),
                Map.of("datasetName", dataset.name())
        );

        workflowService.handleTrainingRequested(event);

        Dataset updated = datasetRepository.findById(dataset.tenantId(), dataset.id()).orElseThrow();
        assertEquals(DatasetStatus.TRAINING_COMPLETED, updated.status());
        assertTrue(resultRepository.findByDataset(dataset.tenantId(), dataset.id()).isPresent());
        assertEquals(1, eventPublisher.events.size());
        assertEquals(1, eventRecordRepository.records.size());
        assertTrue(storageService.objects.values().stream().anyMatch(bytes -> bytes.length > 0));
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
    }
}
