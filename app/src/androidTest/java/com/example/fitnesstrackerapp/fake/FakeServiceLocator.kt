package com.example.fitnesstrackerapp.fake

import android.content.Context

/**
 * Minimal fake service locator for Android instrumented tests.
 * Provides minimal functionality to prevent compilation errors.
 */
object FakeServiceLocator {
    // Fake repositories - minimal stubs for testing
    val workoutRepository = FakeWorkoutRepository()
    val exerciseRepository = FakeExerciseRepository()
    val workoutSetRepository = FakeWorkoutSetRepository()
    val authRepository = FakeAuthRepository()
    val userPreferencesRepository = FakeUserPreferencesRepository()
    val goalRepository = FakeGoalRepository()
    val stepRepository = FakeStepRepository()
    val notificationRepository = FakeNotificationRepository()

    // Fake services
    val notificationService = FakeNotificationService()

    // Type aliases to handle the naming expected in tests
    val NotificationService = FakeNotificationService::class.java

    /**
     * Initialize the fake service locator with application context.
     * This is a minimal stub implementation.
     */
    fun init(context: Context) {
        // Minimal initialization for tests
    }

    /**
     * Clean up the fake service locator.
     * This is a minimal stub implementation.
     */
    fun cleanup() {
        // Minimal cleanup for tests
    }
}
