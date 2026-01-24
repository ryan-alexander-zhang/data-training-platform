"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import MessageDialog from "@/components/message-dialog";
import { createDataset, DatasetResponse } from "@/lib/api";

interface DatasetCreateDialogProps {
  triggerLabel: string;
  onCreated?: (dataset: DatasetResponse) => void;
}

export default function DatasetCreateDialog({
  triggerLabel,
  onCreated
}: DatasetCreateDialogProps) {
  const [open, setOpen] = useState(false);
  const [name, setName] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = async () => {
    if (!name.trim()) {
      setErrorMessage("请输入数据集名称");
      return;
    }

    setIsSubmitting(true);
    try {
      const dataset = await createDataset(name.trim());
      setOpen(false);
      setName("");
      onCreated?.(dataset);
    } catch (error) {
      const message =
        error instanceof Error ? error.message : "创建数据集失败";
      setErrorMessage(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogTrigger asChild>
          <Button disabled={isSubmitting}>{triggerLabel}</Button>
        </DialogTrigger>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>新建数据集</DialogTitle>
            <DialogDescription>
              输入名称后即可创建数据集并进入上传流程。
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-2">
            <Label htmlFor="dataset-name">数据集名称</Label>
            <Input
              id="dataset-name"
              placeholder="例如：风机叶片缺陷"
              value={name}
              onChange={(event) => setName(event.target.value)}
            />
          </div>
          <DialogFooter>
            <Button
              variant="secondary"
              onClick={() => setOpen(false)}
              disabled={isSubmitting}
            >
              取消
            </Button>
            <Button onClick={handleSubmit} disabled={isSubmitting}>
              创建并上传
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      {errorMessage ? (
        <MessageDialog
          open
          message={errorMessage}
          onOpenChange={(nextOpen) => {
            if (!nextOpen) {
              setErrorMessage(null);
            }
          }}
        />
      ) : null}
    </>
  );
}
