package com.example.training.adapter.api;

import jakarta.validation.constraints.NotBlank;

public record CreateDatasetRequest(
        @NotBlank String name
) {
}
