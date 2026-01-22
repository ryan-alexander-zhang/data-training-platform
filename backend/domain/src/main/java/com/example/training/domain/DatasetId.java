package com.example.training.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * 数据集主键，要求使用 UUIDv7。
 * <p>
 * 为了满足排序与分布式写入友好性，UUIDv7 由应用层生成后写入数据库。
 */
public final class DatasetId {
    private final UUID value;

    private DatasetId(UUID value) {
        this.value = Objects.requireNonNull(value, "datasetId is required");
    }

    public static DatasetId of(UUID value) {
        return new DatasetId(value);
    }

    public UUID value() {
        return value;
    }
}
