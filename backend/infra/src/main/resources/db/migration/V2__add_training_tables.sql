CREATE TABLE IF NOT EXISTS dataset_files (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    dataset_id UUID NOT NULL,
    filename TEXT NOT NULL,
    object_key TEXT NOT NULL,
    size BIGINT NOT NULL,
    content_type TEXT,
    uploaded_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_dataset_files_dataset ON dataset_files (dataset_id);
CREATE INDEX IF NOT EXISTS idx_dataset_files_tenant ON dataset_files (tenant_id);

CREATE TABLE IF NOT EXISTS training_results (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    dataset_id UUID NOT NULL,
    artifact_key TEXT NOT NULL,
    metrics_key TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_training_results_dataset ON training_results (dataset_id);
CREATE INDEX IF NOT EXISTS idx_training_results_tenant ON training_results (tenant_id);

CREATE TABLE IF NOT EXISTS training_events (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    dataset_id UUID NOT NULL,
    dataset_name TEXT NOT NULL,
    event_type TEXT NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_training_events_tenant ON training_events (tenant_id);
CREATE INDEX IF NOT EXISTS idx_training_events_occurred ON training_events (occurred_at);

CREATE INDEX IF NOT EXISTS idx_datasets_updated ON datasets (updated_at);
