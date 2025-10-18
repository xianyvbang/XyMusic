import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "cn.xybbz.localdata"
    compileSdk = 35

    defaultConfig {
        minSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        compilerOptions  {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    room {
        // Applies to 'demoDebug' only
        schemaDirectory("demoDebug", "$projectDir/schemas/demoDebug")

        // Applies to 'demoDebug' and 'demoRelease'
        schemaDirectory("demo", "$projectDir/schemas/demo")

        // Applies to 'demoDebug' and 'fullDebug'
        schemaDirectory("debug", "$projectDir/schemas/debug")

        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {

    //Room 保存数据
    api(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    // To use Kotlin Symbol Processing (KSP)
    ksp(libs.androidx.room.compiler)
    // optional - Kotlin Extensions and Coroutines support for Room
    api(libs.androidx.room.ktx)
    // optional - Paging 3 Integration
    api(libs.androidx.room.paging)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}