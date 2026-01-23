"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import MessageDialog from "@/components/message-dialog";
import { getSettings, Settings } from "@/lib/api";

export default function SettingsPage() {
  const [settings, setSettings] = useState<Settings | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const fetchSettings = async () => {
      try {
        const data = await getSettings();
        setSettings(data);
      } catch (error) {
        const message = error instanceof Error ? error.message : "加载配置失败";
        setErrorMessage(message);
      }
    };

    fetchSettings();
  }, []);

  if (!settings) {
    return (
      <div className="space-y-6">
        <p className="text-sm text-muted-foreground">正在加载配置…</p>
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
        <h2 className="text-2xl font-semibold">租户设置</h2>
        <p className="text-sm text-muted-foreground">
          配置对象存储、Label Studio 与训练事件相关参数。
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>对象存储</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="minio-endpoint">Endpoint</Label>
            <Input id="minio-endpoint" value={settings.minioEndpoint} readOnly />
          </div>
          <div className="space-y-2">
            <Label htmlFor="minio-bucket">Bucket</Label>
            <Input id="minio-bucket" value={settings.minioBucket} readOnly />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Label Studio</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="label-studio-base">Base URL</Label>
            <Input id="label-studio-base" value={settings.labelStudioBaseUrl} readOnly />
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
