"use client";

import { useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import DatasetCreateDialog from "@/components/dataset-create-dialog";

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

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold">工作台概览</h2>
          <p className="text-sm text-muted-foreground">
            统一管理数据集、标注与训练任务。
          </p>
        </div>
        <DatasetCreateDialog
          triggerLabel="新建数据集"
          onCreated={(dataset) => router.push(`/datasets/${dataset.id}`)}
        />
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
