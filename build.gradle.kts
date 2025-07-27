// Top-level Gradle build script for the entire FitnessTrackerApp project.
// This script declares common plugin aliases from the Version Catalog (libs.versions.toml)
// but does NOT apply them here — each subproject (e.g., app module) applies them individually.

plugins {
    // Declare Android application plugin alias (applied in module-level build.gradle.kts)
    alias(libs.plugins.android.application) apply false

    // Declare Kotlin Android plugin alias (used for Kotlin support in Android)
    alias(libs.plugins.kotlin.android) apply false

    // Declare Kotlin KAPT plugin alias (required for annotation processing e.g., Room)
    alias(libs.plugins.kotlin.kapt) apply false

    // Declare Kotlin Compose plugin alias (required for Jetpack Compose features)
    alias(libs.plugins.kotlin.compose) apply false
}
