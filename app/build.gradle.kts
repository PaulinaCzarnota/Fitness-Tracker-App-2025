// Top-level build file for the application module.
// This file configures the Android application plugin, Kotlin, Compose, and dependencies.

plugins {
    // Apply the Android Application plugin for building Android apps.
    alias(libs.plugins.android.application)
    // Apply the Kotlin Android plugin for Kotlin language support.
    alias(libs.plugins.kotlin.android)
    // Apply the Kotlin Annotation Processing Tool (KAPT) plugin for annotation processors (e.g., Room, Hilt).
    alias(libs.plugins.kotlin.kapt)
    // Apply the Hilt Gradle plugin for Dagger Hilt dependency injection.
    alias(libs.plugins.hilt.android)
    // Apply the Google Services plugin for Firebase.
    alias(libs.plugins.google.services)
}

android {
    // Define the unique application ID for your app.
    namespace = "com.example.fitnesstrackerapp"
    // Specify the Android API level to compile against.
    compileSdk = 35

    defaultConfig {
        // The application ID for the build variant.
        applicationId = "com.example.fitnesstrackerapp"
        // The minimum Android API level required to run the app.
        minSdk = 24
        // The target Android API level for your app.
        targetSdk = 35
        // The version code of your app, an integer used for internal versioning.
        versionCode = 1
        versionName = "1.0"
        // The test instrumentation runner for Android UI tests.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Whether to enable code shrinking, obfuscation, and optimization for release builds.
            isMinifyEnabled = false
            // ProGuard rules files for release builds.
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Configure lint options
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    // Configure build features, enabling Jetpack Compose.
    buildFeatures {
        compose = true
    }

    // Configure Compose-specific options.
    composeOptions {
        // Specify the Kotlin compiler extension version for Compose.
        // This version should be compatible with your Kotlin version.
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    // Configure Java compatibility options.
    compileOptions {
        // Set source and target compatibility to Java 17.
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Configure Kotlin-specific options.
    kotlinOptions {
        // Set the JVM target version for Kotlin compilation.
        jvmTarget = "17"
    }

    // Configure packaging options for native libraries and resources.
    // This helps resolve issues with duplicate files or unstrippable native libraries,
    // which can sometimes lead to build failures or warnings.
    packaging {
        resources {
            // Exclude common META-INF files that can cause conflicts in APKs.
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // Address the warning: "Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so."
            // By using pickFirsts, we instruct Gradle to pick the first encountered native library
            // if duplicates exist across different architectures, preventing stripping issues.
            pickFirsts += listOf("lib/x86/libandroidx.graphics.path.so", "lib/x86_64/libandroidx.graphics.path.so")
        }
    }
}

// Kapt configuration block.
// This block is used to configure the Kotlin Annotation Processing Tool (KAPT).
// It's crucial for libraries like Room and Hilt that rely on annotation processing
// to generate code at compile time.
kapt {
    // Setting 'correctErrorTypes = true' can sometimes help with KAPT issues
    // by allowing the compiler to proceed even if it encounters certain types of errors
    // during annotation processing. This can sometimes reveal more specific errors
    // that were previously hidden by a generic KAPT failure.
    // Use with caution, as it might mask actual issues.
    correctErrorTypes = true
    // Removed explicit language version argument as it's not needed for Kotlin 1.9.x
}

dependencies {
    // Import the Compose Bill of Materials (BOM) to manage Compose library versions.
    // This ensures all Compose libraries use compatible versions.
    implementation(platform(libs.androidx.compose.bom))

    // Jetpack Compose
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling) // For Compose previews in Android Studio.
    implementation(libs.androidx.compose.material3) // Material Design 3 components.
    implementation(libs.androidx.activity.compose) // Integration with Activity for Compose.
    implementation("androidx.compose.runtime:runtime") // Runtime for collectAsState

    // Android Lifecycle and ViewModel components.
    implementation(libs.androidx.core.ktx) // Kotlin extensions for AndroidX Core.
    implementation(libs.androidx.lifecycle.runtime.ktx) // Lifecycle runtime KTX.
    implementation(libs.androidx.lifecycle.viewmodel.compose) // ViewModel support for Compose.
    implementation(libs.androidx.lifecycle.runtime.compose) // Lifecycle runtime for Compose.
    implementation(libs.androidx.runtime.livedata) // LiveData support for Compose.

    // Jetpack Compose Navigation for navigating between composables.
    implementation(libs.androidx.navigation.compose)

    // Room Persistence Library for local database storage.
    implementation(libs.room.runtime) // Room runtime library.
    implementation(libs.room.ktx) // Kotlin extensions for Room.
    kapt(libs.room.compiler) // Annotation processor for Room.

    // Kotlin Coroutines for asynchronous programming.
    implementation(libs.kotlinx.coroutines.core) // Core coroutines library.
    implementation(libs.kotlinx.coroutines.android) // Coroutines for Android.

    // Firebase Authentication for user authentication.
    implementation(libs.firebase.auth.ktx)

    // Dagger Hilt for dependency injection.
    implementation(libs.hilt.android) // Hilt Android library.
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0") // Hilt navigation for Compose.
    kapt(libs.hilt.compiler) // Hilt annotation processor.

    // Testing dependencies.
    testImplementation(libs.junit) // JUnit for unit tests.
    androidTestImplementation(libs.androidx.ui.test.junit4) // Compose UI testing with JUnit4.
    debugImplementation(libs.androidx.ui.test.manifest) // Required for Compose UI tests in debug builds.
}
