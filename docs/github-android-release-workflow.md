# GitHub Actions Android 正式发布操作说明

本文档说明如何使用 `.github/workflows/android-release.yml` 构建 XyMusic 的 Android 正式包。该 workflow 参考 Kotlin Multiplatform 官方文档的 GitHub Actions 结构，把 JDK/Gradle 初始化拆到 `.github/actions/gradle-setup/action.yml`，但这里明确构建 `release` APK/AAB，不构建 debug 包、androidTest 包或测试应用。

参考文档：https://kotlinlang.org/docs/multiplatform/github-actions-for-kmp.html#define-the-build-workflow

## 产物

- `XyMusic-<versionName>-<versionCode>-release.apk`
- `XyMusic-<versionName>-<versionCode>-release.aab`
- `SHA256SUMS.txt`

workflow 会同时上传到 Actions artifact 和 GitHub Release。GitHub Release 的 tag 默认使用 `gradle/libs.versions.toml` 中的 `app-versionName`，格式为 `v<app-versionName>`，例如 `v1.0.0`。

## 准备 GitHub Secrets

在 GitHub 仓库进入 `Settings` -> `Secrets and variables` -> `Actions`，新增以下 Repository secrets：

- `KEYSTORE_BASE64`：release keystore 文件的 base64 内容。
- `KEYSTORE_PASSWORD`：release keystore 密码。
- `KEY_ALIAS`：release key alias。
- `KEY_PASSWORD`：release key 密码。

如果还没有 release keystore，可以在本地生成：

```powershell
keytool -genkeypair -v -keystore xymusic-release.jks -alias xymusic -keyalg RSA -keysize 2048 -validity 10000
```

Windows PowerShell 生成 base64：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("xymusic-release.jks")) | Set-Content -NoNewline "keystore.base64"
```

macOS/Linux 生成 base64：

```bash
base64 xymusic-release.jks | tr -d '\n' > keystore.base64
```

`keystore.base64`、`.jks`、`.keystore`、`.p12`、`.pfx` 已加入 `.gitignore`，不要提交到仓库。

## 发布步骤

1. 修改 `gradle/libs.versions.toml`：
   - `app-versionName`：发布版本名，例如 `1.0.1`。
   - `app-versionCode`：Android 版本号，需要递增。
2. 确认 GitHub Actions 权限允许写入：
   - `Settings` -> `Actions` -> `General` -> `Workflow permissions`
   - 选择 `Read and write permissions`。
3. 打开 GitHub 仓库的 `Actions` 页面。
4. 选择 `发布 Android 正式应用`。
5. 点击 `Run workflow`：
   - `release_notes` 可留空，workflow 会生成基础说明。
   - `prerelease` 只在需要预发布时勾选。
6. workflow 成功后会创建或复用当前提交上的 `v<app-versionName>` tag，并把 APK/AAB 上传到 GitHub Release。

## 本地验证

本地正式签名构建需要先设置环境变量：

```powershell
$env:ANDROID_RELEASE_KEYSTORE_PATH="D:\path\to\xymusic-release.jks"
$env:ANDROID_RELEASE_KEYSTORE_PASSWORD="你的 keystore 密码"
$env:ANDROID_RELEASE_KEY_ALIAS="xymusic"
$env:ANDROID_RELEASE_KEY_PASSWORD="你的 key 密码"
.\gradlew.bat :androidApp:assembleRelease :androidApp:bundleRelease --stacktrace
```

没有设置这些变量时，`androidApp/build.gradle.kts` 不会绑定 release 签名配置，避免本地普通构建依赖私钥。GitHub workflow 会在缺少任一 Secret 时直接失败，防止生成未签名或测试性质的发布产物。

## 注意事项

- 同名 tag 如果已经存在但不指向当前提交，workflow 会失败；需要先提升 `app-versionName`，或人工确认旧 tag。
- 本 workflow 只发布 Android 正式包。iOS 正式发布需要 Apple 证书、描述文件和 App Store/TestFlight 流程，不能用模拟器构建替代正式应用。
- 本 workflow 不会修改代码或提交 git commit；tag 和 Release 由 GitHub Actions 使用 `GITHUB_TOKEN` 在远端创建。
