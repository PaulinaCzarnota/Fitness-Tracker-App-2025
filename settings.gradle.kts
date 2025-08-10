/**
 * Root Gradle Settings for FitnessTrackerApp
 *
 * - Centralizes plugin and dependency repository configuration.
 * - Enforces repository mode to avoid project-level repositories.
 * - Ensures all modules are included and project is named correctly.
 */

pluginManagement {
    repositories {
        // Google's Maven repository for Android, Jetpack, Firebase, and other Google libraries
        google {
            content {
                includeGroupByRegex("com\\.android.*")   // Android build tools
                includeGroupByRegex("androidx.*")        // Jetpack libraries
                includeGroupByRegex("com\\.google.*")    // Google libraries (Firebase, Play Services, etc.)
            }
        }
        // Maven Central for Kotlin libraries and other dependencies
        mavenCentral()
        // Gradle Plugin Portal for third-party Gradle plugins
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Enforce centralized repository management for all modules
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        // Standard repositories for Android and Kotlin
        google()
        mavenCentral()
        // JitPack repository for GitHub-hosted libraries like MPAndroidChart
        maven { url = uri("https://jitpack.io") }
    }
}

/**
 * Project name (as displayed in Android Studio)
 */
rootProject.name = "FitnessTrackerApp"

/**
 * Module inclusion
 * - Includes the main application module.
 * - Add additional modules here as needed.
 */
include(":app")
