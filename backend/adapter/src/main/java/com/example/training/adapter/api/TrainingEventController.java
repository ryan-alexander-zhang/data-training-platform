package com.example.training.adapter.api;

import com.example.training.app.DatasetApplicationService;
import com.example.training.domain.TrainingEventRecord;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 训练事件查询 API。
 */
@RestController
@RequestMapping("/api/training/events")
public class TrainingEventController {
    private final DatasetApplicationService service;

    public TrainingEventController(DatasetApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public List<TrainingEventResponse> listEvents(@RequestHeader("X-Tenant-Id") UUID tenantId) {
        return service.listTrainingEvents(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    private TrainingEventResponse toResponse(TrainingEventRecord record) {
        return new TrainingEventResponse(
                record.id(),
                record.datasetId().value(),
                record.datasetName(),
                record.type().name(),
                record.occurredAt()
        );
    }
}
