import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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

tasks.withType<JavaExec>().configureEach {
    systemProperty("compose.application.resources.dir", project.layout.projectDirectory.dir("resources"))
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
        }
    }
}
