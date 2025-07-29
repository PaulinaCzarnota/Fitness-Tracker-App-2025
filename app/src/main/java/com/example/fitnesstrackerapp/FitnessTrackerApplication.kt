package com.example.fitnesstrackerapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * FitnessTrackerApplication
 *
 * Custom [Application] class for the Fitness Tracker App.
 * Serves as the root of the Dagger Hilt dependency injection graph.
 *
 * The @HiltAndroidApp annotation:
 * - Triggers Hilt code generation.
 * - Automatically creates a base Application container.
 * - Enables injection into activities, fragments, ViewModels, etc.
 *
 * You can also initialize global tools here such as:
 * - Timber logging
 * - Firebase analytics/crashlytics
 * - StrictMode policies
 * - Local database or cache pre-loading
 */
@HiltAndroidApp
class FitnessTrackerApplication : Application() {

    /**
     * Called once during app startup, before any Activity or Service is created.
     * Ideal for initializing SDKs and global app state.
     */
    override fun onCreate() {
        super.onCreate()

        // Optional: Initialize third-party tools here.
        // Example:
        // Timber.plant(Timber.DebugTree())
        // FirebaseApp.initializeApp(this)
        // StrictMode.setVmPolicy(...)
    }
}
