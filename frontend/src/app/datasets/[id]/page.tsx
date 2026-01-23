"use client";

import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const steps = [
  {
    title: "创建数据集",
    description: "录入基础信息并生成上传会话。",
    status: "完成"
  },
  {
    title: "断点续传",
    description: "支持多文件与压缩包上传，支持 OSS 分片直传。",
    status: "进行中"
  },
  {
    title: "标注",
    description: "跳转 Label Studio 完成缺陷标注。",
    status: "待开始"
  },
  {
    title: "训练",
    description: "标注完成后自动触发训练事件。",
    status: "待开始"
  }
];

export default function DatasetDetailPage({ params }: { params: { id: string } }) {
  const handlePlaceholder = (message: string) => () => {
    window.alert(message);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold">数据集 {params.id}</h2>
          <p className="text-sm text-muted-foreground">
            200 张图片 · 状态：上传中
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button
            variant="outline"
            onClick={handlePlaceholder("OSS 上传凭证功能开发中")}
          >
            获取 OSS 上传凭证
          </Button>
          <Button onClick={handlePlaceholder("上传入口功能开发中")}>
            继续上传
          </Button>
        </div>
      </div>

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
                <p className="text-sm text-muted-foreground">{step.description}</p>
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
            <p className="font-medium">Label Studio Project #128</p>
            <p className="text-sm text-muted-foreground">
              标注类型：缺陷检测，上传图片完成后即可开始。
            </p>
          </div>
          <Link href="/datasets/labeling">
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
                训练完成后将模型与指标上传至 OSS。
              </p>
            </div>
            <Button
              variant="outline"
              onClick={handlePlaceholder("事件日志功能开发中")}
            >
              查看事件日志
            </Button>
          </div>
          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium">训练结果文件</p>
              <p className="text-sm text-muted-foreground">暂无结果</p>
            </div>
            <Button
              variant="secondary"
              onClick={handlePlaceholder("训练结果下载功能开发中")}
            >
              下载结果
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
