plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.javaopencv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.javaopencv"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}



dependencies {
    implementation (libs.cardview)
    implementation (libs.mpandroidchart)
    implementation ("androidx.core:core:1.16.0")
    implementation ("com.google.android.material:material:1.12.0")
    implementation (libs.material)
    implementation("androidx.camera:camera-extensions:1.4.2")
    implementation ("androidx.camera:camera-core:1.4.2")
    implementation ("androidx.camera:camera-camera2:1.4.2")
    implementation ("androidx.camera:camera-lifecycle:1.4.2")
    implementation ("androidx.camera:camera-view:1.4.2")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.room:room-runtime:2.7.0")
    annotationProcessor("androidx.room:room-compiler:2.7.0")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation(project(":openCVLibrary"))
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
