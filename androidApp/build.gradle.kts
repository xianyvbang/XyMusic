import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// CI 正式包签名文件路径，由 GitHub Actions 从 base64 Secret 解码后注入。
val releaseStoreFilePath = providers.environmentVariable("ANDROID_RELEASE_KEYSTORE_PATH")
// CI 正式包签名库密码，通过 GitHub Secrets 注入，避免写入仓库。
val releaseStorePassword = providers.environmentVariable("ANDROID_RELEASE_KEYSTORE_PASSWORD")
// CI 正式包签名别名，通过 GitHub Secrets 注入并匹配 release keystore。
val releaseKeyAlias = providers.environmentVariable("ANDROID_RELEASE_KEY_ALIAS")
// CI 正式包签名 key 密码，通过 GitHub Secrets 注入。
val releaseKeyPassword = providers.environmentVariable("ANDROID_RELEASE_KEY_PASSWORD")
// CI 正式包签名变量完整时才启用签名，保证本地普通构建不依赖私钥。
val hasReleaseSigningConfig = listOf(
    releaseStoreFilePath,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { it.orNull?.isNotBlank() == true }

kotlin {
    target {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    dependencies {
        implementation(projects.composeApp)
        implementation(libs.compose.uiToolingPreview)
        implementation(libs.androidx.activity.compose)
        implementation(libs.kotlin.ktor.android)
        implementation(project.dependencies.platform(libs.koin.bom))
        implementation(libs.koin.core)
        implementation(libs.koin.android)
        //启动页面
        implementation(libs.androidx.core.splashscreen)
        implementation(libs.filekit.dialogs)
    }
}
android {
    namespace = "cn.xybbz"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "cn.xybbz"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.app.versionCode.get().toInt()
        versionName = libs.versions.app.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        if (hasReleaseSigningConfig) {
            // Android 正式发布签名配置，只在 CI 或本地显式提供密钥变量时创建。
            create("release") {
                storeFile = file(releaseStoreFilePath.get())
                storePassword = releaseStorePassword.get()
                keyAlias = releaseKeyAlias.get()
                keyPassword = releaseKeyPassword.get()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            // CI 正式包使用 release keystore 签名；未提供密钥时保留默认未签名构建行为。
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}
