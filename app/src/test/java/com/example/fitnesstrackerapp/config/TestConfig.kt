package com.example.fitnesstrackerapp.config

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.MealType
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import com.example.fitnesstrackerapp.repository.AuthRepository
import com.example.fitnesstrackerapp.repository.GoalRepository
import com.example.fitnesstrackerapp.repository.SimpleNutritionRepository
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import com.example.fitnesstrackerapp.security.CryptoManager
import com.example.fitnesstrackerapp.util.test.TestHelper
import io.mockk.mockk

/**
 * Test configuration for dependency injection during testing.
 *
 * Provides test-specific implementations and mock objects for:
 * - In-memory database for DAO testing
 * - Mock repositories for ViewModel testing
 * - Test security managers
 * - Mock network components
 * - Test-specific utilities and helpers
 */
object TestConfig {

    /**
     * Provides in-memory database for testing.
     * This database is destroyed after each test method.
     */
    fun provideTestDatabase(): AppDatabase {
        return Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
    }

    /**
     * Provides mock AuthRepository for testing.
     * Default behavior can be overridden in individual tests.
     */
    fun provideTestAuthRepository(): AuthRepository {
        return TestHelper.createMockAuthRepository()
    }

    /**
     * Provides mock GoalRepository for testing.
     */
    fun provideTestGoalRepository(): GoalRepository {
        return mockk(relaxed = true)
    }

    /**
     * Provides mock WorkoutRepository for testing.
     */
    fun provideTestWorkoutRepository(): WorkoutRepository {
        return mockk(relaxed = true)
    }

    /**
     * Provides mock SimpleNutritionRepository for testing.
     */
    fun provideTestNutritionRepository(): SimpleNutritionRepository {
        return mockk(relaxed = true)
    }

    /**
     * Provides test CryptoManager with predictable behavior.
     */
    fun provideTestCryptoManager(): CryptoManager {
        return mockk(relaxed = true) {
            // Add any specific mock behavior needed for tests
        }
    }
}

/**
 * Base test class for unit tests with common setup.
 */
abstract class BaseUnitTest {

    /**
     * Common setup for unit tests.
     * Override this method in test classes for specific setup.
     */
    open fun baseSetup() {
        // Common test setup logic
    }

    /**
     * Common cleanup for unit tests.
     * Override this method in test classes for specific cleanup.
     */
    open fun baseCleanup() {
        // Common test cleanup logic
    }
}

/**
 * Base test class for integration tests with database.
 */
abstract class BaseIntegrationTest {

    protected lateinit var database: AppDatabase

    /**
     * Setup in-memory database for integration tests.
     */
    open fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
    }

    /**
     * Cleanup database after tests.
     */
    open fun cleanupDatabase() {
        if (::database.isInitialized) {
            database.close()
        }
    }
}

/**
 * Test data factory for creating consistent test objects.
 */
object TestDataFactory {

    /**
     * Creates a set of test users for bulk testing scenarios.
     */
    fun createTestUsers(count: Int = 5) = TestHelper.createTestUsers(count)

    /**
     * Creates test goals with different statuses for testing.
     */
    fun createMixedGoals(userId: Long, count: Int = 10) = (1..count).map { index ->
        TestHelper.createTestGoal(
            userId = userId,
            title = "Test Goal $index",
        ).copy(
            status = when (index % 3) {
                0 -> com.example.fitnesstrackerapp.data.entity.GoalStatus.ACTIVE
                1 -> com.example.fitnesstrackerapp.data.entity.GoalStatus.COMPLETED
                else -> com.example.fitnesstrackerapp.data.entity.GoalStatus.PAUSED
            },
        )
    }

    /**
     * Creates test workouts with different types and dates.
     */
    fun createMixedWorkouts(userId: Long, count: Int = 10) = (1..count).map { index ->
        TestHelper.createTestWorkout(
            userId = userId,
            title = "Test Workout $index",
            workoutType = WorkoutType.entries[index % WorkoutType.entries.size],
        )
    }

    /**
     * Creates test food entries for different meal types.
     */
    fun createMixedFoodEntries(userId: Long, count: Int = 12) = (1..count).map { index ->
        TestHelper.createTestFoodEntry(
            userId = userId,
            foodName = "Test Food $index",
            mealType = MealType.entries[index % MealType.entries.size],
        )
    }
}

/**
 * Assertion helpers for common test scenarios.
 */
object TestAssertions {

    /**
     * Asserts that a list contains items with specific properties.
     */
    fun <T> assertListContainsItemsWithProperty(
        list: List<T>,
        property: T.() -> Any,
        expectedValues: Set<Any>,
    ) {
        val actualValues = list.map(property).toSet()
        assert(actualValues == expectedValues) {
            "Expected values $expectedValues but got $actualValues"
        }
    }

    /**
     * Asserts that a list is sorted by a specific property.
     */
    fun <T, R : Comparable<R>> assertListSortedBy(
        list: List<T>,
        selector: T.() -> R,
        ascending: Boolean = true,
    ) {
        val values = list.map(selector)
        val sortedValues = if (ascending) values.sorted() else values.sortedDescending()
        assert(values == sortedValues) {
            "List is not sorted ${if (ascending) "ascending" else "descending"}"
        }
    }

    /**
     * Asserts that a date is within a specific range.
     */
    fun assertDateWithinRange(
        actual: java.util.Date,
        start: java.util.Date,
        end: java.util.Date,
    ) {
        assert(actual.time >= start.time && actual.time <= end.time) {
            "Date $actual is not within range [$start, $end]"
        }
    }
}

/**
 * Test utilities for common operations.
 */
object TestUtils {

    /**
     * Executes a block with a timeout.
     */
    suspend fun <T> withTimeout(timeoutMs: Long = 5000, block: suspend () -> T): T {
        return kotlinx.coroutines.withTimeout(timeoutMs) { block() }
    }

    /**
     * Creates a date relative to now.
     */
    fun createRelativeDate(offsetDays: Int): java.util.Date {
        return TestHelper.createRelativeDate(offsetDays)
    }

    /**
     * Waits for a condition to be true with timeout.
     */
    suspend fun waitForCondition(
        timeoutMs: Long = 5000,
        intervalMs: Long = 100,
        condition: suspend () -> Boolean,
    ) {
        val startTime = System.currentTimeMillis()
        while (!condition() && (System.currentTimeMillis() - startTime) < timeoutMs) {
            kotlinx.coroutines.delay(intervalMs)
        }

        if (!condition()) {
            throw AssertionError("Condition was not met within $timeoutMs ms")
        }
    }
}
