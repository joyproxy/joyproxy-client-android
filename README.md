# JoyProxy Android Client

[English](#english) | [中文](#中文)

---

<a id="english"></a>

## English

Android proxy client built on sing-box `libbox`.

### Features

- HTTP / SOCKS5 proxy support (IP or domain + port)
- Three proxy scopes: global / whitelist (selected apps only) / blacklist (exclude selected apps)
- Configurable language (English by default, switchable to Chinese in app settings)
- No root required

### Download

Get the latest signed APK from [Releases](https://github.com/joyproxy/joyproxy-client-android/releases).

1. Open the latest release page
2. Download `app-release.apk`
3. On your Android device, allow installation from unknown sources if prompted
4. Install the APK

### Usage

1. Open JoyProxy and enter the proxy server address and port (HTTP or SOCKS5)
2. Optionally enter username and password
3. Tap **Test** to verify the proxy is reachable (only available while disconnected)
4. Choose a proxy scope:
   - **Global** — route traffic from all apps through the proxy
   - **Whitelist** — only selected apps use the proxy
   - **Blacklist** — selected apps bypass the proxy
5. Tap **Connect** and grant the system connection permission when prompted
6. When connected, a key icon appears in the status bar

To disconnect, tap **Disconnect** in the app.

> After changing proxy scope or the app list, disconnect and reconnect for the changes to take effect.

### Build from Source

This project depends on `libbox.aar` (sing-box core), which is not committed to the repository. GitHub Actions builds it automatically from sing-box v1.13.13 during CI.

**Local build:**

1. Install JDK 17, Android SDK, Go 1.23, and NDK 28
2. Build libbox:

```bash
git clone --depth 1 --branch v1.13.13 https://github.com/SagerNet/sing-box.git
cd sing-box
go install github.com/sagernet/gomobile/cmd/gomobile@v0.1.12
go install github.com/sagernet/gomobile/cmd/gobind@v0.1.12
export PATH="$(go env GOPATH)/bin:$PATH"
gomobile init
go run ./cmd/internal/build_libbox/main.go -target android
cp libbox.aar ../joyproxy-client-android/app/libs/
```

3. Build the APK:

```bash
./gradlew assembleRelease
```

APK output: `app/build/outputs/apk/release/app-release.apk`

### License

This project uses sing-box libbox (GPLv3) and is licensed under GPLv3.

---

<a id="中文"></a>

## 中文

基于 sing-box `libbox` 的 Android 代理客户端。

### 功能特性

- 支持 HTTP / SOCKS5 代理（IP 或域名 + 端口）
- 三种代理范围：全局 / 白名单（仅选中应用）/ 黑名单（排除选中应用）
- 可切换界面语言（默认英文，可在应用设置中切换为中文）
- 无需 Root

### 下载

从 [Releases](https://github.com/joyproxy/joyproxy-client-android/releases) 页面获取最新签名 APK。

1. 打开最新 Release 页面
2. 下载 `app-release.apk`
3. 在 Android 设备上允许安装未知来源应用（如有提示）
4. 安装 APK

### 使用说明

1. 打开 JoyProxy，填写代理服务器地址和端口（HTTP 或 SOCKS5）
2. 可选填写用户名和密码
3. 点击 **测试** 验证代理连通性（仅未连接时可用）
4. 选择代理范围：
   - **全局** — 所有应用流量走代理
   - **白名单** — 仅选中应用走代理
   - **黑名单** — 选中应用不走代理，其余走代理
5. 点击 **连接代理**，按提示授予系统连接权限
6. 连接成功后，状态栏会出现钥匙图标

断开连接：在应用中点击 **断开**。

> 修改代理范围或应用列表后，需断开并重新连接方可生效。

### 从源码构建

本项目依赖 `libbox.aar`（sing-box 核心），未提交到仓库。CI 会自动从 sing-box v1.13.13 编译。

**本地构建：**

1. 安装 JDK 17、Android SDK、Go 1.23、NDK 28
2. 编译 libbox（命令同上）
3. 执行 `./gradlew assembleRelease`

APK 输出路径：`app/build/outputs/apk/release/app-release.apk`

### 许可证

本项目使用 sing-box libbox（GPLv3），整体遵循 GPLv3 许可证。
