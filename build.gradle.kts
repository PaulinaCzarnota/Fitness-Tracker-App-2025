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
    
    // KSP plugin for annotation processing
    alias(libs.plugins.ksp) apply false
    
    // Dokka plugin for generating HTML documentation from KDoc
    id("org.jetbrains.dokka") version "1.9.10" apply false
    
    // Spotless plugin for code formatting
    id("com.diffplug.spotless") version "6.23.3" apply false
    
    // ktlint plugin for Kotlin linting
    id("org.jlleitschuh.gradle.ktlint") version "12.0.3" apply false
}
