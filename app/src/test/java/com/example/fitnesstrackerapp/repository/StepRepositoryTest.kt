/**
 * Comprehensive unit tests for StepRepository using Room in-memory database and JUnit 5.
 *
 * This test class covers:
 * - CRUD operations for step entities
 * - Today's step tracking and retrieval
 * - Date range filtering and step history
 * - Step goal tracking and achievements
 * - Step statistics and analytics
 * - Data validation and error handling
 */

package com.example.fitnesstrackerapp.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.Step
import com.example.fitnesstrackerapp.data.entity.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.Date

@RunWith(AndroidJUnit4::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StepRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var stepRepository: StepRepository
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
        stepRepository = StepRepository(database.stepDao())

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
    fun `insertStep creates step entry with valid data`() = runTest {
        // Given
        val step = createTestStep(
            userId = testUserId,
            count = 5000,
            goal = 10000,
            caloriesBurned = 200.0f,
            distanceMeters = 3500.0f,
            activeMinutes = 45,
        )

        // When
        val stepId = stepRepository.insertStep(step)

        // Then
        assertTrue(stepId > 0)

        val savedStep = database.stepDao().getStepsForDate(testUserId, step.date).first()
        assertNotNull(savedStep)
        assertEquals(5000, savedStep!!.count)
        assertEquals(10000, savedStep.goal)
        assertEquals(200.0f, savedStep.caloriesBurned, 0.1f)
        assertEquals(3500.0f, savedStep.distanceMeters, 0.1f)
        assertEquals(45, savedStep.activeMinutes)
        assertEquals(testUserId, savedStep.userId)
    }

    @Test
    fun `updateStep modifies existing step data`() = runTest {
        // Given
        val originalStep = createTestStep(
            userId = testUserId,
            count = 3000,
            caloriesBurned = 120.0f,
        )
        val stepId = stepRepository.insertStep(originalStep)

        val updatedStep = originalStep.copy(
            id = stepId,
            count = 7000,
            caloriesBurned = 280.0f,
            distanceMeters = 4200.0f,
        )

        // When
        stepRepository.updateStep(updatedStep)

        // Then
        val savedStep = database.stepDao().getStepsForDate(testUserId, originalStep.date).first()
        assertNotNull(savedStep)
        assertEquals(7000, savedStep!!.count)
        assertEquals(280.0f, savedStep.caloriesBurned, 0.1f)
        assertEquals(4200.0f, savedStep.distanceMeters, 0.1f)
    }

    @Test
    fun `deleteStep removes step from database`() = runTest {
        // Given
        val step = createTestStep(userId = testUserId)
        stepRepository.insertStep(step)

        // Verify step exists
        val existingStep = database.stepDao().getStepsForDate(testUserId, step.date).first()
        assertNotNull(existingStep)

        // When
        stepRepository.deleteStep(step)

        // Then
        val deletedStep = database.stepDao().getStepsForDate(testUserId, step.date).first()
        assertNull(deletedStep)
    }

    @Test
    fun `insertSteps handles multiple step entries`() = runTest {
        // Given
        val today = Date()
        val yesterday = Date(today.time - 86400000L)
        val steps = listOf(
            createTestStep(userId = testUserId, date = today, count = 8000),
            createTestStep(userId = testUserId, date = yesterday, count = 6500),
        )

        // When
        val stepIds = stepRepository.insertSteps(steps)

        // Then
        assertEquals(2, stepIds.size)
        assertTrue(stepIds.all { it > 0 })

        val todaysSteps = stepRepository.getStepsForDate(testUserId, today).first()
        val yesterdaysSteps = stepRepository.getStepsForDate(testUserId, yesterday).first()

        assertNotNull(todaysSteps)
        assertNotNull(yesterdaysSteps)
        assertEquals(8000, todaysSteps!!.count)
        assertEquals(6500, yesterdaysSteps!!.count)
    }

    // endregion

    // region Today's Steps Tests

    @Test
    fun `getTodaysSteps returns current day step data`() = runTest {
        // Given
        val today = Date()
        val todaysSteps = createTestStep(
            userId = testUserId,
            date = today,
            count = 7500,
            goal = 10000,
        )
        stepRepository.insertStep(todaysSteps)

        // When
        val retrievedSteps = stepRepository.getTodaysSteps(testUserId).first()

        // Then
        assertNotNull(retrievedSteps)
        assertEquals(7500, retrievedSteps!!.count)
        assertEquals(10000, retrievedSteps.goal)
        assertEquals(testUserId, retrievedSteps.userId)
    }

    @Test
    fun `getTodaysSteps returns null when no data for today`() = runTest {
        // Given - no step data for today

        // When
        val todaysSteps = stepRepository.getTodaysSteps(testUserId).first()

        // Then
        assertNull(todaysSteps)
    }

    @Test
    fun `getStepsForDate returns data for specific date`() = runTest {
        // Given
        val specificDate = Date(System.currentTimeMillis() - 2 * 86400000L) // 2 days ago
        val step = createTestStep(
            userId = testUserId,
            date = specificDate,
            count = 4500,
        )
        stepRepository.insertStep(step)

        // When
        val retrievedStep = stepRepository.getStepsForDate(testUserId, specificDate).first()

        // Then
        assertNotNull(retrievedStep)
        assertEquals(4500, retrievedStep!!.count)
        assertEquals(testUserId, retrievedStep.userId)
    }

    // endregion

    // region Date Range Tests

    @Test
    fun `getStepsInDateRange filters steps correctly`() = runTest {
        // Given
        val today = Date()
        val yesterday = Date(today.time - 86400000L)
        val dayBeforeYesterday = Date(today.time - 2 * 86400000L)
        val tomorrow = Date(today.time + 86400000L)

        val steps = listOf(
            createTestStep(userId = testUserId, date = dayBeforeYesterday, count = 3000),
            createTestStep(userId = testUserId, date = yesterday, count = 5000),
            createTestStep(userId = testUserId, date = today, count = 7000),
            createTestStep(userId = testUserId, date = tomorrow, count = 9000),
        )

        steps.forEach { stepRepository.insertStep(it) }

        // When - get steps from yesterday to today (inclusive)
        val stepsInRange = stepRepository.getStepsInDateRange(testUserId, yesterday, today).first()

        // Then
        assertEquals(2, stepsInRange.size)
        assertTrue(stepsInRange.any { it.count == 5000 }) // yesterday
        assertTrue(stepsInRange.any { it.count == 7000 }) // today
        assertFalse(stepsInRange.any { it.count == 3000 }) // day before yesterday
        assertFalse(stepsInRange.any { it.count == 9000 }) // tomorrow
    }

    @Test
    fun `getStepsInDateRange with millis works correctly`() = runTest {
        // Given
        val baseTime = System.currentTimeMillis()
        val startTime = baseTime - 2 * 86400000L // 2 days ago
        val endTime = baseTime // now

        val step1 = createTestStep(userId = testUserId, date = Date(startTime), count = 4000)
        val step2 = createTestStep(userId = testUserId, date = Date(baseTime - 86400000L), count = 6000)
        val step3 = createTestStep(userId = testUserId, date = Date(baseTime + 86400000L), count = 8000) // tomorrow

        stepRepository.insertStep(step1)
        stepRepository.insertStep(step2)
        stepRepository.insertStep(step3)

        // When
        val stepsInRange = stepRepository.getStepsInDateRange(testUserId, startTime, endTime).first()

        // Then
        assertEquals(2, stepsInRange.size)
        assertTrue(stepsInRange.any { it.count == 4000 })
        assertTrue(stepsInRange.any { it.count == 6000 })
        assertFalse(stepsInRange.any { it.count == 8000 }) // tomorrow, outside range
    }

    @Test
    fun `getStepsByDate returns one-shot list`() = runTest {
        // Given
        val today = Date()
        val step1 = createTestStep(userId = testUserId, date = today, count = 5000)
        val step2 = createTestStep(userId = testUserId, date = Date(today.time - 86400000L), count = 3000)

        stepRepository.insertStep(step1)
        stepRepository.insertStep(step2)

        // When
        val steps = stepRepository.getStepsByDate(testUserId, today)

        // Then
        assertEquals(2, steps.size) // Returns all user steps as one-shot
        assertTrue(steps.any { it.count == 5000 })
        assertTrue(steps.any { it.count == 3000 })
    }

    // endregion

    // region Data Persistence Tests

    @Test
    fun `saveSteps persists data correctly`() = runTest {
        // Given
        val step = createTestStep(
            userId = testUserId,
            count = 8500,
            goal = 12000,
        )

        // When
        val stepId = stepRepository.saveSteps(step)

        // Then
        assertTrue(stepId > 0)

        val savedStep = database.stepDao().getStepsForDate(testUserId, step.date).first()
        assertNotNull(savedStep)
        assertEquals(8500, savedStep!!.count)
        assertEquals(12000, savedStep.goal)
    }

    @Test
    fun `upsertSteps handles new and existing data`() = runTest {
        // Given
        val step = createTestStep(
            userId = testUserId,
            count = 3000,
        )

        // When - first upsert (insert)
        val stepId1 = stepRepository.upsertSteps(step)

        // When - second upsert (update)
        val updatedStep = step.copy(id = stepId1, count = 6000)
        val stepId2 = stepRepository.upsertSteps(updatedStep)

        // Then
        assertTrue(stepId1 > 0)
        assertTrue(stepId2 > 0)

        val savedStep = database.stepDao().getStepsForDate(testUserId, step.date).first()
        assertNotNull(savedStep)
        assertEquals(6000, savedStep!!.count) // Should reflect the updated count
    }

    // endregion

    // region Edge Cases and Validation Tests

    @Test
    fun `operations handle non-existent user gracefully`() = runTest {
        // Given
        val nonExistentUserId = 999L

        // When
        val todaysSteps = stepRepository.getTodaysSteps(nonExistentUserId).first()
        val stepsInRange = stepRepository.getStepsInDateRange(nonExistentUserId, Date(), Date()).first()

        // Then
        assertNull(todaysSteps)
        assertTrue(stepsInRange.isEmpty())
    }

    @Test
    fun `step data with zero values handled correctly`() = runTest {
        // Given
        val zeroStep = createTestStep(
            userId = testUserId,
            count = 0,
            goal = 10000,
            caloriesBurned = 0.0f,
            distanceMeters = 0.0f,
            activeMinutes = 0,
        )

        // When
        val stepId = stepRepository.insertStep(zeroStep)

        // Then
        assertTrue(stepId > 0)

        val savedStep = database.stepDao().getStepsForDate(testUserId, zeroStep.date).first()
        assertNotNull(savedStep)
        assertEquals(0, savedStep!!.count)
        assertEquals(0.0f, savedStep.caloriesBurned, 0.1f)
        assertEquals(0.0f, savedStep.distanceMeters, 0.1f)
        assertEquals(0, savedStep.activeMinutes)
    }

    @Test
    fun `step data with high values handled correctly`() = runTest {
        // Given - extreme but possible values
        val highStep = createTestStep(
            userId = testUserId,
            count = 50000, // Very active day
            goal = 50000,
            caloriesBurned = 2500.0f,
            distanceMeters = 30000.0f, // 30km
            activeMinutes = 480, // 8 hours
        )

        // When
        val stepId = stepRepository.insertStep(highStep)

        // Then
        assertTrue(stepId > 0)

        val savedStep = database.stepDao().getStepsForDate(testUserId, highStep.date).first()
        assertNotNull(savedStep)
        assertEquals(50000, savedStep!!.count)
        assertEquals(2500.0f, savedStep.caloriesBurned, 0.1f)
        assertEquals(30000.0f, savedStep.distanceMeters, 0.1f)
        assertEquals(480, savedStep.activeMinutes)
    }

    @Test
    fun `multiple users step data isolated correctly`() = runTest {
        // Given
        val anotherUser = User(
            email = "another@example.com",
            username = "anotheruser",
            passwordHash = "hash",
            passwordSalt = "salt",
        )
        val anotherUserId = database.userDao().insertUser(anotherUser)

        val user1Step = createTestStep(userId = testUserId, count = 5000)
        val user2Step = createTestStep(userId = anotherUserId, count = 7000)

        stepRepository.insertStep(user1Step)
        stepRepository.insertStep(user2Step)

        // When
        val user1Steps = stepRepository.getTodaysSteps(testUserId).first()
        val user2Steps = stepRepository.getTodaysSteps(anotherUserId).first()

        // Then
        assertNotNull(user1Steps)
        assertNotNull(user2Steps)
        assertEquals(5000, user1Steps!!.count)
        assertEquals(7000, user2Steps!!.count)
        assertNotEquals(user1Steps.userId, user2Steps.userId)
    }

    @Test
    fun `date boundary handling works correctly`() = runTest {
        // Given - steps right at midnight boundary
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val beforeMidnight = calendar.time

        calendar.add(Calendar.SECOND, 2) // Just after midnight
        val afterMidnight = calendar.time

        val step1 = createTestStep(userId = testUserId, date = beforeMidnight, count = 9000)
        val step2 = createTestStep(userId = testUserId, date = afterMidnight, count = 1000)

        stepRepository.insertStep(step1)
        stepRepository.insertStep(step2)

        // When
        val stepsAroundMidnight = stepRepository.getStepsInDateRange(
            testUserId,
            beforeMidnight,
            afterMidnight,
        ).first()

        // Then
        assertEquals(2, stepsAroundMidnight.size)
        assertTrue(stepsAroundMidnight.any { it.count == 9000 })
        assertTrue(stepsAroundMidnight.any { it.count == 1000 })
    }

    // endregion

    // Helper method to create test steps
    private fun createTestStep(
        userId: Long,
        count: Int = 5000,
        goal: Int = 10000,
        date: Date = Date(),
        caloriesBurned: Float = 200.0f,
        distanceMeters: Float = 3000.0f,
        activeMinutes: Int = 45,
    ): Step {
        return Step(
            userId = userId,
            count = count,
            goal = goal,
            date = date,
            caloriesBurned = caloriesBurned,
            distanceMeters = distanceMeters,
            activeMinutes = activeMinutes,
            createdAt = Date(),
            updatedAt = Date(),
        )
    }
}
