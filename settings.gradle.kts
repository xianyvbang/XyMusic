pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
//        maven(url = "https://maven.aliyun.com/repository/public/")
//        maven(url = "https://maven.aliyun.com/repository/google")
//        maven(url = "https://maven.aliyun.com/repository/central")
//        maven(url = "https://maven.aliyun.com/repository/jcenter")
//        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
        maven(url = "https://jitpack.io")
//        maven(url = "https://maven.aliyun.com/repository/public")
        mavenCentral()
    }
}

rootProject.name = "Xybbz-app"
include(":app")
include(":ui")
include(":api")
include(":localdata")
