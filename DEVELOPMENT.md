# JoyProxy Android Development / Android 开发情况

[English](#english) | [中文](#中文)

> Last updated / 最后更新：2026-06-16  
> Current version / 当前版本：**1.0.11** (versionCode 12)  
> Repository / 仓库：https://github.com/joyproxy/joyproxy-client-android

---

<a id="english"></a>

## English

### 1. Overview

JoyProxy is an Android proxy client. Users enter a proxy address (IP or domain + port) to forward device or per-app traffic to HTTP / SOCKS5 proxy servers — no root required.

Core architecture:

- **Traffic capture**: Android system tunnel via `VpnService`
- **Protocol handling**: sing-box `libbox` (Go core, gomobile → AAR) for proxy forwarding and routing
- **UI layer**: Jetpack Compose + ViewModel + DataStore persistence

### 2. Tech Stack

| Category | Choice |
|----------|--------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 |
| Proxy core | sing-box v1.13.13 / libbox.aar |
| Persistence | DataStore Preferences |
| Serialization | kotlinx.serialization (proxy history JSON) |
| Build | Gradle 8.11.1 + JDK 17 |
| CI | GitHub Actions (libbox + signed APK + Release) |

### 3. Architecture

```
UI Layer → Config Layer → Data Layer → Tunnel Layer (VpnController → ProxyVpnService → libbox)
```

Key modules: `ProxyVpnService`, `BoxService`, `ConfigBuilder`, `ProxyTester`, `AppPreferencesStore`.

### 4. Connection Flow

1. User taps **Connect**
2. System connection permission requested (status bar key icon)
3. Notification permission (Android 13+)
4. Config passed via `Intent` to `ProxyVpnService`
5. libbox initializes → sing-box config → TUN established
6. `VpnStatusBus` updates UI state

### 5. Implemented Features

- HTTP / SOCKS5, IP or domain + port, optional auth
- Paste `ip:port` or `[ipv6]:port` auto-split
- Proxy connectivity test (disconnected only)
- Proxy history (max 20, saved on successful connect)
- Global / Whitelist / Blacklist scope with app picker
- Foreground service + status bar key icon
- Crash log at `Android/data/com.joyproxy.app/files/crash.log`
- GitHub Actions signed Release APK

### 6. CI / Release

- Trigger: push to `main`/`master` or `workflow_dispatch`
- Steps: build libbox v1.13.13 → Gradle `assembleRelease` → GitHub Release
- Downloads: https://github.com/joyproxy/joyproxy-client-android/releases
- Secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`

### 7. Local Development

Requirements: JDK 17, Android SDK (compileSdk 35), NDK 28, Go 1.23+

```bash
git clone --depth 1 --branch v1.13.13 https://github.com/SagerNet/sing-box.git
cd sing-box
go install github.com/sagernet/gomobile/cmd/gomobile@v0.1.12
go install github.com/sagernet/gomobile/cmd/gobind@v0.1.12
export PATH="$(go env GOPATH)/bin:$PATH"
gomobile init
go run ./cmd/internal/build_libbox/main.go -target android
cp libbox.aar ../joyproxy-client-android/app/libs/
./gradlew assembleRelease
```

### 8. User Guide (Brief)

1. Install APK
2. Enter proxy address and port (or pick from history)
3. Choose proxy scope (global / whitelist / blacklist)
4. Tap **Connect** → grant system permission → grant notification if prompted
5. Key icon in status bar means connected

Debug logs: `crash.log`, `stderr.log` under app files directory.

### 9. Known Limitations

| Item | Description |
|------|-------------|
| Protocols | HTTP / SOCKS5 only |
| Hot reload | Scope/app list changes require reconnect |
| History limit | 20 entries max |
| Platform | Android only |

### 10. License

Uses sing-box libbox (**GPLv3**). See [LICENSE](LICENSE).

---

<a id="中文"></a>

## 中文

### 一、项目概述

JoyProxy 是一款面向 Android 的代理客户端。用户填写代理地址（IP 或域名 + 端口），即可将整机或指定应用的流量转发到 HTTP / SOCKS5 代理服务器，无需 Root。

核心思路：

- **流量拦截**：Android `VpnService` 建立 TUN 虚拟网卡
- **协议处理**：sing-box `libbox`（Go 核心，gomobile 编译为 AAR）负责代理转发与路由
- **界面层**：Jetpack Compose + ViewModel + DataStore 持久化

### 二、技术栈

| 类别 | 选型 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose（Material 3） |
| 最低 SDK | 24（Android 7.0） |
| 目标 SDK | 35 |
| 代理核心 | sing-box v1.13.13 / libbox.aar |
| 持久化 | DataStore Preferences |
| 序列化 | kotlinx.serialization（代理历史 JSON） |
| 构建 | Gradle 8.11.1 + JDK 17 |
| CI | GitHub Actions（自动编译 libbox + 签名 APK + Release） |

### 三、架构说明

```
UI 层 → 配置层 → 数据层 → 隧道层（VpnController → ProxyVpnService → libbox）
```

关键模块：`ProxyVpnService`、`BoxService`、`ConfigBuilder`、`ProxyTester`、`AppPreferencesStore`。

### 四、连接流程

1. 用户点击「连接代理」
2. 请求系统连接权限（状态栏钥匙图标）
3. 请求通知权限（Android 13+）
4. 配置通过 `Intent` 直传 `ProxyVpnService`
5. 等待 `libbox` 初始化 → 生成 sing-box 配置 → 建立 TUN
6. `VpnStatusBus` 通知 UI 连接状态

### 五、已实现功能

- HTTP / SOCKS5 协议，IP 或域名 + 端口，可选用户名密码
- 粘贴 `ip:端口` 或 `[ipv6]:端口` 自动拆分
- 代理连通性测试（仅未连接时可用）
- 代理历史记录（最多 20 条，连接成功后保存）
- 全局 / 白名单 / 黑名单 + 应用选择器
- 前台服务 + 状态栏钥匙图标
- 崩溃日志：`Android/data/com.joyproxy.app/files/crash.log`
- GitHub Actions 自动构建签名 Release APK

### 六、CI / 发布

- 触发：推送到 `main`/`master` 或 `workflow_dispatch`
- 步骤：编译 libbox v1.13.13 → Gradle `assembleRelease` → 创建 GitHub Release
- 下载：https://github.com/joyproxy/joyproxy-client-android/releases
- Secrets：`KEYSTORE_BASE64`、`KEYSTORE_PASSWORD`、`KEY_ALIAS`、`KEY_PASSWORD`

### 七、本地开发

环境：JDK 17、Android SDK（compileSdk 35）、NDK 28、Go 1.23+

编译命令见英文部分「Local Development」。

### 八、使用说明（简要）

1. 安装 APK
2. 填写代理地址和端口（可从历史记录下拉选择）
3. 选择代理范围（全局 / 白名单 / 黑名单）
4. 点击「连接代理」→ 允许系统权限 → 允许通知（如有）
5. 状态栏出现钥匙图标即表示已连接

调试日志：`crash.log`、`stderr.log`（应用文件目录下）。

### 九、已知限制

| 项目 | 说明 |
|------|------|
| 协议支持 | 仅 HTTP / SOCKS5 |
| 配置热更新 | 修改代理范围或应用列表需手动重连 |
| 历史记录上限 | 最多 20 条 |
| 平台 | 仅 Android |

### 十、许可证

本项目使用 sing-box libbox（**GPLv3**）。详见 [LICENSE](LICENSE)。
