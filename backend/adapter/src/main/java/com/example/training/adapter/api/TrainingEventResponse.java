package com.example.training.adapter.api;

import java.time.Instant;
import java.util.UUID;

public record TrainingEventResponse(
        UUID id,
        UUID datasetId,
        String datasetName,
        String eventType,
        Instant occurredAt
) {
}
