import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const datasets = [
  {
    id: "ds-2024-001",
    name: "风机叶片缺陷",
    status: "READY_FOR_LABELING",
    assets: 200,
    tenant: "ACME-NORTH",
    updatedAt: "2024-07-20 14:35"
  },
  {
    id: "ds-2024-002",
    name: "焊缝裂纹",
    status: "ANNOTATION_COMPLETED",
    assets: 180,
    tenant: "ACME-NORTH",
    updatedAt: "2024-07-19 09:12"
  }
];

const statusLabels: Record<string, string> = {
  READY_FOR_LABELING: "待标注",
  ANNOTATION_COMPLETED: "标注完成",
  TRAINING_REQUESTED: "训练排队",
  TRAINING_COMPLETED: "训练完成"
};

export default function DatasetsPage() {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold">数据集</h2>
          <p className="text-sm text-muted-foreground">
            支持图片或压缩包上传，自动生成标注项目。
          </p>
        </div>
        <Button>上传新数据集</Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>最近数据集</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {datasets.map((dataset) => (
            <div
              key={dataset.id}
              className="flex flex-col gap-3 rounded-lg border border-border p-4 md:flex-row md:items-center md:justify-between"
            >
              <div>
                <div className="flex items-center gap-3">
                  <Link href={`/datasets/${dataset.id}`} className="font-medium">
                    {dataset.name}
                  </Link>
                  <Badge>{statusLabels[dataset.status]}</Badge>
                </div>
                <p className="text-sm text-muted-foreground">
                  {dataset.assets} 张 · 租户 {dataset.tenant}
                </p>
              </div>
              <div className="text-sm text-muted-foreground">更新时间 {dataset.updatedAt}</div>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}
