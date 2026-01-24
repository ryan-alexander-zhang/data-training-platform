package com.example.training.adapter.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUploadSessionRequest(
        @NotBlank String filename,
        @NotBlank String contentType,
        @NotNull Long size
) {
}
