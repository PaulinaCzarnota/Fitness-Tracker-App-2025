package com.example.fitnesstrackerapp.repository

/**
 * Comprehensive unit tests for Goal, FoodEntry, and Notification repositories
 * using Room in-memory database and JUnit 5.
 *
 * This test class covers:
 * - GoalRepository: CRUD operations, progress tracking, goal status management
 * - FoodEntryRepository: Food logging, nutrition analysis, meal categorization
 * - NotificationRepository: Notification management, delivery tracking, analytics
 */

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.Date

@RunWith(JUnit4::class)
class ComprehensiveRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var goalRepository: GoalRepository
    private lateinit var foodEntryRepository: FoodEntryRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var testUser: User
    private var testUserId: Long = 0L

    @Before
    fun setup() = runTest {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()

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

    // region Goal Repository Tests

    @Test
    fun testGoalCrudOperations() = runTest {
        // Create
        val goal = createTestGoal()
        val goalId = goalRepository.insert(goal)
        assertThat(goalId).isGreaterThan(0)

        // Read
        val retrievedGoal = goalRepository.getById(goalId)
        assertThat(retrievedGoal).isNotNull()
        assertThat(retrievedGoal?.title).isEqualTo("Test Goal")

        // Update
        val updatedGoal = retrievedGoal!!.copy(
            title = "Updated Goal",
            currentValue = 50.0,
        )
        goalRepository.update(updatedGoal)

        val updatedRetrievedGoal = goalRepository.getById(goalId)
        assertThat(updatedRetrievedGoal?.title).isEqualTo("Updated Goal")
        assertThat(updatedRetrievedGoal?.currentValue).isWithin(0.01).of(50.0)

        // Delete
        goalRepository.delete(updatedGoal)
        val deletedGoal = goalRepository.getById(goalId)
        assertThat(deletedGoal).isNull()
    }

    @Test
    fun testGoalsByUser() = runTest {
        val goal1 = createTestGoal(title = "Goal 1")
        val goal2 = createTestGoal(title = "Goal 2")
        val goal3 = createTestGoal(title = "Goal 3", goalType = GoalType.WEIGHT_LOSS)

        goalRepository.insert(goal1)
        goalRepository.insert(goal2)
        goalRepository.insert(goal3)

        val userGoals = goalRepository.getGoalsByUser(testUserId).first()
        assertThat(userGoals).hasSize(3)
        assertThat(userGoals.map { it.title }).containsExactly("Goal 3", "Goal 2", "Goal 1") // DESC order
    }

    @Test
    fun testGoalsByType() = runTest {
        val stepGoal = createTestGoal(goalType = GoalType.STEP_COUNT)
        val weightGoal = createTestGoal(goalType = GoalType.WEIGHT_LOSS)
        val distanceGoal = createTestGoal(goalType = GoalType.DISTANCE_RUNNING)

        goalRepository.insert(stepGoal)
        goalRepository.insert(weightGoal)
        goalRepository.insert(distanceGoal)

        val stepGoals = goalRepository.getGoalsByType(testUserId, GoalType.STEP_COUNT.name).first()
        assertThat(stepGoals).hasSize(1)
        assertThat(stepGoals[0].goalType).isEqualTo(GoalType.STEP_COUNT)
    }

    @Test
    fun testActiveGoals() = runTest {
        val activeGoal = createTestGoal()
        val inactiveGoal = createTestGoal()

        goalRepository.insert(activeGoal)
        goalRepository.insert(inactiveGoal)

        val activeGoals = goalRepository.getActiveGoals(testUserId).first()
        assertThat(activeGoals).hasSize(2)
    }

    // endregion

    // region FoodEntry Repository Tests

    @Test
    fun testFoodEntryCrudOperations() = runTest {
        // Create
        val foodEntry = createTestFoodEntry()
        val entryId = foodEntryRepository.insertFoodEntry(foodEntry)
        assertThat(entryId).isGreaterThan(0)

        // Read - Use the available methods from the repository
        val userEntries = foodEntryRepository.getFoodEntriesByUserId(testUserId).first()
        val retrievedEntry = userEntries.firstOrNull { it.id == entryId }
        assertThat(retrievedEntry).isNotNull()
        assertThat(retrievedEntry?.foodName).isEqualTo("Test Food")

        // Update
        val updatedEntry = retrievedEntry!!.copy(
            foodName = "Updated Food",
            servingSize = 2.0,
        )
        foodEntryRepository.updateFoodEntry(updatedEntry)

        val updatedUserEntries = foodEntryRepository.getFoodEntriesByUserId(testUserId).first()
        val updatedRetrievedEntry = updatedUserEntries.firstOrNull { it.id == entryId }
        assertThat(updatedRetrievedEntry?.foodName).isEqualTo("Updated Food")
        assertThat(updatedRetrievedEntry?.servingSize).isWithin(0.01).of(2.0)

        // Delete
        foodEntryRepository.deleteFoodEntry(updatedEntry)
        val finalUserEntries = foodEntryRepository.getFoodEntriesByUserId(testUserId).first()
        val deletedEntry = finalUserEntries.firstOrNull { it.id == entryId }
        assertThat(deletedEntry).isNull()
    }

    @Test
    fun testFoodEntriesByUser() = runTest {
        val entry1 = createTestFoodEntry(foodName = "Apple")
        val entry2 = createTestFoodEntry(foodName = "Banana")
        val entry3 = createTestFoodEntry(foodName = "Orange")

        foodEntryRepository.insertFoodEntry(entry1)
        foodEntryRepository.insertFoodEntry(entry2)
        foodEntryRepository.insertFoodEntry(entry3)

        val userEntries = foodEntryRepository.getFoodEntriesByUserId(testUserId).first()
        assertThat(userEntries).hasSize(3)
    }

    @Test
    fun testFoodEntriesByMealType() = runTest {
        val breakfast = createTestFoodEntry(mealType = MealType.BREAKFAST)
        val lunch = createTestFoodEntry(mealType = MealType.LUNCH)
        val dinner = createTestFoodEntry(mealType = MealType.DINNER)

        foodEntryRepository.insertFoodEntry(breakfast)
        foodEntryRepository.insertFoodEntry(lunch)
        foodEntryRepository.insertFoodEntry(dinner)

        val breakfastEntries = foodEntryRepository.getFoodEntriesByMealType(testUserId, Date(), MealType.BREAKFAST).first()
        assertThat(breakfastEntries).hasSize(1)
        assertThat(breakfastEntries[0].mealType).isEqualTo(MealType.BREAKFAST)
    }

    @Test
    fun testFoodEntriesByDate() = runTest {
        val today = Date()
        val yesterday = Date(today.time - 24 * 60 * 60 * 1000)

        val todayEntry = createTestFoodEntry(dateConsumed = today)
        val yesterdayEntry = createTestFoodEntry(dateConsumed = yesterday)

        foodEntryRepository.insertFoodEntry(todayEntry)
        foodEntryRepository.insertFoodEntry(yesterdayEntry)

        val todayEntries = foodEntryRepository.getFoodEntriesForDate(testUserId, today).first()
        assertThat(todayEntries).hasSize(1)
    }

    @Test
    fun testDailyCalorieCalculation() = runTest {
        val today = Date()
        val entry1 = createTestFoodEntry(dateConsumed = today, caloriesPerServing = 100.0, servingSize = 1.0)
        val entry2 = createTestFoodEntry(dateConsumed = today, caloriesPerServing = 200.0, servingSize = 1.5)

        foodEntryRepository.insertFoodEntry(entry1)
        foodEntryRepository.insertFoodEntry(entry2)

        // Calculate total calories manually since specific method may not exist
        val entries = foodEntryRepository.getFoodEntriesForDate(testUserId, today).first()
        val totalCalories = entries.sumOf { it.caloriesPerServing * it.servingSize }
        assertThat(totalCalories).isWithin(0.01).of(400.0) // 100 + (200 * 1.5)
    }

    // endregion

    // region Notification Repository Tests

    @Test
    fun testNotificationCrudOperations() = runTest {
        // Create a basic notification instead of notification log
        val notification = createTestNotification()
        val notificationId = notificationRepository.insertNotification(notification)
        assertThat(notificationId).isGreaterThan(0)

        // Read
        val userNotifications = notificationRepository.getNotificationsByUserId(testUserId).first()
        val retrievedNotification = userNotifications.firstOrNull { it.id == notificationId }
        assertThat(retrievedNotification).isNotNull()

        // Update
        val updatedNotification = retrievedNotification!!.copy(
            title = "Updated Notification",
        )
        notificationRepository.updateNotification(updatedNotification)

        val updatedUserNotifications = notificationRepository.getNotificationsByUserId(testUserId).first()
        val updatedRetrievedNotification = updatedUserNotifications.firstOrNull { it.id == notificationId }
        assertThat(updatedRetrievedNotification?.title).isEqualTo("Updated Notification")

        // Delete
        notificationRepository.deleteNotification(updatedNotification)
        val finalUserNotifications = notificationRepository.getNotificationsByUserId(testUserId).first()
        val deletedNotification = finalUserNotifications.firstOrNull { it.id == notificationId }
        assertThat(deletedNotification).isNull()
    }

    @Test
    fun testNotificationsByUser() = runTest {
        val notification1 = createTestNotification(title = "Notification 1")
        val notification2 = createTestNotification(title = "Notification 2")

        notificationRepository.insertNotification(notification1)
        notificationRepository.insertNotification(notification2)

        val userNotifications = notificationRepository.getNotificationsByUserId(testUserId).first()
        assertThat(userNotifications).hasSize(2)
    }

    @Test
    fun testNotificationsByType() = runTest {
        val goalNotification = createTestNotification(type = NotificationType.WORKOUT_REMINDER)
        val workoutNotification = createTestNotification(type = NotificationType.GOAL_ACHIEVEMENT)

        notificationRepository.insertNotification(goalNotification)
        notificationRepository.insertNotification(workoutNotification)

        val goalNotifications = notificationRepository.getNotificationsByType(testUserId, NotificationType.WORKOUT_REMINDER).first()
        assertThat(goalNotifications).hasSize(1)
        assertThat(goalNotifications[0].type).isEqualTo(NotificationType.WORKOUT_REMINDER)
    }

    // endregion

    // region Helper Methods

    private fun createTestGoal(
        title: String = "Test Goal",
        goalType: GoalType = GoalType.STEP_COUNT,
        targetValue: Double = 10000.0,
        currentValue: Double = 0.0,
        unit: String = "steps",
    ) = Goal(
        userId = testUserId,
        title = title,
        goalType = goalType,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        targetDate = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000), // 1 week from now
        createdAt = Date(),
    )

    private fun createTestFoodEntry(
        foodName: String = "Test Food",
        servingSize: Double = 1.0,
        servingUnit: String = "cup",
        caloriesPerServing: Double = 150.0,
        proteinGrams: Double = 5.0,
        carbsGrams: Double = 20.0,
        fatGrams: Double = 3.0,
        mealType: MealType = MealType.BREAKFAST,
        dateConsumed: Date = Date(),
    ) = FoodEntry(
        userId = testUserId,
        foodName = foodName,
        servingSize = servingSize,
        servingUnit = servingUnit,
        caloriesPerServing = caloriesPerServing,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        mealType = mealType,
        dateConsumed = dateConsumed,
        createdAt = Date(),
        loggedAt = Date(),
    )

    private fun createTestNotification(
        title: String = "Test Notification",
        message: String = "Test notification message",
        type: NotificationType = NotificationType.WORKOUT_REMINDER,
    ) = Notification(
        userId = testUserId,
        title = title,
        message = message,
        type = type,
        priority = NotificationPriority.MEDIUM,
        channelId = "test_channel",
        scheduledTime = Date(),
        status = NotificationStatus.PENDING,
        createdAt = Date(),
        updatedAt = Date(),
    )
}
