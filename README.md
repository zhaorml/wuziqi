# 网络五子棋（Network Gomoku）

基于 Java Swing + Socket 的**网络五子棋**桌面程序，面向对象程序设计课程设计作品。

支持局域网联机对战、人机对战、实时聊天、悔棋、对局回放与存档。

## 功能特性

| 功能 | 说明 |
| --- | --- |
| 单机人机对战 | 内置评分制 AI，可执黑先行或执白后行 |
| 局域网联机 | 一方开房间（默认端口 `8888`），另一方填 IP 加入 |
| 实时聊天 | 对局中可与对手发送文字消息 |
| 悔棋 | 联机模式下经对手同意后回退一步 |
| 对局回放 | 读取 `.wzg` 记录文件，逐步/自动复盘 |
| 对局存档 | 每局结束自动写入 `records/game_yyyyMMdd_HHmmss.wzg` |

## 技术要点

- **Swing GUI**：`BoardPanel` 自绘棋盘与棋子，`MainFrame` 负责主界面与状态机
- **Socket 网络通信**：`GameServer` / `GameClient` 基于 TCP，使用 `ObjectInputStream` / `ObjectOutputStream` 传输序列化的 `Message` 对象
- **多线程**：服务端用 `ExecutorService` 线程池管理客户端连接，网络收发在独立线程中进行，避免阻塞事件分发线程（EDT）
- **五子棋算法**：`Board` 负责落子、胜负判定与棋型统计；`AIPlayer` 采用棋型评分（成五 / 活四 / 冲四 / 活三）选取最优落点
- **文件持久化**：`GameRecorder` 以纯文本格式读写对局记录

## 目录结构

```
.
├── src/com/wuziqi/
│   ├── Main.java               # 程序入口
│   ├── model/                  # 领域模型
│   │   ├── Board.java          # 棋盘、胜负判定、棋型分析
│   │   ├── Move.java           # 一步落子
│   │   ├── Piece.java          # 棋子（黑/白/空）
│   │   └── AIPlayer.java       # 人机对战 AI
│   ├── net/                    # 网络层
│   │   ├── GameServer.java     # TCP 服务端
│   │   ├── GameClient.java     # TCP 客户端
│   │   ├── Message.java        # 通信消息体（可序列化）
│   │   └── MessageType.java    # 消息类型枚举
│   ├── ui/                     # 界面层
│   │   ├── MainFrame.java      # 主窗口
│   │   ├── BoardPanel.java     # 棋盘绘制与鼠标交互
│   │   ├── StartDialog.java    # 启动模式选择对话框
│   │   ├── ChatPanel.java      # 聊天面板
│   │   └── ReplayFrame.java    # 回放窗口
│   └── util/
│       └── GameRecorder.java   # 对局记录读写
├── samples/                    # 示例对局记录
├── MANIFEST.MF                # 打包可执行 jar 用的清单
├── build.bat                  # Windows 一键编译
└── run.bat                    # Windows 一键运行
```

## 环境要求

- JDK 8 及以上（已在 JDK 21 下验证编译）
- 无需第三方依赖
- 联机功能要求双方网络互通，且防火墙放行所选端口

## 编译

Windows 下双击 `build.bat`，或手动执行：

```bat
mkdir classes
javac -encoding UTF-8 -d classes src\com\wuziqi\*.java src\com\wuziqi\model\*.java src\com\wuziqi\net\*.java src\com\wuziqi\ui\*.java src\com\wuziqi\util\*.java
```

macOS / Linux：

```bash
mkdir -p classes
javac -encoding UTF-8 -d classes $(find src -name "*.java")
```

## 运行

Windows 下双击 `run.bat`，或手动执行：

```bash
java -cp classes com.wuziqi.Main
```

> 请在项目根目录下启动，程序会把对局记录写入当前工作目录的 `records/`。

### 打包成可执行 jar（可选）

```bash
jar cfm wuziqi.jar MANIFEST.MF -C classes .
java -jar wuziqi.jar
```

## 联机对战步骤

1. **主机方**：启动后选择「创建房间」，默认端口 `8888`，点击开始等待对手连接。
2. **加入方**：选择「加入房间」，填写主机局域网 IP 与端口 `8888`。
3. 双方连接成功后自动开始对局，黑棋先行。
4. 对局中可随时发送聊天消息；点击悔棋需对手同意。

同一台机器测试联机时，加入方填写 `127.0.0.1` 即可。

## 对局记录格式

`records/*.wzg` 为纯文本，便于查看与版本管理。`#` 开头为注释，`---` 之后每行一步，字段为 `步数,行,列,棋子值`（行/列从 0 起，棋子值 `1` = 黑、`2` = 白）：

```
# 五子棋对局记录
# 日期: Thu Sep 17 19:51:04 CST 2026
# 胜者: WHITE
# 总步数: 74
---
1,6,6,1
2,5,7,2
3,6,8,1
```

完整示例见 [`samples/game_20260917_195104.wzg`](samples/game_20260917_195104.wzg)。

## 许可

本项目为课程设计作品，可自由学习与参考。
