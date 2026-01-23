"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function LabelingPage() {
  const handleOpenLabelStudio = () => {
    window.open("http://localhost:8080/projects/128", "_blank");
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold">Label Studio 跳转</h2>
        <p className="text-sm text-muted-foreground">
          通过后端生成的 Project URL 进行单点登录或临时授权访问。
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>标注信息</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <p className="font-medium">项目地址</p>
            <p className="text-sm text-muted-foreground">
              http://localhost:8080/projects/128
            </p>
          </div>
          <div>
            <p className="font-medium">说明</p>
            <ul className="list-disc space-y-1 pl-5 text-sm text-muted-foreground">
              <li>标注完成后触发训练事件。</li>
              <li>结果文件将回写至 OSS 并在平台可下载。</li>
            </ul>
          </div>
          <Button onClick={handleOpenLabelStudio}>打开 Label Studio</Button>
        </CardContent>
      </Card>
    </div>
  );
}
