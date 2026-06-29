# 变更记录

## v0.1.2

### 功能

- 新增题库页的科目删除和标签删除入口。
- 新增题目时，支持直接输入新科目、新题型和新标签，并在提交前自动创建基础数据。
- 优化试卷生成：仅支持单学科组卷，并按选定学科下的题型分别填写抽题数量。

### 修复

- 修复新增科目、新增标签弹窗中“科目名称”“标签名称”等表单标签换行显示的问题。

### 文档

- 同步 API、前端、后端文档中的 v0.1.2 组卷接口和功能说明。

## v0.1.1

### 文档

- 更新根目录 README，使其与当前项目真实配置一致：JDK 21、Spring Boot 3.5、SQLite、API 前缀 `/api/v1`、Swagger UI `/swagger-ui.html`。
- 补充 `docs/backend.md`、`docs/database.md`、`docs/api.md`、`docs/frontend.md` 和 `docs/changelog.md`。
- 删除 `docs/README.md`，避免文档规划文件与实际文档重复维护。

### 启动与构建

- 增强 `start.bat`：当 `backend/target/viatrial-backend-0.1.1.jar` 不存在时，自动执行前端依赖安装、前端构建、静态资源复制和后端打包。
- 验证 `npm run build` 可成功完成前端构建。
- 验证 `mvn package` 可成功完成后端打包和测试。
- 验证删除 `backend/target` 后运行 `start.bat` 可自动构建并启动服务。

### Git 与发布

- 将 `.idea/` 和 `*.db` 加入 `.gitignore`。
- 清理 Git 历史中的 IDEA 配置目录和 SQLite 数据库文件。
- 创建 `v0.1.1` tag。
- 创建 GitHub Release `ViaTrial v0.1.1`。

## v0.1.0

- 完成一期后端核心接口。
- 完成前端基础架构和主要页面。
- 完成题目录入、科目题型管理、标签筛选、随机组卷和本地 SQLite 数据存储。
