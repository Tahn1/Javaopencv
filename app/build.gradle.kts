plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.javaopencv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.javaopencv"
        minSdk = 24
        //noinspection OldTargetApi
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
    implementation ("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.room:room-runtime:2.7.0")
    implementation(libs.recyclerview)
    annotationProcessor("androidx.room:room-compiler:2.7.0")
    implementation(project(":openCVLibrary"))
    implementation(libs.appcompat)
    implementation ("androidx.lifecycle:lifecycle-livedata:2.8.7")
    implementation ("androidx.sqlite:sqlite:2.5.0")
    implementation(libs.material)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
