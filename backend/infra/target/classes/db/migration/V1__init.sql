CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE datasets (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_datasets_tenant ON datasets (tenant_id);

CREATE TABLE dataset_assets (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    dataset_id UUID NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    checksum VARCHAR(128),
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_assets_dataset ON dataset_assets (dataset_id);

CREATE TABLE label_projects (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    dataset_id UUID NOT NULL,
    label_studio_project_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE training_jobs (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    dataset_id UUID NOT NULL,
    status VARCHAR(64) NOT NULL,
    result_object_key VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ
);
