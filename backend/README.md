# Backend Modules

此目录提供后端 DDD 分层骨架，核心代码附带详细注释，便于后续扩展。

## 模块职责

- **adapter**：REST API、Webhook、Label Studio 回调适配等。
- **app**：应用服务（用例编排、事务边界）。
- **domain**：领域模型、领域事件、聚合根。
- **infra**：数据库、消息队列、对象存储、第三方适配。
- **start**：Spring Boot 启动模块，负责依赖装配。

## 本地运行（参考）

```bash
cd backend
mvn -pl start spring-boot:run
```

## 多租户约定

- 所有 API 都必须传递 `X-Tenant-Id`。
- 数据库表包含 `tenant_id` 字段，用于行级隔离。
- 领域模型与应用服务以 `tenantId` 作为必填参数。

## 断点续传建议

- 前端选择文件后创建 `upload_session`，后端生成 OSS Multipart Upload 的 `upload_id`。
- 每个分片上传成功后记录 `part_number + etag`。
- 完成上传时调用 `CompleteMultipartUpload`。

## 事件约定

- `TRAINING_REQUESTED`：标注完成 -> 训练开始。
- `TRAINING_COMPLETED`：训练平台完成后回写结果。

事件 payload 结构见 `domain` 模块中的 `TrainingEvent`。
