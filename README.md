# 制造业质量追溯 API 服务

面向小型制造工厂的批次质量追溯后端服务，覆盖工单、批次、检验项、不良记录和追溯查询。

## 快速启动

```bash
cp .env.example .env && docker compose up -d
```

## 访问地址或 CLI 示例

后端健康检查：<http://localhost:21114/health>

批次谱系（拆分/合并/追溯）：

```bash
# 拆分：父批 B2026-0001（100 件）拆成两个子批，子批合计必须等于父批数量
curl -X POST http://localhost:21114/api/batch-genealogy/split \
  -H 'Content-Type: application/json' \
  -d '{"bizNo":"SPLIT-001","parentBatchNo":"B2026-0001","parentQuantity":100,
       "children":[{"batchNo":"B2026-0001-A","quantity":60},{"batchNo":"B2026-0001-B","quantity":40}]}'

# 合并：多个来源批合并，合并量必须等于各来源之和
curl -X POST http://localhost:21114/api/batch-genealogy/merge \
  -H 'Content-Type: application/json' \
  -d '{"bizNo":"MERGE-001","targetBatchNo":"R2026-001","targetQuantity":150,
       "sources":[{"batchNo":"B2026-0002","quantity":100},{"batchNo":"B2026-0003","quantity":50}]}'

# 追溯：任一产品批号 -> 上游工单、原料批、下游批次
curl http://localhost:21114/api/batch-genealogy/trace/B2026-0001

# 谱系登记清单
curl http://localhost:21114/api/batch-genealogy
```

规则：数量不符（子批合计≠父批、合并量≠来源之和）或来源绕回原批（成环）即拒绝并返回 400 + `errorCode`；同一 `bizNo` 业务单重复到达时返回首次登记结果，不重复入账。


## 本地开发方式


- 后端：进入 `backend` 后按技术栈运行开发命令，接口统一挂在 `/api`。


## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | - |
| 后端 | Spring Boot 3 + Java 17 + MyBatis-Plus |
| 数据库 | PostgreSQL 15 |
| 部署 | Docker Compose |

## 项目目录结构

```text

backend/src/routes, controllers, services, models, repositories, middlewares, constants, constructors, validators, utils, types, config
```

## 环境变量说明

- `COMPOSE_PROJECT_NAME`: Compose 项目名，默认 `quality-trace`

- `BACKEND_PORT`: 后端端口，默认 `21114`
- `DB_PORT`: 数据库宿主机端口
- `DB_USER/DB_PASSWORD/DB_NAME`: 本地数据库凭据

## Docker 部署说明

- 根 Compose 文件不写 `version`，顶层 `name: quality-trace`。
- 容器名均使用 `${COMPOSE_PROJECT_NAME:-quality-trace}` 前缀。
- 数据库使用命名卷，避免绑定中文路径。
- 常见问题：端口占用时修改 `.env` 中端口后重启；需要重置数据时执行 `docker compose down -v`。
- 当前仓储层为内存实现，`backend/src/main/resources/application.properties` 中排除了数据源相关自动装配；接入真实 PostgreSQL 时移除该排除项并配置 `spring.datasource.*`。

## 枚举/常量出现位置清单

- WorkOrderStatus: constants/WorkOrderStatus、types/WorkOrderStatus、constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用。
- InspectionResultStatus: constants/InspectionResultStatus、types/InspectionResultStatus、constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用。
- DefectSeverity: constants/DefectSeverity、types/DefectSeverity、constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用。
- GenealogyAction: constants/GenealogyAction（SPLIT/MERGE）、services/BatchGenealogyService、constructors/BatchGenealogyDtoFactory、models/BatchTransform(Line)、types/BatchGenealogyPayload、repositories/BatchGenealogyRepository、controllers/BatchGenealogyController、routes/BatchGenealogyRoutes、logTemplates、errorCodes/errorMessages 均有引用。

## 为什么会牵一发动全身

实体字段、枚举、日志模板、错误消息、构造器、筛选器和展示组件被刻意拆散到多个目录；修改一个状态值通常需要同步类型、构造器、服务、控制器、store、页面、README 与数据库种子。

## License

MIT
