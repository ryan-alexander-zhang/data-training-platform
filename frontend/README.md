# Frontend

该前端基于 Next.js + Shadcn UI 风格组件，覆盖核心业务流程的页面与交互，方便后续对接后端 API。

## 页面与流程

- **概览**：平台入口，展示快捷动作。
- **数据集列表**：查看数据集状态，触发上传。
- **数据集详情**：断点续传、标注入口、训练事件/结果下载入口。
- **标注入口**：跳转到 Label Studio 项目。
- **训练事件**：查看 Kafka 事件状态。
- **租户设置**：配置 OSS、Label Studio 与训练参数。

## 本地运行

```bash
npm install
npm run dev
```

## 接入后端建议

- 使用 `X-Tenant-Id` 作为多租户标识。
- 上传流程建议调用：
  1. `POST /api/datasets` 创建数据集
  2. `POST /api/uploads` 获取 OSS Multipart Upload 凭证
  3. 完成上传后通知 `POST /api/datasets/{id}/annotation/complete`
