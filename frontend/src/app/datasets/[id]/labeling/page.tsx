"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import MessageDialog from "@/components/message-dialog";
import { DatasetDetail, getDataset } from "@/lib/api";

export default function LabelingPage({ params }: { params: { id: string } }) {
  const [dataset, setDataset] = useState<DatasetDetail | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const fetchDataset = async () => {
      try {
        const data = await getDataset(params.id);
        setDataset(data);
      } catch (error) {
        const message = error instanceof Error ? error.message : "加载标注信息失败";
        setErrorMessage(message);
      }
    };

    fetchDataset();
  }, [params.id]);

  const handleOpenLabelStudio = () => {
    if (dataset?.labelingUrl) {
      window.open(dataset.labelingUrl, "_blank");
    }
  };

  if (!dataset) {
    return (
      <div className="space-y-6">
        <p className="text-sm text-muted-foreground">正在加载标注信息…</p>
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

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold">Label Studio 跳转</h2>
        <p className="text-sm text-muted-foreground">
          通过后端生成的 Project URL 进行访问。
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>标注信息</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="label-studio-url">项目地址</Label>
            <Input id="label-studio-url" value={dataset.labelingUrl ?? ""} readOnly />
          </div>
          <div>
            <p className="font-medium">说明</p>
            <ul className="list-disc space-y-1 pl-5 text-sm text-muted-foreground">
              <li>标注完成后触发训练事件。</li>
              <li>结果文件将回写至 MinIO 并在平台可下载。</li>
            </ul>
          </div>
          <Button onClick={handleOpenLabelStudio}>打开 Label Studio</Button>
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
