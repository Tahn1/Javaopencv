pluginManagement {
    repositories {
        // Thứ tự ưu tiên: Gradle Plugin Portal → Google → Maven Central → JitPack
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

dependencyResolutionManagement {
    // Cho phép dùng repositories từ settings, cấm khai báo thêm trong module
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "Javaopencv"
include(":app", ":openCVLibrary")
