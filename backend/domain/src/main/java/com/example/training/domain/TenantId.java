package com.example.training.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * 多租户标识。
 * <p>
 * 业务上要求所有资源必须绑定 tenantId，以便做行级隔离。
 */
public final class TenantId {
    private final UUID value;

    private TenantId(UUID value) {
        this.value = Objects.requireNonNull(value, "tenantId is required");
    }

    public static TenantId of(UUID value) {
        return new TenantId(value);
    }

    public UUID value() {
        return value;
    }
}
