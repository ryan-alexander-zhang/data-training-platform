package com.example.training.start;

import com.example.training.app.TrainingWorkflowService;
import com.example.training.domain.TrainingEvent;
import com.example.training.domain.TrainingEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 训练事件消费者（模拟模型处理）。
 */
@Component
public class TrainingEventConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingEventConsumer.class);
    private final TrainingWorkflowService workflowService;

    public TrainingEventConsumer(TrainingWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @KafkaListener(topics = "${training.events.topic:training.events}", groupId = "training-platform")
    public void handleEvent(TrainingEvent event) {
        LOGGER.info("training event received: type={}, tenantId={}, datasetId={}",
                event.type(),
                event.tenantId().value(),
                event.datasetId().value());
        if (event.type() == TrainingEventType.TRAINING_REQUESTED) {
            LOGGER.info("training requested event start: datasetId={}", event.datasetId().value());
            workflowService.handleTrainingRequested(event);
            LOGGER.info("training requested event done: datasetId={}", event.datasetId().value());
        } else if (event.type() == TrainingEventType.TRAINING_COMPLETED) {
            LOGGER.info("training completed event start: datasetId={}", event.datasetId().value());
            workflowService.handleTrainingCompleted(event);
            LOGGER.info("training completed event done: datasetId={}", event.datasetId().value());
        } else {
            LOGGER.info("training event ignored: type={}", event.type());
        }
    }
}
