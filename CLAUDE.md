# CLAUDE.md

本文档为 Claude Code（claude.ai/code）在此仓库中工作时提供指引。

## 构建与运行命令

```bash
# 编译（跳过测试，因为目前几乎没有测试）
mvn compile

# 打包为 JAR
mvn package -DskipTests

# 本地运行（需要本地 MySQL + Redis，默认端口 3306 / 6379）
mvn spring-boot:run -DskipTests

# 运行单个测试
mvn test -Dtest=YuntukuBackendApplicationTests

# 运行全部测试
mvn test

# 清理构建
mvn clean install -DskipTests
```

**前提条件：** Java 8+、Maven 3.8+、MySQL（数据库 `yuntuku`）、Redis。当前激活的 Spring profile 为 `local`，数据库凭证和云服务密钥在 `application-local.yml` 中配置。

## 项目架构概述

基于 Spring Boot 2.7.6 的单体 REST API，包路径 `com.guatai.yuntukubackend`。对外暴露 `/api/**` 端口 8080。API 文档地址 `/api/doc.html`（Knife4j/Swagger）。

### 分层结构

```
controller/  → REST 控制器（7 个）
service/     → 业务逻辑接口 + 实现（service/impl/）
mapper/      → MyBatis-Plus 映射器（4 个，对应 4 张表）
manger/      → 通用管理器（认证、上传、分片、WebSocket、COS）
model/
  ├── entity/  → 数据库实体（User、Picture、Space、SpaceUser）
  ├── dto/     → 请求 DTO（按领域分包）
  ├── vo/      → 响应视图对象（脱敏、聚合）
  └── enums/   → 枚举（用户角色、审核状态、空间等级/类型/角色）
config/      → Spring 配置类（CORS、MyBatis-Plus、JSON 序列化、COS 客户端）
common/      → 统一响应包装 BaseResponse<T>、ResultUtils、分页基类
exception/   → BusinessException、ErrorCode 枚举、GlobalExceptionHandler
api/         → 外部 API 集成（阿里云 AI 扩图、以图搜图）
annotation/  → @AuthCheck（角色鉴权注解）
aop/         → AuthInterceptor（@AuthCheck 的 AOP 拦截器）
utils/       → 颜色相似度工具、会员兑换工具
```

### 数据库（MySQL + ShardingSphere）

四张表：`user`、`picture`、`space`、`space_user`。逻辑删除字段 `isDelete`。`picture` 表按 `spaceId` 分片（ShardingSphere JDBC 5.2.0），仅旗舰版团队空间会创建独立分片 `picture_{spaceId}`。建表脚本见 `sql/create_table.sql`。

### 安全认证 — 双系统并行

1. **Session 认证：** `HttpSession` 存储登录用户；`@AuthCheck` AOP 拦截器校验角色（user/admin）。
2. **Sa-Token 空间权限：** 双 `StpLogic` 实例（`StpKit.DEFAULT` + `StpKit.SPACE`）。`StpInterfaceImpl` 通过解析请求 URI 和请求体提取 `spaceId`，再根据 `space_user` 表中的角色从 `biz/spaceUserAuthConfig.json` 读取权限映射。`@SaSpaceCheckPermission` 在控制器层执行权限校验。

### 关键基础设施

- **缓存：** Caffeine（本地 L1）+ Redis（分布式 L2，同时用于 Spring Session）
- **存储：** 腾讯云 COS（通过 `CosManager` 上传/下载/删除）
- **AI：** 阿里云 AI 扩图（创建任务 + 查询结果）
- **实时协作：** WebSocket `/ws/picture/edit` + LMAX Disruptor 异步事件处理
- **上传策略：** 模板模式 — `PictureUploadTemplate` 抽象类，`FilePictureUpload` / `UrlPictureUpload` 具体实现
- **动态分片：** `DynamicShardingManager` 在旗舰版团队空间创建时动态更新 ShardingSphere 规则

### 会员系统

兑换码预生成在 `biz/vipcode.json` 中。兑换后用户角色升级为 `vip`，有效期 365 天（续期在原有效期上叠加）。

### 前端

本仓库不包含前端代码。前端为独立项目。
