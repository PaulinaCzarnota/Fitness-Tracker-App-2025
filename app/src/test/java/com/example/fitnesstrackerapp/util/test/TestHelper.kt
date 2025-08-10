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
        priority: com.example.fitnesstrackerapp.data.entity.NotificationPriority = com.example.fitnesstrackerapp.data.entity.NotificationPriority.MEDIUM,
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
     * Creates a test NotificationLog entity with customizable parameters.
     */
    fun createTestNotificationLog(
        id: Long = 0,
        userId: Long = TEST_USER_ID,
        notificationId: Long = 1L,
        eventType: com.example.fitnesstrackerapp.data.entity.NotificationLogEvent = com.example.fitnesstrackerapp.data.entity.NotificationLogEvent.SENT,
        deliveryChannel: com.example.fitnesstrackerapp.data.entity.NotificationDeliveryChannel = com.example.fitnesstrackerapp.data.entity.NotificationDeliveryChannel.PUSH,
        isSuccess: Boolean = true,
        errorCode: String? = null,
        errorMessage: String? = null,
        retryCount: Int = 0,
        processingDurationMs: Long = 0L,
        deliveryDurationMs: Long? = null,
        priorityLevel: Int = 2,
        eventTimestamp: java.util.Date = java.util.Date(),
        createdAt: java.util.Date = java.util.Date(),
    ) = com.example.fitnesstrackerapp.data.entity.NotificationLog(
        id = id,
        userId = userId,
        notificationId = notificationId,
        eventType = eventType,
        deliveryChannel = deliveryChannel,
        isSuccess = isSuccess,
        errorCode = errorCode,
        errorMessage = errorMessage,
        retryCount = retryCount,
        processingDurationMs = processingDurationMs,
        deliveryDurationMs = deliveryDurationMs,
        priorityLevel = priorityLevel,
        eventTimestamp = eventTimestamp,
        createdAt = createdAt,
    )

    /**
     * Creates a test NutritionEntry entity with customizable parameters.
     */
    fun createTestNutritionEntry(
        id: Long = 0,
        userId: Long = TEST_USER_ID,
        foodName: String = "Test Food",
        brandName: String? = null,
        servingSize: Double = 1.0,
        servingUnit: String = "serving",
        caloriesPerServing: Double = 100.0,
        proteinGrams: Double = 5.0,
        carbsGrams: Double = 15.0,
        fatGrams: Double = 3.0,
        saturatedFatGrams: Double = 1.0,
        transFatGrams: Double = 0.0,
        cholesterolMg: Double = 0.0,
        fiberGrams: Double = 2.0,
        sugarGrams: Double = 5.0,
        addedSugarsGrams: Double = 0.0,
        sodiumMg: Double = 100.0,
        potassiumMg: Double = 200.0,
        vitaminCMg: Double = 10.0,
        vitaminDMcg: Double = 2.0,
        calciumMg: Double = 50.0,
        ironMg: Double = 1.0,
        mealType: com.example.fitnesstrackerapp.data.entity.MealType = com.example.fitnesstrackerapp.data.entity.MealType.LUNCH,
        dateConsumed: java.util.Date = java.util.Date(),
        notes: String? = null,
        barcode: String? = null,
        recipeId: Long? = null,
        isHomemade: Boolean = false,
        confidenceLevel: Double = 1.0,
        createdAt: java.util.Date = java.util.Date(),
        updatedAt: java.util.Date = java.util.Date(),
        loggedAt: java.util.Date = java.util.Date(),
    ) = com.example.fitnesstrackerapp.data.entity.NutritionEntry(
        id = id,
        userId = userId,
        foodName = foodName,
        brandName = brandName,
        servingSize = servingSize,
        servingUnit = servingUnit,
        caloriesPerServing = caloriesPerServing,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        saturatedFatGrams = saturatedFatGrams,
        transFatGrams = transFatGrams,
        cholesterolMg = cholesterolMg,
        fiberGrams = fiberGrams,
        sugarGrams = sugarGrams,
        addedSugarsGrams = addedSugarsGrams,
        sodiumMg = sodiumMg,
        potassiumMg = potassiumMg,
        vitaminCMg = vitaminCMg,
        vitaminDMcg = vitaminDMcg,
        calciumMg = calciumMg,
        ironMg = ironMg,
        mealType = mealType,
        dateConsumed = dateConsumed,
        notes = notes,
        barcode = barcode,
        recipeId = recipeId,
        isHomemade = isHomemade,
        confidenceLevel = confidenceLevel,
        createdAt = createdAt,
        updatedAt = updatedAt,
        loggedAt = loggedAt,
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


