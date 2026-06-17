import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// 桌面端包版本与 Android versionName 统一由版本目录维护。
val appVersionName = libs.versions.app.versionName.get()
// macOS 的 DMG/PKG 版本号不接受 0 开头，因此单独映射成可通过校验的版本。
val macOSPackageVersion = appVersionName.replaceFirst(Regex("^0\\."), "1.")

kotlin {
    dependencies {
        implementation(projects.composeApp)
        implementation(projects.ui)
        implementation(projects.localdata)

        implementation(compose.desktop.currentOs)
        implementation(libs.compose.material3)
        implementation(libs.compose.components.resources)
        // 桌面入口负责初始化 FileKit，并向 composeApp 注入原生弹窗 parent window。
        implementation(libs.filekit.dialogs.compose)
        implementation(libs.kotlinx.coroutinesSwing)
        implementation(libs.kotlin.ktor.apache)
        implementation(project.dependencies.platform(libs.koin.bom))
        implementation(libs.koin.core)
        runtimeOnly(libs.logback.classic)
    }
}

tasks.withType<JavaExec>().configureEach {
    systemProperty("compose.application.resources.dir", project.layout.projectDirectory.dir("resources/windows-x64"))
    systemProperty("cn.xybbz.packageVersion", appVersionName)
}

compose.desktop {
    application {
        mainClass = "cn.xybbz.MainKt"
        jvmArgs += listOf("-Dcn.xybbz.packageVersion=$appVersionName")

        buildTypes.release.proguard {
            obfuscate.set(true)
            optimize.set(true)
            joinOutputJars.set(false)
            configurationFiles.from(project.file("compose-desktop.pro"))
        }
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "cn.xybbz"
            packageVersion = appVersionName
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            macOS {
                // 只对 macOS 包做版本兼容转换，避免 DMG 校验拒绝 0 开头的版本号。
                packageVersion = macOSPackageVersion
            }
            linux {
                // FileKit 的 Linux 原生文件选择器依赖该 JDK 模块访问系统认证/用户信息。
                modules("jdk.security.auth")
            }
        }
    }
}
