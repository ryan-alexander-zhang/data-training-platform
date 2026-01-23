package com.example.training.start;

import com.example.training.app.TrainingWorkflowService;
import com.example.training.domain.TrainingEvent;
import com.example.training.domain.TrainingEventType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 训练事件消费者（模拟模型处理）。
 */
@Component
public class TrainingEventConsumer {
    private final TrainingWorkflowService workflowService;

    public TrainingEventConsumer(TrainingWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @KafkaListener(topics = "${training.events.topic:training.events}", groupId = "training-platform")
    public void handleEvent(TrainingEvent event) {
        if (event.type() == TrainingEventType.TRAINING_REQUESTED) {
            workflowService.handleTrainingRequested(event);
        } else if (event.type() == TrainingEventType.TRAINING_COMPLETED) {
            workflowService.handleTrainingCompleted(event);
        }
    }
}
