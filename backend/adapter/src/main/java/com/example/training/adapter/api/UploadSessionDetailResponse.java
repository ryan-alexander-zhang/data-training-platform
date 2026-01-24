package com.example.training.adapter.api;

import java.util.List;

public record UploadSessionDetailResponse(
        String uploadId,
        long partSize,
        List<UploadPartResponse> parts
) {
}
