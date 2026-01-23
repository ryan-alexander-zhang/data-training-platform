package com.example.training.adapter.api;

import java.time.Instant;
import java.util.UUID;

public record DatasetSummaryResponse(
        UUID id,
        String name,
        String status,
        long assetCount,
        Instant createdAt,
        Instant updatedAt
) {
}
