/**
 * Comprehensive unit tests for WorkoutRepository using Room in-memory database and JUnit 5.
 *
 * This test class covers:
 * - CRUD operations for workout entities
 * - Date range filtering and workout history
 * - Statistical calculations and analytics
 * - Weekly stats computation with proper date handling
 * - Workout type categorization and filtering
 * - Performance metrics and duration calculations
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
import java.util.Calendar
import java.util.Date

@RunWith(AndroidJUnit4::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkoutRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var testUser: User
    private var testUserId: Long = 0L

    @BeforeAll
    fun setupDatabase() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
    }

    @BeforeEach
    fun setup() = runTest {
        workoutRepository = WorkoutRepository(database.workoutDao())

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

    @AfterEach
    fun cleanup() = runTest {
        database.clearAllTables()
    }

    @AfterAll
    fun closeDatabase() {
        database.close()
    }

    // region CRUD Operations Tests

    @Test
    fun `insertWorkout creates workout with valid data`() = runTest {
        // Given
        val workout = createTestWorkout(
            userId = testUserId,
            name = "Morning Run",
            type = WorkoutType.RUNNING,
            duration = 30,
            caloriesBurned = 300,
            distance = 5.0,
        )

        // When
        val workoutId = workoutRepository.insertWorkout(workout)

        // Then
        assertTrue(workoutId > 0)

        val savedWorkout = database.workoutDao().getWorkoutById(workoutId)
        assertNotNull(savedWorkout)
        assertEquals(workout.name, savedWorkout!!.name)
        assertEquals(workout.type, savedWorkout.type)
        assertEquals(workout.durationMinutes, savedWorkout.durationMinutes)
        assertEquals(workout.caloriesBurned, savedWorkout.caloriesBurned)
        assertEquals(workout.distanceKm, savedWorkout.distanceKm, 0.001)
        assertEquals(testUserId, savedWorkout.userId)
    }

    @Test
    fun `updateWorkout modifies existing workout`() = runTest {
        // Given
        val originalWorkout = createTestWorkout(
            userId = testUserId,
            name = "Original Workout",
            duration = 20,
        )
        val workoutId = workoutRepository.insertWorkout(originalWorkout)

        val updatedWorkout = originalWorkout.copy(
            id = workoutId,
            name = "Updated Workout",
            durationMinutes = 45,
            caloriesBurned = 400,
        )

        // When
        workoutRepository.updateWorkout(updatedWorkout)

        // Then
        val savedWorkout = database.workoutDao().getWorkoutById(workoutId)
        assertNotNull(savedWorkout)
        assertEquals("Updated Workout", savedWorkout!!.name)
        assertEquals(45, savedWorkout.durationMinutes)
        assertEquals(400, savedWorkout.caloriesBurned)
    }

    @Test
    fun `deleteWorkout removes workout from database`() = runTest {
        // Given
        val workout = createTestWorkout(userId = testUserId)
        val workoutId = workoutRepository.insertWorkout(workout)

        // Verify workout exists
        assertNotNull(database.workoutDao().getWorkoutById(workoutId))

        // When
        workoutRepository.deleteWorkout(workoutId)

        // Then
        assertNull(database.workoutDao().getWorkoutById(workoutId))
    }

    // endregion

    // region Query Operations Tests

    @Test
    fun `getWorkoutsByUserId returns user-specific workouts`() = runTest {
        // Given
        val workout1 = createTestWorkout(userId = testUserId, name = "Workout 1")
        val workout2 = createTestWorkout(userId = testUserId, name = "Workout 2")

        // Create workout for different user
        val anotherUser = User(
            email = "another@example.com",
            username = "anotheruser",
            passwordHash = "hash",
            passwordSalt = "salt",
        )
        val anotherUserId = database.userDao().insertUser(anotherUser)
        val workout3 = createTestWorkout(userId = anotherUserId, name = "Other User's Workout")

        workoutRepository.insertWorkout(workout1)
        workoutRepository.insertWorkout(workout2)
        workoutRepository.insertWorkout(workout3)

        // When
        val userWorkouts = workoutRepository.getWorkoutsByUserId(testUserId).first()

        // Then
        assertEquals(2, userWorkouts.size)
        assertTrue(userWorkouts.all { it.userId == testUserId })
        assertTrue(userWorkouts.any { it.name == "Workout 1" })
        assertTrue(userWorkouts.any { it.name == "Workout 2" })
        assertFalse(userWorkouts.any { it.name == "Other User's Workout" })
    }

    @Test
    fun `getWorkoutsByDateRange filters workouts correctly`() = runTest {
        // Given
        val today = Date()
        val yesterday = Date(today.time - 86400000L)
        val tomorrow = Date(today.time + 86400000L)
        val dayAfterTomorrow = Date(today.time + 2 * 86400000L)

        val workout1 = createTestWorkout(userId = testUserId, name = "Yesterday", date = yesterday)
        val workout2 = createTestWorkout(userId = testUserId, name = "Today", date = today)
        val workout3 = createTestWorkout(userId = testUserId, name = "Tomorrow", date = tomorrow)
        val workout4 = createTestWorkout(userId = testUserId, name = "Day After", date = dayAfterTomorrow)

        workoutRepository.insertWorkout(workout1)
        workoutRepository.insertWorkout(workout2)
        workoutRepository.insertWorkout(workout3)
        workoutRepository.insertWorkout(workout4)

        // When - get workouts from today to tomorrow (inclusive)
        val workoutsInRange = workoutRepository.getWorkoutsByDateRange(
            userId = testUserId,
            startDate = today,
            endDate = tomorrow,
        ).first()

        // Then
        assertEquals(2, workoutsInRange.size)
        assertTrue(workoutsInRange.any { it.name == "Today" })
        assertTrue(workoutsInRange.any { it.name == "Tomorrow" })
        assertFalse(workoutsInRange.any { it.name == "Yesterday" })
        assertFalse(workoutsInRange.any { it.name == "Day After" })
    }

    @Test
    fun `getWorkoutsByType filters by workout type`() = runTest {
        // Given
        val runningWorkout = createTestWorkout(userId = testUserId, name = "Run", type = WorkoutType.RUNNING)
        val cyclingWorkout = createTestWorkout(userId = testUserId, name = "Bike", type = WorkoutType.CYCLING)
        val strengthWorkout = createTestWorkout(userId = testUserId, name = "Lift", type = WorkoutType.STRENGTH)

        workoutRepository.insertWorkout(runningWorkout)
        workoutRepository.insertWorkout(cyclingWorkout)
        workoutRepository.insertWorkout(strengthWorkout)

        // When
        val runningWorkouts = workoutRepository.getWorkoutsByType(testUserId, WorkoutType.RUNNING).first()

        // Then
        assertEquals(1, runningWorkouts.size)
        assertEquals("Run", runningWorkouts[0].name)
        assertEquals(WorkoutType.RUNNING, runningWorkouts[0].type)
    }

    @Test
    fun `getRecentWorkouts returns limited recent workouts`() = runTest {
        // Given - create 5 workouts with different dates
        val baseTime = System.currentTimeMillis()
        for (i in 1..5) {
            val workout = createTestWorkout(
                userId = testUserId,
                name = "Workout $i",
                date = Date(baseTime - (i * 86400000L)), // i days ago
            )
            workoutRepository.insertWorkout(workout)
        }

        // When - get 3 most recent
        val recentWorkouts = workoutRepository.getRecentWorkouts(testUserId, 3).first()

        // Then
        assertEquals(3, recentWorkouts.size)
        // Should be ordered by date descending (most recent first)
        assertTrue(recentWorkouts[0].name == "Workout 1") // Most recent
        assertTrue(recentWorkouts[1].name == "Workout 2")
        assertTrue(recentWorkouts[2].name == "Workout 3")
    }

    // endregion

    // region Statistics Tests

    @Test
    fun `getTotalWorkoutCount returns correct count`() = runTest {
        // Given
        repeat(3) { i ->
            val workout = createTestWorkout(userId = testUserId, name = "Workout $i")
            workoutRepository.insertWorkout(workout)
        }

        // When
        val count = workoutRepository.getTotalWorkoutCount(testUserId)

        // Then
        assertEquals(3, count)
    }

    @Test
    fun `getTotalWorkoutDuration calculates total duration correctly`() = runTest {
        // Given
        val workout1 = createTestWorkout(userId = testUserId, duration = 30)
        val workout2 = createTestWorkout(userId = testUserId, duration = 45)
        val workout3 = createTestWorkout(userId = testUserId, duration = 25)

        workoutRepository.insertWorkout(workout1)
        workoutRepository.insertWorkout(workout2)
        workoutRepository.insertWorkout(workout3)

        // When
        val totalDuration = workoutRepository.getTotalWorkoutDuration(testUserId)

        // Then
        assertEquals(100, totalDuration) // 30 + 45 + 25
    }

    @Test
    fun `getAverageWorkoutDuration calculates average correctly`() = runTest {
        // Given
        val workout1 = createTestWorkout(userId = testUserId, duration = 20)
        val workout2 = createTestWorkout(userId = testUserId, duration = 40)
        val workout3 = createTestWorkout(userId = testUserId, duration = 30)

        workoutRepository.insertWorkout(workout1)
        workoutRepository.insertWorkout(workout2)
        workoutRepository.insertWorkout(workout3)

        // When
        val avgDuration = workoutRepository.getAverageWorkoutDuration(testUserId)

        // Then
        assertEquals(30.0f, avgDuration, 0.1f) // (20 + 40 + 30) / 3 = 30
    }

    @Test
    fun `getLongestWorkout returns workout with maximum duration`() = runTest {
        // Given
        val workout1 = createTestWorkout(userId = testUserId, name = "Short", duration = 15)
        val workout2 = createTestWorkout(userId = testUserId, name = "Longest", duration = 60)
        val workout3 = createTestWorkout(userId = testUserId, name = "Medium", duration = 30)

        workoutRepository.insertWorkout(workout1)
        workoutRepository.insertWorkout(workout2)
        workoutRepository.insertWorkout(workout3)

        // When
        val longestWorkout = workoutRepository.getLongestWorkout(testUserId)

        // Then
        assertNotNull(longestWorkout)
        assertEquals("Longest", longestWorkout!!.name)
        assertEquals(60, longestWorkout.durationMinutes)
    }

    @Test
    fun `getWorkoutStatsByType calculates type-specific statistics`() = runTest {
        // Given
        val runWorkout1 = createTestWorkout(userId = testUserId, type = WorkoutType.RUNNING, duration = 30, calories = 300, distance = 5.0)
        val runWorkout2 = createTestWorkout(userId = testUserId, type = WorkoutType.RUNNING, duration = 45, calories = 450, distance = 7.0)
        val cyclingWorkout = createTestWorkout(userId = testUserId, type = WorkoutType.CYCLING, duration = 60, calories = 400, distance = 20.0)

        workoutRepository.insertWorkout(runWorkout1)
        workoutRepository.insertWorkout(runWorkout2)
        workoutRepository.insertWorkout(cyclingWorkout)

        // When
        val runningStats = workoutRepository.getWorkoutStatsByType(testUserId, WorkoutType.RUNNING)

        // Then
        assertEquals(2, runningStats.workoutCount) // 2 running workouts
        assertEquals(75, runningStats.totalDuration) // 30 + 45
        assertEquals(750, runningStats.totalCalories) // 300 + 450
        assertEquals(12.0f, runningStats.totalDistance, 0.1f) // 5.0 + 7.0
    }

    // endregion

    // region Weekly Statistics Tests

    @Test
    fun `getWeeklyStats calculates current week statistics`() = runTest {
        // Given - Create workouts for this week
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val mondayThisWeek = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, 1)
        val tuesdayThisWeek = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, -8) // Previous Monday
        val previousWeek = calendar.time

        val thisWeekWorkout1 = createTestWorkout(userId = testUserId, date = mondayThisWeek, duration = 30, calories = 300, distance = 5.0)
        val thisWeekWorkout2 = createTestWorkout(userId = testUserId, date = tuesdayThisWeek, duration = 45, calories = 400, distance = 7.0)
        val previousWeekWorkout = createTestWorkout(userId = testUserId, date = previousWeek, duration = 60, calories = 500, distance = 10.0)

        workoutRepository.insertWorkout(thisWeekWorkout1)
        workoutRepository.insertWorkout(thisWeekWorkout2)
        workoutRepository.insertWorkout(previousWeekWorkout)

        // When
        val weeklyStats = workoutRepository.getWeeklyStats(testUserId)

        // Then
        assertEquals(2, weeklyStats.workoutCount) // Only this week's workouts
        assertEquals(75, weeklyStats.totalDuration) // 30 + 45
        assertEquals(700, weeklyStats.totalCalories) // 300 + 400
        assertEquals(12.0f, weeklyStats.totalDistance, 0.1f) // 5.0 + 7.0
    }

    @Test
    fun `statistics handle empty data gracefully`() = runTest {
        // When - no workouts exist
        val totalCount = workoutRepository.getTotalWorkoutCount(testUserId)
        val totalDuration = workoutRepository.getTotalWorkoutDuration(testUserId)
        val avgDuration = workoutRepository.getAverageWorkoutDuration(testUserId)
        val longestWorkout = workoutRepository.getLongestWorkout(testUserId)
        val weeklyStats = workoutRepository.getWeeklyStats(testUserId)

        // Then
        assertEquals(0, totalCount)
        assertEquals(0, totalDuration)
        assertEquals(0.0f, avgDuration)
        assertNull(longestWorkout)
        assertEquals(0, weeklyStats.workoutCount)
        assertEquals(0, weeklyStats.totalDuration)
        assertEquals(0, weeklyStats.totalCalories)
        assertEquals(0.0f, weeklyStats.totalDistance)
    }

    // endregion

    // region Edge Cases Tests

    @Test
    fun `operations handle non-existent user gracefully`() = runTest {
        // Given
        val nonExistentUserId = 999L

        // When
        val workouts = workoutRepository.getWorkoutsByUserId(nonExistentUserId).first()
        val count = workoutRepository.getTotalWorkoutCount(nonExistentUserId)
        val weeklyStats = workoutRepository.getWeeklyStats(nonExistentUserId)

        // Then
        assertTrue(workouts.isEmpty())
        assertEquals(0, count)
        assertEquals(0, weeklyStats.workoutCount)
    }

    @Test
    fun `date range queries handle same start and end date`() = runTest {
        // Given
        val specificDate = Date()
        val workout = createTestWorkout(userId = testUserId, date = specificDate)
        workoutRepository.insertWorkout(workout)

        // When - same start and end date (single day)
        val workouts = workoutRepository.getWorkoutsByDateRange(
            userId = testUserId,
            startDate = specificDate,
            endDate = specificDate,
        ).first()

        // Then
        assertEquals(1, workouts.size)
    }

    @Test
    fun `workout with null optional fields handled correctly`() = runTest {
        // Given
        val minimalWorkout = Workout(
            userId = testUserId,
            name = "Minimal Workout",
            type = WorkoutType.OTHER,
            date = Date(),
            durationMinutes = 30,
            caloriesBurned = null, // nullable field
            distanceKm = null, // nullable field
            notes = null, // nullable field
            createdAt = Date(),
            updatedAt = Date(),
        )

        // When
        val workoutId = workoutRepository.insertWorkout(minimalWorkout)

        // Then
        assertTrue(workoutId > 0)
        val savedWorkout = database.workoutDao().getWorkoutById(workoutId)
        assertNotNull(savedWorkout)
        assertEquals("Minimal Workout", savedWorkout!!.name)
        assertNull(savedWorkout.caloriesBurned)
        assertNull(savedWorkout.distanceKm)
        assertNull(savedWorkout.notes)
    }

    // endregion

    // Helper method to create test workouts
    private fun createTestWorkout(
        userId: Long,
        name: String = "Test Workout",
        type: WorkoutType = WorkoutType.OTHER,
        duration: Int = 30,
        calories: Int? = 250,
        distance: Double? = null,
        date: Date = Date(),
        notes: String? = null,
    ): Workout {
        return Workout(
            userId = userId,
            name = name,
            type = type,
            date = date,
            durationMinutes = duration,
            caloriesBurned = calories,
            distanceKm = distance,
            notes = notes,
            createdAt = Date(),
            updatedAt = Date(),
        )
    }
}
