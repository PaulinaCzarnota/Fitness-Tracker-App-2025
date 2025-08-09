package com.example.fitnesstrackerapp.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

/**
 * Comprehensive unit tests for WorkoutDao.
 *
 * Tests all workout-related database operations including:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Analytics and performance tracking
 * - Date range queries and filtering
 * - Statistics calculation
 * - Data integrity and constraints
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class WorkoutDaoTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var workoutDao: WorkoutDao
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        workoutDao = database.workoutDao()
        userDao = database.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetWorkout() = runTest {
        val userId = createTestUser()
        val workout = createTestWorkout(
            userId = userId,
            workoutType = WorkoutType.RUNNING,
            title = "Morning Run",
            duration = 30,
            distance = 5.0f,
            caloriesBurned = 300
        )

        val workoutId = workoutDao.insertWorkout(workout)
        assertThat(workoutId).isGreaterThan(0)

        val retrievedWorkout = workoutDao.getWorkoutById(workoutId)
        assertThat(retrievedWorkout).isNotNull()
        assertThat(retrievedWorkout?.title).isEqualTo("Morning Run")
        assertThat(retrievedWorkout?.workoutType).isEqualTo(WorkoutType.RUNNING)
        assertThat(retrievedWorkout?.duration).isEqualTo(30)
        assertThat(retrievedWorkout?.distance).isEqualTo(5.0f)
        assertThat(retrievedWorkout?.caloriesBurned).isEqualTo(300)
    }

    @Test
    fun getWorkoutsByUserId() = runTest {
        val userId = createTestUser()

        // Insert multiple workouts for the user
        val workouts = listOf(
            createTestWorkout(userId = userId, title = "Workout 1", workoutType = WorkoutType.RUNNING),
            createTestWorkout(userId = userId, title = "Workout 2", workoutType = WorkoutType.STRENGTH_TRAINING),
            createTestWorkout(userId = userId, title = "Workout 3", workoutType = WorkoutType.CYCLING)
        )

        workouts.forEach { workout ->
            workoutDao.insertWorkout(workout)
        }

        val userWorkouts = workoutDao.getWorkoutsByUserId(userId).first()
        assertThat(userWorkouts).hasSize(3)
        // Should be ordered by start time DESC
        assertThat(userWorkouts.map { it.title }).containsExactly("Workout 3", "Workout 2", "Workout 1")
    }

    @Test
    fun getWorkoutsByType() = runTest {
        val userId = createTestUser()

        workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "Run 1", workoutType = WorkoutType.RUNNING))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "Run 2", workoutType = WorkoutType.RUNNING))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "Strength", workoutType = WorkoutType.STRENGTH_TRAINING))

        val runningWorkouts = workoutDao.getWorkoutsByType(userId, WorkoutType.RUNNING).first()
        assertThat(runningWorkouts).hasSize(2)
        assertThat(runningWorkouts.all { it.workoutType == WorkoutType.RUNNING }).isTrue()
        assertThat(runningWorkouts.map { it.title }).containsExactly("Run 2", "Run 1")
    }

    @Test
    fun getRecentWorkouts() = runTest {
        val userId = createTestUser()

        // Insert 5 workouts
        (1..5).forEach { i ->
            workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "Workout $i"))
        }

        val recentWorkouts = workoutDao.getRecentWorkouts(userId, 3).first()
        assertThat(recentWorkouts).hasSize(3)
        assertThat(recentWorkouts.map { it.title }).containsExactly("Workout 5", "Workout 4", "Workout 3")
    }

    @Test
    fun getWorkoutsInDateRange() = runTest {
        val userId = createTestUser()
        val baseTime = System.currentTimeMillis()

        val workout1 = createTestWorkout(
            userId = userId,
            title = "Old Workout",
            startTime = Date(baseTime - (10 * 24 * 60 * 60 * 1000L)) // 10 days ago
        )
        val workout2 = createTestWorkout(
            userId = userId,
            title = "Recent Workout",
            startTime = Date(baseTime - (2 * 24 * 60 * 60 * 1000L)) // 2 days ago
        )
        val workout3 = createTestWorkout(
            userId = userId,
            title = "Today Workout",
            startTime = Date(baseTime)
        )

        workoutDao.insertWorkout(workout1)
        workoutDao.insertWorkout(workout2)
        workoutDao.insertWorkout(workout3)

        val startDate = Date(baseTime - (3 * 24 * 60 * 60 * 1000L)) // 3 days ago
        val endDate = Date(baseTime + (24 * 60 * 60 * 1000L)) // Tomorrow

        val workoutsInRange = workoutDao.getWorkoutsInDateRange(userId, startDate, endDate).first()
        assertThat(workoutsInRange).hasSize(2)
        assertThat(workoutsInRange.map { it.title }).containsExactly("Today Workout", "Recent Workout")
    }

    @Test
    fun getTotalWorkoutCount() = runTest {
        val userId = createTestUser()

        assertThat(workoutDao.getTotalWorkoutCount(userId)).isEqualTo(0)

        workoutDao.insertWorkout(createTestWorkout(userId = userId))
        assertThat(workoutDao.getTotalWorkoutCount(userId)).isEqualTo(1)

        workoutDao.insertWorkout(createTestWorkout(userId = userId))
        assertThat(workoutDao.getTotalWorkoutCount(userId)).isEqualTo(2)
    }

    @Test
    fun getTotalCaloriesBurned() = runTest {
        val userId = createTestUser()

        workoutDao.insertWorkout(createTestWorkout(userId = userId, caloriesBurned = 200))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, caloriesBurned = 300))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, caloriesBurned = 150))

        val totalCalories = workoutDao.getTotalCaloriesBurned(userId)
        assertThat(totalCalories).isEqualTo(650)
    }

    @Test
    fun getTotalWorkoutDuration() = runTest {
        val userId = createTestUser()

        workoutDao.insertWorkout(createTestWorkout(userId = userId, duration = 30))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, duration = 45))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, duration = 60))

        val totalDuration = workoutDao.getTotalWorkoutDuration(userId)
        assertThat(totalDuration).isEqualTo(135)
    }

    @Test
    fun getTotalDistance() = runTest {
        val userId = createTestUser()

        workoutDao.insertWorkout(createTestWorkout(userId = userId, distance = 5.0f))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, distance = 3.5f))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, distance = 7.2f))

        val totalDistance = workoutDao.getTotalDistance(userId)
        assertThat(totalDistance).isWithin(0.1f).of(15.7f)
    }

    @Test
    fun getAverageWorkoutDuration() = runTest {
        val userId = createTestUser()

        workoutDao.insertWorkout(createTestWorkout(userId = userId, duration = 30))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, duration = 60))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, duration = 45))

        val avgDuration = workoutDao.getAverageWorkoutDuration(userId)
        assertThat(avgDuration).isWithin(0.1f).of(45f)
    }

    @Test
    fun getAverageCaloriesBurned() = runTest {
        val userId = createTestUser()

        workoutDao.insertWorkout(createTestWorkout(userId = userId, caloriesBurned = 200))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, caloriesBurned = 400))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, caloriesBurned = 300))

        val avgCalories = workoutDao.getAverageCaloriesBurned(userId)
        assertThat(avgCalories).isWithin(0.1f).of(300f)
    }

    @Test
    fun getWorkoutCountByType() = runTest {
        val userId = createTestUser()

        workoutDao.insertWorkout(createTestWorkout(userId = userId, workoutType = WorkoutType.RUNNING))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, workoutType = WorkoutType.RUNNING))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, workoutType = WorkoutType.STRENGTH_TRAINING))

        val runningCount = workoutDao.getWorkoutCountByType(userId, WorkoutType.RUNNING)
        assertThat(runningCount).isEqualTo(2)

        val strengthCount = workoutDao.getWorkoutCountByType(userId, WorkoutType.STRENGTH_TRAINING)
        assertThat(strengthCount).isEqualTo(1)
    }

    @Test
    fun getTotalCaloriesByType() = runTest {
        val userId = createTestUser()

        workoutDao.insertWorkout(createTestWorkout(userId = userId, workoutType = WorkoutType.RUNNING, caloriesBurned = 300))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, workoutType = WorkoutType.RUNNING, caloriesBurned = 250))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, workoutType = WorkoutType.STRENGTH_TRAINING, caloriesBurned = 200))

        val runningCalories = workoutDao.getTotalCaloriesByType(userId, WorkoutType.RUNNING)
        assertThat(runningCalories).isEqualTo(550)

        val strengthCalories = workoutDao.getTotalCaloriesByType(userId, WorkoutType.STRENGTH_TRAINING)
        assertThat(strengthCalories).isEqualTo(200)
    }

    @Test
    fun getMonthlyWorkoutCount() = runTest {
        val userId = createTestUser()
        val calendar = Calendar.getInstance()

        // Insert workouts in current month and previous month
        val currentMonthWorkout = createTestWorkout(
            userId = userId,
            startTime = Date() // Current time
        )
        
        calendar.add(Calendar.MONTH, -1)
        val previousMonthWorkout = createTestWorkout(
            userId = userId,
            startTime = calendar.time
        )

        workoutDao.insertWorkout(currentMonthWorkout)
        workoutDao.insertWorkout(previousMonthWorkout)

        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR).toString()
        val month = String.format("%02d", currentDate.get(Calendar.MONTH) + 1)

        val monthlyCount = workoutDao.getMonthlyWorkoutCount(userId, year, month)
        assertThat(monthlyCount).isEqualTo(1)
    }

    @Test
    fun getBestWorkoutByCalories() = runTest {
        val userId = createTestUser()

        workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "Low", caloriesBurned = 200))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "High", caloriesBurned = 500))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "Medium", caloriesBurned = 350))

        val bestWorkout = workoutDao.getBestWorkoutByCalories(userId)
        assertThat(bestWorkout).isNotNull()
        assertThat(bestWorkout?.title).isEqualTo("High")
        assertThat(bestWorkout?.caloriesBurned).isEqualTo(500)
    }

    @Test
    fun getLongestWorkout() = runTest {
        val userId = createTestUser()

        workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "Short", duration = 30))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "Long", duration = 120))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "Medium", duration = 60))

        val longestWorkout = workoutDao.getLongestWorkout(userId)
        assertThat(longestWorkout).isNotNull()
        assertThat(longestWorkout?.title).isEqualTo("Long")
        assertThat(longestWorkout?.duration).isEqualTo(120)
    }

    @Test
    fun getOngoingWorkouts() = runTest {
        val userId = createTestUser()
        val currentTime = Date()

        // Workout with no end time (ongoing)
        val ongoingWorkout = createTestWorkout(
            userId = userId,
            title = "Ongoing",
            startTime = currentTime,
            endTime = null
        )

        // Completed workout
        val completedWorkout = createTestWorkout(
            userId = userId,
            title = "Completed",
            startTime = Date(currentTime.time - 60000),
            endTime = currentTime
        )

        workoutDao.insertWorkout(ongoingWorkout)
        workoutDao.insertWorkout(completedWorkout)

        val ongoingWorkouts = workoutDao.getOngoingWorkouts(userId)
        assertThat(ongoingWorkouts).hasSize(1)
        assertThat(ongoingWorkouts[0].title).isEqualTo("Ongoing")
        assertThat(ongoingWorkouts[0].endTime).isNull()
    }

    @Test
    fun getCompletedWorkouts() = runTest {
        val userId = createTestUser()
        val currentTime = Date()

        // Completed workout
        val completedWorkout = createTestWorkout(
            userId = userId,
            title = "Completed",
            startTime = Date(currentTime.time - 60000),
            endTime = currentTime
        )

        // Ongoing workout
        val ongoingWorkout = createTestWorkout(
            userId = userId,
            title = "Ongoing",
            startTime = currentTime,
            endTime = null
        )

        workoutDao.insertWorkout(completedWorkout)
        workoutDao.insertWorkout(ongoingWorkout)

        val completedWorkouts = workoutDao.getCompletedWorkouts(userId).first()
        assertThat(completedWorkouts).hasSize(1)
        assertThat(completedWorkouts[0].title).isEqualTo("Completed")
        assertThat(completedWorkouts[0].endTime).isNotNull()
    }

    @Test
    fun updateWorkoutEndTime() = runTest {
        val userId = createTestUser()
        val startTime = Date()
        val workout = createTestWorkout(
            userId = userId,
            title = "To Complete",
            startTime = startTime,
            endTime = null,
            duration = 0
        )

        val workoutId = workoutDao.insertWorkout(workout)
        val endTime = Date(startTime.time + (30 * 60 * 1000)) // 30 minutes later
        val duration = 30

        workoutDao.updateWorkoutEndTime(workoutId, endTime, duration)

        val updatedWorkout = workoutDao.getWorkoutById(workoutId)
        assertThat(updatedWorkout?.endTime).isEqualTo(endTime)
        assertThat(updatedWorkout?.duration).isEqualTo(duration)
        assertThat(updatedWorkout?.updatedAt).isEqualTo(endTime)
    }

    @Test
    fun updateWorkout() = runTest {
        val userId = createTestUser()
        val workout = createTestWorkout(
            userId = userId,
            title = "Original",
            duration = 30,
            caloriesBurned = 200
        )

        val workoutId = workoutDao.insertWorkout(workout)
        val updatedWorkout = workout.copy(
            id = workoutId,
            title = "Updated",
            duration = 45,
            caloriesBurned = 300,
            updatedAt = Date()
        )

        workoutDao.updateWorkout(updatedWorkout)

        val retrievedWorkout = workoutDao.getWorkoutById(workoutId)
        assertThat(retrievedWorkout?.title).isEqualTo("Updated")
        assertThat(retrievedWorkout?.duration).isEqualTo(45)
        assertThat(retrievedWorkout?.caloriesBurned).isEqualTo(300)
    }

    @Test
    fun deleteWorkout() = runTest {
        val userId = createTestUser()
        val workout = createTestWorkout(userId = userId, title = "To Delete")

        val workoutId = workoutDao.insertWorkout(workout)

        // Verify workout exists
        var retrievedWorkout = workoutDao.getWorkoutById(workoutId)
        assertThat(retrievedWorkout).isNotNull()

        // Delete workout
        workoutDao.deleteWorkout(workout.copy(id = workoutId))

        // Verify workout is deleted
        retrievedWorkout = workoutDao.getWorkoutById(workoutId)
        assertThat(retrievedWorkout).isNull()
    }

    @Test
    fun deleteWorkoutById() = runTest {
        val userId = createTestUser()
        val workout = createTestWorkout(userId = userId, title = "To Delete By ID")

        val workoutId = workoutDao.insertWorkout(workout)

        // Verify workout exists
        var retrievedWorkout = workoutDao.getWorkoutById(workoutId)
        assertThat(retrievedWorkout).isNotNull()

        // Delete by ID
        workoutDao.deleteWorkoutById(workoutId)

        // Verify workout is deleted
        retrievedWorkout = workoutDao.getWorkoutById(workoutId)
        assertThat(retrievedWorkout).isNull()
    }

    @Test
    fun deleteAllWorkoutsForUser() = runTest {
        val userId = createTestUser()

        // Insert multiple workouts
        workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "Workout 1"))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "Workout 2"))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "Workout 3"))

        // Verify workouts exist
        var userWorkouts = workoutDao.getWorkoutsByUserId(userId).first()
        assertThat(userWorkouts).hasSize(3)

        // Delete all workouts for user
        workoutDao.deleteAllWorkoutsForUser(userId)

        // Verify all workouts are deleted
        userWorkouts = workoutDao.getWorkoutsByUserId(userId).first()
        assertThat(userWorkouts).isEmpty()
    }

    @Test
    fun testForeignKeyConstraint() = runTest {
        val workout = createTestWorkout(
            userId = 999L, // Non-existent user
            title = "Invalid Workout"
        )

        try {
            workoutDao.insertWorkout(workout)
            Assert.fail("Should throw foreign key constraint exception")
        } catch (e: Exception) {
            // Expected foreign key constraint violation
            assertThat(e.message).containsAnyOf("FOREIGN KEY", "constraint", "no such table")
        }
    }

    @Test
    fun testWorkoutCascadeDelete() = runTest {
        val userId = createTestUser()
        val workout = createTestWorkout(userId = userId, title = "Cascade Test")
        val workoutId = workoutDao.insertWorkout(workout)

        // Verify workout exists
        var retrievedWorkout = workoutDao.getWorkoutById(workoutId)
        assertThat(retrievedWorkout).isNotNull()

        // Delete user (should cascade delete workout)
        userDao.deleteUserById(userId)

        // Verify workout is also deleted
        retrievedWorkout = workoutDao.getWorkoutById(workoutId)
        assertThat(retrievedWorkout).isNull()
    }

    @Test
    fun getWorkoutsForDate() = runTest {
        val userId = createTestUser()
        val today = Date()
        val yesterday = Date(today.time - (24 * 60 * 60 * 1000))

        workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "Today", startTime = today))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, title = "Yesterday", startTime = yesterday))

        val todayWorkouts = workoutDao.getWorkoutsForDate(userId, today).first()
        assertThat(todayWorkouts).hasSize(1)
        assertThat(todayWorkouts[0].title).isEqualTo("Today")
    }

    @Test
    fun getWorkoutCountInDateRange() = runTest {
        val userId = createTestUser()
        val baseTime = System.currentTimeMillis()

        val startDate = Date(baseTime - (7 * 24 * 60 * 60 * 1000L)) // 7 days ago
        val endDate = Date(baseTime) // Now

        // Insert workouts: 2 in range, 1 out of range
        workoutDao.insertWorkout(createTestWorkout(userId = userId, startTime = Date(baseTime - (5 * 24 * 60 * 60 * 1000L))))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, startTime = Date(baseTime - (3 * 24 * 60 * 60 * 1000L))))
        workoutDao.insertWorkout(createTestWorkout(userId = userId, startTime = Date(baseTime - (10 * 24 * 60 * 60 * 1000L)))) // Out of range

        val count = workoutDao.getWorkoutCountInDateRange(userId, startDate, endDate)
        assertThat(count).isEqualTo(2)
    }

    private suspend fun createTestUser(): Long {
        val user = User(
            email = "workout_test@example.com",
            username = "workoutuser",
            passwordHash = "test_hash",
            passwordSalt = "test_salt",
            createdAt = Date(),
            updatedAt = Date()
        )
        return userDao.insertUser(user)
    }

    private fun createTestWorkout(
        userId: Long,
        workoutType: WorkoutType = WorkoutType.RUNNING,
        title: String = "Test Workout",
        startTime: Date = Date(),
        endTime: Date? = Date(),
        duration: Int = 30,
        distance: Float = 3.0f,
        caloriesBurned: Int = 200,
        steps: Int = 3000,
        avgHeartRate: Int? = 150,
        maxHeartRate: Int? = 180
    ) = Workout(
        userId = userId,
        workoutType = workoutType,
        title = title,
        startTime = startTime,
        endTime = endTime,
        duration = duration,
        distance = distance,
        caloriesBurned = caloriesBurned,
        steps = steps,
        avgHeartRate = avgHeartRate,
        maxHeartRate = maxHeartRate,
        createdAt = Date(),
        updatedAt = Date()
    )
}
