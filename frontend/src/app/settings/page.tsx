"use client";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function SettingsPage() {
  const handlePlaceholder = (message: string) => () => {
    window.alert(message);
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold">租户设置</h2>
        <p className="text-sm text-muted-foreground">
          配置 OSS、Label Studio 与训练事件相关参数。
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>对象存储</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div>
            <p className="text-sm text-muted-foreground">Bucket</p>
            <p className="font-medium">training-results</p>
          </div>
          <div>
            <p className="text-sm text-muted-foreground">STS 角色</p>
            <p className="font-medium">oss-upload-role</p>
          </div>
          <Button
            variant="outline"
            onClick={handlePlaceholder("对象存储配置更新功能开发中")}
          >
            更新配置
          </Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Label Studio</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div>
            <p className="text-sm text-muted-foreground">Base URL</p>
            <p className="font-medium">http://localhost:8080</p>
          </div>
          <div>
            <p className="text-sm text-muted-foreground">Webhook</p>
            <p className="font-medium">/api/label-studio/webhook</p>
          </div>
          <Button
            variant="outline"
            onClick={handlePlaceholder("Label Studio 回调配置功能开发中")}
          >
            配置回调
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
