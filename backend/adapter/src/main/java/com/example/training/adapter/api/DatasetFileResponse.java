package com.example.training.adapter.api;

import java.time.Instant;
import java.util.UUID;

public record DatasetFileResponse(
        UUID id,
        String filename,
        String objectKey,
        long size,
        String contentType,
        Instant uploadedAt
) {
}
