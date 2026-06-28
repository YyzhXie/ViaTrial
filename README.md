# ViaTrial

ViaTrial 是一个本地运行的错题录入、标签筛选和随机组卷工具。

## 环境要求

- JDK 21
- Maven 3.9.x
- Node.js 22 或更高版本
- npm

## 构建

先构建前端：

```bash
cd frontend
npm install
npm run build
```

将 `frontend/dist` 中的文件复制到：

```text
backend/src/main/resources/static/
```

再构建后端可执行 jar：

```bash
cd backend
mvn package
```

## 启动

在项目根目录运行：

```bat
start.bat
```

启动后访问：

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

## 数据库

SQLite 数据库文件位于：

```text
data/viatrial.db
```

首次启动时会自动创建 `data` 目录和数据库表。

## 备份数据库

停止程序后，复制：

```text
data/viatrial.db
```

例如备份到：

```text
data/backup/viatrial-20260628.db
```

恢复时，先停止程序，再用备份文件替换 `data/viatrial.db`。
