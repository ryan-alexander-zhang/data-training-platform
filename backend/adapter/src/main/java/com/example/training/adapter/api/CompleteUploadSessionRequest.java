package com.example.training.adapter.api;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CompleteUploadSessionRequest(@NotEmpty List<UploadPartResponse> parts) {
}
