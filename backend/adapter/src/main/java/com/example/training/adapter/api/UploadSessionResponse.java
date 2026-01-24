package com.example.training.adapter.api;

import java.util.UUID;

public record UploadSessionResponse(
        UUID sessionId,
        String uploadId,
        long partSize,
        String objectKey,
        String contentType
) {
}
