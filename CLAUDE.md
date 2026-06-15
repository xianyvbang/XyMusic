# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

XyMusic is a Kotlin Multiplatform (KMP) + Compose Multiplatform music client that connects to self-hosted music servers (Jellyfin, Emby, Subsonic, Navidrome, Plex). It targets Android, JVM Desktop (Windows/macOS/Linux), and iOS from a single shared codebase.

## Project conventions (from AGENTS.md)

These are hard rules for this repo:
- 不要随意提交到本地 git 或 GitHub。Only commit when explicitly asked.
- Git commit messages must be written in Chinese.
- All newly added code, methods, properties, and objects must carry Chinese comments (中文注释). Existing source follows this consistently — match it.

This project is managed by Trellis. When working knowledge exists under `.trellis/` (workflow, layer specs, tasks), read the relevant layer spec before writing code in that layer. The `.trellis/`, `.claude/`, `.codex/`, `.agents/` directories are git-ignored.

## Build, run, and test

Uses the Gradle wrapper (`./gradlew` / `gradlew.bat`) with a JVM 21 toolchain. JDK 21 is required. Dependency versions are centralized in `gradle/libs.versions.toml`; the app version (`app-versionName` / `app-versionCode`) lives there and is shared across Android and Desktop.

```bash
# Desktop (JVM) — run during development
./gradlew :desktopApp:run

# Desktop release installer (CI uses packageReleaseMsi on Windows)
./gradlew :desktopApp:packageReleaseMsi      # Windows MSI
./gradlew :desktopApp:packageReleaseDmg      # macOS
./gradlew :desktopApp:packageReleaseDeb      # Linux

# Android release
./gradlew :androidApp:assembleRelease :androidApp:bundleRelease

# Tests
./gradlew test                               # all host/unit tests
./gradlew :composeApp:jvmTest                # JVM tests for one module
./gradlew :composeApp:jvmTest --tests "fully.qualified.TestClass"   # single test class

# Lint (Android Lint is applied to KMP library modules)
./gradlew lint
```

Note: Gradle configuration cache and build cache are enabled (`gradle.properties`). The desktop release build runs ProGuard/obfuscation (`desktopApp/compose-desktop.pro`); Android release minifies via R8 (`androidApp/proguard-rules.pro`). Android release signing only activates when `ANDROID_RELEASE_KEYSTORE_*` env vars are present (CI via GitHub Secrets) — local builds stay unsigned.

## Module structure

The build is split into focused Gradle modules (see `settings.gradle.kts`). Most are KMP library modules using the `com.android.kotlin.multiplatform.library` plugin with `commonMain` + per-platform source sets (`androidMain`, `jvmMain`, `iosMain`).

- **`composeApp`** — the heart of the app. Contains UI screens, ViewModels, DI wiring, navigation, the music player controllers, and the server data-source layer. All other modules feed into it.
- **`ui`** — reusable Compose components, theming, popups, placeholders (the `cn.xybbz.ui.xy` design system, derived in part from SaltUI). No app logic.
- **`api`** — pure HTTP/server client layer. Ktor-based clients for each supported server type. No Compose, no Room.
- **`localdata`** — Room entities and DAOs for the main app database (`LocalDatabaseClient`) plus migrations.
- **`xy-database`** — shared Room base (`DatabaseClient`), platform `DatasourceFactory` (expect/actual), and type converters that `localdata` and `download` build on.
- **`download`** — self-contained download engine (dispatcher, downloader, task scheduler, its own Room `DownloadDatabaseClient`, notifications).
- **`xy-platform`** — thin platform abstraction (`ContextWrapper`) so common code can reach Android `Context` without depending on Android directly.
- **`androidApp` / `desktopApp` / `iosApp`** — thin platform entry points. They initialize Koin and launch the shared `App` composable. Desktop entry is `desktopApp/src/main/java/cn/xybbz/main.kt` (`mainClass = cn.xybbz.MainKt`); Android entry is `XyApplication` + `MainActivity`.

All packages live under `cn.xybbz`.

## Architecture: the big picture

### Multi-server data source abstraction
Supporting five different music servers behind one UI is the central design problem. The layering:

- **`api` module**: `ApiFactory` / `DefaultApiClient` define the contract for a raw server client. Concrete clients (`JellyfinApiClient`, `EmbyApiClient`, `SubsonicApiClient`, `NavidromeApiClient`, `PlexApiClient`, plus `CustomMediaApiClient`) each wrap a Ktor `HttpClient` and translate that server's protocol. These are created as Koin `@Factory` beans in `ApiModule`.
- **`IDataSourceParentServer`** (in `composeApp`): the rich domain-level server interface — login, paging media, favorites, transcoding URLs, etc. It bridges raw API clients to Room caching and Paging3 `RemoteMediator`s.
- **`DataSourceManager`**: the single façade the rest of the app talks to. It holds the *currently active* server as `dataSourceServerFlow: StateFlow<IDataSourceParentServer?>` and the active `connectionIdFlow`. Switching servers swaps the flow value; the UI gates on it being non-null. `getApiClient(downloadTypes)` selects the right client for a given operation. Treat `DataSourceManager` as the seam — feature code should depend on it, not on concrete server clients.

When adding support for server behavior, the change usually spans: the concrete `*ApiClient` (protocol) → `IDataSourceParentServer` impl (domain + caching) → exposed via `DataSourceManager`.

### Dependency injection (Koin + annotations)
DI uses Koin with the annotations/KSP compiler plugin. `StartKoinApp` is annotated `@KoinApplication` and `ViewModelModule` does `@ComponentScan("cn.xybbz")`, so most beans are discovered automatically via `@Single` / `@Factory` / `@Singleton` annotations rather than manual module DSL. Hand-written modules live in `composeApp/src/commonMain/kotlin/cn/xybbz/di/` (e.g. `DatabaseModule`, `ApiModule`), with `.jvm.kt` / `.android.kt` variants for platform-specific bindings. `initKoin()` (in `di/KoinInit.kt`) is called by each platform entry point; `startCommonKoinModule()` then kicks off `StartupInitializer` on an app-root coroutine scope.

### Local persistence (Room, two databases)
There are **two** Room databases, both built via `DatabaseModule`:
- `LocalDatabaseClient` (`localdata`) — the main cache: albums, artists, music, genres, playlists, connections, settings, play history, search history, etc.
- `DownloadDatabaseClient` (`download`) — download tasks/state, kept separate so the download engine is self-contained.

Both extend the shared `DatabaseClient : RoomDatabase()` from `xy-database`. Platform database construction goes through `DatasourceFactory` (expect/actual). Room DAOs return Paging3 `PagingSource`s that the data-source layer wraps with `RemoteMediator`s for network-backed paging.

### Platform abstraction (expect/actual)
Cross-platform differences are handled with Kotlin `expect`/`actual` (the build sets `-Xexpect-actual-classes`). Common code declares an interface or `expect`; each platform source set provides the `actual`. Examples: media playback (Media3 ExoPlayer on Android, VLCJ on JVM), network monitoring (`JvmNetWorkMonitor` vs Android), clipboard/share utils, language management, and the `ContextWrapper` in `xy-platform`. When adding platform-specific behavior, add the `expect` (or interface) in `commonMain` and implement per platform — don't `if (platform)` branch in common code.

### Navigation
Uses Jetbrains **Navigation3** (`cn.xybbz.router`), integrated with Koin ViewModels via `koin-compose-viewmodel-navigation`. Screens live in `composeApp/.../ui/screens/`, each typically paired with a ViewModel in `composeApp/.../viewmodel/`.

### Key supporting libraries
- **Sketch** for image loading (not Coil) — `SketchModule` wires it.
- **Media3** (Android) / **VLCJ** (JVM desktop) for playback, behind `MusicCommonController` and the player controllers in `config/music/`.
- **FileKit** for cross-platform file pick/save (playlist file import/export, background images).
- **kotlinx.serialization** for JSON, **cryptography-kotlin** for credential encryption, **TinyPinyin** for Chinese-to-pinyin sorting.

## Domain vocabulary (from CONTEXT.md)

Be precise with these terms — they are distinct concepts in this codebase:
- **Music Favorite Toggle**: favoriting one song from a row/menu/player. Distinct from album favorites, artist favorites, and batch operations.
- **Playlist** (server-side) vs **Playlist File** (local `.txt`/`.m3u8`).
- **Playlist File Import** (read + parse the local file) vs **Server Playlist Import** (apply parsed contents to the server, only after user confirmation). They are separate steps.
