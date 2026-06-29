# API 说明

ViaTrial 后端 API 前缀为 `/api/v1`。Swagger UI 地址为 `/swagger-ui.html`，OpenAPI JSON 地址为 `/v3/api-docs`。

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
| 404 | 404 | 数据不存在 |
| 409 | 409 | 数据冲突 |
| 500 | 500 | 系统内部错误 |

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

规则：科目不存在返回 404；科目下存在题型或题目时返回 409。

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

规则：标签不存在返回 404；标签仍被题目使用时返回 409。

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
- `imageUrl` 和 `answerImageUrl` 长度不超过 500。
- `tagIds` 可为空；传入时不能重复，且标签必须存在。

返回：新增题目 ID。

### 分页查询题目

`GET /api/v1/questions/page`

查询参数：

| 参数 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `page` | 否 | `1` | 页码 |
| `size` | 否 | `10` | 每页数量 |
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

### 生成试卷

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
- `typeCountMap` 不能为空。
- 题型 ID 不能为空，且必须属于 `subjectId`。
- 每个题型的抽题数量必须大于 0。
- 当某题型题量不足时，返回该题型下全部题目，并在 `warnings` 中说明。
- 当某题型没有题目时，不返回该题型的题目，并在 `warnings` 中说明。

返回：

```json
{
  "paperId": "PAPER-20260629120000-ABC123",
  "totalRequested": 8,
  "totalActual": 7,
  "warnings": [],
  "questions": []
}
```
