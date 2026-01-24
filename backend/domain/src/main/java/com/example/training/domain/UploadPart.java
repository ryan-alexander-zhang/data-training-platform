package com.example.training.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class UploadPart {
    private final UUID id;
    private final UUID sessionId;
    private final int partNumber;
    private final String etag;
    private final long size;
    private final Instant createdAt;

    public UploadPart(UUID id,
                      UUID sessionId,
                      int partNumber,
                      String etag,
                      long size,
                      Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId is required");
        this.partNumber = partNumber;
        this.etag = Objects.requireNonNull(etag, "etag is required");
        this.size = size;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
    }

    public UUID id() {
        return id;
    }

    public UUID sessionId() {
        return sessionId;
    }

    public int partNumber() {
        return partNumber;
    }

    public String etag() {
        return etag;
    }

    public long size() {
        return size;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
