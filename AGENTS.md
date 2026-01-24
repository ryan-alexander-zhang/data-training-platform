# AGENTS

## Repository overview
- Monorepo with `frontend/` (Next.js + Shadcn UI + Tailwind) and `backend/` (Spring Boot, DDD modules).
- Multi-tenant API: all backend requests must include `X-Tenant-Id`.
- Label Studio and MinIO are expected in local docker compose.

## Build, lint, test commands

### Frontend (Next.js)
- Install: `cd frontend && npm install`
- Dev server: `cd frontend && npm run dev`
- Production build: `cd frontend && npm run build`
- Start built app: `cd frontend && npm run start`
- Lint: `cd frontend && npm run lint`
- Tests: no test scripts configured in `frontend/package.json`.

### Backend (Maven multi-module)
- Run app: `cd backend && mvn -pl start spring-boot:run`
- Build all modules: `cd backend && mvn clean install`
- Unit tests (all modules): `cd backend && mvn test`
- Single module tests: `cd backend && mvn -pl app test`
- Single test class: `cd backend && mvn -pl app -Dtest=DatasetApplicationServiceTest test`
- Single test method: `cd backend && mvn -pl app -Dtest=DatasetApplicationServiceTest#uploadFile test`
- Skip tests (if needed): `cd backend && mvn -DskipTests clean install`

### Infrastructure
- Local services: `docker compose up -d`
  - Provides PostgreSQL, Kafka, Label Studio, MinIO.

## Code style and conventions

### General
- Prefer small, focused functions and keep business flow in application services.
- Avoid Unicode in identifiers; use ASCII naming.
- Do not add comments unless a non-obvious block needs clarification.

### Frontend (TypeScript + React)

#### Imports
- Use absolute imports with `@/` alias for local modules.
- Order: React/Next imports first, then UI components, then local modules.

#### Formatting
- 2-space indent, trailing commas where existing style uses them.
- JSX props align with existing components and wrap for readability.
- Use double quotes for strings.

#### Components and files
- Default export React components for pages and main UI components.
- File names are kebab-case in `components/` and `app/` routes.
- Place hooks at top-level of component functions.

#### Types
- Prefer explicit types for API responses in `frontend/src/lib/api.ts`.
- Use `type` aliases for API models (see `DatasetDetail`, `DatasetFile`).
- Use `string | null` state for error messages to avoid undefined checks.

#### Naming
- Use `camelCase` for variables and functions.
- Use `PascalCase` for components and types.
- Use verb-based handler names: `handleSubmit`, `handleUpload`.

#### Error handling
- API utilities throw on non-OK responses; UI catches with `try/catch`.
- Map `error instanceof Error ? error.message : "..."` for fallback.
- Surface errors via `MessageDialog` component.

#### Data flow
- Fetch data in `useEffect` and update local state.
- Use `useMemo` for derived display data (e.g., steps/progress).

#### UI
- Use Shadcn UI components in `frontend/src/components/ui`.
- Prefer `Card`, `Badge`, `Button` patterns already in use.

### Backend (Java + Spring Boot)

#### Packages
- DDD modules:
  - `adapter`: REST APIs and webhooks.
  - `app`: application services and orchestration.
  - `domain`: aggregates, entities, domain events.
  - `infra`: persistence, storage, messaging adapters.
  - `start`: Spring Boot entry point.

#### Imports
- Java standard library imports after project imports.
- Avoid wildcard imports.

#### Formatting
- 4-space indent.
- Place annotations on their own lines.
- Use `record` types for API DTOs when immutable (see adapter responses).

#### Naming
- `CamelCase` for classes, `camelCase` for methods/variables.
- Use clear verbs for service methods: `createDataset`, `completeUpload`.
- UUID-based identifiers are wrapped by value objects in domain (`DatasetId`, `TenantId`).

#### Error handling
- Use `IllegalArgumentException` for not-found or invalid state in app services.
- Controllers catch and translate errors only when needed; otherwise allow global handling.

#### API conventions
- Every endpoint expects `X-Tenant-Id` header.
- Controller endpoints in `adapter` call application services in `app`.

#### Storage
- Object storage interactions go through `ObjectStorageService` abstraction.

## API conventions
- Base URL configured via `NEXT_PUBLIC_API_BASE_URL` in frontend.
- Tenant defaults to `NEXT_PUBLIC_TENANT_ID` in frontend API wrapper.
- Label Studio base URL is configurable via `training.label-studio.base-url`.

## Project-specific notes
- README documents target capabilities and docker services.
- Frontend uses `MessageDialog` for error display; follow this pattern.
- Dataset status labels are mapped in UI via `statusLabels` object.

## Cursor/Copilot rules
- No `.cursor/rules/`, `.cursorrules`, or `.github/copilot-instructions.md` found in this repo.
