@file:Suppress("UnstableApiUsage")

// Import necessary Kotlin Gradle DSL
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Android Application Plugin (from version catalog)
    alias(libs.plugins.android.application)

    // Kotlin Android Plugin
    alias(libs.plugins.kotlin.android)

    // Kotlin Annotation Processing Tool (for Room and others)
    alias(libs.plugins.kotlin.kapt)

    // Kotlin Compose Plugin (required for Jetpack Compose)
    alias(libs.plugins.kotlin.compose)
}

android {
    // Application namespace matches your package structure
    namespace = "com.example.fitnesstrackerapp"

    // Compile SDK version 35 (Android 14 compatibility)
    compileSdk = 35

    defaultConfig {
        // Application ID for Play Store and installation
        applicationId = "com.example.fitnesstrackerapp"

        // Define supported Android versions
        minSdk = 24
        targetSdk = 35

        // Versioning for releases
        versionCode = 1
        versionName = "1.0"

        // Test runner configuration for instrumented tests
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Vector drawable support for backward compatibility
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            // Disable code shrinking by default (enable in future as needed)
            isMinifyEnabled = false

            // ProGuard rules for potential code obfuscation and optimization
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        // Enable Jetpack Compose
        compose = true
    }

    composeOptions {
        // Kotlin Compose Compiler version (compatible with Kotlin 2.1.10)
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    compileOptions {
        // Java 17 compatibility for source and target
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        // JVM target compatibility to Java 17
        jvmTarget = "17"
    }

    kotlin {
        // Ensure Kotlin JVM toolchain uses Java 17
        jvmToolchain(17)
    }

    packaging {
        resources {
            // Exclude unnecessary license files from packaged APK
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --------------------------------------------------
    // Jetpack Compose BOM for version management
    // --------------------------------------------------
    implementation(platform(libs.androidx.compose.bom))

    // --------------------------------------------------
    // Core Compose UI components and Material 3 support
    // --------------------------------------------------
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // --------------------------------------------------
    // Lifecycle components (for state management in Compose)
    // --------------------------------------------------
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // --------------------------------------------------
    // LiveData support (for integration with legacy components)
    // --------------------------------------------------
    implementation(libs.androidx.runtime.livedata)

    // --------------------------------------------------
    // Room Database dependencies
    // --------------------------------------------------
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // --------------------------------------------------
    // Android Core and Kotlin Coroutines for async operations
    // --------------------------------------------------
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // --------------------------------------------------
    // Debug-only Compose tools (previewing and inspection)
    // --------------------------------------------------
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // --------------------------------------------------
    // Unit testing libraries
    // --------------------------------------------------
    testImplementation(libs.junit)

    // --------------------------------------------------
    // Android Instrumentation (UI) testing dependencies
    // --------------------------------------------------
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
