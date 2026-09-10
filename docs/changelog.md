# 变更记录

## 未发布

### 安全

- 新增写接口访问控制：`POST`/`PUT`/`PATCH`/`DELETE` 需通过同源校验或携带 `X-ViaTrial-Token` 访问令牌，令牌由后端生成并保存在数据目录的 `.write-token`；新增 `GET /api/v1/system/session` 供前端获取令牌（仅对回环来源下发）。
- 默认监听地址由 `0.0.0.0` 收敛为 `127.0.0.1`，绑定到非回环地址时启动日志输出明确警告。
- 接口文档（springdoc/Swagger UI）默认关闭，避免向未授权访问者暴露完整 API 面（含删除端点）。
- 补全输入上限：分页 `page ≤ 10000`、`size ≤ 100`，题目正文/答案/解析各 ≤ 20000 字符，`tagIds ≤ 50`，组卷题型数 ≤ 50 且单题型抽题 ≤ 200，阻断超大 `LIMIT/OFFSET`、超长 TEXT 与超长 `IN` 子句造成的资源放大。
- 前端图片地址增加协议白名单，仅放行绝对 `http(s)://`；KaTeX 显式关闭 `trust` 并限制 `maxSize`/`maxExpand`。
- `.gitignore` 补充 `/data/`、`/backend/data/`、`*.db-wal`、`*.db-shm`、`*.db-journal`、`.write-token`。

### 数据可靠性

- 修复数据源 URL 占位符未解析导致启动失败的问题：数据库 URL 改由 `DataSourceConfig` 用解析后的绝对路径构造，不再依赖 YAML 占位符解析时机。
- 数据目录统一解析为绝对路径并支持 `viatrial.data-dir` 配置，消除同一代码在不同工作目录下产生两个数据库文件的问题。
- schema 真相源去重：删除与 `schema.sql` 内容重复的 `sql/init.sql`，只保留一份基线。
- 引入版本化迁移：新增 `schema_version` 表和 `db/migration/V*.sql` 增量脚本机制，既有库升级可执行结构变更；改用 Spring `ScriptUtils` 解析脚本，替代按 `;` 裸切分的脆弱实现。
- 删除 `DatabaseManager` 原生 `DriverManager` 连接与重复的 `data` 目录创建逻辑，全部走连接池；外键开关集中到连接池 `connection-init-sql`，并加入不变量测试。
- 测试数据源隔离：测试数据目录重定向到 `target/test-data`，`mvn test` 不再读写真实数据库。
- 索引维护：删除与 `UNIQUE` 隐式索引重复的 `idx_question_type_subject_id`、`idx_question_tag_question_id`，补充组卷热路径所需的 `idx_question_subject_type`。

### 可观测性

- `GlobalExceptionHandler` 所有分支补充日志（客户端错误 `warn`、服务端错误 `error` 带异常栈），补齐 `HttpMessageNotReadableException`（400）、`DataIntegrityViolationException`（409）、`NoResourceFoundException`（404）映射。
- `Main` 启动日志改为通过日志框架输出，并打印监听地址与数据目录。
- mapper 层 SQL 调试日志默认关闭，需要时用 `--logging.level.com.viatrial.mapper=debug` 临时开启。

### 测试与文档

- 新增 `DataSecurityTest`（9 项）：覆盖写接口令牌校验、跨站写请求拦截、令牌下发、外键不变量、schema 单一真相源、接口文档默认关闭、测试数据源隔离。
- 同步更新 `README.md` 与 `docs/backend.md`、`docs/database.md`、`docs/api.md`。

## v0.3.1

### 功能

- 新增“公式输入”模块：在新增/编辑题目的题目、答案、解析输入框旁，以及做题模式填空题答案输入框旁提供可视化公式编辑器。
- 公式编辑器默认采用“源码编辑 + 模板输入 + 实时预览”形态，支持按分类插入公式模板、KaTeX 实时预览，并可按行内 `$…$`、独立 `$$…$$` 或无包裹三种方式输出源码。

### 优化

- 抽取 LaTeX 分隔符解析与包裹逻辑到 `src/utils/latex.ts`，供 `LatexRenderer.vue` 与公式编辑器复用，避免规则漂移。
- 升级项目版本号为 `v0.3.1`，统一前端、后端、启动脚本及项目文档中的版本声明。

## v0.3.0

- 统一前端、后端、启动脚本及项目文档中的版本声明为 `v0.3.0`。

## v0.2.1

### 功能

* 组卷系统新增做题功能。点击“生成试卷”后，右部试卷栏将变更为可交互的做题界面。
* 组卷系统新增题目预览功能。将现有的“生成试卷”替换为“预览试卷”。

### 优化

* 优化题目编辑。在删除科目时，不要求科目下存在标签、题型等内容，在删除科目时可直接删除，并附带删除其科目下所有子内容。在删除时，将进行弹窗提示。
* 优化删除逻辑。删除科目、标签时无需在科目、标签页输入或选择，点击后将出现科目、标签列表，并支持在列表中删除、批量删除。
* 优化UI显示。科目、标签操作按钮合并，点击科目、标签后出现“新增”“删除”的展开菜单。

### 修复

* 修复题目操作栏中，编辑和删除按钮错位的情况。
* 修复浏览器在分屏显示等情况中，按钮会因界面折叠而发生重合的情况。

## v0.1.3

### 功能

- 题库列表操作栏新增“编辑”，支持修改题目内容、答案和解析并保存。
- 新增科目时自动创建“选择题”“判断题”“填空题”三种默认题型。

### 优化

- 查询和重置按钮保持与科目、题型、标签筛选项同一行展示。

### 修复

- 生成试卷时，题量不足或无题目的 warning 改为中文提示。

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
