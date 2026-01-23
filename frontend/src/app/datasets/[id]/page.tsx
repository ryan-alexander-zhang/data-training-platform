"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import MessageDialog from "@/components/message-dialog";
import {
  completeAnnotation,
  completeDatasetUpload,
  DatasetDetail,
  getApiBaseUrl,
  getDataset,
  uploadDatasetFile
} from "@/lib/api";

const statusLabels: Record<string, string> = {
  CREATED: "已创建",
  UPLOADING: "上传中",
  READY_FOR_LABELING: "待标注",
  ANNOTATION_COMPLETED: "标注完成",
  TRAINING_REQUESTED: "训练排队",
  TRAINING_COMPLETED: "训练完成"
};

const stepConfig = [
  { key: "CREATED", title: "创建数据集" },
  { key: "UPLOADING", title: "上传文件" },
  { key: "READY_FOR_LABELING", title: "准备标注" },
  { key: "ANNOTATION_COMPLETED", title: "标注完成" },
  { key: "TRAINING_REQUESTED", title: "训练排队" },
  { key: "TRAINING_COMPLETED", title: "训练完成" }
];

function formatSize(size: number) {
  if (size >= 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(2)} MB`;
  }
  if (size >= 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }
  return `${size} B`;
}

export default function DatasetDetailPage({ params }: { params: { id: string } }) {
  const apiBaseUrl = getApiBaseUrl();
  const [dataset, setDataset] = useState<DatasetDetail | null>(null);
  const [selectedFiles, setSelectedFiles] = useState<FileList | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const loadDataset = async () => {
    try {
      const data = await getDataset(params.id);
      setDataset(data);
    } catch (error) {
      const message = error instanceof Error ? error.message : "加载数据集失败";
      setErrorMessage(message);
    }
  };

  useEffect(() => {
    loadDataset();
  }, [params.id]);

  const steps = useMemo(() => {
    if (!dataset) {
      return [];
    }
    return stepConfig.map((step) => {
      const statusIndex = stepConfig.findIndex((item) => item.key === dataset.status);
      const stepIndex = stepConfig.findIndex((item) => item.key === step.key);
      return {
        title: step.title,
        status:
          stepIndex < statusIndex
            ? "完成"
            : stepIndex === statusIndex
              ? "进行中"
              : "待开始"
      };
    });
  }, [dataset]);

  const handleUpload = async () => {
    if (!selectedFiles || selectedFiles.length === 0) {
      setErrorMessage("请先选择要上传的文件");
      return;
    }
    setIsSubmitting(true);
    try {
      await Promise.all(
        Array.from(selectedFiles).map((file) => uploadDatasetFile(params.id, file))
      );
      setSelectedFiles(null);
      await loadDataset();
    } catch (error) {
      const message = error instanceof Error ? error.message : "上传文件失败";
      setErrorMessage(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCompleteUpload = async () => {
    setIsSubmitting(true);
    try {
      await completeDatasetUpload(params.id);
      await loadDataset();
    } catch (error) {
      const message = error instanceof Error ? error.message : "提交上传失败";
      setErrorMessage(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCompleteAnnotation = async () => {
    setIsSubmitting(true);
    try {
      await completeAnnotation(params.id);
      await loadDataset();
    } catch (error) {
      const message = error instanceof Error ? error.message : "标注完成失败";
      setErrorMessage(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!dataset) {
    return (
      <div className="space-y-6">
        <p className="text-sm text-muted-foreground">正在加载数据集详情…</p>
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
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold">数据集 {dataset.name}</h2>
          <p className="text-sm text-muted-foreground">
            {dataset.assetCount} 张图片 · 状态：{statusLabels[dataset.status] ?? dataset.status}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button
            variant="secondary"
            onClick={handleCompleteUpload}
            disabled={isSubmitting}
          >
            完成上传
          </Button>
          <Button onClick={handleCompleteAnnotation} disabled={isSubmitting}>
            标注完成
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>上传文件</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="dataset-files">选择图片或压缩包</Label>
            <Input
              id="dataset-files"
              type="file"
              multiple
              onChange={(event) => setSelectedFiles(event.target.files)}
            />
          </div>
          <Button onClick={handleUpload} disabled={isSubmitting}>
            上传选中文件
          </Button>
          {dataset.files.length > 0 ? (
            <div className="space-y-2">
              <p className="text-sm font-medium">已上传文件</p>
              <div className="space-y-2">
                {dataset.files.map((file) => (
                  <div
                    key={file.id}
                    className="flex flex-col gap-1 rounded-md border border-border p-3 text-sm md:flex-row md:items-center md:justify-between"
                  >
                    <div>
                      <p className="font-medium">{file.filename}</p>
                      <p className="text-xs text-muted-foreground">
                        {formatSize(file.size)} · {new Date(file.uploadedAt).toLocaleString()}
                      </p>
                    </div>
                    <Badge variant="secondary">已上传</Badge>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">暂无文件。</p>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>流程进度</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {steps.map((step) => (
            <div
              key={step.title}
              className="flex flex-col gap-2 rounded-lg border border-border p-4 md:flex-row md:items-center md:justify-between"
            >
              <div>
                <p className="font-medium">{step.title}</p>
                <p className="text-sm text-muted-foreground">
                  状态：{step.status}
                </p>
              </div>
              <Badge>{step.status}</Badge>
            </div>
          ))}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>标注入口</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <p className="font-medium">Label Studio Project</p>
            <p className="text-sm text-muted-foreground">
              上传完成后即可进入标注。
            </p>
          </div>
          <Link href={`/datasets/${dataset.id}/labeling`}>
            <Button>进入标注</Button>
          </Link>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>训练与结果</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium">训练事件</p>
              <p className="text-sm text-muted-foreground">
                标注完成后自动触发训练请求。
              </p>
            </div>
            <Link href="/training">
              <Button variant="secondary">查看训练事件</Button>
            </Link>
          </div>
          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium">训练结果文件</p>
              <p className="text-sm text-muted-foreground">
                {dataset.trainingResult
                  ? "训练完成，可下载模型与指标。"
                  : "暂无结果"}
              </p>
            </div>
            {dataset.trainingResult ? (
              <div className="flex gap-2">
                <Button asChild variant="secondary">
                  <a href={`${apiBaseUrl}/api/datasets/${dataset.id}/results/metrics`}>
                    下载指标
                  </a>
                </Button>
                <Button asChild>
                  <a href={`${apiBaseUrl}/api/datasets/${dataset.id}/results/model`}>
                    下载模型
                  </a>
                </Button>
              </div>
            ) : (
              <Button variant="secondary" disabled>
                下载结果
              </Button>
            )}
          </div>
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
