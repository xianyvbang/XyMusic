import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

val keystoreProperties = Properties()

// 尝试读取 keystore.properties，如果不存在（例如本地未配置），就跳过
val keystorePropertiesFile = rootProject.file("app/keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

val versionCodeNumber:String? by project
val versionString:String? by project


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.hilt)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "cn.xybbz"
    compileSdk = 36

    defaultConfig {
        applicationId = "cn.xybbz"
        minSdk = 34
        targetSdk = 36
//        versionCode = 1
//        versionName = "0.0.1"

        if (!versionCodeNumber.isNullOrBlank()){
            versionCode = (versionCodeNumber)?.toInt()
        }

        if (!versionString.isNullOrBlank()){
            versionName = versionString

        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            // 只有在配置文件存在时才设置
            if (keystoreProperties.isNotEmpty()) {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            // Enables code-related app optimization.
            isMinifyEnabled = true
            // Enables resource shrinking.
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")
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
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }


}
composeCompiler {

    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFiles = setOf(
        rootProject.layout.projectDirectory.file("stability_config.conf")
    )
}


dependencies {
    implementation(libs.kotlinx.serialization.json)
    //颜色提取
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    //异常捕获
    implementation(libs.androidx.runtime)

    //图标
    implementation(libs.androidx.material.icons.extended)
//    //跟ListItem差不多,只是可以自定义位置
    implementation(libs.androidx.constraintlayout)
    //路由导航
    implementation(libs.androidx.navigation.compose)
    //权限
    implementation(libs.accompanist.permissions)
    //图片加载
    implementation(libs.coil.compose)
    //coil加载gif
    implementation(libs.coil.gif)


    //work 后台调度-定时任务
    implementation(libs.androidx.work.runtime.ktx)
    //音乐播放 https://android-dot-google-developers.gonglchuangl.net/jetpack/androidx/releases/media3

    // For media playback using ExoPlayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.datasource.okhttp)
    // For exposing and controlling media sessions
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.exoplayer.hls)

    //moshi 数据解析 类似json
    implementation(libs.squareup.converter.moshi)
    implementation(libs.moshi.kotlin)

    //启动页面
    implementation(libs.androidx.core.splashscreen)
    //json
//    implementation(libs.google.gson)
    //分页加载
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)

    //service
    implementation(libs.androidx.lifecycle.service)
    //弹窗
    implementation(libs.dialogx)

    //hilt
    implementation(libs.google.hilt)
    ksp(libs.google.hilt.compiler)
    implementation(libs.google.hilt.navigation)

    //多语种适配框架
    implementation(libs.multi.languages)

    //颜色选择工具
    implementation(libs.compose.colorpicker)

    //UI的module
    implementation(project(path = ":ui"))
    //api的module
    implementation(project(":api"))

    //本地缓存的module
    implementation(project(":localdata"))
    implementation(project(":download"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}