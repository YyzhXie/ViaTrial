# 本地编程题判题

编程题代码由 ViaTrial 后端在本机编译和运行，不再提交到 Judge0。Windows x64 桌面版支持 C、C++、Java 和 Python 3。

## 工具链

- Java 使用应用自带的 Java 运行时，并调用其中的 `jdk.compiler` 模块编译。桌面版无需另装 JDK。
- 找不到本机 C/C++ 编译器时，首次运行 C 或 C++ 题目会下载 Zig 0.17.0 Windows x64 压缩包（约 96 MiB）。
- 找不到本机 Python 时，首次运行 Python 题目会下载 Python 3.14.8 Windows x64 嵌入包（约 12 MiB）。
- 下载文件来自 [Zig 官方下载站](https://ziglang.org/download/0.17.0/) 和 [Python 官方发布页](https://www.python.org/downloads/release/python-3148/)，安装前会校验 SHA-256。工具链解压到 ViaTrial 数据目录下的 `toolchains/`，后续运行可离线完成。
- 可通过 `VIATRIAL__CODE_EXECUTION__COMPILER__C`、`VIATRIAL__CODE_EXECUTION__COMPILER__CPP`、`VIATRIAL__CODE_EXECUTION__RUNTIME__JAVA`、`VIATRIAL__CODE_EXECUTION__RUNTIME__PYTHON` 指定已有工具。

## 执行限制

每个判题任务使用独立临时目录；编译最长 20 秒，标准输出和错误输出各最多保留 64 KiB，任务结束后会尝试终止进程树并清理目录。普通模式运行最长 5 秒。

编程题可开启竞赛模式并分别设置运行时间（50–30000 毫秒）和内存（16–2048 MB）上限。时间超限返回 TLE；本机监测到进程树内存超过上限时返回 MLE。内存按操作系统可见的进程工作集采样，快速的瞬时峰值可能无法被采到。答案按统一换行符并忽略末尾空白进行比较。

这些时间和输出限制不能隔离 Windows 文件、网络或账户权限。代码以启动 ViaTrial 的当前用户身份执行，因此只应运行自己信任的代码，并保持 ViaTrial 仅监听本机地址。
