# JoyProxy Android Client

Android proxy client built on sing-box `libbox`.

## Features

- HTTP / SOCKS5 proxy support (IP or domain + port)
- Three proxy scopes: global / whitelist (selected apps only) / blacklist (exclude selected apps)
- Configurable language (English by default, switchable to Chinese in app settings)
- No root required

## Download

Get the latest signed APK from [Releases](https://github.com/joyproxy/joyproxy-client-android/releases).

1. Open the latest release page
2. Download `app-release.apk`
3. On your Android device, allow installation from unknown sources if prompted
4. Install the APK

## Usage

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

## Build from Source

This project depends on `libbox.aar` (sing-box core), which is not committed to the repository. GitHub Actions builds it automatically from sing-box v1.13.13 during CI.

### Local build

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

## License

This project uses sing-box libbox (GPLv3) and is licensed under GPLv3.
