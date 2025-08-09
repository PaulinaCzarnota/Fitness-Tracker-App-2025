/**
 * Comprehensive unit tests for Goal, FoodEntry, and Notification repositories 
 * using Room in-memory database and JUnit 5.
 *
 * This test class covers:
 * - GoalRepository: CRUD operations, progress tracking, goal status management
 * - FoodEntryRepository: Food logging, nutrition analysis, meal categorization
 * - NotificationRepository: Notification management, delivery tracking, analytics
 */

package com.example.fitnesstrackerapp.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComprehensiveRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var goalRepository: GoalRepository
    private lateinit var foodEntryRepository: FoodEntryRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var testUser: User
    private var testUserId: Long = 0L

    @BeforeAll
    fun setupDatabase() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    @BeforeEach
    fun setup() = runTest {
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
            updatedAt = Date()
        )
        testUserId = database.userDao().insertUser(testUser)
    }

    @AfterEach
    fun cleanup() = runTest {
        database.clearAllTables()
    }

    @AfterAll
    fun closeDatabase() {
        database.close()
    }

    // region GoalRepository Tests

    @Test
    fun `goalRepository insertGoal creates goal with valid data`() = runTest {
        // Given
        val goal = createTestGoal(
            userId = testUserId,
            title = "Lose 10 pounds",
            type = GoalType.WEIGHT_LOSS,
            targetValue = 10.0,
            currentValue = 2.0,
            unit = "lbs"
        )

        // When
        val goalId = goalRepository.insertGoal(goal)

        // Then
        assertTrue(goalId > 0)

        val savedGoal = goalRepository.getGoalById(goalId)
        assertNotNull(savedGoal)
        assertEquals("Lose 10 pounds", savedGoal!!.title)
        assertEquals(GoalType.WEIGHT_LOSS, savedGoal.goalType)
        assertEquals(10.0, savedGoal.targetValue, 0.01)
        assertEquals(2.0, savedGoal.currentValue, 0.01)
        assertEquals("lbs", savedGoal.unit)
        assertEquals(testUserId, savedGoal.userId)
    }

    @Test
    fun `goalRepository updateGoalProgress updates current value`() = runTest {
        // Given
        val goal = createTestGoal(
            userId = testUserId,
            targetValue = 10000.0,
            currentValue = 5000.0
        )
        val goalId = goalRepository.insertGoal(goal)

        // When
        goalRepository.updateGoalProgress(goalId, 7500.0, System.currentTimeMillis())

        // Then
        val updatedGoal = goalRepository.getGoalById(goalId)
        assertNotNull(updatedGoal)
        assertEquals(7500.0, updatedGoal!!.currentValue, 0.01)
    }

    @Test
    fun `goalRepository markGoalAsAchieved updates status`() = runTest {
        // Given
        val goal = createTestGoal(
            userId = testUserId,
            status = GoalStatus.ACTIVE
        )
        val goalId = goalRepository.insertGoal(goal)

        // When
        goalRepository.markGoalAsAchieved(goalId, System.currentTimeMillis())

        // Then
        val achievedGoal = goalRepository.getGoalById(goalId)
        assertNotNull(achievedGoal)
        assertEquals(GoalStatus.COMPLETED, achievedGoal!!.status)
    }

    @Test
    fun `goalRepository getActiveGoals returns only active goals`() = runTest {
        // Given
        val activeGoal1 = createTestGoal(userId = testUserId, title = "Active 1", status = GoalStatus.ACTIVE)
        val activeGoal2 = createTestGoal(userId = testUserId, title = "Active 2", status = GoalStatus.ACTIVE)
        val completedGoal = createTestGoal(userId = testUserId, title = "Completed", status = GoalStatus.COMPLETED)

        goalRepository.insertGoal(activeGoal1)
        goalRepository.insertGoal(activeGoal2)
        goalRepository.insertGoal(completedGoal)

        // When
        val activeGoals = goalRepository.getActiveGoals(testUserId).first()

        // Then
        assertEquals(2, activeGoals.size)
        assertTrue(activeGoals.all { it.status == GoalStatus.ACTIVE })
        assertTrue(activeGoals.any { it.title == "Active 1" })
        assertTrue(activeGoals.any { it.title == "Active 2" })
        assertFalse(activeGoals.any { it.title == "Completed" })
    }

    @Test
    fun `goalRepository getGoalsByType filters by goal type`() = runTest {
        // Given
        val weightGoal = createTestGoal(userId = testUserId, type = GoalType.WEIGHT_LOSS, title = "Weight Goal")
        val stepGoal = createTestGoal(userId = testUserId, type = GoalType.STEP_COUNT, title = "Step Goal")
        val distanceGoal = createTestGoal(userId = testUserId, type = GoalType.DISTANCE, title = "Distance Goal")

        goalRepository.insertGoal(weightGoal)
        goalRepository.insertGoal(stepGoal)
        goalRepository.insertGoal(distanceGoal)

        // When
        val weightGoals = goalRepository.getGoalsByType(testUserId, "WEIGHT_LOSS").first()

        // Then
        assertEquals(1, weightGoals.size)
        assertEquals("Weight Goal", weightGoals[0].title)
        assertEquals(GoalType.WEIGHT_LOSS, weightGoals[0].goalType)
    }

    @Test
    fun `goalRepository handles goal deletion`() = runTest {
        // Given
        val goal = createTestGoal(userId = testUserId)
        val goalId = goalRepository.insertGoal(goal)

        // Verify goal exists
        assertNotNull(goalRepository.getGoalById(goalId))

        // When
        goalRepository.deleteGoalById(goalId)

        // Then
        assertNull(goalRepository.getGoalById(goalId))
    }

    // endregion

    // region FoodEntryRepository Tests

    @Test
    fun `foodEntryRepository insertFoodEntry creates entry with nutrition data`() = runTest {
        // Given
        val foodEntry = createTestFoodEntry(
            userId = testUserId,
            foodName = "Apple",
            servingSize = 1.0,
            caloriesPerServing = 95.0,
            proteinGrams = 0.5,
            carbsGrams = 25.0,
            fatGrams = 0.3,
            mealType = MealType.SNACK
        )

        // When
        val entryId = foodEntryRepository.insertFoodEntry(foodEntry)

        // Then
        assertTrue(entryId > 0)

        val savedEntries = foodEntryRepository.getFoodEntriesByUserId(testUserId).first()
        assertEquals(1, savedEntries.size)
        
        val savedEntry = savedEntries[0]
        assertEquals("Apple", savedEntry.foodName)
        assertEquals(95.0, savedEntry.caloriesPerServing, 0.01)
        assertEquals(25.0, savedEntry.carbsGrams, 0.01)
        assertEquals(MealType.SNACK, savedEntry.mealType)
        assertEquals(testUserId, savedEntry.userId)
    }

    @Test
    fun `foodEntryRepository validates food entry data`() = runTest {
        // Given - invalid food entry with negative calories
        val invalidEntry = createTestFoodEntry(
            userId = 0L, // Invalid user ID
            foodName = "",  // Empty food name
            servingSize = -1.0, // Negative serving size
            caloriesPerServing = -50.0 // Negative calories
        )

        // When
        val isValid = foodEntryRepository.validateFoodEntry(invalidEntry)

        // Then
        assertFalse(isValid)

        // Test insertion throws exception
        assertThrows<IllegalArgumentException> {
            runTest {
                foodEntryRepository.insertFoodEntry(invalidEntry)
            }
        }
    }

    @Test
    fun `foodEntryRepository getFoodEntriesForDate filters by date`() = runTest {
        // Given
        val today = Date()
        val yesterday = Date(today.time - 86400000L)

        val todayEntry = createTestFoodEntry(userId = testUserId, dateConsumed = today, foodName = "Today Food")
        val yesterdayEntry = createTestFoodEntry(userId = testUserId, dateConsumed = yesterday, foodName = "Yesterday Food")

        foodEntryRepository.insertFoodEntry(todayEntry)
        foodEntryRepository.insertFoodEntry(yesterdayEntry)

        // When
        val todayEntries = foodEntryRepository.getFoodEntriesForDate(testUserId, today).first()

        // Then
        assertEquals(1, todayEntries.size)
        assertEquals("Today Food", todayEntries[0].foodName)
    }

    @Test
    fun `foodEntryRepository getFoodEntriesByMealType filters by meal type`() = runTest {
        // Given
        val breakfastEntry = createTestFoodEntry(userId = testUserId, mealType = MealType.BREAKFAST, foodName = "Cereal")
        val lunchEntry = createTestFoodEntry(userId = testUserId, mealType = MealType.LUNCH, foodName = "Sandwich")
        val dinnerEntry = createTestFoodEntry(userId = testUserId, mealType = MealType.DINNER, foodName = "Pasta")

        foodEntryRepository.insertFoodEntry(breakfastEntry)
        foodEntryRepository.insertFoodEntry(lunchEntry)
        foodEntryRepository.insertFoodEntry(dinnerEntry)

        // When
        val breakfastEntries = foodEntryRepository.getFoodEntriesByMealType(
            testUserId, Date(), MealType.BREAKFAST
        ).first()

        // Then
        assertEquals(1, breakfastEntries.size)
        assertEquals("Cereal", breakfastEntries[0].foodName)
        assertEquals(MealType.BREAKFAST, breakfastEntries[0].mealType)
    }

    @Test
    fun `foodEntryRepository searchFoodEntries finds matching entries`() = runTest {
        // Given
        val appleEntry = createTestFoodEntry(userId = testUserId, foodName = "Red Apple")
        val appleJuiceEntry = createTestFoodEntry(userId = testUserId, foodName = "Apple Juice")
        val bananaEntry = createTestFoodEntry(userId = testUserId, foodName = "Banana")

        foodEntryRepository.insertFoodEntry(appleEntry)
        foodEntryRepository.insertFoodEntry(appleJuiceEntry)
        foodEntryRepository.insertFoodEntry(bananaEntry)

        // When
        val appleEntries = foodEntryRepository.searchFoodEntries(testUserId, "apple").first()

        // Then
        assertEquals(2, appleEntries.size)
        assertTrue(appleEntries.any { it.foodName == "Red Apple" })
        assertTrue(appleEntries.any { it.foodName == "Apple Juice" })
        assertFalse(appleEntries.any { it.foodName == "Banana" })
    }

    @Test
    fun `foodEntryRepository handles bulk operations`() = runTest {
        // Given
        val entries = listOf(
            createTestFoodEntry(userId = testUserId, foodName = "Entry 1"),
            createTestFoodEntry(userId = testUserId, foodName = "Entry 2"),
            createTestFoodEntry(userId = testUserId, foodName = "Entry 3")
        )

        // When
        val entryIds = foodEntryRepository.insertAllFoodEntries(entries)

        // Then
        assertEquals(3, entryIds.size)
        assertTrue(entryIds.all { it > 0 })

        val savedEntries = foodEntryRepository.getFoodEntriesByUserId(testUserId).first()
        assertEquals(3, savedEntries.size)
    }

    // endregion

    // region NotificationRepository Tests

    @Test
    fun `notificationRepository insertNotification creates notification with valid data`() = runTest {
        // Given
        val notification = createTestNotification(
            userId = testUserId,
            title = "Workout Reminder",
            message = "Time for your morning workout!",
            type = NotificationType.WORKOUT_REMINDER,
            channelId = "workout_reminders"
        )

        // When
        val notificationId = notificationRepository.insertNotification(notification)

        // Then
        assertTrue(notificationId > 0)

        val savedNotification = notificationRepository.getNotificationById(notificationId)
        assertNotNull(savedNotification)
        assertEquals("Workout Reminder", savedNotification!!.title)
        assertEquals("Time for your morning workout!", savedNotification.message)
        assertEquals(NotificationType.WORKOUT_REMINDER, savedNotification.type)
        assertEquals("workout_reminders", savedNotification.channelId)
        assertEquals(testUserId, savedNotification.userId)
    }

    @Test
    fun `notificationRepository validates notification data`() = runTest {
        // Given - invalid notification
        val invalidNotification = createTestNotification(
            userId = 0L, // Invalid user ID
            title = "",   // Empty title
            message = "", // Empty message
            channelId = "" // Empty channel ID
        )

        // When
        val isValid = notificationRepository.validateNotification(invalidNotification)

        // Then
        assertFalse(isValid)

        // Test insertion throws exception
        assertThrows<IllegalArgumentException> {
            runTest {
                notificationRepository.insertNotification(invalidNotification)
            }
        }
    }

    @Test
    fun `notificationRepository getNotificationsByType filters by type`() = runTest {
        // Given
        val workoutNotification = createTestNotification(userId = testUserId, type = NotificationType.WORKOUT_REMINDER, title = "Workout")
        val goalNotification = createTestNotification(userId = testUserId, type = NotificationType.GOAL_ACHIEVED, title = "Goal")
        val reminderNotification = createTestNotification(userId = testUserId, type = NotificationType.GENERAL_REMINDER, title = "Reminder")

        notificationRepository.insertNotification(workoutNotification)
        notificationRepository.insertNotification(goalNotification)
        notificationRepository.insertNotification(reminderNotification)

        // When
        val workoutNotifications = notificationRepository.getNotificationsByType(
            testUserId, NotificationType.WORKOUT_REMINDER
        ).first()

        // Then
        assertEquals(1, workoutNotifications.size)
        assertEquals("Workout", workoutNotifications[0].title)
        assertEquals(NotificationType.WORKOUT_REMINDER, workoutNotifications[0].type)
    }

    @Test
    fun `notificationRepository getNotificationsByStatus filters by status`() = runTest {
        // Given
        val pendingNotification = createTestNotification(userId = testUserId, status = NotificationStatus.PENDING, title = "Pending")
        val sentNotification = createTestNotification(userId = testUserId, status = NotificationStatus.SENT, title = "Sent")
        val readNotification = createTestNotification(userId = testUserId, status = NotificationStatus.READ, title = "Read")

        notificationRepository.insertNotification(pendingNotification)
        notificationRepository.insertNotification(sentNotification)
        notificationRepository.insertNotification(readNotification)

        // When
        val pendingNotifications = notificationRepository.getNotificationsByStatus(
            testUserId, NotificationStatus.PENDING
        ).first()

        // Then
        assertEquals(1, pendingNotifications.size)
        assertEquals("Pending", pendingNotifications[0].title)
        assertEquals(NotificationStatus.PENDING, pendingNotifications[0].status)
    }

    @Test
    fun `notificationRepository updateNotificationStatus changes status`() = runTest {
        // Given
        val notification = createTestNotification(
            userId = testUserId,
            status = NotificationStatus.PENDING
        )
        val notificationId = notificationRepository.insertNotification(notification)

        // When
        notificationRepository.updateNotificationStatus(
            notificationId, 
            NotificationStatus.SENT, 
            Date()
        )

        // Then
        val updatedNotification = notificationRepository.getNotificationById(notificationId)
        assertNotNull(updatedNotification)
        assertEquals(NotificationStatus.SENT, updatedNotification!!.status)
    }

    @Test
    fun `notificationRepository markNotificationAsRead updates read status`() = runTest {
        // Given
        val notification = createTestNotification(
            userId = testUserId,
            status = NotificationStatus.SENT
        )
        val notificationId = notificationRepository.insertNotification(notification)

        // When
        notificationRepository.markNotificationAsRead(notificationId, Date())

        // Then
        val readNotification = notificationRepository.getNotificationById(notificationId)
        assertNotNull(readNotification)
        assertEquals(NotificationStatus.READ, readNotification!!.status)
        assertNotNull(readNotification.readAt)
    }

    @Test
    fun `notificationRepository getUnreadNotifications returns unread only`() = runTest {
        // Given
        val unreadNotification1 = createTestNotification(userId = testUserId, status = NotificationStatus.SENT, title = "Unread 1")
        val unreadNotification2 = createTestNotification(userId = testUserId, status = NotificationStatus.PENDING, title = "Unread 2")
        val readNotification = createTestNotification(userId = testUserId, status = NotificationStatus.READ, title = "Read")

        notificationRepository.insertNotification(unreadNotification1)
        notificationRepository.insertNotification(unreadNotification2)
        notificationRepository.insertNotification(readNotification)

        // When
        val unreadNotifications = notificationRepository.getUnreadNotifications(testUserId).first()

        // Then
        assertEquals(2, unreadNotifications.size)
        assertTrue(unreadNotifications.any { it.title == "Unread 1" })
        assertTrue(unreadNotifications.any { it.title == "Unread 2" })
        assertFalse(unreadNotifications.any { it.title == "Read" })
    }

    @Test
    fun `notificationRepository searchNotifications finds matching notifications`() = runTest {
        // Given
        val workoutNotification = createTestNotification(userId = testUserId, title = "Workout Time", message = "Ready to workout?")
        val reminderNotification = createTestNotification(userId = testUserId, title = "Daily Reminder", message = "Don't forget your workout")
        val goalNotification = createTestNotification(userId = testUserId, title = "Goal Achieved", message = "Congratulations on your achievement")

        notificationRepository.insertNotification(workoutNotification)
        notificationRepository.insertNotification(reminderNotification)
        notificationRepository.insertNotification(goalNotification)

        // When
        val workoutNotifications = notificationRepository.searchNotifications(testUserId, "workout").first()

        // Then
        assertEquals(2, workoutNotifications.size)
        assertTrue(workoutNotifications.any { it.title == "Workout Time" })
        assertTrue(workoutNotifications.any { it.title == "Daily Reminder" })
        assertFalse(workoutNotifications.any { it.title == "Goal Achieved" })
    }

    // endregion

    // region Integration Tests

    @Test
    fun `repositories handle multi-user data isolation`() = runTest {
        // Given
        val anotherUser = User(
            email = "another@example.com",
            username = "anotheruser",
            passwordHash = "hash",
            passwordSalt = "salt"
        )
        val anotherUserId = database.userDao().insertUser(anotherUser)

        // Create data for both users
        val goal1 = createTestGoal(userId = testUserId, title = "User1 Goal")
        val goal2 = createTestGoal(userId = anotherUserId, title = "User2 Goal")
        
        goalRepository.insertGoal(goal1)
        goalRepository.insertGoal(goal2)

        // When
        val user1Goals = goalRepository.getGoalsByUser(testUserId).first()
        val user2Goals = goalRepository.getGoalsByUser(anotherUserId).first()

        // Then
        assertEquals(1, user1Goals.size)
        assertEquals(1, user2Goals.size)
        assertEquals("User1 Goal", user1Goals[0].title)
        assertEquals("User2 Goal", user2Goals[0].title)
        assertNotEquals(user1Goals[0].userId, user2Goals[0].userId)
    }

    @Test
    fun `repositories handle empty data gracefully`() = runTest {
        // When - query empty database
        val goals = goalRepository.getGoalsByUser(testUserId).first()
        val foodEntries = foodEntryRepository.getFoodEntriesByUserId(testUserId).first()
        val notifications = notificationRepository.getNotificationsByUserId(testUserId).first()

        // Then
        assertTrue(goals.isEmpty())
        assertTrue(foodEntries.isEmpty())
        assertTrue(notifications.isEmpty())
    }

    // endregion

    // Helper methods
    private fun createTestGoal(
        userId: Long,
        title: String = "Test Goal",
        description: String? = "Test goal description",
        type: GoalType = GoalType.STEP_COUNT,
        targetValue: Double = 10000.0,
        currentValue: Double = 0.0,
        unit: String = "steps",
        targetDate: Date = Date(System.currentTimeMillis() + 86400000L),
        status: GoalStatus = GoalStatus.ACTIVE
    ): Goal {
        return Goal(
            userId = userId,
            title = title,
            description = description,
            goalType = type,
            targetValue = targetValue,
            currentValue = currentValue,
            unit = unit,
            targetDate = targetDate,
            status = status,
            reminderEnabled = false,
            reminderFrequency = null,
            createdAt = Date(),
            updatedAt = Date()
        )
    }

    private fun createTestFoodEntry(
        userId: Long,
        foodName: String = "Test Food",
        brandName: String? = null,
        servingSize: Double = 1.0,
        servingUnit: String = "serving",
        caloriesPerServing: Double = 100.0,
        proteinGrams: Double = 5.0,
        carbsGrams: Double = 15.0,
        fatGrams: Double = 2.0,
        fiberGrams: Double? = null,
        sugarGrams: Double? = null,
        sodiumMg: Double? = null,
        mealType: MealType = MealType.LUNCH,
        dateConsumed: Date = Date()
    ): FoodEntry {
        return FoodEntry(
            userId = userId,
            foodName = foodName,
            brandName = brandName,
            servingSize = servingSize,
            servingUnit = servingUnit,
            caloriesPerServing = caloriesPerServing,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            fiberGrams = fiberGrams,
            sugarGrams = sugarGrams,
            sodiumMg = sodiumMg,
            mealType = mealType,
            dateConsumed = dateConsumed,
            createdAt = Date(),
            updatedAt = Date()
        )
    }

    private fun createTestNotification(
        userId: Long,
        title: String = "Test Notification",
        message: String = "Test notification message",
        type: NotificationType = NotificationType.GENERAL_REMINDER,
        status: NotificationStatus = NotificationStatus.PENDING,
        priority: NotificationPriority = NotificationPriority.NORMAL,
        channelId: String = "test_channel",
        scheduledTime: Date = Date(),
        relatedEntityType: String? = null,
        relatedEntityId: Long? = null
    ): Notification {
        return Notification(
            userId = userId,
            title = title,
            message = message,
            type = type,
            status = status,
            priority = priority,
            channelId = channelId,
            scheduledTime = scheduledTime,
            sentAt = null,
            readAt = null,
            clickedAt = null,
            dismissedAt = null,
            relatedEntityType = relatedEntityType,
            relatedEntityId = relatedEntityId,
            isRecurring = false,
            recurringPattern = null,
            retryCount = 0,
            systemNotificationId = null,
            createdAt = Date(),
            updatedAt = Date()
        )
    }
}
