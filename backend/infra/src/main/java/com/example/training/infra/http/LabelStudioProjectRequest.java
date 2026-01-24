package com.example.training.infra.http;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LabelStudioProjectRequest(
        @JsonProperty("title") String title,
        @JsonProperty("label_config") String labelConfig,
        @JsonProperty("description") String description
) {
}
