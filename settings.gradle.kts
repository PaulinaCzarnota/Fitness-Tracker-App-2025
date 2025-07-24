// Configure where Gradle should fetch plugins from
pluginManagement {
    repositories {
        // Google's repository for Android plugins
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Maven Central for Java/Kotlin/Jetpack libraries
        mavenCentral()
        // Gradle Plugin Portal for third-party Gradle plugins
        gradlePluginPortal()
    }
}

// Control dependency resolution for all modules
dependencyResolutionManagement {
    // Enforce that subprojects should NOT declare their own repositories
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        // Official repositories
        google()
        mavenCentral()
    }
}

// Set the root project name
rootProject.name = "FitnessTrackerApp"

// Include app module in the build
include(":app")
