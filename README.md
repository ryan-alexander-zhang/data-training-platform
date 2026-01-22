# Data Training Platform

面向多租户的数据集管理与训练编排平台。核心目标是：支持海量图片上传（断点续传）、在线标注（Label Studio）、训练事件编排、训练结果回传与下载，并满足生产级的工程约束。

## 目标能力

- **海量数据上传**：单次 200 张图片、总量约 5GB，支持分片上传与断点续传。
- **标注集成**：通过 **Label Studio** 进行缺陷标注，并从平台跳转到标注页面。
- **训练编排**：标注完成后发出训练事件，训练完成后回写结果并通知应用。
- **多租户**：全链路包含 `tenant_id`，确保数据隔离、审计与访问控制。

## 技术栈选择（已对齐）

**前端**
- React + TypeScript
- Next.js + Shadcn UI + Tailwind CSS + lucide-react

**后端**
- Java + Spring Boot
- PostgreSQL + MyBatis-Plus + Flyway
- Kafka 事件编排
- DDD 分层：`adapter`、`app`、`domain`、`infra`、`start`

> 说明：当前仓库提供了**后端骨架 + 核心流程注释 + 数据库设计 + Docker Compose**，用于快速启动开发和展示整体方案。前端与训练平台可以在此基础上继续扩展。

## 架构概览

```
┌────────────────────┐        ┌───────────────────────────┐
│   Web / Console     │        │ Label Studio (标注)        │
│ Next.js + Shadcn UI │◀──────▶│ 通过 Project 绑定数据集     │
└─────────┬──────────┘        └────────────┬──────────────┘
          │                                   │
          │ REST / Webhook                    │
          ▼                                   ▼
┌────────────────────────────┐     ┌────────────────────────┐
│ Spring Boot (DDD)           │     │   Training Worker       │
│ adapter/app/domain/infra    │◀───▶│ 监听 Kafka 训练事件      │
│ - 断点续传策略               │     │ 训练结果写回 OSS         │
│ - 事件发布 (Kafka)           │     └────────────────────────┘
│ - 多租户隔离                 │
└───────────┬────────────────┘
            │
            ▼
┌────────────────────────────┐
│  OSS / S3 (Aliyun OSS)       │
│  - 原始图片 / 标注结果 / 模型 │
└────────────────────────────┘
```

## 核心流程

1. 用户创建数据集（含 `tenant_id`）。
2. 前端发起分片上传（断点续传），服务端返回 OSS 直传凭证（推荐）或走后端中转。
3. 数据上传完成后，创建 Label Studio Project，并建立 dataset ↔ project 映射。
4. 标注完成触发 `ANNOTATION_COMPLETED` 事件，平台发送 `TRAINING_REQUESTED`。
5. 训练平台监听事件完成训练，并回写结果文件到 OSS，发布 `TRAINING_COMPLETED`。
6. 应用页面下载训练结果。

## 数据库设计（摘要）

- **UUIDv7** 作为主键，保证分布式排序与时间友好性。
- 多租户字段 `tenant_id` 贯穿所有实体。
- 关键表：`tenants`、`datasets`、`dataset_assets`、`label_projects`、`training_jobs`。

详见 Flyway：`backend/infra/src/main/resources/db/migration/V1__init.sql`。

## 本地开发（Docker Compose）

本仓库提供 `docker-compose.yml`，包含：
- PostgreSQL 16
- Kafka (KRaft)
- Label Studio
- MinIO (本地模拟 OSS)

### 启动

```bash
docker compose up -d
```

### 关键服务

| 服务 | 地址 | 说明 |
| --- | --- | --- |
| PostgreSQL | `localhost:5432` | DB: `training_platform` |
| Kafka | `localhost:9092` | 事件总线 |
| Label Studio | `localhost:8080` | 标注系统 |
| MinIO | `localhost:9000` | S3 兼容，模拟 OSS |
| MinIO Console | `localhost:9001` | 控制台 |

## 后端工程结构

```
backend/
├── adapter/         # REST API / Webhook / 外部适配
├── app/             # 应用服务与用例编排
├── domain/          # 领域模型、事件、聚合
├── infra/           # DB/消息/对象存储适配
└── start/           # Spring Boot 启动层
```

详见：`backend/README.md`。

## 前端工程结构

```
frontend/
├── src/
│   ├── app/                # Next.js App Router 页面
│   ├── components/         # 复用组件（含基础 UI）
│   └── lib/                # 工具方法与类型
├── tailwind.config.ts
└── package.json
```

### 前端本地运行

```bash
cd frontend
npm install
npm run dev
```

## 生产部署建议

- **对象存储**：建议使用阿里云 OSS，前端通过 STS 或表单直传减少后端带宽。
- **断点续传**：建议使用 OSS 分片上传（Multipart Upload），并保存 `upload_id` 与分片索引。
- **Label Studio**：使用外部托管实例；应用侧维护 `project_id` 与 `dataset_id` 的映射。
- **消息总线**：Kafka 作为事件中心，训练平台以消费者身份消费事件。
- **多租户**：通过 `tenant_id` 做数据库隔离（行级隔离或 schema 隔离）。

## 下一步建议

- 前端实现：基于 Next.js + Shadcn UI 的数据集管理台。
- 完整训练平台：消费 Kafka 事件，回写训练模型与指标。
- 权限体系：接入 OAuth2 或自建权限系统。
