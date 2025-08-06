/**
 * App Module Gradle Build Script (Kotlin DSL)
 *
 * Responsibilities:
 * - Configures the Android application plugin, Kotlin, Compose, DI, and all dependencies for the app.
 * - Sets up build types, Java/Kotlin compatibility, Compose, and packaging options.
 * - Follows consistent Javadoc-style comment structure.
 */

plugins {
    // Android application plugin
    alias(libs.plugins.android.application)
    // Kotlin Android plugin
    alias(libs.plugins.kotlin.android)
    // Kotlin annotation processing (for Room, Hilt)
    alias(libs.plugins.ksp)
}

android {
    /**
     * Android configuration block
     *
     * - Sets namespace, SDK versions, and default config.
     * - Configures build types, Java/Kotlin compatibility, Compose, and packaging.
     */
    namespace = "com.example.fitnesstrackerapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fitnesstrackerapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests.isReturnDefaultValues = true
    }

    buildFeatures {
        // Enable Jetpack Compose and ViewBinding
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    // Java and Kotlin compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // Explicitly set the source sets
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            java.srcDirs("src/main/java")
            res.srcDirs("src/main/res")
        }
        getByName("test") {
            java.srcDirs("src/test/java")
        }
        getByName("androidTest") {
            java.srcDirs("src/androidTest/java")
        }
    }

    // Handle potential packaging conflicts
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Configuration for Room schema
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
        arg("room.expandProjection", "true")
    }

    // Test configuration
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    /**
     * Dependencies block
     *
     * - Declares all libraries and tools used in the app.
     * - Groups dependencies by feature for clarity.
     */

    // AndroidX Core and Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // AppCompat for theme support
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Room dependencies
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.identity.jvm)
    ksp(libs.room.compiler)
    testImplementation(libs.room.testing)

    // Compose UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.kotlinx.coroutines.test)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Material Icons Extended for complete icon set
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Security
    implementation(libs.androidx.security.crypto)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Additional testing utilities
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestUtil(libs.androidx.test.orchestrator)
}
