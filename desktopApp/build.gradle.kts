import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.api.tasks.Sync
import java.io.File

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// 生成到 build 目录的临时资源目录，后续会作为 desktopApp 的 resources 一部分参与打包。
val generatedVlcResourcesDir = layout.buildDirectory.dir("generated/vlcResources/main")
// 约定的本地 VLC 运行时压缩包位置。
val defaultVlcRuntimeArchive = layout.projectDirectory.file("vlc/windows-x64/vlc-runtime.zip")
// 支持通过 Gradle 属性或环境变量覆盖默认压缩包路径，便于 CI/本地构建切换运行时来源。
val configuredVlcRuntimeArchive = providers.gradleProperty("vlcRuntimeArchive")
    .orElse(providers.environmentVariable("VLC_RUNTIME_ARCHIVE"))

val bundledVlcRuntimeArchivePath: String = configuredVlcRuntimeArchive.orNull
    ?.let { project.file(it).absolutePath }
    ?: defaultVlcRuntimeArchive.asFile.absolutePath

val prepareBundledVlcRuntime = tasks.register<Sync>("prepareBundledVlcRuntime") {
    val archiveFile = File(bundledVlcRuntimeArchivePath)
    into(generatedVlcResourcesDir)
    if (archiveFile.isFile) {
        // 将外部 zip 统一拷贝为固定资源名，运行时只需要按约定路径读取。
        from(archiveFile) {
            into("vlc/windows-x64")
            rename { "vlc-runtime.zip" }
        }
    }
}

kotlin {
    dependencies {
        implementation(projects.composeApp)

        implementation(compose.desktop.currentOs)
        implementation(libs.kotlinx.coroutinesSwing)
        implementation(libs.kotlin.ktor.apache)
        implementation(project.dependencies.platform(libs.koin.bom))
        implementation(libs.koin.core)
    }
}

sourceSets.named("main") {
    // 把生成的 VLC 资源目录并入主资源集，确保 run/package 时都能带上 zip。
    resources.srcDir(generatedVlcResourcesDir)
}

tasks.named("processResources") {
    // 在处理 resources 前先准备好 VLC zip。
    dependsOn(prepareBundledVlcRuntime)
}

compose.desktop {
    application {
        mainClass = "cn.xybbz.MainKt"

        buildTypes.release.proguard {
            obfuscate.set(true)
            optimize.set(true)
            joinOutputJars.set(false)
            configurationFiles.from(project.file("compose-desktop.pro"))
        }
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "cn.xybbz"
            packageVersion = "1.0.0"
        }
    }
}
