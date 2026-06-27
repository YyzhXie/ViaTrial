# ViaTrial 智能错题组卷系统

ViaTrial 是一个面向学生的智能错题录入、分类管理、标签筛选与随机组卷系统。项目名称寓意为 `ViaTrial = 通过试炼`，当前阶段为第一期 MVP。

本项目以“错题录入 -> 分类筛选 -> 随机抽题 -> 试卷预览”为核心闭环，优先完成轻量、稳定、可维护的题库管理流程。

## 当前阶段

当前仓库已完成后端基础结构：

- `backend/`：Spring Boot 后端模块
- `docs/`：项目文档目录

尚未实现业务接口、数据库表、前端页面和组卷逻辑。

## 一期功能范围

一期计划实现：

- 科目管理：新增、查询、删除
- 题型管理：新增、按科目查询、删除
- 轻量级标签管理：新增、查询、删除未使用标签
- 错题管理：新增、分页查询、按科目/题型/单个标签筛选、删除
- 组卷功能：按科目数量随机抽题，题量不足时返回 warnings
- LaTeX 内容存储：后端只保存并原样返回字符串，由前端渲染
- 图片处理：一期只保存图片 URL 字符串

一期明确不做：

- 登录 / 注册
- JWT 鉴权
- Spring Security 权限系统
- Redis
- OCR 图片识别
- AI 自动解题、分类或打标签
- 云存储上传
- 复杂标签系统
- 试卷持久化保存
- PDF / Word 导出
- 移动端、鸿蒙端、桌面端安装包

## 技术栈

### 后端

| 技术 | 版本 / 约束 |
|---|---|
| JDK | 21 LTS |
| Spring Boot | 3.5.x |
| 构建工具 | Maven 3.9.x |
| ORM | MyBatis-Plus 3.5.16 Boot3 Starter |
| 数据库 | MySQL 8.4 LTS 优先，兼容 MySQL 8.0.x |
| 参数校验 | Spring Validation，使用 `jakarta.*` |
| 工具库 | Hutool 5.8.x |
| 接口文档 | springdoc-openapi 2.8.x |
| Lombok | 1.18.x |

### 前端

前端尚未创建，计划技术栈为：

| 技术 | 版本 / 约束 |
|---|---|
| Node.js | 22 LTS |
| Vue | 3.5.x |
| Vite | 7.x |
| TypeScript | 5.x |
| UI 组件库 | Element Plus 2.14.x |
| 路由 | Vue Router 4.x |
| 状态管理 | Pinia 3.x |
| HTTP 请求 | Axios 1.x |
| LaTeX 渲染 | KaTeX 0.16.x |

## 后端目录

```text
backend/
  pom.xml
  src/main/java/com/viatrial/
    ViaTrialApplication.java
  src/main/resources/
    application.yml
```

后续后端代码必须放在 `backend/src/main/java/com/viatrial/` 下。

## 本地运行

当前后端只包含项目骨架，可先验证构建：

```bash
cd backend
mvn -DskipTests package
```

运行后端：

```bash
cd backend
mvn spring-boot:run
```

默认配置：

- 服务端口：`8080`
- 数据库：`jdbc:mysql://localhost:3306/viatrial`
- 接口文档地址：`/swagger-ui.html`

运行前需要准备 MySQL 数据库：

```sql
CREATE DATABASE viatrial
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
```

## 开发约束

- 后端必须使用 Spring Boot 3.x 与 `jakarta.*`。
- 禁止使用旧版 `javax.*`。
- MyBatis-Plus 必须使用 `mybatis-plus-spring-boot3-starter`。
- 后端统一返回 `Result<T>`，不得直接返回实体类、`Map`、字符串或裸数组。
- DTO、VO、Entity 必须分离。
- Controller 只处理参数校验和调用 Service。
- Service 负责业务规则。
- Mapper 只负责数据库访问。
- 数据库字段、接口路径和返回格式以项目计划书为准，不得自行扩展。

## 文档

更多项目文档见 [docs/README.md](docs/README.md)。
