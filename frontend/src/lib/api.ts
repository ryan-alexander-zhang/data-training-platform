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
  labelingUrl: string;
  files: DatasetFile[];
  trainingResult: TrainingResult | null;
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
  process.env.NEXT_PUBLIC_TENANT_ID ?? "00000000-0000-0000-0000-000000000001";

export function getApiBaseUrl() {
  return apiBaseUrl;
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "请求失败");
  }
  return response.json();
}

export async function createDataset(
  name: string,
  tenantId: string = defaultTenantId
): Promise<DatasetResponse> {
  const response = await fetch(`${apiBaseUrl}/api/datasets`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-Tenant-Id": tenantId
    },
    body: JSON.stringify({ name })
  });

  return handleResponse(response);
}

export async function listDatasets(
  tenantId: string = defaultTenantId
): Promise<DatasetSummary[]> {
  const response = await fetch(`${apiBaseUrl}/api/datasets`, {
    headers: {
      "X-Tenant-Id": tenantId
    }
  });

  return handleResponse(response);
}

export async function getDataset(
  datasetId: string,
  tenantId: string = defaultTenantId
): Promise<DatasetDetail> {
  const response = await fetch(`${apiBaseUrl}/api/datasets/${datasetId}`, {
    headers: {
      "X-Tenant-Id": tenantId
    }
  });

  return handleResponse(response);
}

export async function uploadDatasetFile(
  datasetId: string,
  file: File,
  tenantId: string = defaultTenantId
): Promise<DatasetFile> {
  const formData = new FormData();
  formData.append("file", file);
  const response = await fetch(`${apiBaseUrl}/api/datasets/${datasetId}/files`, {
    method: "POST",
    headers: {
      "X-Tenant-Id": tenantId
    },
    body: formData
  });

  return handleResponse(response);
}

export async function completeDatasetUpload(
  datasetId: string,
  tenantId: string = defaultTenantId
): Promise<void> {
  const response = await fetch(
    `${apiBaseUrl}/api/datasets/${datasetId}/upload/complete`,
    {
      method: "POST",
      headers: {
        "X-Tenant-Id": tenantId
      }
    }
  );

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "提交上传失败");
  }
}

export async function completeAnnotation(
  datasetId: string,
  tenantId: string = defaultTenantId
): Promise<void> {
  const response = await fetch(
    `${apiBaseUrl}/api/datasets/${datasetId}/annotation/complete`,
    {
      method: "POST",
      headers: {
        "X-Tenant-Id": tenantId
      }
    }
  );

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "标注完成失败");
  }
}

export async function listTrainingEvents(
  tenantId: string = defaultTenantId
): Promise<TrainingEvent[]> {
  const response = await fetch(`${apiBaseUrl}/api/training/events`, {
    headers: {
      "X-Tenant-Id": tenantId
    }
  });

  return handleResponse(response);
}

export async function getSettings(): Promise<Settings> {
  const response = await fetch(`${apiBaseUrl}/api/settings`);

  return handleResponse(response);
}
