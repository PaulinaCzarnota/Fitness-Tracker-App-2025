/**
 * Test Helper
 *
 * Responsibilities:
 * - Provides utilities for testing
 * - Sets up test environments
 * - Manages test data and state
 */

package com.example.fitnesstrackerapp.util.test

import com.example.fitnesstrackerapp.data.entity.User
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/**
 * Test helper class providing utility functions to create test data objects for unit tests.
 * Each function returns a pre-populated instance of a model for consistent test scenarios.
 */
object TestHelper {
    const val TEST_USER_ID = 1L
    private const val TEST_EMAIL = "test@example.com"
    const val TEST_PASSWORD = "Test123!"

    /**
     * Creates a test User entity with customizable parameters.
     */
    fun createTestUser(
        id: Long = 0,
        email: String = "test@example.com",
        username: String = "testuser",
        firstName: String? = "Test",
        lastName: String? = "User",
        passwordHash: String = "hashedpassword",
        passwordSalt: String = "salt123",
        isActive: Boolean = true,
        isAccountLocked: Boolean = false,
        failedLoginAttempts: Int = 0,
    ) = User(
        id = id,
        email = email,
        username = username,
        passwordHash = passwordHash,
        passwordSalt = passwordSalt,
        firstName = firstName,
        lastName = lastName,
        dateOfBirth = createPastDate(25),
        heightCm = 175.0f,
        weightKg = 70.0f,
        gender = com.example.fitnesstrackerapp.data.entity.Gender.PREFER_NOT_TO_SAY,
        activityLevel = com.example.fitnesstrackerapp.data.entity.ActivityLevel.MODERATELY_ACTIVE,
        registrationDate = java.util.Date(),
        createdAt = java.util.Date(),
        updatedAt = java.util.Date(),
        isActive = isActive,
        isAccountLocked = isAccountLocked,
        failedLoginAttempts = failedLoginAttempts,
    )

    /**
     * Creates a test Workout entity with customizable parameters.
     */
    fun createTestWorkout(
        id: Long = 0,
        userId: Long = TEST_USER_ID,
        workoutType: com.example.fitnesstrackerapp.data.entity.WorkoutType = com.example.fitnesstrackerapp.data.entity.WorkoutType.RUNNING,
        title: String = "Test Workout",
        duration: Int = 60,
        distance: Float = 5.0f,
        caloriesBurned: Int = 500,
        date: java.util.Date = java.util.Date(),
    ) = com.example.fitnesstrackerapp.data.entity.Workout(
        id = id,
        userId = userId,
        workoutType = workoutType,
        title = title,
        startTime = date,
        endTime = java.util.Date(date.time + duration * 60 * 1000),
        duration = duration,
        distance = distance,
        caloriesBurned = caloriesBurned,
        steps = (distance * 1300).toInt(),
        notes = "Test workout notes",
        createdAt = date,
        updatedAt = date,
    )

    /**
     * Creates a test FoodEntry entity with customizable parameters.
     */
    fun createTestFoodEntry(
        id: Long = 0,
        userId: Long = TEST_USER_ID,
        foodName: String = "Test Food",
        caloriesPerServing: Double = 100.0,
        servingSize: Double = 1.0,
        mealType: com.example.fitnesstrackerapp.data.entity.MealType = com.example.fitnesstrackerapp.data.entity.MealType.LUNCH,
        proteinGrams: Double = 5.0,
    ) = com.example.fitnesstrackerapp.data.entity.FoodEntry(
        id = id,
        userId = userId,
        foodName = foodName,
        servingSize = servingSize,
        servingUnit = "piece",
        caloriesPerServing = caloriesPerServing,
        proteinGrams = proteinGrams,
        carbsGrams = 20.0,
        fatGrams = 2.0,
        mealType = mealType,
        dateConsumed = java.util.Date(),
        createdAt = java.util.Date(),
    )

    /**
     * Creates a test Goal entity with customizable parameters.
     */
    fun createTestGoal(
        id: Long = 0,
        userId: Long = TEST_USER_ID,
        title: String = "Test Goal",
        targetValue: Double = 100.0,
        currentValue: Double = 0.0,
        unit: String = "units",
    ) = com.example.fitnesstrackerapp.data.entity.Goal(
        id = id,
        userId = userId,
        title = title,
        goalType = com.example.fitnesstrackerapp.data.entity.GoalType.WEIGHT_LOSS,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        targetDate = createFutureDate(30),
        startDate = java.util.Date(),
        status = com.example.fitnesstrackerapp.data.entity.GoalStatus.ACTIVE,
        createdAt = java.util.Date(),
        updatedAt = java.util.Date(),
    )

    /**
     * Creates a test Notification entity with customizable parameters.
     */
    fun createTestNotification(
        id: Long = 0,
        userId: Long = TEST_USER_ID,
        type: com.example.fitnesstrackerapp.data.entity.NotificationType = com.example.fitnesstrackerapp.data.entity.NotificationType.WORKOUT_REMINDER,
        title: String = "Test Notification",
        message: String = "This is a test notification",
        priority: com.example.fitnesstrackerapp.data.entity.NotificationPriority = com.example.fitnesstrackerapp.data.entity.NotificationPriority.DEFAULT,
        scheduledTime: java.util.Date = java.util.Date(),
        channelId: String = "test_channel",
        relatedEntityId: Long? = null,
        relatedEntityType: String? = null,
    ) = com.example.fitnesstrackerapp.data.entity.Notification(
        id = id,
        userId = userId,
        type = type,
        title = title,
        message = message,
        priority = priority,
        status = com.example.fitnesstrackerapp.data.entity.NotificationStatus.PENDING,
        scheduledTime = scheduledTime,
        channelId = channelId,
        relatedEntityId = relatedEntityId,
        relatedEntityType = relatedEntityType,
        createdAt = java.util.Date(),
        updatedAt = java.util.Date(),
    )

    /**
     * Creates a test Step entity with customizable parameters.
     */
    fun createTestStep(
        id: Long = 0,
        userId: Long = TEST_USER_ID,
        stepCount: Int = 1000,
        date: java.util.Date = java.util.Date(),
        distance: Float = 0.8f,
        caloriesBurned: Int = 50,
    ) = com.example.fitnesstrackerapp.data.entity.Step(
        id = id,
        userId = userId,
        stepCount = stepCount,
        distance = distance,
        caloriesBurned = caloriesBurned,
        date = date,
        createdAt = date,
        updatedAt = date,
    )

    /**
     * Creates a date in the past by specified years.
     */
    private fun createPastDate(years: Int): java.util.Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.YEAR, -years)
        return calendar.time
    }

    /**
     * Creates a date in the future by specified days.
     */
    private fun createFutureDate(days: Int): java.util.Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_MONTH, days)
        return calendar.time
    }

    /**
     * Creates multiple test users for bulk testing.
     */
    fun createTestUsers(count: Int): List<User> {
        return (1..count).map { i ->
            createTestUser(
                email = "testuser$i@example.com",
                username = "testuser$i",
                firstName = "User",
                lastName = "$i",
            )
        }
    }

    /**
     * Creates test WorkoutSet entities with customizable parameters.
     */
    fun createTestWorkoutSet(
        id: Long = 0,
        workoutId: Long = 1L,
        exerciseId: Long = 1L,
        reps: Int = 10,
        weight: Double = 50.0,
        duration: Int = 60,
        restTime: Int = 30,
    ) = com.example.fitnesstrackerapp.data.entity.WorkoutSet(
        id = id,
        workoutId = workoutId,
        exerciseId = exerciseId,
        setNumber = 1,
        reps = reps,
        weight = weight,
        duration = duration,
        restTime = restTime,
        createdAt = java.util.Date(),
    )

    /**
     * Creates test Exercise entities with customizable parameters.
     */
    fun createTestExercise(
        id: Long = 0,
        name: String = "Test Exercise",
        category: String = "Strength",
        muscleGroups: List<String> = listOf("Chest", "Triceps"),
        difficulty: String = "Beginner",
    ) = com.example.fitnesstrackerapp.data.entity.Exercise(
        id = id,
        name = name,
        category = category,
        description = "A test exercise",
        muscleGroups = muscleGroups,
        difficulty = difficulty,
        equipment = "None",
        instructions = "Perform as instructed",
        createdAt = java.util.Date(),
    )

    /**
     * Creates a mock AuthRepository for testing
     */
    fun createMockAuthRepository() = mockk<com.example.fitnesstrackerapp.repository.AuthRepository> {
        every { isAuthenticated } returns flowOf(true) as StateFlow<Boolean>
        every { currentUser } returns flowOf(createTestUser()) as StateFlow<User?>
        coEvery { login(any(), any()) } returns com.example.fitnesstrackerapp.repository.AuthResult.Success("Login successful")
        coEvery { register(any(), any(), any()) } returns com.example.fitnesstrackerapp.repository.AuthResult.Success("Registration successful")
        coEvery { logout() } returns Unit
    }

    /**
     * Creates a date relative to current time
     */
    fun createRelativeDate(offsetDays: Int): java.util.Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_MONTH, offsetDays)
        return calendar.time
    }

    /**
     * Creates a mock Android Context for testing.
     */
    fun createMockContext(): android.content.Context {
        return mockk<android.content.Context>(relaxed = true)
    }
}

object TestData {
    const val TEST_USER_ID = 1L
    private const val TEST_EMAIL = "test@example.com"
    const val TEST_PASSWORD = "Test123!"
    const val VALID_EMAIL = "valid@example.com"
    const val INVALID_EMAIL = "invalid-email"
    const val WEAK_PASSWORD = "123"
    const val STRONG_PASSWORD = "StrongPassword123!"
    const val VALID_NAME = "Test User"

    // Mock crypto data for tests
    const val MOCK_SALT = "mocksalt12345678"
    const val MOCK_HASH = "mockhash12345678"
    const val MOCK_HEX_STRING = "6d6f636b686173683132333435363738"

    // Test data for nutrition
    const val TEST_FOOD_NAME = "Test Food"
    const val TEST_CALORIES = 250.0
    const val TEST_PROTEIN = 10.5
    const val TEST_CARBS = 30.0
    const val TEST_FAT = 8.0

    // Test data for workouts
    const val TEST_WORKOUT_TITLE = "Morning Run"
    const val TEST_WORKOUT_DURATION = 30
    const val TEST_WORKOUT_DISTANCE = 5.0f
    const val TEST_WORKOUT_CALORIES = 300

    // Test data for goals
    const val TEST_GOAL_TITLE = "Lose Weight"
    const val TEST_GOAL_TARGET = 10.0
    const val TEST_GOAL_CURRENT = 2.0
    const val TEST_GOAL_UNIT = "kg"
}
