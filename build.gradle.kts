/**
 * Top-level Gradle build script (Kotlin DSL)
 *
 * Purpose:
 * - Declares plugin aliases centrally using version catalog
 * - Ensures consistent plugin versions across all modules
 */

plugins {
    // Android Application plugin (applied in :app module)
    alias(libs.plugins.android.application) apply false

    // Kotlin plugin for Android
    alias(libs.plugins.kotlin.android) apply false

    // Google Services plugin for Firebase
    alias(libs.plugins.google.services) apply false

    // Hilt (Dagger) plugin for dependency injection
    alias(libs.plugins.hilt.android) apply false
}
