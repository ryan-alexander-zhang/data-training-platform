package com.example.training.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadPartRepository {
    UploadPart save(UploadPart part);

    Optional<UploadPart> findBySessionAndPartNumber(UUID sessionId, int partNumber);

    List<UploadPart> findBySession(UUID sessionId);
}
