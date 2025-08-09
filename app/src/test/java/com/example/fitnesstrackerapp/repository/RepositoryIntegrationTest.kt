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
            description = "Lose 10kg by year end",
            goalType = GoalType.WEIGHT_LOSS,
            targetValue = 10.0,
            currentValue = 2.5,
            unit = "kg",
            targetDate = Date(System.currentTimeMillis() + 86400000L * 30), // 30 days from now
            reminderEnabled = true,
            reminderFrequency = "daily",
        )

        // Test insertion
        val goalId = goalRepository.insertGoal(testGoal)
        Assert.assertTrue("Goal ID should be positive", goalId > 0)

        // Test retrieval
        val retrievedGoal = goalRepository.getGoalById(goalId)
        Assert.assertNotNull("Should retrieve goal", retrievedGoal)
        Assert.assertEquals("Title should match", "Lose Weight", retrievedGoal?.title)
        Assert.assertEquals("Target value should match", 10.0, retrievedGoal?.targetValue, 0.01)
        Assert.assertEquals("Current value should match", 2.5, retrievedGoal?.currentValue, 0.01)
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

        val goalId = goalRepository.insertGoal(testGoal)

        // Test progress update
        goalRepository.updateGoalProgress(goalId, 8500.0, System.currentTimeMillis())

        // Verify update
        val updatedGoal = goalRepository.getGoalById(goalId)
        Assert.assertEquals("Current value should be updated", 8500.0, updatedGoal?.currentValue, 0.01)
        Assert.assertEquals("Progress percentage should be 85%", 85.0f, updatedGoal?.getProgressPercentage(), 0.1f)
    }

    @Test
    fun testGoalRepository_markAsAchieved() = runTest {
        val testGoal = Goal(
            userId = testUserId,
            title = "Run 5K",
            goalType = GoalType.DISTANCE,
            targetValue = 5.0,
            currentValue = 4.8,
            unit = "km",
            targetDate = Date(System.currentTimeMillis() + 86400000L),
            status = GoalStatus.ACTIVE,
        )

        val goalId = goalRepository.insertGoal(testGoal)

        // Mark as achieved
        goalRepository.markGoalAsAchieved(goalId, System.currentTimeMillis())

        // Verify achievement
        val achievedGoal = goalRepository.getGoalById(goalId)
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
            status = GoalStatus.ACTIVE,
        )
        val activeGoal2 = Goal(
            userId = testUserId,
            title = "Active Goal 2",
            goalType = GoalType.STEP_COUNT,
            targetValue = 10000.0,
            unit = "steps",
            targetDate = Date(System.currentTimeMillis() + 86400000L * 2),
            status = GoalStatus.ACTIVE,
        )
        val completedGoal = Goal(
            userId = testUserId,
            title = "Completed Goal",
            goalType = GoalType.DISTANCE,
            targetValue = 5.0,
            unit = "km",
            targetDate = Date(System.currentTimeMillis() + 86400000L),
            status = GoalStatus.COMPLETED,
        )

        goalRepository.insertGoal(activeGoal1)
        goalRepository.insertGoal(activeGoal2)
        goalRepository.insertGoal(completedGoal)

        // Test active goals query
        val activeGoals = goalRepository.getActiveGoals(testUserId).first()
        Assert.assertEquals("Should have 2 active goals", 2, activeGoals.size)
        Assert.assertTrue("Should contain Active Goal 1", activeGoals.any { it.title == "Active Goal 1" })
        Assert.assertTrue("Should contain Active Goal 2", activeGoals.any { it.title == "Active Goal 2" })
        Assert.assertFalse("Should not contain completed goal", activeGoals.any { it.title == "Completed Goal" })
    }

    // endregion

    // region FoodEntryRepository Tests

    @Test
    fun testFoodEntryRepository_insertAndRetrieve() = runTest {
        val testFoodEntry = FoodEntry(
            userId = testUserId,
            foodName = "Apple",
            brandName = "Red Delicious",
            servingSize = 1.0,
            servingUnit = "medium",
            caloriesPerServing = 95.0,
            proteinGrams = 0.5,
            carbsGrams = 25.0,
            fatGrams = 0.3,
            fiberGrams = 4.0,
            sugarGrams = 19.0,
            sodiumMg = 2.0,
            mealType = MealType.SNACK,
            dateConsumed = Date(),
        )

        // Test insertion with validation
        val foodEntryId = foodEntryRepository.insertFoodEntry(testFoodEntry)
        Assert.assertTrue("FoodEntry ID should be positive", foodEntryId > 0)

        // Test retrieval
        val foodEntries = foodEntryRepository.getFoodEntriesByUserId(testUserId).first()
        Assert.assertEquals("Should have 1 food entry", 1, foodEntries.size)

        val retrievedEntry = foodEntries[0]
        Assert.assertEquals("Food name should match", "Apple", retrievedEntry.foodName)
        Assert.assertEquals("Brand name should match", "Red Delicious", retrievedEntry.brandName)
        Assert.assertEquals("Calories should match", 95.0, retrievedEntry.caloriesPerServing, 0.01)
        Assert.assertEquals("Meal type should match", MealType.SNACK, retrievedEntry.mealType)
    }

    @Test
    fun testFoodEntryRepository_validation() = runTest {
        val invalidFoodEntry = FoodEntry(
            userId = 0L, // Invalid user ID
            foodName = "", // Invalid empty name
            servingSize = -1.0, // Invalid negative serving size
            servingUnit = "piece",
            caloriesPerServing = -50.0, // Invalid negative calories
            mealType = MealType.BREAKFAST,
            dateConsumed = Date(),
        )

        // Test validation fails
        Assert.assertFalse("Should fail validation", foodEntryRepository.validateFoodEntry(invalidFoodEntry))

        // Test insertion throws exception for invalid data
        try {
            foodEntryRepository.insertFoodEntry(invalidFoodEntry)
            Assert.fail("Should throw exception for invalid data")
        } catch (e: IllegalArgumentException) {
            Assert.assertTrue("Should be validation error", e.message?.contains("user ID") == true)
        }
    }

    @Test
    fun testFoodEntryRepository_nutritionCalculations() = runTest {
        val testFoodEntry = FoodEntry(
            userId = testUserId,
            foodName = "Banana",
            servingSize = 2.0, // 2 bananas
            servingUnit = "medium",
            caloriesPerServing = 105.0, // per banana
            proteinGrams = 1.3, // per banana
            carbsGrams = 27.0, // per banana
            fatGrams = 0.4, // per banana
            mealType = MealType.BREAKFAST,
            dateConsumed = Date(),
        )

        foodEntryRepository.insertFoodEntry(testFoodEntry)
        val retrieved = foodEntryRepository.getFoodEntriesByUserId(testUserId).first()[0]

        // Test nutrition calculations
        Assert.assertEquals("Total calories should be 210", 210.0, retrieved.getTotalCalories(), 0.01)
        Assert.assertEquals("Total protein should be 2.6g", 2.6, retrieved.getTotalProtein(), 0.01)
        Assert.assertEquals("Total carbs should be 54g", 54.0, retrieved.getTotalCarbs(), 0.01)
        Assert.assertEquals("Total fat should be 0.8g", 0.8, retrieved.getTotalFat(), 0.01)
    }

    @Test
    fun testFoodEntryRepository_dailyTotals() = runTest {
        val today = Date()

        // Insert multiple food entries for today
        val breakfast = FoodEntry(
            userId = testUserId, foodName = "Oatmeal", servingSize = 1.0, servingUnit = "cup",
            caloriesPerServing = 150.0, proteinGrams = 5.0, carbsGrams = 30.0, fatGrams = 2.0,
            mealType = MealType.BREAKFAST, dateConsumed = today,
        )
        val lunch = FoodEntry(
            userId = testUserId, foodName = "Chicken Salad", servingSize = 1.0, servingUnit = "plate",
            caloriesPerServing = 300.0, proteinGrams = 25.0, carbsGrams = 15.0, fatGrams = 15.0,
            mealType = MealType.LUNCH, dateConsumed = today,
        )

        foodEntryRepository.insertFoodEntry(breakfast)
        foodEntryRepository.insertFoodEntry(lunch)

        // Test daily totals
        val totalCalories = foodEntryRepository.getTotalCaloriesForDate(testUserId, today)
        Assert.assertEquals("Total calories should be 450", 450.0, totalCalories, 0.01)

        val macroTotals = foodEntryRepository.getTotalMacrosForDate(testUserId, today)
        Assert.assertEquals("Total protein should be 30g", 30.0, macroTotals.total_protein, 0.01)
        Assert.assertEquals("Total carbs should be 45g", 45.0, macroTotals.total_carbs, 0.01)
        Assert.assertEquals("Total fat should be 17g", 17.0, macroTotals.total_fat, 0.01)
    }

    // endregion

    // region NotificationRepository Tests

    @Test
    fun testNotificationRepository_insertAndRetrieve() = runTest {
        val testNotification = Notification(
            userId = testUserId,
            type = NotificationType.WORKOUT_REMINDER,
            title = "Workout Time!",
            message = "Don't forget your daily workout",
            priority = NotificationPriority.HIGH,
            status = NotificationStatus.PENDING,
            scheduledTime = Date(System.currentTimeMillis() + 3600000L), // 1 hour from now
            channelId = "workout_reminders",
            relatedEntityType = "workout",
            relatedEntityId = 1L,
        )

        // Test insertion with validation
        val notificationId = notificationRepository.insertNotification(testNotification)
        Assert.assertTrue("Notification ID should be positive", notificationId > 0)

        // Test retrieval
        val retrievedNotification = notificationRepository.getNotificationById(notificationId)
        Assert.assertNotNull("Should retrieve notification", retrievedNotification)
        Assert.assertEquals("Type should match", NotificationType.WORKOUT_REMINDER, retrievedNotification?.type)
        Assert.assertEquals("Title should match", "Workout Time!", retrievedNotification?.title)
        Assert.assertEquals("Priority should match", NotificationPriority.HIGH, retrievedNotification?.priority)
        Assert.assertEquals("Status should match", NotificationStatus.PENDING, retrievedNotification?.status)
    }

    @Test
    fun testNotificationRepository_statusUpdates() = runTest {
        val testNotification = Notification(
            userId = testUserId,
            type = NotificationType.GOAL_ACHIEVEMENT,
            title = "Goal Achieved!",
            message = "Congratulations on reaching your goal",
            scheduledTime = Date(),
            channelId = "achievements",
        )

        val notificationId = notificationRepository.insertNotification(testNotification)

        // Test status progression
        notificationRepository.markNotificationAsSent(notificationId)
        var updated = notificationRepository.getNotificationById(notificationId)
        Assert.assertEquals("Should be marked as sent", NotificationStatus.SENT, updated?.status)
        Assert.assertNotNull("Should have sent time", updated?.sentTime)

        notificationRepository.markNotificationAsRead(notificationId)
        updated = notificationRepository.getNotificationById(notificationId)
        Assert.assertEquals("Should be marked as read", NotificationStatus.read, updated?.status)
        Assert.assertTrue("Should be marked as read flag", updated?.isRead == true)
        Assert.assertNotNull("Should have read time", updated?.readTime)

        notificationRepository.markNotificationAsClicked(notificationId)
        updated = notificationRepository.getNotificationById(notificationId)
        Assert.assertEquals("Should be marked as clicked", NotificationStatus.CLICKED, updated?.status)
        Assert.assertNotNull("Should have clicked time", updated?.clickedTime)
    }

    @Test
    fun testNotificationRepository_filtering() = runTest {
        val now = Date()

        // Create notifications with different types and statuses
        val workoutReminder = Notification(
            userId = testUserId,
            type = NotificationType.WORKOUT_REMINDER,
            title = "Workout",
            message = "Time to workout",
            scheduledTime = now,
            channelId = "workouts",
            status = NotificationStatus.PENDING,
        )
        val goalAchievement = Notification(
            userId = testUserId,
            type = NotificationType.GOAL_ACHIEVEMENT,
            title = "Goal!",
            message = "Goal achieved",
            scheduledTime = now,
            channelId = "goals",
            status = NotificationStatus.SENT,
        )
        val stepMilestone = Notification(
            userId = testUserId,
            type = NotificationType.STEP_MILESTONE,
            title = "Steps!",
            message = "10K steps reached",
            scheduledTime = now,
            channelId = "steps",
            status = NotificationStatus.read,
            isRead = true,
        )

        notificationRepository.insertNotification(workoutReminder)
        notificationRepository.insertNotification(goalAchievement)
        notificationRepository.insertNotification(stepMilestone)

        // Test filtering by type
        val workoutNotifications = notificationRepository.getNotificationsByType(testUserId, NotificationType.WORKOUT_REMINDER).first()
        Assert.assertEquals("Should have 1 workout notification", 1, workoutNotifications.size)
        Assert.assertEquals("Should be workout reminder", "Workout", workoutNotifications[0].title)

        // Test filtering by status
        val sentNotifications = notificationRepository.getNotificationsByStatus(testUserId, NotificationStatus.SENT).first()
        Assert.assertEquals("Should have 1 sent notification", 1, sentNotifications.size)
        Assert.assertEquals("Should be goal achievement", "Goal!", sentNotifications[0].title)

        // Test unread notifications
        val unreadNotifications = notificationRepository.getUnreadNotifications(testUserId).first()
        Assert.assertEquals("Should have 2 unread notifications", 2, unreadNotifications.size)
        Assert.assertFalse("Should not contain read notification", unreadNotifications.any { it.isRead })
    }

    @Test
    fun testNotificationRepository_validation() = runTest {
        val invalidNotification = Notification(
            userId = 0L, // Invalid user ID
            type = NotificationType.WORKOUT_REMINDER,
            title = "", // Invalid empty title
            message = "", // Invalid empty message
            scheduledTime = Date(),
            channelId = "", // Invalid empty channel
        )

        // Test validation fails
        Assert.assertFalse("Should fail validation", notificationRepository.validateNotification(invalidNotification))

        // Test insertion throws exception for invalid data
        try {
            notificationRepository.insertNotification(invalidNotification)
            Assert.fail("Should throw exception for invalid data")
        } catch (e: IllegalArgumentException) {
            Assert.assertTrue("Should be validation error", e.message?.contains("user ID") == true)
        }
    }

    @Test
    fun testNotificationRepository_analytics() = runTest {
        val now = Date()

        // Create notifications with different outcomes
        val sent1 = Notification(userId = testUserId, type = NotificationType.WORKOUT_REMINDER, title = "Sent 1", message = "Message", scheduledTime = now, channelId = "test", status = NotificationStatus.SENT)
        val clicked1 = Notification(userId = testUserId, type = NotificationType.GOAL_ACHIEVEMENT, title = "Clicked 1", message = "Message", scheduledTime = now, channelId = "test", status = NotificationStatus.CLICKED)
        val dismissed1 = Notification(userId = testUserId, type = NotificationType.STEP_MILESTONE, title = "Dismissed 1", message = "Message", scheduledTime = now, channelId = "test", status = NotificationStatus.DISMISSED)
        val failed1 = Notification(userId = testUserId, type = NotificationType.DAILY_MOTIVATION, title = "Failed 1", message = "Message", scheduledTime = now, channelId = "test", status = NotificationStatus.FAILED)

        notificationRepository.insertNotification(sent1)
        notificationRepository.insertNotification(clicked1)
        notificationRepository.insertNotification(dismissed1)
        notificationRepository.insertNotification(failed1)

        // Test analytics
        val stats = notificationRepository.getNotificationStats(testUserId)
        Assert.assertEquals("Should have 4 total notifications", 4, stats.totalNotifications)
        Assert.assertEquals("Should have 1 sent notification", 1, stats.sentCount)
        Assert.assertEquals("Should have 1 clicked notification", 1, stats.clickedCount)

        // Delivery success rate calculation: (sent + clicked + dismissed) / total * 100
        val expectedSuccessRate = (1 + 1 + 1) / 4.0 * 100 // 75%
        Assert.assertEquals("Delivery success rate should be 75%", expectedSuccessRate, stats.deliverySuccessRate, 0.1)

        // Test count by type
        val workoutCount = notificationRepository.getNotificationCountByType(testUserId, NotificationType.WORKOUT_REMINDER)
        Assert.assertEquals("Should have 1 workout reminder", 1, workoutCount)

        val goalCount = notificationRepository.getNotificationCountByType(testUserId, NotificationType.GOAL_ACHIEVEMENT)
        Assert.assertEquals("Should have 1 goal achievement", 1, goalCount)
    }

    // endregion

    // region Cross-Repository Integration Tests

    @Test
    fun testRepositoryIntegration_goalWithNotification() = runTest {
        // Create a goal
        val testGoal = Goal(
            userId = testUserId,
            title = "Complete 30 Workouts",
            goalType = GoalType.FREQUENCY,
            targetValue = 30.0,
            currentValue = 28.0,
            unit = "workouts",
            targetDate = Date(System.currentTimeMillis() + 86400000L * 7), // 1 week
            status = GoalStatus.ACTIVE,
        )
        val goalId = goalRepository.insertGoal(testGoal)

        // Create related notification
        val relatedNotification = Notification(
            userId = testUserId,
            type = NotificationType.GOAL_DEADLINE_APPROACHING,
            title = "Goal Deadline Approaching",
            message = "Your goal 'Complete 30 Workouts' is due in 7 days",
            scheduledTime = Date(System.currentTimeMillis() + 86400000L * 6), // 6 days from now
            channelId = "goal_reminders",
            relatedEntityType = "goal",
            relatedEntityId = goalId,
        )
        val notificationId = notificationRepository.insertNotification(relatedNotification)

        // Verify relationship
        val relatedNotifications = notificationRepository.getNotificationsByRelatedEntity(testUserId, "goal", goalId).first()
        Assert.assertEquals("Should have 1 related notification", 1, relatedNotifications.size)
        Assert.assertEquals("Should be the correct notification", notificationId, relatedNotifications[0].id)

        // Update goal progress and verify
        goalRepository.updateGoalProgress(goalId, 30.0, System.currentTimeMillis())
        val updatedGoal = goalRepository.getGoalById(goalId)
        Assert.assertTrue("Goal should be completed", updatedGoal?.isCompleted() == true)
    }

    @Test
    fun testRepositoryIntegration_stepTrackingWithGoals() = runTest {
        // Create step goal
        val stepGoal = Goal(
            userId = testUserId,
            title = "Daily Steps",
            goalType = GoalType.STEP_COUNT,
            targetValue = 10000.0,
            currentValue = 0.0,
            unit = "steps",
            targetDate = Date(),
            status = GoalStatus.ACTIVE,
        )
        val goalId = goalRepository.insertGoal(stepGoal)

        // Track steps throughout the day
        val todaysSteps = Step(
            userId = testUserId,
            count = 8500,
            goal = 10000,
            date = Date(),
            caloriesBurned = 340.0f,
            distanceMeters = 5100.0f,
            activeMinutes = 85,
        )
        stepRepository.insertStep(todaysSteps)

        // Update goal progress based on step data
        goalRepository.updateGoalProgress(goalId, 8500.0, System.currentTimeMillis())

        val updatedGoal = goalRepository.getGoalById(goalId)
        Assert.assertEquals("Goal progress should match step count", 8500.0, updatedGoal?.currentValue, 0.01)
        Assert.assertEquals("Progress should be 85%", 85.0f, updatedGoal?.getProgressPercentage(), 0.1f)
        Assert.assertFalse("Goal should not be completed yet", updatedGoal?.isCompleted() == true)

        // Complete the daily goal
        val finalSteps = todaysSteps.copy(count = 10500)
        stepRepository.updateStep(finalSteps)
        goalRepository.updateGoalProgress(goalId, 10500.0, System.currentTimeMillis())

        val completedGoal = goalRepository.getGoalById(goalId)
        Assert.assertTrue("Goal should be completed", completedGoal?.isCompleted() == true)
    }

    // endregion
}
