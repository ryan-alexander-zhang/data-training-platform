package com.example.training.adapter.api;

import java.time.Instant;
import java.util.UUID;

public record DatasetResponse(
        UUID id,
        UUID tenantId,
        String name,
        String status,
        Instant createdAt
) {
}
