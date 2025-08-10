/**
 * Top-level Gradle build script (Kotlin DSL)
 *
 * Responsibilities:
 * - Declares plugin aliases centrally using the version catalog.
 * - Ensures consistent plugin versions across all modules.
 * - Applies plugins only in relevant submodules (not at the root).
 */

plugins {
    // Android Application plugin (applied in :app module)
    alias(libs.plugins.android.application) apply false

    // Kotlin plugin for Android (applied in :app module)
    alias(libs.plugins.kotlin.android) apply false
    
    // Kotlin Compose Compiler plugin (required for Kotlin 2.0+)
    alias(libs.plugins.kotlin.compose) apply false
    
    // KSP plugin for annotation processing
    alias(libs.plugins.ksp) apply false
    
    // Dokka plugin for generating HTML documentation from KDoc
    alias(libs.plugins.dokka) apply false
    
    // Spotless plugin for code formatting
    alias(libs.plugins.spotless) apply false
    
    // ktlint plugin for Kotlin linting
    alias(libs.plugins.ktlint) apply false
    
    // Detekt plugin for static analysis
    alias(libs.plugins.detekt) apply false
    
    // Gradle Versions plugin for dependency updates
    alias(libs.plugins.versions) apply false
}
