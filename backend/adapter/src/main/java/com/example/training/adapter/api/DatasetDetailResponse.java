package com.example.training.adapter.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DatasetDetailResponse(
        UUID id,
        String name,
        String status,
        long assetCount,
        Instant createdAt,
        Instant updatedAt,
        String labelingUrl,
        List<DatasetFileResponse> files,
        TrainingResultResponse trainingResult
) {
}
