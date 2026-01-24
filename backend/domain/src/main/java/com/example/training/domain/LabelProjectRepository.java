package com.example.training.domain;

import java.util.Optional;

public interface LabelProjectRepository {
    LabelProject save(LabelProject project);

    Optional<LabelProject> findByDataset(TenantId tenantId, DatasetId datasetId);
}
