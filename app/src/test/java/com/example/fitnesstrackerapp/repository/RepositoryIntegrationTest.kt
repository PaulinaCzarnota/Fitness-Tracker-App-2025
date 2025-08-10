package com.example.fitnesstrackerapp.repository

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import java.util.*

/**
 * Integration tests for repository classes in the Fitness Tracker application.
 *
 * This test suite validates the repository layer functionality including:
 * - Repository pattern implementation
 * - Data validation and business logic
 * - Database interaction through DAOs
 * - Error handling and edge cases
 * - Repository method contracts
 *
 * Uses in-memory database for fast, isolated testing.
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RepositoryIntegrationTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var database: AppDatabase

    // Repositories under test
    private lateinit var stepRepository: StepRepository
    private lateinit var goalRepository: GoalRepository
    private lateinit var foodEntryRepository: FoodEntryRepository
    private lateinit var notificationRepository: NotificationRepository

    // Test data
    private lateinit var testUser: User
    private var testUserId: Long = 0L

    @Before
    fun setUp() = runTest {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getInMemoryDatabase(context)

        // Initialize repositories
        stepRepository = StepRepository(database.stepDao())
        goalRepository = GoalRepository(database.goalDao())
        foodEntryRepository = FoodEntryRepository(database.foodEntryDao())
        notificationRepository = NotificationRepository(database.notificationDao())

        // Create test user
        testUser = User(
            email = "test@example.com",
            username = "testuser",
            passwordHash = "hash",
            passwordSalt = "salt",
            createdAt = Date(),
            updatedAt = Date(),
        )
        testUserId = database.userDao().insertUser(testUser)
    }

    @After
    fun tearDown() {
        database.close()
    }

    // region StepRepository Tests

    @Test
    fun testStepRepository_insertAndRetrieve() = runTest {
        val testStep = Step(
            userId = testUserId,
            count = 5000,
            goal = 10000,
            date = Date(),
            caloriesBurned = 200.0f,
            distanceMeters = 3000.0f,
            activeMinutes = 45,
        )

        // Test insertion
        val stepId = stepRepository.insertStep(testStep)
        Assert.assertTrue("Step ID should be positive", stepId > 0)

        // Test retrieval
        val retrievedSteps = stepRepository.getTodaysSteps(testUserId).first()
        Assert.assertNotNull("Should retrieve step data", retrievedSteps)
        Assert.assertEquals("Step count should match", 5000, retrievedSteps?.count)
        Assert.assertEquals("Goal should match", 10000, retrievedSteps?.goal)
        Assert.assertEquals("User ID should match", testUserId, retrievedSteps?.userId)
    }

    @Test
    fun testStepRepository_updateStep() = runTest {
        val testStep = Step(
            userId = testUserId,
            count = 3000,
            goal = 10000,
            date = Date(),
            caloriesBurned = 120.0f,
            distanceMeters = 1800.0f,
            activeMinutes = 30,
        )

        val stepId = stepRepository.insertStep(testStep)
        val updatedStep = testStep.copy(id = stepId, count = 7000, caloriesBurned = 280.0f)

        // Test update
        stepRepository.updateStep(updatedStep)

        // Verify update
        val retrieved = stepRepository.getTodaysSteps(testUserId).first()
        Assert.assertEquals("Count should be updated", 7000, retrieved?.count)
        Assert.assertEquals("Calories should be updated", 280.0f, retrieved?.caloriesBurned)
    }

    @Test
    fun testStepRepository_dateRangeQuery() = runTest {
        val now = Date()
        val yesterday = Date(now.time - 86400000L)
        val tomorrow = Date(now.time + 86400000L)

        // Insert steps for different dates
        val step1 = Step(userId = testUserId, count = 5000, goal = 10000, date = yesterday)
        val step2 = Step(userId = testUserId, count = 7000, goal = 10000, date = now)
        val step3 = Step(userId = testUserId, count = 6000, goal = 10000, date = tomorrow)

        stepRepository.insertStep(step1)
        stepRepository.insertStep(step2)
        stepRepository.insertStep(step3)

        // Test date range query
        val stepsInRange = stepRepository.getStepsInDateRange(
            testUserId,
            Date(yesterday.time - 1000L),
            Date(now.time + 1000L),
        ).first()

        Assert.assertEquals("Should return 2 steps in range", 2, stepsInRange.size)
        Assert.assertTrue("Should contain yesterday's steps", stepsInRange.any { it.count == 5000 })
        Assert.assertTrue("Should contain today's steps", stepsInRange.any { it.count == 7000 })
    }

    // endregion

    // region GoalRepository Tests

    @Test
    fun testGoalRepository_insertAndRetrieve() = runTest {
        val testGoal = Goal(
            userId = testUserId,
            title = "Lose Weight",
            goalType = GoalType.WEIGHT_LOSS,
            targetValue = 10.0,
            currentValue = 2.5,
            unit = "kg",
            targetDate = Date(System.currentTimeMillis() + 86400000L * 30),
        )

        // Test insertion
        val goalId = goalRepository.insert(testGoal)
        Assert.assertTrue("Goal ID should be positive", goalId > 0)

        // Test retrieval
        val retrievedGoal = goalRepository.getById(goalId)
        Assert.assertNotNull("Should retrieve goal", retrievedGoal)
        Assert.assertEquals("Title should match", "Lose Weight", retrievedGoal?.title)
        Assert.assertEquals("Target value should match", 10.0, retrievedGoal?.targetValue ?: 0.0, 0.01)
        Assert.assertEquals("Current value should match", 2.5, retrievedGoal?.currentValue ?: 0.0, 0.01)
        Assert.assertEquals("Goal type should match", GoalType.WEIGHT_LOSS, retrievedGoal?.goalType)
    }

    @Test
    fun testGoalRepository_progressUpdate() = runTest {
        val testGoal = Goal(
            userId = testUserId,
            title = "Daily Steps",
            goalType = GoalType.STEP_COUNT,
            targetValue = 10000.0,
            currentValue = 5000.0,
            unit = "steps",
            targetDate = Date(System.currentTimeMillis() + 86400000L),
        )

        val goalId = goalRepository.insert(testGoal)

        // Test progress update
        goalRepository.updateGoalProgress(goalId, 8500.0, System.currentTimeMillis())

        // Verify update
        val updatedGoal = goalRepository.getById(goalId)
        Assert.assertEquals("Current value should be updated", 8500.0, updatedGoal?.currentValue ?: 0.0, 0.01)
        Assert.assertEquals("Progress percentage should be 85%", 85.0f, updatedGoal?.getProgressPercentage() ?: 0.0f, 0.1f)
    }

    @Test
    fun testGoalRepository_markAsAchieved() = runTest {
        val testGoal = Goal(
            userId = testUserId,
            title = "Run 5K",
            goalType = GoalType.DISTANCE_RUNNING,
            targetValue = 5.0,
            currentValue = 4.8,
            unit = "km",
            targetDate = Date(System.currentTimeMillis() + 86400000L),
        )

        val goalId = goalRepository.insert(testGoal)

        // Mark as achieved
        goalRepository.markGoalAsAchieved(goalId, System.currentTimeMillis())

        // Verify achievement
        val achievedGoal = goalRepository.getById(goalId)
        Assert.assertEquals("Status should be completed", GoalStatus.COMPLETED, achievedGoal?.status)
    }

    @Test
    fun testGoalRepository_activeGoalsQuery() = runTest {
        // Create multiple goals with different statuses
        val activeGoal1 = Goal(
            userId = testUserId,
            title = "Active Goal 1",
            goalType = GoalType.WEIGHT_LOSS,
            targetValue = 5.0,
            unit = "kg",
            targetDate = Date(System.currentTimeMillis() + 86400000L),
        )
        val activeGoal2 = Goal(
            userId = testUserId,
            title = "Active Goal 2",
            goalType = GoalType.STEP_COUNT,
            targetValue = 10000.0,
            unit = "steps",
            targetDate = Date(System.currentTimeMillis() + 86400000L * 2),
        )
        val completedGoal = Goal(
            userId = testUserId,
            title = "Completed Goal",
            goalType = GoalType.DISTANCE_RUNNING,
            targetValue = 5.0,
            unit = "km",
            targetDate = Date(System.currentTimeMillis() + 86400000L),
        )

        goalRepository.insert(activeGoal1)
        goalRepository.insert(activeGoal2)
        val completedGoalId = goalRepository.insert(completedGoal)
        goalRepository.markGoalAsAchieved(completedGoalId, System.currentTimeMillis())

        // Test active goals query
        val activeGoals = goalRepository.getActiveGoals(testUserId).first()
        Assert.assertEquals("Should have 2 active goals", 2, activeGoals.size)
        Assert.assertTrue("Should contain active goal 1", activeGoals.any { it.title == "Active Goal 1" })
        Assert.assertTrue("Should contain active goal 2", activeGoals.any { it.title == "Active Goal 2" })
        Assert.assertFalse("Should not contain completed goal", activeGoals.any { it.title == "Completed Goal" })
    }

    // endregion

    // region FoodEntryRepository Tests

    @Test
    fun testFoodEntryRepository_insertAndRetrieve() = runTest {
        val testFoodEntry = FoodEntry(
            userId = testUserId,
            foodName = "Apple",
            servingSize = 1.0,
            servingUnit = "medium",
            caloriesPerServing = 95.0,
            proteinGrams = 0.5,
            carbsGrams = 25.0,
            fatGrams = 0.3,
            mealType = MealType.SNACK,
            dateConsumed = Date(),
            createdAt = Date(),
            loggedAt = Date(),
        )

        // Test insertion
        val entryId = foodEntryRepository.insertFoodEntry(testFoodEntry)
        Assert.assertTrue("Entry ID should be positive", entryId > 0)

        // Test retrieval
        val retrievedEntries = foodEntryRepository.getFoodEntriesByUserId(testUserId).first()
        Assert.assertNotNull("Should retrieve food entries", retrievedEntries)
        Assert.assertEquals("Should have 1 entry", 1, retrievedEntries.size)
        val retrievedEntry = retrievedEntries.first()
        Assert.assertEquals("Food name should match", "Apple", retrievedEntry.foodName)
        Assert.assertEquals("Meal type should match", MealType.SNACK, retrievedEntry.mealType)
    }

    @Test
    fun testFoodEntryRepository_mealTypeFiltering() = runTest {
        // Create entries for different meal types
        val breakfast = FoodEntry(
            userId = testUserId,
            foodName = "Oatmeal",
            servingSize = 1.0,
            servingUnit = "cup",
            caloriesPerServing = 150.0,
            proteinGrams = 5.0,
            carbsGrams = 30.0,
            fatGrams = 3.0,
            mealType = MealType.BREAKFAST,
            dateConsumed = Date(),
            createdAt = Date(),
            loggedAt = Date(),
        )

        val lunch = FoodEntry(
            userId = testUserId,
            foodName = "Sandwich",
            servingSize = 1.0,
            servingUnit = "whole",
            caloriesPerServing = 350.0,
            proteinGrams = 15.0,
            carbsGrams = 45.0,
            fatGrams = 12.0,
            mealType = MealType.LUNCH,
            dateConsumed = Date(),
            createdAt = Date(),
            loggedAt = Date(),
        )

        foodEntryRepository.insertFoodEntry(breakfast)
        foodEntryRepository.insertFoodEntry(lunch)

        // Test meal type filtering
        val today = Date()
        val breakfastEntries = foodEntryRepository.getFoodEntriesByMealType(testUserId, today, MealType.BREAKFAST).first()
        val lunchEntries = foodEntryRepository.getFoodEntriesByMealType(testUserId, today, MealType.LUNCH).first()

        Assert.assertEquals("Should have 1 breakfast entry", 1, breakfastEntries.size)
        Assert.assertEquals("Should have 1 lunch entry", 1, lunchEntries.size)
        Assert.assertEquals("Breakfast entry should be oatmeal", "Oatmeal", breakfastEntries.first().foodName)
        Assert.assertEquals("Lunch entry should be sandwich", "Sandwich", lunchEntries.first().foodName)
    }

    @Test
    fun testFoodEntryRepository_nutritionCalculation() = runTest {
        val entry1 = FoodEntry(
            userId = testUserId,
            foodName = "Chicken Breast",
            servingSize = 100.0,
            servingUnit = "grams",
            caloriesPerServing = 165.0,
            proteinGrams = 31.0,
            carbsGrams = 0.0,
            fatGrams = 3.6,
            mealType = MealType.DINNER,
            dateConsumed = Date(),
            createdAt = Date(),
            loggedAt = Date(),
        )

        val entry2 = FoodEntry(
            userId = testUserId,
            foodName = "Brown Rice",
            servingSize = 100.0,
            servingUnit = "grams",
            caloriesPerServing = 111.0,
            proteinGrams = 2.6,
            carbsGrams = 23.0,
            fatGrams = 0.9,
            mealType = MealType.DINNER,
            dateConsumed = Date(),
            createdAt = Date(),
            loggedAt = Date(),
        )

        foodEntryRepository.insertFoodEntry(entry1)
        foodEntryRepository.insertFoodEntry(entry2)

        // Test nutrition totals calculation
        val todayEntries = foodEntryRepository.getFoodEntriesForDate(testUserId, Date()).first()
        val totalCalories = todayEntries.sumOf { it.caloriesPerServing * it.servingSize / 100.0 }
        val totalProtein = todayEntries.sumOf { it.proteinGrams * it.servingSize / 100.0 }
        val totalCarbs = todayEntries.sumOf { it.carbsGrams * it.servingSize / 100.0 }
        val totalFat = todayEntries.sumOf { it.fatGrams * it.servingSize / 100.0 }

        Assert.assertEquals("Total calories should be 276", 276.0, totalCalories, 1.0)
        Assert.assertEquals("Total protein should be 33.6g", 33.6, totalProtein, 0.1)
        Assert.assertEquals("Total carbs should be 23g", 23.0, totalCarbs, 0.1)
        Assert.assertEquals("Total fat should be 4.5g", 4.5, totalFat, 0.1)
    }

    // endregion

    // region NotificationRepository Tests

    @Test
    fun testNotificationRepository_insertAndRetrieve() = runTest {
        val testNotification = Notification(
            userId = testUserId,
            type = NotificationType.WORKOUT_REMINDER,
            title = "Time to Exercise!",
            message = "Don't forget your daily workout",
            scheduledTime = Date(System.currentTimeMillis() + 3600000L), // 1 hour from now
            channelId = "workout_reminders",
            priority = NotificationPriority.MEDIUM,
            status = NotificationStatus.PENDING,
            createdAt = Date(),
            updatedAt = Date(),
        )

        // Test insertion
        val notificationId = notificationRepository.insertNotification(testNotification)
        Assert.assertTrue("Notification ID should be positive", notificationId > 0)

        // Test retrieval
        val retrievedNotifications = notificationRepository.getNotificationsByUserId(testUserId).first()
        Assert.assertNotNull("Should retrieve notifications", retrievedNotifications)
        Assert.assertEquals("Should have 1 notification", 1, retrievedNotifications.size)
        val retrievedNotification = retrievedNotifications.first()
        Assert.assertEquals("Title should match", "Time to Exercise!", retrievedNotification.title)
        Assert.assertEquals("Type should match", NotificationType.WORKOUT_REMINDER, retrievedNotification.type)
    }

    @Test
    fun testNotificationRepository_statusUpdate() = runTest {
        val testNotification = Notification(
            userId = testUserId,
            type = NotificationType.GOAL_ACHIEVEMENT,
            title = "Goal Achieved!",
            message = "Congratulations on reaching your goal",
            scheduledTime = Date(),
            channelId = "achievements",
            priority = NotificationPriority.HIGH,
            status = NotificationStatus.PENDING,
            createdAt = Date(),
            updatedAt = Date(),
        )

        val notificationId = notificationRepository.insertNotification(testNotification)

        // Mark as sent
        val updatedNotification = testNotification.copy(
            id = notificationId,
            status = NotificationStatus.SENT,
        )
        notificationRepository.updateNotification(updatedNotification)

        // Verify status update
        val retrievedNotifications = notificationRepository.getNotificationsByUserId(testUserId).first()
        val sentNotification = retrievedNotifications.first()
        Assert.assertEquals("Status should be SENT", NotificationStatus.SENT, sentNotification.status)
    }

    // endregion

    // region Cross-Repository Integration Tests

    @Test
    fun testRepositoryIntegration_goalWithNotification() = runTest {
        // Create a goal
        val testGoal = Goal(
            userId = testUserId,
            title = "Complete 30 Workouts",
            goalType = GoalType.STEP_COUNT, // Use existing enum value instead of FREQUENCY
            targetValue = 30.0,
            currentValue = 28.0,
            unit = "workouts",
            targetDate = Date(System.currentTimeMillis() + 86400000L * 7), // 1 week
        )
        val goalId = goalRepository.insert(testGoal)

        // Create related notification
        val relatedNotification = Notification(
            userId = testUserId,
            type = NotificationType.GOAL_DEADLINE_APPROACHING,
            title = "Goal Deadline Approaching",
            message = "Your goal 'Complete 30 Workouts' is due in 7 days",
            scheduledTime = Date(System.currentTimeMillis() + 86400000L * 6), // 6 days from now
            channelId = "goal_reminders",
            priority = NotificationPriority.MEDIUM,
            status = NotificationStatus.PENDING,
            createdAt = Date(),
            updatedAt = Date(),
        )
        notificationRepository.insertNotification(relatedNotification)

        // Verify integration
        val retrievedGoal = goalRepository.getById(goalId)
        val retrievedNotifications = notificationRepository.getNotificationsByUserId(testUserId).first()

        Assert.assertNotNull("Goal should exist", retrievedGoal)
        Assert.assertEquals("Should have 1 notification", 1, retrievedNotifications.size)
        Assert.assertEquals("Goal title should match", "Complete 30 Workouts", retrievedGoal?.title)
        Assert.assertTrue(
            "Notification message should reference goal",
            retrievedNotifications.first().message.contains("Complete 30 Workouts"),
        )
    }

    @Test
    fun testRepositoryIntegration_nutritionGoalTracking() = runTest {
        // Create a nutrition goal
        val nutritionGoal = Goal(
            userId = testUserId,
            title = "Daily Protein Goal",
            goalType = GoalType.WEIGHT_LOSS,
            targetValue = 100.0, // 100g protein per day
            currentValue = 0.0,
            unit = "grams",
            targetDate = Date(System.currentTimeMillis() + 86400000L), // Today
        )
        val goalId = goalRepository.insert(nutritionGoal)

        // Add food entries that contribute to protein goal
        val proteinFoods = listOf(
            FoodEntry(
                userId = testUserId,
                foodName = "Chicken Breast",
                servingSize = 150.0,
                servingUnit = "grams",
                caloriesPerServing = 165.0,
                proteinGrams = 31.0,
                carbsGrams = 0.0,
                fatGrams = 3.6,
                mealType = MealType.LUNCH,
                dateConsumed = Date(),
                createdAt = Date(),
                loggedAt = Date(),
            ),
            FoodEntry(
                userId = testUserId,
                foodName = "Greek Yogurt",
                servingSize = 200.0,
                servingUnit = "grams",
                caloriesPerServing = 100.0,
                proteinGrams = 10.0,
                carbsGrams = 6.0,
                fatGrams = 0.4,
                mealType = MealType.BREAKFAST,
                dateConsumed = Date(),
                createdAt = Date(),
                loggedAt = Date(),
            ),
        )

        proteinFoods.forEach { foodEntryRepository.insertFoodEntry(it) }

        // Calculate total protein consumed
        val todayEntries = foodEntryRepository.getFoodEntriesForDate(testUserId, Date()).first()
        val totalProtein = todayEntries.sumOf { (it.proteinGrams ?: 0.0) * it.servingSize / 100.0 }

        // Update goal progress
        goalRepository.updateGoalProgress(goalId, totalProtein, System.currentTimeMillis())

        // Verify integration
        val updatedGoal = goalRepository.getById(goalId)
        Assert.assertEquals("Protein progress should be tracked", 66.5, updatedGoal?.currentValue ?: 0.0, 0.1)
        Assert.assertEquals("Progress percentage should be 66.5%", 66.5f, updatedGoal?.getProgressPercentage() ?: 0.0f, 0.1f)
    }

    // endregion
}
