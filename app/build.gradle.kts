plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace   = "com.example.javaopencv"
    compileSdk  = 35

    defaultConfig {
        applicationId        = "com.example.javaopencv"
        minSdk               = 26
        targetSdk            = 35
        versionCode          = 1
        versionName          = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        // Java 11
        sourceCompatibility            = JavaVersion.VERSION_11
        targetCompatibility            = JavaVersion.VERSION_11
        // bật desugaring
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // --- Apache POI ---
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3") {
        // loại bỏ cái schemas cũ gây trùng class
        exclude(group = "org.apache.poi", module = "poi-ooxml-schemas")
    }

    // desugaring cho MethodHandle, java.time, v.v.
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // Các dependency còn lại giữ nguyên (đổi alias nếu cần)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.gridlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.appcompat.v161)
    implementation(libs.cardview)
    implementation(libs.mpandroidchart)
    implementation(libs.core)
    implementation(libs.camera.extensions)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.gson)
    implementation(project(":openCVLibrary"))
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
