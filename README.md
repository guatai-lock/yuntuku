# yuntuku-backhand 云图库后端

基于 Spring Boot 2.7.6 的云图片管理平台后端 API 服务，提供图片上传存储、空间管理、团队协作、AI 扩图、以图搜图、会员体系等功能。

## 技术栈

| 类别 | 技术 |
|---|---|
| **框架** | Spring Boot 2.7.6、MyBatis-Plus 3.5.9 |
| **数据库** | MySQL + ShardingSphere JDBC 5.2.0（动态分表） |
| **缓存** | Caffeine（本地 L1）+ Redis（分布式 L2） |
| **会话** | Spring Session + Redis |
| **认证** | HttpSession + Sa-Token 1.39.0（双认证体系） |
| **存储** | 腾讯云 COS（对象存储） |
| **AI** | 阿里云 AI 扩图 API |
| **实时协作** | WebSocket + LMAX Disruptor |
| **API 文档** | Knife4j / Swagger |
| **构建工具** | Maven 3.8+ |

## 快速开始

### 前提条件

- Java 8+
- Maven 3.8+
- MySQL（数据库 `yuntuku`）
- Redis（默认端口 6379）

### 启动

```bash
# 编译
mvn compile

# 本地运行（激活 profile: local）
mvn spring-boot:run -DskipTests

# 打包
mvn package -DskipTests
```

应用启动后访问：`http://localhost:8080/api`

API 文档：`http://localhost:8080/api/doc.html`

### 测试

```bash
# 运行全部测试
mvn clean test

# 运行单个测试类
mvn test -Dtest=UserServiceImplTest
```

## 项目结构

```
src/main/java/com/guatai/yuntukubackend/
├── annotation/        # 自定义注解（@AuthCheck）
├── aop/               # AOP 拦截器（角色鉴权）
├── api/               # 外部 API 集成（阿里云 AI、以图搜图）
├── common/            # 统一响应封装（BaseResponse、ResultUtils）
├── config/            # Spring 配置（CORS、MyBatis-Plus、JSON、COS）
├── constant/          # 常量定义
├── controller/        # REST 控制器（7 个）
├── exception/         # 异常处理（BusinessException、全局处理器）
├── manger/            # 管理组件
│   ├── auth/          # Sa-Token 空间权限
│   ├── sharding/      # ShardingSphere 动态分片
│   ├── upload/        # 上传策略（模板模式）
│   └── websocket/     # 实时协作编辑
├── mapper/            # MyBatis-Plus 映射器（4 个）
├── model/
│   ├── entity/        # 数据库实体
│   ├── dto/           # 请求 DTO
│   ├── vo/            # 响应视图对象
│   └── enums/         # 枚举
├── service/           # 业务逻辑接口 + 实现
└── utils/             # 工具类（颜色相似度、会员兑换）
```

## 核心功能

### 用户体系
- 注册/登录（MD5 加密）
- 角色：user / admin / vip
- 会员兑换码体系（365 天有效期，支持续期）

### 图片管理
- 文件上传 / URL 上传 / 批量导入
- 自动 WebP 压缩 + 128x128 缩略图生成
- 图片审核流程（待审/通过/拒绝）
- 颜色搜索、以图搜图

### 空间体系
| 等级 | 容量 | 图片数 |
|------|------|--------|
| 普通版 | 100MB | 100 |
| 专业版 | 1GB | 1000 |
| 旗舰版 | 10GB | 10000 |

- 私有空间 / 团队空间
- 空间角色：查看者、编辑者、管理员
- 空间使用分析（分类、标签、容量排行）

### AI 功能
- 阿里云 AI 扩图（图片延展）

### 实时协作
- WebSocket 多人协同编辑图片

## 数据库

四张核心表：
- `user` — 用户账户
- `picture` — 图片元数据（按 spaceId 分片）
- `space` — 空间
- `space_user` — 空间成员

建表脚本：`sql/create_table.sql`

## 配置说明

数据库凭证、云服务密钥等配置在 `application-local.yml` 中，Spring profile 激活方式：

```
spring.profiles.active=local
```

关键业务配置文件：
- `biz/spaceUserAuthConfig.json` — 空间角色权限映射
- `biz/vipcode.json` — 预生成会员兑换码
