"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { createDataset } from "@/lib/api";

const quickActions = [
  {
    title: "创建数据集",
    description: "上传 200 张以内图片或压缩包，自动创建标注任务。"
  },
  {
    title: "进入标注",
    description: "跳转至 Label Studio，完成缺陷标注。"
  },
  {
    title: "查看训练事件",
    description: "追踪训练请求与完成情况。"
  }
];

export default function DashboardPage() {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleCreateDataset = async () => {
    const name = window.prompt("请输入数据集名称");
    if (!name) {
      return;
    }

    setIsSubmitting(true);
    try {
      const dataset = await createDataset(name);
      router.push(`/datasets/${dataset.id}`);
    } catch (error) {
      const message =
        error instanceof Error ? error.message : "创建数据集失败";
      window.alert(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold">工作台概览</h2>
          <p className="text-sm text-muted-foreground">
            统一管理数据集、标注与训练任务。
          </p>
        </div>
        <Button onClick={handleCreateDataset} disabled={isSubmitting}>
          新建数据集
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        {quickActions.map((item) => (
          <Card key={item.title}>
            <CardHeader>
              <CardTitle>{item.title}</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-sm text-muted-foreground">{item.description}</p>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
