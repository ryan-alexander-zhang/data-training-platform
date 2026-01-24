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
  completeUploadSession,
  createUploadSession,
  apiFetch,
  DatasetDetail,
  fetchAnnotations,
  getApiBaseUrl,
  getDataset,
  getTenantId,
  getUploadSession,
  uploadPart
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
  const tenantId = getTenantId();

  const downloadResult = async (path: string, fallbackFilename: string) => {
    try {
      const response = await apiFetch(`${apiBaseUrl}${path}`, {}, tenantId);
      if (!response.ok) {
        throw new Error("下载失败");
      }
      const blob = await response.blob();
      const disposition = response.headers.get("content-disposition");
      const match = disposition?.match(/filename="(.+)"/);
      const filename = match?.[1] ?? fallbackFilename;
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      const message = error instanceof Error ? error.message : "下载失败";
      setErrorMessage(message);
    }
  };
  const [dataset, setDataset] = useState<DatasetDetail | null>(null);
  const [selectedFiles, setSelectedFiles] = useState<FileList | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [uploadProgress, setUploadProgress] = useState<Record<string, number>>({});
  const [isUploading, setIsUploading] = useState(false);
  const [annotationJson, setAnnotationJson] = useState<string | null>(null);
  const [isFetchingAnnotations, setIsFetchingAnnotations] = useState(false);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

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
      const isCompleted = dataset.status === "TRAINING_COMPLETED";
      return {
        title: step.title,
        status: isCompleted
          ? stepIndex <= statusIndex
            ? "完成"
            : "待开始"
          : stepIndex < statusIndex
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
    setIsUploading(true);
    try {
      for (const file of Array.from(selectedFiles)) {
        await uploadWithResume(file);
      }
      setSelectedFiles(null);
      await loadDataset();
    } catch (error) {
      const message = error instanceof Error ? error.message : "上传文件失败";
      setErrorMessage(message);
    } finally {
      setIsSubmitting(false);
      setIsUploading(false);
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

  const handleFetchAnnotations = async () => {
    setIsFetchingAnnotations(true);
    try {
      const response = await fetchAnnotations(params.id);
      setAnnotationJson(response.payload);
    } catch (error) {
      const message = error instanceof Error ? error.message : "加载标注失败";
      setErrorMessage(message);
    } finally {
      setIsFetchingAnnotations(false);
    }
  };

  const uploadWithResume = async (file: File) => {
    const relativePath = "webkitRelativePath" in file ? file.webkitRelativePath : "";
    const cacheKey = `upload:${params.id}:${file.name}:${file.size}:${file.lastModified}:${file.type}:${relativePath}`;
    const existing = localStorage.getItem(cacheKey);
    let uploadId: string | null = null;
    let partSize = 0;
    let uploadedParts: Record<number, string> = {};

    if (existing) {
      const cached = JSON.parse(existing) as {
        uploadId: string;
        partSize: number;
        parts: Record<number, string>;
      };
      uploadId = cached.uploadId;
      partSize = cached.partSize;
      uploadedParts = cached.parts;
    }

    if (!uploadId) {
      const session = await createUploadSession(params.id, file);
      uploadId = session.uploadId;
      partSize = session.partSize;
      uploadedParts = {};
    } else {
      const sessionDetail = await getUploadSession(params.id, uploadId);
      partSize = sessionDetail.partSize;
      uploadedParts = sessionDetail.parts.reduce<Record<number, string>>((acc, part) => {
        acc[part.partNumber] = part.etag;
        return acc;
      }, {});
    }

    const totalParts = Math.ceil(file.size / partSize);
    for (let partNumber = 1; partNumber <= totalParts; partNumber += 1) {
      if (uploadedParts[partNumber]) {
        const progress = Math.round((partNumber / totalParts) * 100);
        setUploadProgress((prev) => ({ ...prev, [file.name]: progress }));
        continue;
      }
      const start = (partNumber - 1) * partSize;
      const end = Math.min(file.size, start + partSize);
      const chunk = file.slice(start, end);
      const part = await uploadPart(params.id, uploadId, partNumber, chunk);
      uploadedParts[part.partNumber] = part.etag;
      localStorage.setItem(
        cacheKey,
        JSON.stringify({ uploadId, partSize, parts: uploadedParts })
      );
      const progress = Math.round((partNumber / totalParts) * 100);
      setUploadProgress((prev) => ({ ...prev, [file.name]: progress }));
    }

    const partsPayload = Object.entries(uploadedParts)
      .map(([partNumber, etag]) => ({ partNumber: Number(partNumber), etag }))
      .sort((a, b) => a.partNumber - b.partNumber);
    await completeUploadSession(params.id, uploadId, partsPayload);
    localStorage.removeItem(cacheKey);
  };

  const currentFile = dataset?.files[currentImageIndex];

  useEffect(() => {
    let isActive = true;
    const loadPreview = async () => {
      if (!dataset || !currentFile) {
        setPreviewUrl(null);
        return;
      }
      try {
        const response = await apiFetch(
          `${apiBaseUrl}/api/datasets/${dataset.id}/files/${currentFile.id}/preview`,
          {},
          tenantId
        );
        const blob = await response.blob();
        if (!isActive) {
          return;
        }
        setPreviewUrl((prev) => {
          if (prev) {
            URL.revokeObjectURL(prev);
          }
          return URL.createObjectURL(blob);
        });
      } catch (error) {
        if (isActive) {
          setPreviewUrl(null);
        }
      }
    };

    loadPreview();

    return () => {
      isActive = false;
    };
  }, [apiBaseUrl, currentFile, dataset, tenantId]);

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
            {isUploading ? "上传中…" : "上传选中文件"}
          </Button>
          {selectedFiles && selectedFiles.length > 0 ? (
            <div className="space-y-2">
              <p className="text-sm font-medium">上传进度</p>
              <div className="space-y-2">
                {Array.from(selectedFiles).map((file) => (
                  <div
                    key={file.name}
                    className="rounded-md border border-border p-3 text-sm"
                  >
                    <div className="flex items-center justify-between">
                      <p className="font-medium">{file.name}</p>
                      <span className="text-xs text-muted-foreground">
                        {uploadProgress[file.name] ?? 0}%
                      </span>
                    </div>
                    <div className="mt-2 h-2 w-full rounded-full bg-muted">
                      <div
                        className="h-2 rounded-full bg-primary transition-all"
                        style={{ width: `${uploadProgress[file.name] ?? 0}%` }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ) : null}
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
                    <Badge className="bg-muted text-muted-foreground">已上传</Badge>
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
          {dataset.labelingUrl ? (
            <Button onClick={() => window.open(dataset.labelingUrl ?? "", "_blank")}>
              进入标注
            </Button>
          ) : (
            <Button variant="secondary" disabled>
              暂无标注项目
            </Button>
          )}
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
                <Button
                  variant="secondary"
                  onClick={() =>
                    downloadResult(
                      `/api/datasets/${dataset.id}/results/metrics`,
                      "metrics.json"
                    )
                  }
                >
                  下载指标
                </Button>
                <Button
                  onClick={() =>
                    downloadResult(
                      `/api/datasets/${dataset.id}/results/model`,
                      "model.bin"
                    )
                  }
                >
                  下载模型
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

      <Card>
        <CardHeader>
          <CardTitle>图片浏览</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {dataset.files.length > 0 ? (
            <div className="space-y-4">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <p className="font-medium">{currentFile?.filename}</p>
                  <p className="text-xs text-muted-foreground">
                    {currentImageIndex + 1} / {dataset.files.length}
                  </p>
                </div>
                <div className="flex gap-2">
                  <Button
                    variant="secondary"
                    onClick={() =>
                      setCurrentImageIndex((prev) => Math.max(prev - 1, 0))
                    }
                    disabled={currentImageIndex === 0}
                  >
                    上一张
                  </Button>
                  <Button
                    variant="secondary"
                    onClick={() =>
                      setCurrentImageIndex((prev) =>
                        Math.min(prev + 1, dataset.files.length - 1)
                      )
                    }
                    disabled={currentImageIndex === dataset.files.length - 1}
                  >
                    下一张
                  </Button>
                </div>
              </div>
              {currentFile ? (
                <div className="overflow-hidden rounded-lg border border-border">
                  {previewUrl ? (
                    <img
                      src={previewUrl}
                      alt={currentFile.filename}
                      className="h-auto w-full object-contain"
                    />
                  ) : (
                    <div className="flex h-64 items-center justify-center text-sm text-muted-foreground">
                      无法加载预览
                    </div>
                  )}
                </div>
              ) : null}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">暂无图片可预览。</p>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>已标注数据</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p className="font-medium">标注导出 JSON</p>
              <p className="text-sm text-muted-foreground">
                查看已标注的任务与结果。
              </p>
            </div>
            <Button
              variant="secondary"
              onClick={handleFetchAnnotations}
              disabled={isFetchingAnnotations}
            >
              {isFetchingAnnotations ? "加载中…" : "加载标注"}
            </Button>
          </div>
          {annotationJson ? (
            <pre className="max-h-80 overflow-auto rounded-md bg-muted p-4 text-xs">
              {annotationJson}
            </pre>
          ) : (
            <p className="text-sm text-muted-foreground">暂无标注结果。</p>
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
