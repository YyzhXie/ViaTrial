# 后端说明

本文档说明 ViaTrial 后端模块结构、运行方式和主要业务约束。

## 技术栈

- Java 21
- Spring Boot 3.5.0
- MyBatis-Plus 3.5.16
- SQLite JDBC 3.53.2.0
- springdoc-openapi 2.8.9
- Maven

## 目录结构

```text
backend/
  pom.xml
  src/main/java/com/viatrial/
    Main.java
    common/          统一响应、分页响应、错误码、业务异常
    config/          CORS、数据目录/数据源、安全配置、MyBatis-Plus、OpenAPI、MVC
    controller/      REST API 控制器
    database/        建表基线与版本化迁移
    dto/             请求和响应 DTO
    entity/          MyBatis-Plus 实体
    exception/       全局异常处理
    mapper/          数据访问 Mapper
    security/        写接口访问控制与会话接口
    service/         业务接口与实现
  src/main/resources/
    application.yml
    schema.sql               建表基线（唯一真相源）
    db/migration/V*.sql      增量结构变更
    static/                  前端构建产物
```

## 运行配置

主配置文件为 `backend/src/main/resources/application.yml`。数据源在 `DataSourceConfig` 中用解析后的绝对路径构造，不在 YAML 中声明 `spring.datasource.*`。

| 配置项 | 当前值 | 说明 |
| --- | --- | --- |
| `server.port` | `8080` | 后端服务端口 |
| `server.address` | `127.0.0.1` | 只监听回环地址，局域网设备无法直接访问 |
| `viatrial.data-dir` | `data` | 数据目录，相对路径按进程工作目录解析，启动时转为绝对路径 |
| `viatrial.security.write-token-enabled` | `true` | 状态变更请求是否校验访问令牌 |
| `viatrial.security.write-token` | 空 | 留空表示由后端在数据目录生成 `.write-token` 并复用 |
| `viatrial.security.allow-cross-site-writes` | `false` | 是否允许跨站来源发起写请求 |
| `spring.sql.init.mode` | `never` | 不使用 Spring SQL 自动初始化 |
| `springdoc.api-docs.enabled` | `false` | 接口文档默认关闭 |

数据库表由 `DatabaseInitializer` 在启动时依据 `schema.sql` 建表、再执行 `db/migration` 下的增量迁移。数据目录会先解析为绝对路径，因此从仓库根或 `backend/` 启动都得到可预期的库文件位置。

## 访问控制

ViaTrial 是本地单机应用，没有登录体系。为避免局域网内任意设备调用未鉴权接口，采用两层控制：

1. **网络层**：默认 `server.address: 127.0.0.1`，服务只监听回环地址。若改为 `0.0.0.0`，启动日志会打印明确警告。
2. **应用层**：`WriteAccessInterceptor` 拦截所有状态变更请求（POST/PUT/PATCH/DELETE）：

   | 请求来源 | 结果 |
   | --- | --- |
   | 同源页面请求（Origin/Referer 与 Host 一致，或回环地址） | 直接放行 |
   | 跨站页面请求 | 先被 CORS 过滤器拒绝（403），即使带令牌也不放行（可通过 `allow-cross-site-writes` 放开） |
   | 其他来源（局域网设备、本机其他进程） | 必须携带 `X-ViaTrial-Token`，否则 401 |

访问令牌 32 字节随机十六进制，保存在 `<data-dir>/.write-token`（非 POSIX 文件系统无法设权限，依赖目录本身的用户隔离），也可用 `viatrial.security.write-token` 或环境变量 `VIATRIAL__SECURITY__WRITE_TOKEN` 提供。前端通过 `GET /api/v1/system/session` 获取令牌，该接口**只对回环来源**下发令牌，因此即使把服务开放到局域网，远程调用者也需要自行读取令牌文件。轮换令牌只需删除 `.write-token` 并重启。

需要调试接口文档时临时开启：

```bash
java -jar viatrial-backend-0.3.1.jar --springdoc.api-docs.enabled=true --springdoc.swagger-ui.enabled=true
```

## 构建与启动

独立构建后端：

```bash
cd backend
mvn package
```

生成 jar：

```text
backend/target/viatrial-backend-0.3.1.jar
```

项目根目录启动：

```bat
start.bat
```

当 jar 不存在时，`start.bat` 会先执行前端构建、复制静态资源，再打包后端并启动。

## 模块职责

| 模块 | 职责 |
| --- | --- |
| `SubjectController` / `SubjectService` | 科目新增、列表查询、删除 |
| `QuestionTypeController` / `QuestionTypeService` | 按科目维护题型 |
| `TagController` / `TagService` | 标签新增、列表查询、删除 |
| `QuestionController` / `QuestionService` | 题目录入、编辑、分页筛选、删除 |
| `PaperController` / `PaperService` | 按单科目下的题型抽题数量随机预览试卷 |
| `WriteAccessInterceptor` / `SessionController` | 写接口访问控制与前端令牌下发 |
| `GlobalExceptionHandler` | 将校验异常、业务异常、系统异常转换为统一响应并记录日志 |

## 业务约束

- 科目名称全局唯一，长度不超过 50 个字符。
- 同一科目下题型名称唯一，长度不超过 50 个字符。
- 标签名称全局唯一，长度不超过 50 个字符。
- 删除科目会同时删除该科目下的题目、题型和题目标签关联。
- 删除题型前，该题型下不能存在题目。
- 删除标签会先删除题目和标签的关联记录，不会删除题目本身。
- 新增题目时，题型必须属于指定科目。
- 题目难度只允许 `1`、`2`、`3`，未传时默认 `1`。
- 题目标签 ID 不能重复，且必须全部存在。
- 删除题目会先删除题目和标签的关联记录。
- 新增科目时会自动创建“选择题”“判断题”“填空题”三种默认题型。
- 预览试卷仅支持单科目；每个题型的抽题数量必须大于 0，且题型必须属于选定科目；题量不足时返回已有题目并给出中文 warning。

## 输入上限

| 字段 | 上限 |
| --- | --- |
| 分页 `page` / `size` | ≤ 10000 / ≤ 100 |
| 题目 `content` / `answer` / `analysis` | 各 ≤ 20000 字符 |
| 题目 `tagIds` | ≤ 50 个，且不能重复 |
| 组卷 `typeCountMap` | ≤ 50 个题型，每个题型抽题数 ≤ 200 |
| 图片 URL | ≤ 500 字符 |

这些上限用于阻断超大 `LIMIT/OFFSET`、超长 TEXT 与超长 `IN` 子句造成的资源放大。

## 响应与异常

所有接口返回统一结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

错误码：

| code | 含义 |
| --- | --- |
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权访问（缺少或无效的访问令牌） |
| 404 | 数据不存在 |
| 409 | 数据冲突 |
| 500 | 系统内部错误 |

客户端错误记 `warn` 日志，服务端错误记 `error` 日志并带异常栈；数据库约束冲突映射为 409。

## 测试

执行后端测试：

```bash
cd backend
mvn test
```

测试数据目录由 `TestDataDirectoryInitializer` 在上下文刷新前覆盖为 `target/test-data`，随 `mvn clean` 清除，**不会**读写真实的 `data/viatrial.db`。`DataSecurityTest` 覆盖写接口访问控制、外键不变量、schema 单一真相源与接口文档默认关闭。
