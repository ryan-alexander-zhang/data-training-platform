package com.example.training.infra.kafka;

import com.example.training.domain.TrainingEvent;
import com.example.training.domain.TrainingEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Kafka 训练事件发布器。
 * <p>
 * 事件主题建议使用 training.events。
 */
public class KafkaTrainingEventPublisher implements TrainingEventPublisher {
    private final KafkaTemplate<String, TrainingEvent> kafkaTemplate;
    private final String topic;

    public KafkaTrainingEventPublisher(KafkaTemplate<String, TrainingEvent> kafkaTemplate, String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(TrainingEvent event) {
        kafkaTemplate.send(topic, event.datasetId().value().toString(), event);
    }
}
