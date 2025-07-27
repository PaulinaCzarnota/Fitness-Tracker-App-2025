@file:Suppress("UnstableApiUsage")

/**
 * This file configures repository sources, plugin management, and included modules.
 */

pluginManagement {
    repositories {
        // Google's Maven repository — primary source for Android tools and Jetpack libraries
        google {
            content {
                // Include only groups related to Android and Google tools to optimize resolution speed
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        // Maven Central — provides Kotlin, third-party libraries, and general purpose tools
        mavenCentral()

        // Gradle Plugin Portal — used for community-developed Gradle plugins
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Prevent subprojects (like :app) from using their own repositories
    // This ensures all dependencies come from the repositories declared below
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FitnessTrackerApp"

include(":app")
