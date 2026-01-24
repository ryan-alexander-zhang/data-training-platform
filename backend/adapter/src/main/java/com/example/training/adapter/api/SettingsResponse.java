package com.example.training.adapter.api;

public record SettingsResponse(
        String labelStudioBaseUrl,
        String minioBucket,
        String minioEndpoint
) {
}
