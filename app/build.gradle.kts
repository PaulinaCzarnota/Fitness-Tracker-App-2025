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
    // Spotless plugin for code formatting
    id("com.diffplug.spotless")
    // ktlint plugin for Kotlin linting
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    /**
     * Android configuration block
     *
     * - Sets namespace, SDK versions, and default config.
     * - Configures build types, Java/Kotlin compatibility, Compose, and packaging.
     */
    namespace = "com.example.fitnesstrackerapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fitnesstrackerapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Enable multidex for apps with many dependencies
        multiDexEnabled = true
    }

    // ksp configuration moved outside defaultConfig
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    buildTypes {
        release {
            // Disable code shrinking for release builds
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        // Enable Jetpack Compose and ViewBinding
        compose = true
        // Enable BuildConfig generation
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Suppress incubating warning for DSL testOptions
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        animationsDisabled = true
    }

    packaging {
        // Handle potential packaging conflicts
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
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
    implementation(libs.androidx.appcompat)

    // Room dependencies
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.core.splashscreen)
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
    implementation(libs.androidx.compose.material.icons.extended)

    // Biometric Authentication
    implementation(libs.androidx.biometric)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Security
    implementation(libs.androidx.security.crypto)
    
    // Multidex support for devices with API < 21
    implementation(libs.androidx.multidex)

    // Testing
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
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

    // Compose dependencies
    implementation(libs.androidx.compose.material3)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
}

/**
 * Spotless configuration for code formatting
 *
 * Applies consistent formatting rules across all source files
 * including Kotlin, XML, and Gradle files.
 */
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint("0.50.0").editorConfigOverride(
            mapOf(
                "ktlint_standard_max-line-length" to "disabled",
                "ktlint_standard_no-wildcard-imports" to "disabled",
            ),
        )
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint("0.50.0")
    }

    format("xml") {
        target("**/*.xml")
        targetExclude("**/build/**/*.xml")
        indentWithSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }
}

/**
 * ktlint configuration for Kotlin linting
 *
 * Enforces Kotlin coding conventions and style guidelines
 */
ktlint {
    version.set("0.50.0")
    android.set(true)
    ignoreFailures.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
    // Disable some rules that conflict with our coding standards
    additionalEditorconfig = mapOf(
        "max_line_length" to "off",
    )
}
