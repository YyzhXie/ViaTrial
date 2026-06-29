# ViaTrial

ViaTrial 是一个面向学生复习场景的本地题目管理系统，支持错题录入、科目与题型分类、标签筛选、LaTeX 公式渲染，以及按指定数量随机生成练习试卷。

当前发行版本：`v0.1.1`

## 核心功能

- 错题录入：按科目、题型维护题目、答案和解析。
- 题库管理：支持题目分页查询、关键字搜索、标签筛选与删除。
- 标签管理：为题目添加自定义标签，便于按知识点复习。
- 随机组卷：按题型数量配置生成练习试卷。
- LaTeX 渲染：前端集成 KaTeX，用于展示数学公式。
- 本地数据：使用 SQLite 数据库，首次启动自动创建数据库与表结构。

> 本版本不包含 OCR 图片识别、移动端、登录认证或云端部署能力。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.5.0, Java 21 |
| 数据访问 | MyBatis-Plus 3.5.16 |
| 数据库 | SQLite |
| 接口文档 | springdoc-openapi |
| 前端 | Vue 3, Vite, TypeScript |
| UI | Element Plus |
| 公式渲染 | KaTeX |

## 环境要求

- JDK 21
- Maven 3.9.x
- Node.js 22 或更高版本
- npm

## 配置说明

后端配置文件位于：

```text
backend/src/main/resources/application.yml
```

默认配置：

```text
服务端口: 8080
数据库: jdbc:sqlite:./data/viatrial.db
API 前缀: /api/v1
Swagger UI: /swagger-ui.html
OpenAPI JSON: /v3/api-docs
```

数据库文件位于项目根目录的 `data/viatrial.db`。数据库文件不会提交到 Git，首次启动时会自动创建 `data` 目录和数据库表。

## 构建

构建前端：

```bash
cd frontend
npm install
npm run build
```

将 `frontend/dist` 中的构建结果复制到后端静态资源目录：

```text
backend/src/main/resources/static/
```

构建后端可执行 jar：

```bash
cd backend
mvn package
```

生成的 jar 位于：

```text
backend/target/viatrial-backend-0.1.1.jar
```

## 启动

在项目根目录运行：

```bat
start.bat
```

如果 `backend/target/viatrial-backend-0.1.1.jar` 不存在，脚本会自动执行前端构建、复制静态资源并打包后端。启动后访问：

```text
http://localhost:8080
```

接口地址：

```text
http://localhost:8080/api/v1
```

## 停止

在启动窗口按：

```text
Ctrl + C
```

然后输入 `Y` 确认停止。

## 数据备份

停止程序后，复制：

```text
data/viatrial.db
```

例如备份到：

```text
data/backup/viatrial-20260629.db
```

恢复时，先停止程序，再用备份文件替换 `data/viatrial.db`。

## 文档说明

项目开发文档、设计说明和阶段性记录统一放在 `docs/` 目录。根目录 `README.md` 保留面向使用者的快速说明，模块细节请查看 `docs/backend.md`、`docs/database.md`、`docs/api.md`、`docs/frontend.md` 和 `docs/changelog.md`。
