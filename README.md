# JoyProxy

Android proxy client built on sing-box `libbox` and `VpnService`.

## Features

- HTTP / SOCKS5 proxy support (IP or domain + port)
- Three proxy scopes: global / whitelist (selected apps only) / blacklist (exclude selected apps)
- DNS options: Fake-IP, DoH, custom DNS, system default
- Configurable language (English by default, switchable to Chinese in app settings)
- No root required

## Build

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
cp libbox.aar ../joy-proxy-android/app/libs/
```

3. Build the APK:

```bash
./gradlew assembleRelease
```

APK output: `app/build/outputs/apk/release/app-release.apk`

## License

This project uses sing-box libbox (GPLv3) and is licensed under GPLv3.
