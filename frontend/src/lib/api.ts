export type DatasetResponse = {
  id: string;
  tenantId: string;
  name: string;
  status: string;
  createdAt: string;
};

export type DatasetSummary = {
  id: string;
  name: string;
  status: string;
  assetCount: number;
  createdAt: string;
  updatedAt: string;
};

export type DatasetFile = {
  id: string;
  filename: string;
  objectKey: string;
  size: number;
  contentType: string;
  uploadedAt: string;
};

export type TrainingResult = {
  artifactKey: string;
  metricsKey: string;
  createdAt: string;
};

export type DatasetDetail = {
  id: string;
  name: string;
  status: string;
  assetCount: number;
  createdAt: string;
  updatedAt: string;
  labelingUrl: string | null;
  files: DatasetFile[];
  trainingResult: TrainingResult | null;
};

export type UploadSession = {
  sessionId: string;
  uploadId: string;
  partSize: number;
  objectKey: string;
  contentType: string;
};

export type UploadPart = {
  partNumber: number;
  etag: string;
};

export type UploadSessionDetail = {
  uploadId: string;
  partSize: number;
  parts: UploadPart[];
};

export type AnnotationResponse = {
  payload: string;
};

export type TrainingEvent = {
  id: string;
  datasetId: string;
  datasetName: string;
  eventType: string;
  occurredAt: string;
};

export type Settings = {
  labelStudioBaseUrl: string;
  minioBucket: string;
  minioEndpoint: string;
};

const apiBaseUrl =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8081";
const defaultTenantId =
  // TODO: replace with user-selected tenant when multi-tenant support is added.
  process.env.NEXT_PUBLIC_TENANT_ID ?? "00000000-0000-0000-0000-000000000001";

export function getApiBaseUrl() {
  return apiBaseUrl;
}

export function getTenantId() {
  return defaultTenantId;
}

export async function apiFetch(
  input: RequestInfo | URL,
  init: RequestInit = {},
  tenantId: string = defaultTenantId
) {
  const headers = new Headers(init.headers);
  if (!headers.has("X-Tenant-Id")) {
    headers.set("X-Tenant-Id", tenantId);
  }
  return fetch(input, {
    ...init,
    headers
  });
}

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
};

async function handleResponse<T>(response: Response): Promise<T> {
  const payload = (await response.json()) as ApiResponse<T>;
  if (!response.ok || !payload.success) {
    throw new Error(payload.message || "请求失败");
  }
  return payload.data;
}

export async function createDataset(
  name: string,
  tenantId: string = defaultTenantId
): Promise<DatasetResponse> {
  const response = await apiFetch(
    `${apiBaseUrl}/api/datasets`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ name })
    },
    tenantId
  );

  return handleResponse(response);
}

export async function listDatasets(
  tenantId: string = defaultTenantId
): Promise<DatasetSummary[]> {
  const response = await apiFetch(`${apiBaseUrl}/api/datasets`, {}, tenantId);

  return handleResponse(response);
}

export async function getDataset(
  datasetId: string,
  tenantId: string = defaultTenantId
): Promise<DatasetDetail> {
  const response = await apiFetch(
    `${apiBaseUrl}/api/datasets/${datasetId}`,
    {},
    tenantId
  );

  return handleResponse(response);
}

export async function uploadDatasetFile(
  datasetId: string,
  file: File,
  tenantId: string = defaultTenantId
): Promise<DatasetFile> {
  const formData = new FormData();
  formData.append("file", file);
  const response = await apiFetch(
    `${apiBaseUrl}/api/datasets/${datasetId}/files`,
    {
      method: "POST",
      body: formData
    },
    tenantId
  );

  return handleResponse(response);
}

export async function createUploadSession(
  datasetId: string,
  file: File,
  tenantId: string = defaultTenantId
): Promise<UploadSession> {
  const response = await apiFetch(
    `${apiBaseUrl}/api/datasets/${datasetId}/uploads`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        filename: file.name,
        contentType: file.type || "application/octet-stream",
        size: file.size
      })
    },
    tenantId
  );

  return handleResponse(response);
}

export async function getUploadSession(
  datasetId: string,
  uploadId: string,
  tenantId: string = defaultTenantId
): Promise<UploadSessionDetail> {
  const response = await apiFetch(
    `${apiBaseUrl}/api/datasets/${datasetId}/uploads/${uploadId}`,
    {},
    tenantId
  );

  return handleResponse(response);
}

export async function uploadPart(
  datasetId: string,
  uploadId: string,
  partNumber: number,
  chunk: Blob,
  tenantId: string = defaultTenantId
): Promise<UploadPart> {
  const formData = new FormData();
  formData.append("file", chunk);
  const response = await apiFetch(
    `${apiBaseUrl}/api/datasets/${datasetId}/uploads/${uploadId}/parts/${partNumber}`,
    {
      method: "PUT",
      body: formData
    },
    tenantId
  );

  return handleResponse(response);
}

export async function completeUploadSession(
  datasetId: string,
  uploadId: string,
  parts: UploadPart[],
  tenantId: string = defaultTenantId
): Promise<DatasetFile> {
  const response = await apiFetch(
    `${apiBaseUrl}/api/datasets/${datasetId}/uploads/${uploadId}/complete`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ parts })
    },
    tenantId
  );

  return handleResponse(response);
}

export async function fetchAnnotations(
  datasetId: string,
  tenantId: string = defaultTenantId
): Promise<AnnotationResponse> {
  const response = await apiFetch(
    `${apiBaseUrl}/api/datasets/${datasetId}/annotations`,
    {},
    tenantId
  );

  return handleResponse(response);
}

export async function completeDatasetUpload(
  datasetId: string,
  tenantId: string = defaultTenantId
): Promise<void> {
  const response = await apiFetch(
    `${apiBaseUrl}/api/datasets/${datasetId}/upload/complete`,
    {
      method: "POST"
    },
    tenantId
  );

  await handleResponse(response);
}

export async function completeAnnotation(
  datasetId: string,
  tenantId: string = defaultTenantId
): Promise<void> {
  const response = await apiFetch(
    `${apiBaseUrl}/api/datasets/${datasetId}/annotation/complete`,
    {
      method: "POST"
    },
    tenantId
  );

  await handleResponse(response);
}

export async function listTrainingEvents(
  tenantId: string = defaultTenantId
): Promise<TrainingEvent[]> {
  const response = await apiFetch(
    `${apiBaseUrl}/api/training/events`,
    {},
    tenantId
  );

  return handleResponse(response);
}

export async function getSettings(): Promise<Settings> {
  const response = await fetch(`${apiBaseUrl}/api/settings`);

  return handleResponse(response);
}
