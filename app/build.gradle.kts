/**
 * App Module Gradle Build Script (Kotlin DSL)
 *
 * Purpose:
 * - Configures the Android application plugin, Kotlin, Compose, DI, Firebase, and all dependencies for the app
 * - Sets up build types, Java/Kotlin compatibility, Compose, and packaging options
 */

plugins {
    // Android application plugin
    alias(libs.plugins.android.application)
    // Kotlin Android plugin
    alias(libs.plugins.kotlin.android)
    // Kotlin annotation processing (for Room, Hilt)
    id("kotlin-kapt")
    // Hilt for dependency injection
    alias(libs.plugins.hilt.android)
    // Google services (for Firebase)
    alias(libs.plugins.google.services)
}

android {
    // App namespace and SDK versions
    namespace = "com.example.fitnesstrackerapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fitnesstrackerapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            // Disable code shrinking for release builds
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    // Java and Kotlin compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    // Enable Jetpack Compose
    buildFeatures {
        compose = true
    }
    
    // Compose compiler version
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    
    // Exclude some license files from packaging
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM for version alignment
    implementation(platform(libs.androidx.compose.bom))
    
    // Core Android libraries
    implementation(libs.androidx.core.ktx) // Kotlin extensions for Android core
    implementation(libs.androidx.lifecycle.runtime.ktx) // Lifecycle-aware components
    implementation(libs.androidx.activity.compose) // Compose integration for activities
    
    // Jetpack Compose UI
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling) // UI tooling for preview/debug
    implementation(libs.androidx.compose.material3) // Material 3 design system
    implementation(libs.androidx.navigation.compose) // Navigation for Compose
    implementation(libs.androidx.hilt.navigation.compose) // Hilt navigation integration
    implementation(libs.androidx.lifecycle.runtime.compose) // Compose lifecycle
    
    // Room Database (local storage)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    
    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.work) // Hilt integration for WorkManager
    kapt(libs.androidx.hilt.compiler)

    // WorkManager for background tasks (reminders, step sync, etc.)
    implementation(libs.androidx.work.runtime.ktx)
    
    // Kotlin Coroutines for async/background work
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // Firebase Authentication
    implementation(libs.firebase.auth.ktx)
    
    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
