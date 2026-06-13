# GitHub Actions Windows JVM 正式发布操作说明

本文档说明如何使用 `.github/workflows/release.yml` 构建 XyMusic 的 Windows JVM 桌面端正式包。该 workflow 会在一次正式发布流程中同时构建 Android release APK/AAB 和 Windows JVM release MSI；Windows 端基于 `desktopApp` 的 Compose Desktop 配置，明确运行 `:desktopApp:packageReleaseMsi`，不会运行 dev、hot reload、run 或测试任务。

## 产物

- `XyMusic-<versionName>-windows-x64.msi`
- `XyMusic-<versionName>-<versionCode>-release.apk`
- `XyMusic-<versionName>-<versionCode>-release.aab`
- `SHA256SUMS-windows.txt`
- `SHA256SUMS-android.txt`

workflow 会同时上传到 Actions artifact 和 GitHub Release。GitHub Release 的 tag 默认使用 `gradle/libs.versions.toml` 中的 `app-versionName`，格式为 `v<app-versionName>`，例如 `v1.0.0`。

## 发布前准备

1. 修改 `gradle/libs.versions.toml`：
   - `app-versionName`：Windows 包版本名，同时也是 GitHub Release tag 的来源。
   - `app-versionCode`：与 Android 共用的版本号字段，建议和 Android 发布一起递增维护。
2. 确认 GitHub Actions 权限允许写入：
   - `Settings` -> `Actions` -> `General` -> `Workflow permissions`
   - 选择 `Read and write permissions`。
3. 确认 Android 发布需要的 Repository secrets 已配置；统一 workflow 会同时构建 Android 和 Windows，并在最后统一创建或更新同一个 Release。

## 发布步骤

1. 打开 GitHub 仓库的 `Actions` 页面。
2. 选择 `发布正式应用`。
3. 点击 `Run workflow`：
   - `release_notes` 可留空，workflow 会生成基础说明。
   - `prerelease` 只在需要预发布时勾选。
4. workflow 成功后会创建或复用当前提交上的 `v<app-versionName>` tag，并把 Android APK/AAB 和 Windows MSI 上传到 GitHub Release。

## 本地验证

本地 Windows MSI 构建需要先安装 WiX Toolset：

```powershell
choco install wixtoolset --no-progress -y
```

然后运行 release MSI 打包任务：

```powershell
.\gradlew.bat :desktopApp:packageReleaseMsi --stacktrace
```

成功后，MSI 会生成在：

```text
desktopApp/build/compose/binaries/main-release/msi/
```

## 注意事项

- 同名 tag 如果已经存在但不指向当前提交，workflow 会失败；需要先提升 `app-versionName`，或人工确认旧 tag。
- `desktopApp/build.gradle.kts` 里 `packageVersion` 已经读取 `app-versionName`，Windows MSI 版本会跟发布 tag 对齐。
- 当前 workflow 生成 Android APK/AAB 和 Windows MSI；如果后续要发布 macOS DMG 或 Linux DEB，可以参考 `packageReleaseDmg` 和 `packageReleaseDeb` 增加独立 job。
- 本 workflow 不会修改代码或提交 git commit；tag 和 Release 由 GitHub Actions 使用 `GITHUB_TOKEN` 在远端创建。
