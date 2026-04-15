# Repository Guidelines

## Project Structure & Module Organization
`XyMusic-KMP` is a Kotlin Multiplatform project targeting Android, iOS, and Desktop. Shared app code lives in `composeApp/src/commonMain`, with platform-specific code in `androidMain`, `iosMain`, and `jvmMain`. Reusable libraries are split into `ui`, `api`, `localdata`, `download`, `xy-database`, and `xy-platform`, each using the same KMP source-set layout. Android packaging lives in `androidApp`, the desktop launcher in `desktopApp`, and the Xcode entry point in `iosApp`.

Keep new shared business logic in a library module or `composeApp/commonMain` first. Treat `build/`, crash logs, and generated outputs as disposable.

## Build, Test, and Development Commands
Use the Gradle wrapper from the repository root.

- `.\gradlew.bat :androidApp:assembleDebug` builds the Android debug app.
- `.\gradlew.bat :desktopApp:run` starts the desktop client.
- `.\gradlew.bat check` runs the standard verification tasks across modules.
- `.\gradlew.bat :composeApp:check` verifies the shared app module and its tests.
- `.\gradlew.bat :androidApp:testDebugUnitTest` runs Android JVM tests.

If Gradle cannot use the default home in your environment, set `GRADLE_USER_HOME` to the repo-local `.gradle-user-home`.

## Coding Style & Naming Conventions
Follow Kotlin defaults: 4-space indentation, no tabs, trailing commas only when they improve diffs, and concise expression bodies where readable. Use `PascalCase` for classes, objects, and composables, `camelCase` for functions and properties, and lower-case package names under `cn.xybbz.*`.

Prefer shared dependencies through `gradle/libs.versions.toml` instead of hardcoded coordinates. Keep platform-specific APIs out of `commonMain`.

## Testing Guidelines
Shared tests live under `src/commonTest/kotlin` and use `kotlin.test`. Android local tests use `src/androidHostTest/kotlin`; instrumented tests use `src/androidDeviceTest/kotlin` or `androidApp/src/androidTest/java`. Name tests after the subject under test, for example `PasswordUtilsTest`, and write method names that describe behavior such as `encryptAndDecryptRoundTrip`.

## Commit & Pull Request Guidelines
Recent history uses short imperative subjects in Chinese, for example `完善播放器功能`. Keep commits focused and module-specific. Pull requests should include a brief summary, affected modules, linked issues, test commands run, and screenshots or recordings for UI changes on Android/Desktop when applicable.

## Configuration Notes
Do not commit `local.properties`, local SDK paths, or temporary logs such as `hs_err_pid*.log` and `replay_pid*.log`.

<!-- TRELLIS:START -->
# Trellis Instructions

These instructions are for AI assistants working in this project.

Use the `/trellis:start` command when starting a new session to:
- Initialize your developer identity
- Understand current project context
- Read relevant guidelines

Use `@/.trellis/` to learn:
- Development workflow (`workflow.md`)
- Project structure guidelines (`spec/`)
- Developer workspace (`workspace/`)

If you're using Codex, project-scoped helpers may also live in:
- `.agents/skills/` for reusable Trellis skills
- `.codex/agents/` for optional custom subagents

Keep this managed block so 'trellis update' can refresh the instructions.

<!-- TRELLIS:END -->

