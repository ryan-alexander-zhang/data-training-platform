package com.example.training.adapter.api;

import java.time.Instant;

public record TrainingResultResponse(
        String artifactKey,
        String metricsKey,
        Instant createdAt
) {
}
