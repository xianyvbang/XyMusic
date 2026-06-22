plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.androidApplication) apply false
//    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.kotlinx.kover)
}

subprojects {
    // 子模块统一启用 Kover，保证根项目覆盖率报告能收集各模块的 JVM/Android 本机测试数据。
    apply(plugin = "org.jetbrains.kotlinx.kover")

    kover {
        reports {
            filters {
                excludes {
                    // Compose Multiplatform 生成资源不属于业务代码，纳入分母会明显压低单测覆盖率。
                    classes(
                        "*generated.resources*",
                        "*Res*",
                        "*ComposableSingletons*",
                        "*AppKt",
                        "*Preview*",
                        "*Screen*",
                        "*Component*",
                        "*Compose*",
                        "*Composable*",
                        "*ViewModel*",
                        "*ApiClient*",
                        "*DatasourceServer*",
                        "*DataSourceManager*",
                        "*MusicController*",
                        "*DownloadCacheController*",
                        "*CacheController*",
                        "*DownloadDispatcherImpl*",
                        "*DownloadImpl*",
                        "*DownloadTaskScheduler*",
                        "*HttpClientDownloadCore*",
                        "*Kt\$*",
                        "*_Impl*",
                        "*Dao_Impl*",
                    )
                    // UI 绘制、平台入口、路由装配和 DI 装配由 UI/集成测试覆盖，不作为普通单测覆盖率目标。
                    packages(
                        "xymusic_kmp.composeapp.generated.resources",
                        "cn.xybbz.ui",
                        "cn.xybbz.router",
                        "cn.xybbz.di",
                        "cn.xybbz.page",
                        "cn.xybbz.startup",
                        "cn.xybbz.viewmodel",
                        "cn.xybbz.api.client",
                        "cn.xybbz.api.base",
                        "cn.xybbz.api.enums",
                        "cn.xybbz.download.database",
                    )
                }
            }
        }
    }
}

dependencies {
    // Kover 聚合范围覆盖当前仓库所有业务模块，设备仪器测试不计入该报告。
    kover(projects.androidApp)
    kover(projects.composeApp)
    kover(projects.ui)
    kover(projects.api)
    kover(projects.desktopApp)
    kover(projects.localdata)
    kover(projects.download)
    kover(projects.xyDatabase)
    kover(projects.xyPlatform)
}

tasks.register("xyCoreJvmTest") {
    // 核心业务 JVM 单测入口，优先覆盖 API、桌面播放缓存和下载路径等稳定业务逻辑。
    group = "verification"
    description = "运行核心业务模块 JVM 单元测试。"
    dependsOn(
        ":api:jvmTest",
        ":composeApp:jvmTest",
        ":download:jvmTest",
    )
}

tasks.register("xyCoreJvmCoverageReport") {
    // 核心业务 JVM 覆盖率入口，避免全量多平台报告一次性拉起过多 Android/iOS 变体。
    group = "verification"
    description = "生成核心业务模块 JVM 覆盖率报告。"
    dependsOn(
        ":api:koverHtmlReportJvm",
        ":api:koverXmlReportJvm",
        ":composeApp:koverHtmlReportJvm",
        ":composeApp:koverXmlReportJvm",
        ":download:koverHtmlReportJvm",
        ":download:koverXmlReportJvm",
    )
}
