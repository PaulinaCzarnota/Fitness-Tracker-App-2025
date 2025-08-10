package com.example.fitnesstrackerapp.data.database

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.fitnesstrackerapp.data.entity.MealType
import com.example.fitnesstrackerapp.data.entity.NotificationStatus
import com.example.fitnesstrackerapp.data.entity.NotificationType
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import com.example.fitnesstrackerapp.data.entity.isCompleted
import com.example.fitnesstrackerapp.util.test.TestHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Date

/**
 * Comprehensive unit tests for AppDatabase and all DAOs.
 *
 * Tests database operations, entity relationships, migrations,
 * and data integrity using in-memory database for fast execution.
 */
@ExperimentalCoroutinesApi
@RunWith(org.junit.runners.JUnit4::class)
class AppDatabaseTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var appDatabase: AppDatabase
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        appDatabase = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java,
        )
            .allowMainThreadQueries() // For testing only
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        appDatabase.close()
    }

    @Test
    fun testDatabaseCreation() {
        Assert.assertNotNull("Database should not be null", appDatabase)
        Assert.assertTrue("Database should be open", appDatabase.isOpen)
    }

    @Test
    fun testAllDaosAccessible() {
        // Verify all DAOs are accessible
        Assert.assertNotNull("UserDao should not be null", appDatabase.userDao())
        Assert.assertNotNull("WorkoutDao should not be null", appDatabase.workoutDao())
        Assert.assertNotNull("StepDao should not be null", appDatabase.stepDao())
        Assert.assertNotNull("GoalDao should not be null", appDatabase.goalDao())
        Assert.assertNotNull("FoodEntryDao should not be null", appDatabase.foodEntryDao())
        Assert.assertNotNull("NotificationDao should not be null", appDatabase.notificationDao())
    }

    // User Entity Tests
    @Test
    fun testUserInsertAndRetrieve() = runTest {
        val user = TestHelper.createTestUser(
            email = "test@example.com",
            username = "testuser",
        )

        val userId = appDatabase.userDao().insertUser(user)
        Assert.assertTrue("User ID should be valid", userId > 0)

        val retrievedUser = appDatabase.userDao().getUserById(userId)
        Assert.assertNotNull("Retrieved user should not be null", retrievedUser)
        Assert.assertEquals("Email should match", user.email, retrievedUser?.email)
        Assert.assertEquals("Username should match", user.username, retrievedUser?.username)
    }

    @Test
    fun testUserEmailUniqueness() = runTest {
        val user1 = TestHelper.createTestUser(
            email = "unique@example.com",
            username = "user1",
        )
        val user2 = TestHelper.createTestUser(
            email = "unique@example.com", // Same email
            username = "user2",
        )

        val user1Id = appDatabase.userDao().insertUser(user1)
        Assert.assertTrue("First user should be inserted", user1Id > 0)

        try {
            appDatabase.userDao().insertUser(user2)
            Assert.fail("Should throw exception for duplicate email")
        } catch (e: Exception) {
            // Expected exception for unique constraint violation
            Assert.assertTrue(
                "Exception should be constraint related",
                e.message?.contains("UNIQUE") == true ||
                    e.message?.contains("constraint") == true,
            )
        }
    }

    @Test
    fun testUserUpdate() = runTest {
        val user = TestHelper.createTestUser(
            email = "update@example.com",
            username = "updateuser",
            firstName = "Original",
        )

        val userId = appDatabase.userDao().insertUser(user)
        val updatedUser = user.copy(id = userId, firstName = "Updated")

        appDatabase.userDao().updateUser(updatedUser)

        val retrievedUser = appDatabase.userDao().getUserById(userId)
        Assert.assertEquals("First name should be updated", "Updated", retrievedUser?.firstName)
    }

    // Workout Entity Tests
    @Test
    fun testWorkoutInsertAndRetrieve() = runTest {
        // First create a user
        val user = TestHelper.createTestUser(
            email = "workout@example.com",
            username = "workoutuser",
        )
        val userId = appDatabase.userDao().insertUser(user)

        // Then create workout
        val workout = TestHelper.createTestWorkout(
            userId = userId,
            workoutType = WorkoutType.RUNNING,
            title = "Morning Run",
        )

        val workoutId = appDatabase.workoutDao().insertWorkout(workout)
        Assert.assertTrue("Workout ID should be valid", workoutId > 0)

        val retrievedWorkout = appDatabase.workoutDao().getWorkoutById(workoutId)
        Assert.assertNotNull("Retrieved workout should not be null", retrievedWorkout)
        Assert.assertEquals("User ID should match", userId, retrievedWorkout?.userId)
        Assert.assertEquals("Workout type should match", WorkoutType.RUNNING, retrievedWorkout?.workoutType)
    }

    @Test
    fun testWorkoutForeignKeyConstraint() = runTest {
        val workout = TestHelper.createTestWorkout(
            userId = 999L, // Non-existent user
            workoutType = WorkoutType.RUNNING,
            title = "Invalid Workout",
        )

        try {
            appDatabase.workoutDao().insertWorkout(workout)
            Assert.fail("Should throw foreign key constraint exception")
        } catch (e: Exception) {
            // Expected foreign key constraint violation
            Assert.assertTrue(
                "Exception should be foreign key related",
                e.message?.contains("FOREIGN KEY") == true ||
                    e.message?.contains("constraint") == true,
            )
        }
    }

    @Test
    fun testWorkoutCascadeDelete() = runTest {
        // Create user and workout
        val user = TestHelper.createTestUser(
            email = "cascade@example.com",
            username = "cascadeuser",
        )
        val userId = appDatabase.userDao().insertUser(user)

        val workout = TestHelper.createTestWorkout(
            userId = userId,
            workoutType = WorkoutType.RUNNING,
            title = "Cascade Test",
        )
        val workoutId = appDatabase.workoutDao().insertWorkout(workout)

        // Verify workout exists
        var retrievedWorkout = appDatabase.workoutDao().getWorkoutById(workoutId)
        Assert.assertNotNull("Workout should exist before user deletion", retrievedWorkout)

        // Delete user (should cascade delete workout)
        appDatabase.userDao().deleteUserById(userId)

        // Verify workout is also deleted
        retrievedWorkout = appDatabase.workoutDao().getWorkoutById(workoutId)
        Assert.assertNull("Workout should be deleted after user deletion", retrievedWorkout)
    }

    // Goal Entity Tests
    @Test
    fun testGoalInsertAndRetrieve() = runTest {
        val user = TestHelper.createTestUser(
            email = "goal@example.com",
            username = "goaluser",
        )
        val userId = appDatabase.userDao().insertUser(user)

        val goal = TestHelper.createTestGoal(
            userId = userId,
            title = "Lose Weight",
            targetValue = 10.0,
            unit = "kg",
        )

        val goalId = appDatabase.goalDao().insertGoal(goal)
        Assert.assertTrue("Goal ID should be valid", goalId > 0)

        val retrievedGoal = appDatabase.goalDao().getGoalById(goalId)
        Assert.assertNotNull("Retrieved goal should not be null", retrievedGoal)
        Assert.assertEquals("Title should match", "Lose Weight", retrievedGoal?.title)
        Assert.assertEquals(
            "Target value should match",
            10.0,
            retrievedGoal?.targetValue ?: 0.0,
            0.01,
        )
    }

    @Test
    fun testGoalProgressCalculation() = runTest {
        val user = TestHelper.createTestUser(
            email = "progress@example.com",
            username = "progressuser",
        )
        val userId = appDatabase.userDao().insertUser(user)

        val goal = TestHelper.createTestGoal(
            userId = userId,
            title = "Run Distance",
            targetValue = 100.0,
            currentValue = 75.0,
            unit = "km",
        )

        val goalId = appDatabase.goalDao().insertGoal(goal)
        val retrievedGoal = appDatabase.goalDao().getGoalById(goalId)

        Assert.assertNotNull("Goal should exist", retrievedGoal)
        Assert.assertEquals(
            "Progress should be 75%",
            75.0f,
            retrievedGoal?.getProgressPercentage() ?: 0.0f,
            0.1f,
        )
        Assert.assertFalse("Goal should not be completed", retrievedGoal?.isCompleted() ?: true)
    }

    // Notification Entity Tests
    @Test
    fun testNotificationInsertAndRetrieve() = runTest {
        val user = TestHelper.createTestUser(
            email = "notification@example.com",
            username = "notifyuser",
        )
        val userId = appDatabase.userDao().insertUser(user)

        val notification = TestHelper.createTestNotification(
            userId = userId,
            type = NotificationType.WORKOUT_REMINDER,
            title = "Workout Reminder",
            message = "Time for your workout!",
        )

        val notificationId = appDatabase.notificationDao().insertNotification(notification)
        Assert.assertTrue("Notification ID should be valid", notificationId > 0)

        val retrievedNotification = appDatabase.notificationDao().getNotificationById(notificationId)
        Assert.assertNotNull("Retrieved notification should not be null", retrievedNotification)
        Assert.assertEquals(
            "Type should match",
            NotificationType.WORKOUT_REMINDER,
            retrievedNotification?.type,
        )
        Assert.assertEquals(
            "Title should match",
            "Workout Reminder",
            retrievedNotification?.title,
        )
    }

    @Test
    fun testNotificationStatusUpdate() = runTest {
        val user = TestHelper.createTestUser(
            email = "status@example.com",
            username = "statususer",
        )
        val userId = appDatabase.userDao().insertUser(user)

        val notification = TestHelper.createTestNotification(
            userId = userId,
            type = NotificationType.GOAL_ACHIEVEMENT,
            title = "Goal Achieved!",
            message = "Congratulations!",
        )

        val notificationId = appDatabase.notificationDao().insertNotification(notification)
        val currentTime = Date()

        // Mark as sent
        appDatabase.notificationDao().markNotificationAsSent(notificationId, currentTime)

        val updatedNotification = appDatabase.notificationDao().getNotificationById(notificationId)
        Assert.assertEquals(
            "Status should be SENT",
            NotificationStatus.SENT,
            updatedNotification?.status,
        )
        Assert.assertNotNull("Sent time should be set", updatedNotification?.sentTime)
    }

    // Food Entry Entity Tests
    @Test
    fun testFoodEntryInsertAndRetrieve() = runTest {
        val user = TestHelper.createTestUser(
            email = "food@example.com",
            username = "fooduser",
        )
        val userId = appDatabase.userDao().insertUser(user)

        val foodEntry = TestHelper.createTestFoodEntry(
            userId = userId,
            foodName = "Apple",
            caloriesPerServing = 95.0,
            mealType = MealType.BREAKFAST,
        )

        val foodEntryId = appDatabase.foodEntryDao().insertFoodEntry(foodEntry)
        Assert.assertTrue("Food entry ID should be valid", foodEntryId > 0)

        val retrievedFoodEntry = appDatabase.foodEntryDao().getFoodEntriesByUserId(userId).first()
        Assert.assertNotNull("Food entries should not be null", retrievedFoodEntry)
        Assert.assertTrue(
            "Should contain the inserted food entry",
            retrievedFoodEntry.any { it.foodName == "Apple" },
        )
    }

    @Test
    fun testFoodEntryNutritionCalculation() = runTest {
        val user = TestHelper.createTestUser(
            email = "nutrition@example.com",
            username = "nutritionuser",
        )
        val userId = appDatabase.userDao().insertUser(user)

        val foodEntry = TestHelper.createTestFoodEntry(
            userId = userId,
            foodName = "Banana",
            servingSize = 2.0, // 2 bananas
            caloriesPerServing = 105.0, // per banana
            proteinGrams = 1.3,
            mealType = MealType.SNACK,
        )

        appDatabase.foodEntryDao().insertFoodEntry(foodEntry)

        // Total calories should be 2 * 105 = 210
        Assert.assertEquals(
            "Total calories should be correct",
            210.0,
            foodEntry.getTotalCalories(),
            0.01,
        )
        Assert.assertEquals(
            "Total protein should be correct",
            2.6,
            foodEntry.getTotalProtein(),
            0.01,
        )
    }

    // Integration Tests
    @Test
    fun testMultipleEntityRelationships() = runTest {
        // Create a user
        val user = TestHelper.createTestUser(
            email = "integration@example.com",
            username = "intuser",
        )
        val userId = appDatabase.userDao().insertUser(user)

        // Create a goal for the user
        val goal = TestHelper.createTestGoal(
            userId = userId,
            title = "Daily Steps",
            targetValue = 10000.0,
            unit = "steps",
        )
        val goalId = appDatabase.goalDao().insertGoal(goal)

        // Create a notification related to the goal
        val notification = TestHelper.createTestNotification(
            userId = userId,
            type = NotificationType.GOAL_DEADLINE_APPROACHING,
            title = "Goal Deadline",
            message = "Your goal deadline is approaching",
            relatedEntityId = goalId,
            relatedEntityType = "goal",
        )
        val notificationId = appDatabase.notificationDao().insertNotification(notification)

        // Verify all entities are connected properly
        val retrievedUser = appDatabase.userDao().getUserById(userId)
        val retrievedGoal = appDatabase.goalDao().getGoalById(goalId)
        val retrievedNotification = appDatabase.notificationDao().getNotificationById(notificationId)

        Assert.assertNotNull("User should exist", retrievedUser)
        Assert.assertNotNull("Goal should exist", retrievedGoal)
        Assert.assertNotNull("Notification should exist", retrievedNotification)

        Assert.assertEquals("Goal should belong to user", userId, retrievedGoal?.userId)
        Assert.assertEquals("Notification should belong to user", userId, retrievedNotification?.userId)
        Assert.assertEquals(
            "Notification should reference goal",
            goalId,
            retrievedNotification?.relatedEntityId,
        )
    }

    @Test
    fun testDatabaseIntegrityValidation() = runTest {
        // This would typically test the validateDatabaseIntegrity method
        // For in-memory database, we'll test basic operations work correctly

        val user = TestHelper.createTestUser(
            email = "integrity@example.com",
            username = "integrityuser",
        )
        val userId = appDatabase.userDao().insertUser(user)

        // Perform multiple operations
        val workout = TestHelper.createTestWorkout(userId = userId, title = "Test Workout")
        val goal = TestHelper.createTestGoal(userId = userId, title = "Test Goal")
        val foodEntry = TestHelper.createTestFoodEntry(userId = userId, foodName = "Test Food")
        val notification = TestHelper.createTestNotification(userId = userId, title = "Test Notification")

        appDatabase.workoutDao().insertWorkout(workout)
        appDatabase.goalDao().insertGoal(goal)
        appDatabase.foodEntryDao().insertFoodEntry(foodEntry)
        appDatabase.notificationDao().insertNotification(notification)

        // Verify all data is accessible
        Assert.assertNotNull("User should exist", appDatabase.userDao().getUserById(userId))
        Assert.assertTrue(
            "Workouts should exist",
            appDatabase.workoutDao().getWorkoutsByUserId(userId).first().isNotEmpty(),
        )
        Assert.assertTrue(
            "Goals should exist",
            appDatabase.goalDao().getGoalsByUser(userId).first().isNotEmpty(),
        )
        Assert.assertTrue(
            "Food entries should exist",
            appDatabase.foodEntryDao().getFoodEntriesByUserId(userId).first().isNotEmpty(),
        )
        Assert.assertTrue(
            "Notifications should exist",
            appDatabase.notificationDao().getNotificationsByUserId(userId).first().isNotEmpty(),
        )
    }

    @Test
    fun testInMemoryDatabaseCleanup() = runTest {
        // Insert some data
        val user = TestHelper.createTestUser(
            email = "cleanup@example.com",
            username = "cleanupuser",
        )
        appDatabase.userDao().insertUser(user)

        Assert.assertTrue(
            "User count should be > 0",
            appDatabase.userDao().getUserCount() > 0,
        )

        // Clear all tables
        appDatabase.clearAllAppTables()

        // Verify data is cleared
        Assert.assertEquals(
            "User count should be 0 after cleanup",
            0,
            appDatabase.userDao().getUserCount(),
        )
    }

    @Test
    fun testDatabaseVersionAndMetadata() {
        // Test database version consistency
        Assert.assertEquals(
            "Database version should match",
            2,
            AppDatabase.getCurrentVersion(),
        )

        // Test database name and configuration
        Assert.assertNotNull("Database should have proper configuration", appDatabase)
        Assert.assertTrue("Database should be open", appDatabase.isOpen)
    }

    // Edge Case Tests
    @Test
    fun testLargeDataInsertion() = runTest {
        val user = TestHelper.createTestUser(
            email = "bulk@example.com",
            username = "bulkuser",
        )
        val userId = appDatabase.userDao().insertUser(user)

        // Insert multiple workouts
        val workouts = (1..10).map { i ->
            TestHelper.createTestWorkout(
                userId = userId,
                title = "Workout $i",
                workoutType = WorkoutType.RUNNING,
            )
        }

        workouts.forEach { workout ->
            appDatabase.workoutDao().insertWorkout(workout)
        }

        val retrievedWorkouts = appDatabase.workoutDao().getWorkoutsByUserId(userId).first()
        Assert.assertEquals("Should have 10 workouts", 10, retrievedWorkouts.size)
    }

    @Test
    fun testNullHandling() = runTest {
        val user = TestHelper.createTestUser(
            email = "null@example.com",
            username = "nulluser",
            firstName = null, // Test null handling
            lastName = null,
        )

        val userId = appDatabase.userDao().insertUser(user)
        val retrievedUser = appDatabase.userDao().getUserById(userId)

        Assert.assertNotNull("User should exist", retrievedUser)
        Assert.assertNull("First name should be null", retrievedUser?.firstName)
        Assert.assertNull("Last name should be null", retrievedUser?.lastName)
        Assert.assertEquals("Full name should be empty", "", retrievedUser?.getFullName())
    }
}
