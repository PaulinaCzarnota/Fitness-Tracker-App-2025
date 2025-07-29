// ============================================================
// Top-level Gradle build script (Kotlin DSL)
// ============================================================
//
// Purpose:
// - Declares plugin aliases centrally using version catalog
// - Does NOT apply any plugin directly (apply false)
// - Keeps plugin versions consistent across modules
// ============================================================

plugins {
    // Android Application plugin (applied in :app module)
    alias(libs.plugins.android.application) apply false

    // Kotlin plugin for Android
    alias(libs.plugins.kotlin.android) apply false

    // Kotlin KAPT plugin for annotation processing (used by Room, Hilt)
    alias(libs.plugins.kotlin.kapt) apply false

    // Dagger Hilt plugin for dependency injection
    alias(libs.plugins.hilt.android) apply false

    // Google Services plugin for Firebase
    alias(libs.plugins.google.services) apply false
}
