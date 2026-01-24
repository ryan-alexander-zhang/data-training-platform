package com.example.training.infra.http;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record LabelStudioImportRequest(
        @JsonProperty("tasks") List<Map<String, Object>> tasks
) {
}
