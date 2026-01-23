"use client";

import { useEffect, useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import MessageDialog from "@/components/message-dialog";
import { listTrainingEvents, TrainingEvent } from "@/lib/api";

const statusLabels: Record<string, string> = {
  TRAINING_REQUESTED: "已发起",
  TRAINING_COMPLETED: "已完成"
};

export default function TrainingPage() {
  const [events, setEvents] = useState<TrainingEvent[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fetchEvents = async () => {
    setIsLoading(true);
    try {
      const data = await listTrainingEvents();
      setEvents(data);
    } catch (error) {
      const message = error instanceof Error ? error.message : "加载训练事件失败";
      setErrorMessage(message);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-semibold">训练事件</h2>
        <p className="text-sm text-muted-foreground">
          事件由 Kafka 驱动，可用于训练调度与状态回写。
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>最新事件</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {isLoading ? (
            <p className="text-sm text-muted-foreground">正在加载训练事件…</p>
          ) : events.length === 0 ? (
            <p className="text-sm text-muted-foreground">暂无训练事件。</p>
          ) : (
            events.map((event) => (
              <div
                key={event.id}
                className="flex flex-col gap-2 rounded-lg border border-border p-4 md:flex-row md:items-center md:justify-between"
              >
                <div>
                  <p className="font-medium">{event.datasetName}</p>
                  <p className="text-sm text-muted-foreground">{event.id}</p>
                </div>
                <div className="flex items-center gap-3">
                  <Badge>{statusLabels[event.eventType] ?? event.eventType}</Badge>
                  <span className="text-sm text-muted-foreground">
                    {new Date(event.occurredAt).toLocaleString()}
                  </span>
                </div>
              </div>
            ))
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
