/**
 * Root Gradle Settings (Kotlin DSL) for FitnessTrackerApp
 *
 * Responsibilities:
 * - Sets up secure and optimized plugin & dependency resolution.
 * - Avoids duplicate repository definitions.
 * - Enforces repository mode and clean module structure.
 * - Prevents issues related to version catalog multiple-from() calls.
 * - Ensures all modules are included and project is named correctly.
 *
 * Coding Standards:
 * - Consistent Javadoc-style header and inline comments.
 * - All configuration blocks are documented for clarity and maintainability.
 *
 * Note:
 * - The use of 'repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)' and related APIs
 *   is officially recommended by Gradle for centralized dependency management.
 * - These APIs are marked @Incubating (unstable) by Gradle, but are safe and correct to use.
 * - The warnings can be safely ignored and do not affect build correctness.
 */

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
    /**
     * Enforce centralized repository management for all modules.
     * - Prevents accidental use of project-level repositories.
     * - Ensures all dependencies are resolved from the defined repositories.
     * - Note: This API is @Incubating and may show warnings, but is correct and recommended.
     */
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        // Standard repositories needed for Android + Kotlin
        google()
        mavenCentral()
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
