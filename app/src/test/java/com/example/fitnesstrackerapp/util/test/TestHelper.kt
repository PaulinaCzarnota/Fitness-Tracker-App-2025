/**
 * Test Helper
 *
 * Responsibilities:
 * - Provides utilities for testing
 * - Sets up test environments
 * - Manages test data and state
 */

package com.example.fitnesstrackerapp.util.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

object TestData {
    const val TEST_USER_ID = 1L
    private const val TEST_EMAIL = "test@example.com"
    const val TEST_PASSWORD = "Test123!"

    val testUserProfile = com.example.fitnesstrackerapp.data.entity.UserProfile(
        id = 1L,
        userId = TEST_USER_ID,
        height = 170.0,
        weight = 70.0,
        age = 25,
        gender = "OTHER",
        activityLevel = "moderately_active"
    )

    /**
     * TestHelper provides utility functions to create test data objects for unit tests.
     * Each function returns a pre-populated instance of a model for consistent test scenarios.
     */

    /**
     * Creates a test Workout entity.
     */
    fun createTestWorkout(
        userId: Long = TEST_USER_ID,
        type: com.example.fitnesstrackerapp.data.entity.WorkoutType = com.example.fitnesstrackerapp.data.entity.WorkoutType.RUNNING
    ) = com.example.fitnesstrackerapp.data.entity.Workout(
        userId = userId,
        workoutType = type,
        title = "Test Workout",
        startTime = java.util.Date(),
        duration = 60,
        caloriesBurned = 500,
        steps = 8000,
        notes = "Test workout"
    )

    /**
     * Creates a test FoodEntry entity.
     */
    fun createTestFoodEntry(
        userId: Long = TEST_USER_ID,
        mealType: com.example.fitnesstrackerapp.data.entity.MealType = com.example.fitnesstrackerapp.data.entity.MealType.LUNCH
    ) = com.example.fitnesstrackerapp.data.entity.FoodEntry(
        userId = userId,
        foodName = "Test Meal",
        servingSize = 1.0,
        servingUnit = "serving",
        caloriesPerServing = 500.0,
        proteinGrams = 30.0,
        carbsGrams = 50.0,
        fatGrams = 20.0,
        mealType = mealType
    )
}
