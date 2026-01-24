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

CREATE TABLE IF NOT EXISTS upload_sessions (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    dataset_id UUID NOT NULL,
    filename TEXT NOT NULL,
    object_key TEXT NOT NULL,
    content_type TEXT NOT NULL,
    upload_id TEXT NOT NULL,
    part_size BIGINT NOT NULL,
    total_size BIGINT NOT NULL,
    status TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_upload_sessions_dataset ON upload_sessions (dataset_id);
CREATE INDEX IF NOT EXISTS idx_upload_sessions_tenant ON upload_sessions (tenant_id);

CREATE TABLE IF NOT EXISTS upload_parts (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    part_number INT NOT NULL,
    etag TEXT NOT NULL,
    size BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_upload_parts_unique ON upload_parts (session_id, part_number);
CREATE INDEX IF NOT EXISTS idx_upload_parts_session ON upload_parts (session_id);
