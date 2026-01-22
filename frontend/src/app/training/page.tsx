import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const events = [
  {
    id: "evt-789",
    dataset: "风机叶片缺陷",
    status: "TRAINING_REQUESTED",
    timestamp: "2024-07-20 15:02"
  },
  {
    id: "evt-790",
    dataset: "焊缝裂纹",
    status: "TRAINING_COMPLETED",
    timestamp: "2024-07-19 20:14"
  }
];

const statusLabels: Record<string, string> = {
  TRAINING_REQUESTED: "已发起",
  TRAINING_COMPLETED: "已完成"
};

export default function TrainingPage() {
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
          {events.map((event) => (
            <div
              key={event.id}
              className="flex flex-col gap-2 rounded-lg border border-border p-4 md:flex-row md:items-center md:justify-between"
            >
              <div>
                <p className="font-medium">{event.dataset}</p>
                <p className="text-sm text-muted-foreground">{event.id}</p>
              </div>
              <div className="flex items-center gap-3">
                <Badge>{statusLabels[event.status]}</Badge>
                <span className="text-sm text-muted-foreground">{event.timestamp}</span>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}
