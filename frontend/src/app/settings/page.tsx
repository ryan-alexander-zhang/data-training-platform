"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import ConfirmDialogButton from "@/components/confirm-dialog-button";

export default function SettingsPage() {
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
          <ConfirmDialogButton
            triggerLabel="更新配置"
            title="对象存储配置"
            description="对象存储配置更新功能开发中。"
          />
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
          <ConfirmDialogButton
            triggerLabel="配置回调"
            title="Label Studio 回调"
            description="Label Studio 回调配置功能开发中。"
          />
        </CardContent>
      </Card>
    </div>
  );
}
