# ViaTrial

ViaTrial 是一个面向学生复习场景的本地题目管理系统，支持错题录入、科目与题型分类、标签筛选、LaTeX 公式渲染，以及按指定数量随机生成练习试卷。

当前发行版本：`v0.3.1`

## 核心功能

- 错题录入：按科目、题型维护题目、答案和解析。
- 题库管理：支持题目分页查询、关键字搜索、标签筛选与删除。
- 标签管理：为题目添加自定义标签，便于按知识点复习。
- 随机组卷：按题型数量配置预览试卷，并支持进入做题模式。
- LaTeX 渲染：前端集成 KaTeX，用于展示数学公式。
- 公式输入：题目录入（题目/答案/解析）与填空题作答处提供可视化公式编辑器，支持源码编辑、模板插入与实时预览。
- 本地数据：使用 SQLite 数据库，首次启动自动创建数据库与表结构。
- 本地安全：默认只监听本机地址，写操作叠加访问令牌校验，局域网设备无法直接修改题库。

> 本版本不包含 OCR 图片识别、移动端、登录认证或云端部署能力。

## 数据安全

ViaTrial 没有登录体系（本地单机工具），因此采用两层控制保护题库数据：

1. **只监听本机**：默认 `server.address: 127.0.0.1`，局域网内的设备无法连接。确需局域网访问时改为 `0.0.0.0`，启动日志会给出明确警告。
2. **写操作令牌**：新增/修改/删除类请求需要访问令牌，令牌保存在数据目录的 `.write-token` 文件中，由后端首次启动自动生成（也可用 `viatrial.security.write-token` 或环境变量 `VIATRIAL__SECURITY__WRITE_TOKEN` 指定）。前端页面自动获取，无需手工配置；用 curl/脚本调用写接口时需带上请求头 `X-ViaTrial-Token`。

```bash
# 读取令牌
cat data/.write-token

# 带令牌调用写接口
curl -X POST http://127.0.0.1:8080/api/v1/subjects \
  -H "Content-Type: application/json" \
  -H "X-ViaTrial-Token: <令牌>" \
  -d '{"name":"高等数学"}'
```

接口文档默认关闭（会暴露完整 API 面，含删除端点）。需要时临时开启：

```bash
java -jar backend/target/viatrial-backend-0.3.1.jar --springdoc.api-docs.enabled=true --springdoc.swagger-ui.enabled=true
```

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
监听地址: 127.0.0.1（仅本机）
数据目录: data            （可用 viatrial.data-dir 指定，支持绝对路径）
API 前缀: /api/v1
写接口令牌: <数据目录>/.write-token
接口文档: 默认关闭
```

数据库文件位于数据目录下（默认 `<项目根>/data/viatrial.db`）。数据库文件与访问令牌不会提交到 Git，首次启动时会自动创建目录、数据库表和令牌。

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
backend/target/viatrial-backend-0.3.1.jar
```

## 启动

在项目根目录运行：

```bat
start.bat
```

如果 `backend/target/viatrial-backend-0.3.1.jar` 不存在，脚本会自动执行前端构建、复制静态资源并打包后端。启动后访问：

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
