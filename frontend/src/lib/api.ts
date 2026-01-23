export type DatasetResponse = {
  id: string;
  tenantId: string;
  name: string;
  status: string;
  createdAt: string;
};

const apiBaseUrl =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8081";
const defaultTenantId =
  process.env.NEXT_PUBLIC_TENANT_ID ?? "00000000-0000-0000-0000-000000000001";

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

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "创建数据集失败");
  }

  return response.json();
}
