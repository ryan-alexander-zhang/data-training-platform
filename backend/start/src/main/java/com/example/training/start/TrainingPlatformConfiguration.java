package com.example.training.start;

import com.example.training.app.DatasetApplicationService;
import com.example.training.app.TrainingWorkflowService;
import com.example.training.domain.DatasetFileRepository;
import com.example.training.domain.DatasetRepository;
import com.example.training.domain.ObjectStorageService;
import com.example.training.domain.TrainingEventPublisher;
import com.example.training.domain.TrainingEventRecordRepository;
import com.example.training.domain.TrainingResultRepository;
import com.example.training.infra.kafka.KafkaTrainingEventPublisher;
import com.example.training.infra.mapper.DatasetFileMapper;
import com.example.training.infra.mapper.DatasetMapper;
import com.example.training.infra.mapper.TrainingEventMapper;
import com.example.training.infra.mapper.TrainingResultMapper;
import com.example.training.infra.repository.DatasetFileRepositoryImpl;
import com.example.training.infra.repository.DatasetRepositoryImpl;
import com.example.training.infra.repository.TrainingEventRecordRepositoryImpl;
import com.example.training.infra.repository.TrainingResultRepositoryImpl;
import com.example.training.infra.storage.MinioObjectStorageService;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;

@EnableKafka
@Configuration
public class TrainingPlatformConfiguration {

    @Bean
    public DatasetRepository datasetRepository(DatasetMapper mapper) {
        return new DatasetRepositoryImpl(mapper);
    }

    @Bean
    public DatasetFileRepository datasetFileRepository(DatasetFileMapper mapper) {
        return new DatasetFileRepositoryImpl(mapper);
    }

    @Bean
    public TrainingEventRecordRepository trainingEventRecordRepository(TrainingEventMapper mapper) {
        return new TrainingEventRecordRepositoryImpl(mapper);
    }

    @Bean
    public TrainingResultRepository trainingResultRepository(TrainingResultMapper mapper) {
        return new TrainingResultRepositoryImpl(mapper);
    }

    @Bean
    public MinioClient minioClient(
            @Value("${training.storage.minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${training.storage.minio.access-key:minio}") String accessKey,
            @Value("${training.storage.minio.secret-key:minio123}") String secretKey
    ) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    public ObjectStorageService objectStorageService(
            MinioClient minioClient,
            @Value("${training.storage.minio.bucket:training-data}") String bucket
    ) {
        return new MinioObjectStorageService(minioClient, bucket);
    }

    @Bean
    public TrainingEventPublisher trainingEventPublisher(
            KafkaTemplate<String, com.example.training.domain.TrainingEvent> kafkaTemplate,
            @Value("${training.events.topic:training.events}") String topic
    ) {
        return new KafkaTrainingEventPublisher(kafkaTemplate, topic);
    }

    @Bean
    public DatasetApplicationService datasetApplicationService(
            DatasetRepository repository,
            TrainingEventPublisher publisher,
            DatasetFileRepository fileRepository,
            TrainingEventRecordRepository eventRecordRepository,
            TrainingResultRepository resultRepository,
            ObjectStorageService storageService
    ) {
        return new DatasetApplicationService(
                repository,
                publisher,
                fileRepository,
                eventRecordRepository,
                resultRepository,
                storageService
        );
    }

    @Bean
    public TrainingWorkflowService trainingWorkflowService(
            DatasetRepository repository,
            TrainingResultRepository resultRepository,
            TrainingEventRecordRepository eventRecordRepository,
            TrainingEventPublisher publisher,
            ObjectStorageService storageService
    ) {
        return new TrainingWorkflowService(
                repository,
                resultRepository,
                eventRecordRepository,
                publisher,
                storageService
        );
    }
}
