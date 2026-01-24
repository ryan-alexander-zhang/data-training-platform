package com.example.training.domain;

public record LabelStudioProjectSummary(long taskNumber, long numTasksWithAnnotations) {
    public boolean isCompleted() {
        return taskNumber > 0 && numTasksWithAnnotations >= taskNumber;
    }
}
