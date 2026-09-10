# API 说明

ViaTrial 后端 API 前缀为 `/api/v1`。接口文档默认关闭，需要时用 `--springdoc.api-docs.enabled=true --springdoc.swagger-ui.enabled=true` 临时开启（Swagger UI `/swagger-ui.html`，OpenAPI JSON `/v3/api-docs`）。

## 访问控制

- 仅 `GET`/`HEAD`/`OPTIONS`/`TRACE` 可直接调用。
- `POST`/`PUT`/`PATCH`/`DELETE` 属于状态变更请求，需要访问令牌：
  - 同源页面请求（浏览器地址栏打开本站页面后由前端发起）自动放行；
  - 其他来源必须带请求头 `X-ViaTrial-Token`，令牌取自数据目录的 `.write-token`；
  - 跨站来源的请求会先被 CORS 过滤器拒绝（403）。
- 令牌可通过 `GET /api/v1/system/session` 获取，但该接口只对回环来源（本机）下发令牌。

```bash
curl -X POST http://127.0.0.1:8080/api/v1/subjects \
  -H "Content-Type: application/json" \
  -H "X-ViaTrial-Token: $(cat data/.write-token)" \
  -d '{"name":"高等数学"}'
```

## 系统信息

### 获取会话信息

`GET /api/v1/system/session`

返回前端启动所需的会话信息。非回环来源调用时 `writeToken` 为 `null`。

```json
{
  "writeTokenRequired": true,
  "writeToken": "32 字节随机令牌"
}
```

## 统一响应

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

分页响应中的 `data`：

```json
{
  "total": 1,
  "page": 1,
  "size": 10,
  "records": []
}
```

错误码：

| code | HTTP 状态 | 说明 |
| --- | --- | --- |
| 400 | 400 | 参数错误或参数校验失败 |
| 401 | 401 | 未授权访问（缺少或无效的访问令牌） |
| 404 | 404 | 数据不存在 |
| 409 | 409 | 数据冲突（唯一约束、外键引用等） |
| 500 | 500 | 系统内部错误 |

状态变更请求被 CORS 过滤器拒绝时返回 HTTP 403，响应体为 `Invalid CORS request`（非统一结构）。

## 科目管理

### 新增科目

`POST /api/v1/subjects`

请求体：

```json
{
  "name": "高等数学"
}
```

规则：`name` 必填，长度不超过 50，且全局唯一。

返回：新增科目 ID。

### 查询科目列表

`GET /api/v1/subjects`

返回：按 ID 升序排列的科目列表。

```json
[
  {
    "id": 1,
    "name": "高等数学"
  }
]
```

### 删除科目

`DELETE /api/v1/subjects/{id}`

规则：科目不存在返回 404；删除科目会同时删除该科目下的题目、题型和题目标签关联。

返回：`true`。

## 题型管理

### 新增题型

`POST /api/v1/question-types`

请求体：

```json
{
  "subjectId": 1,
  "name": "选择题"
}
```

规则：`subjectId` 必须存在；`name` 必填，长度不超过 50；同一科目下题型名称不能重复。

返回：新增题型 ID。

### 按科目查询题型

`GET /api/v1/question-types?subjectId=1`

规则：`subjectId` 必填且必须存在。

返回：题型列表。

### 删除题型

`DELETE /api/v1/question-types/{id}`

规则：题型不存在返回 404；题型下存在题目时返回 409。

返回：`true`。

## 标签管理

### 新增标签

`POST /api/v1/tags`

请求体：

```json
{
  "name": "期末重点"
}
```

规则：`name` 必填，长度不超过 50，且全局唯一。

返回：新增标签 ID。

### 查询标签列表

`GET /api/v1/tags`

返回：按 ID 升序排列的标签列表。

### 删除标签

`DELETE /api/v1/tags/{id}`

规则：标签不存在返回 404；删除标签会先删除题目和标签的关联记录，不会删除题目本身。

返回：`true`。

## 题目管理

### 新增题目

`POST /api/v1/questions`

请求体：

```json
{
  "subjectId": 1,
  "typeId": 1,
  "content": "求 $x^2$ 的导数。",
  "answer": "$2x$",
  "analysis": "幂函数求导。",
  "imageUrl": null,
  "answerImageUrl": null,
  "difficulty": 1,
  "tagIds": [1]
}
```

规则：

- `subjectId`、`typeId`、`content` 必填。
- `typeId` 必须属于 `subjectId`。
- `difficulty` 只能为 1、2、3；不传时默认 1。
- `content`、`answer`、`analysis` 各不超过 20000 字符。
- `imageUrl` 和 `answerImageUrl` 长度不超过 500，只接受 `http(s)://` 绝对地址。
- `tagIds` 可为空；传入时不能重复、不超过 50 个，且标签必须存在。

返回：新增题目 ID。

### 更新题目

`PUT /api/v1/questions/{id}`

请求体：与新增题目一致。

规则：

- 题目不存在返回 404。
- `subjectId`、`typeId`、`content` 必填。
- `typeId` 必须属于 `subjectId`。
- `difficulty` 只能为 1、2、3；不传时默认 1。
- `tagIds` 可为空；传入时不能重复，且标签必须存在。
- 更新题目时会替换题目和标签的关联记录。

返回：`true`。

### 分页查询题目

`GET /api/v1/questions/page`

查询参数：

| 参数 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `page` | 否 | `1` | 页码，不超过 10000 |
| `size` | 否 | `10` | 每页数量，不超过 100 |
| `subjectId` | 否 | 无 | 按科目筛选 |
| `typeId` | 否 | 无 | 按题型筛选 |
| `tagId` | 否 | 无 | 按标签筛选 |

排序：按 `created_time`、`id` 倒序。

返回：分页题目列表。题目响应包含科目名、题型名、标签列表、题目内容、答案、解析、图片 URL、难度和创建时间。

### 删除题目

`DELETE /api/v1/questions/{id}`

规则：题目不存在返回 404；删除题目时会删除对应的题目标签关联。

返回：`true`。

## 组卷管理

### 预览试卷

`POST /api/v1/papers/generate`

请求体：

```json
{
  "subjectId": 1,
  "typeCountMap": {
    "1": 5,
    "2": 3
  }
}
```

规则：

- `subjectId` 不能为空，且必须存在。
- `typeCountMap` 不能为空，最多 50 个题型。
- 题型 ID 不能为空，且必须属于 `subjectId`。
- 每个题型的抽题数量必须大于 0，且不超过 200。
- 当某题型题量不足时，返回该题型下全部题目，并在 `warnings` 中给出中文说明。
- 当某题型没有题目时，不返回该题型的题目，并在 `warnings` 中给出中文说明。

返回：前端可基于返回题目进入做题模式。

```json
{
  "paperId": "高等数学-20260706120000",
  "totalRequested": 8,
  "totalActual": 7,
  "warnings": [],
  "questions": []
}
```
