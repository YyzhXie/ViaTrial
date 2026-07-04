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
    config/          CORS、SQLite、MyBatis-Plus、OpenAPI、MVC 配置
    controller/      REST API 控制器
    database/        数据库目录创建与 schema 初始化
    dto/             请求和响应 DTO
    entity/          MyBatis-Plus 实体
    exception/       全局异常处理
    mapper/          数据访问 Mapper
    service/         业务接口与实现
  src/main/resources/
    application.yml
    schema.sql
    sql/init.sql
    static/          前端构建产物
```

## 运行配置

主配置文件为 `backend/src/main/resources/application.yml`。

| 配置项 | 当前值 | 说明 |
| --- | --- | --- |
| `server.port` | `8080` | 后端服务端口 |
| `spring.application.name` | `viatrial` | 应用名称 |
| `spring.datasource.driver-class-name` | `org.sqlite.JDBC` | SQLite 驱动 |
| `spring.datasource.url` | `jdbc:sqlite:./data/viatrial.db` | 项目根目录下的数据文件 |
| `spring.sql.init.mode` | `never` | 不使用 Spring SQL 自动初始化 |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | Swagger UI 路径 |
| `springdoc.api-docs.path` | `/v3/api-docs` | OpenAPI JSON 路径 |

数据库表由 `DatabaseInitializer` 在启动时依据 `schema.sql` 初始化。`start.bat` 会在项目根目录启动 jar，因此数据库默认落在 `data/viatrial.db`。

## 构建与启动

独立构建后端：

```bash
cd backend
mvn package
```

生成 jar：

```text
backend/target/viatrial-backend-0.1.3.jar
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
| `PaperController` / `PaperService` | 按单科目下的题型抽题数量随机生成试卷 |
| `GlobalExceptionHandler` | 将校验异常、业务异常、系统异常转换为统一响应 |

## 业务约束

- 科目名称全局唯一，长度不超过 50 个字符。
- 同一科目下题型名称唯一，长度不超过 50 个字符。
- 标签名称全局唯一，长度不超过 50 个字符。
- 删除科目前，科目下不能存在题型或题目。
- 删除题型前，该题型下不能存在题目。
- 删除标签前，该标签不能被任何题目使用。
- 新增题目时，题型必须属于指定科目。
- 题目难度只允许 `1`、`2`、`3`，未传时默认 `1`。
- 题目标签 ID 不能重复，且必须全部存在。
- 删除题目会先删除题目和标签的关联记录。
- 新增科目时会自动创建“选择题”“判断题”“填空题”三种默认题型。
- 生成试卷仅支持单科目；每个题型的抽题数量必须大于 0，且题型必须属于选定科目；题量不足时返回已有题目并给出中文 warning。

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
| 404 | 数据不存在 |
| 409 | 数据冲突 |
| 500 | 系统内部错误 |

## 测试

执行后端测试：

```bash
cd backend
mvn test
```
