"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import MessageDialog from "@/components/message-dialog";
import DatasetCreateDialog from "@/components/dataset-create-dialog";
import { listDatasets, DatasetSummary } from "@/lib/api";

const statusLabels: Record<string, string> = {
  CREATED: "已创建",
  UPLOADING: "上传中",
  READY_FOR_LABELING: "待标注",
  ANNOTATION_COMPLETED: "标注完成",
  TRAINING_REQUESTED: "训练排队",
  TRAINING_COMPLETED: "训练完成"
};

export default function DatasetsPage() {
  const [datasets, setDatasets] = useState<DatasetSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fetchDatasets = async () => {
    setIsLoading(true);
    try {
      const data = await listDatasets();
      setDatasets(data);
    } catch (error) {
      const message = error instanceof Error ? error.message : "加载数据集失败";
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDatasets();
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold">数据集</h2>
          <p className="text-sm text-muted-foreground">
            支持图片或压缩包上传，自动生成标注项目。
          </p>
        </div>
        <DatasetCreateDialog triggerLabel="上传新数据集" onCreated={fetchDatasets} />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>最近数据集</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {isLoading ? (
            <p className="text-sm text-muted-foreground">正在加载数据集…</p>
          ) : datasets.length === 0 ? (
            <p className="text-sm text-muted-foreground">暂无数据集，请先创建。</p>
          ) : (
            datasets.map((dataset) => (
              <div
                key={dataset.id}
                className="flex flex-col gap-3 rounded-lg border border-border p-4 md:flex-row md:items-center md:justify-between"
              >
                <div>
                  <div className="flex items-center gap-3">
                    <Link href={`/datasets/${dataset.id}`} className="font-medium">
                      {dataset.name}
                    </Link>
                    <Badge>{statusLabels[dataset.status] ?? dataset.status}</Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">
                    {dataset.assetCount} 张 · 最近更新 {new Date(dataset.updatedAt).toLocaleString()}
                  </p>
                </div>
                <div className="text-sm text-muted-foreground">
                  创建于 {new Date(dataset.createdAt).toLocaleString()}
                </div>
              </div>
            ))
          )}
        </CardContent>
      </Card>
      {errorMessage ? (
        <MessageDialog
          open
          message={errorMessage}
          onOpenChange={(open) => {
            if (!open) {
              setErrorMessage(null);
            }
          }}
        />
      ) : null}
    </div>
  );
}
