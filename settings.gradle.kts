pluginManagement {
    repositories {
        // Google's Maven repository for Android Gradle Plugin and Jetpack libraries
        google {
            content {
                // Limit to Android and Google plugin groups to speed up resolution
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        // Maven Central for standard Kotlin/Java libraries and third-party dependencies
        mavenCentral()

        // Gradle Plugin Portal for any plugins not in Google/MavenCentral
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Fail if any subproject declares its own repositories to enforce central control
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        // Android libraries and Google-specific dependencies
        google()

        // Kotlin/Java and other third-party dependencies
        mavenCentral()
    }
}

// Set the name of the root project (used in IDEs and Gradle outputs)
rootProject.name = "FitnessTrackerApp"

// Include the app module (source lives in /app)
include(":app")
