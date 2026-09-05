# 前端说明

ViaTrial 前端位于 `frontend/`，使用 Vue 3、Vite、TypeScript 和 Element Plus 构建。

## 技术栈

- Vue 3
- Vue Router
- TypeScript
- Vite
- Element Plus
- Axios
- KaTeX

## 目录结构

```text
frontend/
  package.json
  vite.config.ts
  src/
    main.ts
    App.vue
    api/          后端接口封装
    components/   通用组件
    data/         静态数据（公式模板等）
    router/       路由配置
    styles/       全局样式
    types/        TypeScript 类型
    utils/        工具函数
    views/        页面视图
```

## 路由

| 路径 | 页面 | 说明 |
| --- | --- | --- |
| `/` | `QuestionListView.vue` | 题库管理页面 |
| `/paper` | `PaperGenerateView.vue` | 试卷预览与做题页面 |

后端 `WebMvcConfig` 对 `/paper` 和 `/paper/` 做了前端路由转发，因此打包后可直接访问组卷页面。

## API 封装

基础请求实例位于 `src/api/request.ts`。

- `baseURL` 为 `/api/v1`。
- 超时时间为 10 秒。
- 响应拦截器读取统一响应结构。
- 当 `code !== 200` 时，通过 Element Plus 消息提示错误，并拒绝 Promise。

接口文件：

| 文件 | 说明 |
| --- | --- |
| `src/api/subject.ts` | 科目新增、列表、删除 |
| `src/api/questionType.ts` | 题型新增、按科目列表、删除 |
| `src/api/tag.ts` | 标签新增、列表、删除 |
| `src/api/question.ts` | 题目新增、分页查询、删除 |
| `src/api/paper.ts` | 试卷生成 |

## 页面说明

### 题库管理页

文件：`src/views/QuestionListView.vue`

主要能力：

- 加载科目、标签和题目列表。
- 按科目、题型、标签筛选题目。
- 分页展示题目内容、答案、解析、难度、标签、图片和创建时间。
- 通过科目操作菜单新增科目，或在科目列表弹窗中删除、批量删除科目。
- 通过标签操作菜单新增标签，或在标签列表弹窗中删除、批量删除标签。
- 打开题目录入弹窗新增题目，支持输入新科目、新题型和新标签并自动创建。
- 在题库列表操作栏编辑题目内容、答案和解析。
- 在题目、答案、解析输入框旁提供“插入公式”按钮，通过公式编辑器可视化录入 LaTeX。
- 删除题目。

题型筛选依赖科目：选择科目后才加载该科目下的题型。

### 组卷页

文件：`src/views/PaperGenerateView.vue`

主要能力：

- 选择单个组卷科目。
- 按选定科目下的题型填写抽题数量。
- 调用 `/papers/generate` 预览试卷。
- 展示试卷编号、请求题数、实际题数。
- 展示题量不足或无题目的中文 warning。
- 预览试卷时只展示题干，不展示答案和解析。
- 从预览试卷进入做题模式，支持选项作答、文本作答、上一题/下一题、答题卡跳转、交卷判分和答案解析展示。
- 填空题等文本作答处提供“插入公式”按钮，可将公式编辑器生成的 LaTeX 插入答案。

## 组件说明

| 组件 | 说明 |
| --- | --- |
| `LatexRenderer.vue` | 使用 KaTeX 渲染题目、答案和解析中的 LaTeX 内容 |
| `LatexFormulaEditor.vue` | 可视化公式编辑器，提供模板插入、源码编辑与实时预览 |
| `QuestionFormDialog.vue` | 题目录入和编辑弹窗，负责选择科目、题型、标签并提交题目 |
| `TagSelector.vue` | 标签选择组件 |

LaTeX 分隔符解析、包裹与占位符清理等纯函数位于 `src/utils/latex.ts`，公式模板字典位于 `src/data/latexTemplates.ts`。

## 类型定义

类型文件位于 `src/types/`。

| 文件 | 说明 |
| --- | --- |
| `common.ts` | `Result<T>`、`PageResult<T>` |
| `subject.ts` | 科目类型和新增科目请求 |
| `questionType.ts` | 题型类型和新增题型请求 |
| `tag.ts` | 标签类型和新增标签请求 |
| `question.ts` | 题目、题目新增请求、题目分页请求 |
| `paper.ts` | 组卷请求、组卷响应、试卷题目 |

## 构建

```bash
cd frontend
npm install
npm run build
```

构建产物输出到 `frontend/dist/`。发行 jar 需要将该目录内容复制到：

```text
backend/src/main/resources/static/
```

项目根目录的 `start.bat` 在 jar 不存在时会自动完成上述复制。
