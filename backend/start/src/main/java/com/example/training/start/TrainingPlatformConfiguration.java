package com.example.training.start;

import com.example.training.app.DatasetApplicationService;
import com.example.training.domain.DatasetRepository;
import com.example.training.domain.TrainingEventPublisher;
import com.example.training.infra.kafka.KafkaTrainingEventPublisher;
import com.example.training.infra.mapper.DatasetMapper;
import com.example.training.infra.repository.DatasetRepositoryImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class TrainingPlatformConfiguration {

    @Bean
    public DatasetRepository datasetRepository(DatasetMapper mapper) {
        return new DatasetRepositoryImpl(mapper);
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
            TrainingEventPublisher publisher
    ) {
        return new DatasetApplicationService(repository, publisher);
    }
}
