package com.example.training.domain;

/**
 * 数据集生命周期状态。
 */
public enum DatasetStatus {
    CREATED,
    UPLOADING,
    READY_FOR_LABELING,
    ANNOTATION_COMPLETED,
    TRAINING_REQUESTED,
    TRAINING_COMPLETED
}
