import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

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
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            linux {
                // FileKit 的 Linux 原生文件选择器依赖该 JDK 模块访问系统认证/用户信息。
                modules("jdk.security.auth")
            }
        }
    }
}
