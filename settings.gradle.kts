@file:Suppress("UnstableApiUsage")

// ============================================================
// Root Gradle Settings
// ============================================================
//
// Purpose:
// - Set up secure and optimized plugin & dependency resolution
// - Avoid duplicate repository definitions
// - Enforce repository mode and clean module structure
// - Prevent issues related to version catalog multiple-from() calls
// ============================================================

pluginManagement {
    repositories {
        // Google's Maven repository for Android Gradle Plugin, Jetpack, Firebase, etc.
        google {
            content {
                includeGroupByRegex("com\\.android.*")   // Android build tools
                includeGroupByRegex("androidx.*")        // Jetpack libraries
                includeGroupByRegex("com\\.google.*")    // Firebase, Play Services, etc.
            }
        }

        // Maven Central repository for Kotlin libraries and other essentials
        mavenCentral()

        // Gradle Plugin Portal for third-party Gradle plugins like Kotlin, Hilt, etc.
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Enforce centralized repository management for all modules
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        // Standard repositories needed for Android + Kotlin
        google()
        mavenCentral()
    }

    // NOTE: Intentionally no versionCatalogs block here to avoid from() duplication error
    // If you’re using a libs.versions.toml, make sure it’s configured only in one location
}

// Project name (as displayed in Android Studio)
rootProject.name = "FitnessTrackerApp"

// Include the main application module
include(":app")
