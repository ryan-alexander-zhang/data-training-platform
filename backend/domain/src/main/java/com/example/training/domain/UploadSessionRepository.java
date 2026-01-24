package com.example.training.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadSessionRepository {
    UploadSession save(UploadSession session);

    Optional<UploadSession> findByUploadId(TenantId tenantId, UUID datasetId, String uploadId);

    List<UploadSession> findByDataset(TenantId tenantId, DatasetId datasetId);
}
