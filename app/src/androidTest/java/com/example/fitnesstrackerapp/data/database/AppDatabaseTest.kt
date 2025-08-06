package com.example.fitnesstrackerapp.data.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.fitnesstrackerapp.util.test.TestDatabaseRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * AppDatabaseTest
 *
 * Responsibilities:
 * - Validates Room database operations for all entities and DAOs
 * - Ensures data integrity and correct CRUD operations
 */

@LargeTest
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    @get:Rule
    val databaseRule = TestDatabaseRule()

    @Test
    fun insertAndRetrieveUserProfile() = runTest {
        // Given
        val testProfile = TestDatabaseRule.createTestUserProfile()

        // When
        databaseRule.database.userProfileDao().insertUserProfile(testProfile)
        val retrieved = databaseRule.database.userProfileDao().getUserProfile(testProfile.userId).first()

        // Then
        assertNotNull(retrieved)
        assertEquals(testProfile.userId, retrieved?.userId)
        assertEquals(testProfile.email, retrieved?.email)
    }

    @Test
    fun insertAndRetrieveWorkout() = runTest {
        // Given
        val testWorkout = TestDatabaseRule.createTestWorkout()

        // When
        databaseRule.database.workoutDao().insertWorkout(testWorkout)
        val retrieved = databaseRule.database.workoutDao().getAllWorkouts(testWorkout.userId).first()

        // Then
        assertNotNull(retrieved)
        assertEquals(testWorkout.userId, retrieved.firstOrNull()?.userId)
        assertEquals(testWorkout.title, retrieved.firstOrNull()?.title)
    }

    @Test
    fun insertAndRetrieveMealEntry() = runTest {
        // Given
        val testMeal = TestDatabaseRule.createTestMealEntry()

        // When
        databaseRule.database.nutritionDao().insertMealEntry(testMeal)
        val retrieved = databaseRule.database.nutritionDao()
            .getMealEntriesForDateRange(
                testMeal.userId,
                testMeal.timestamp.minusDays(1),
                testMeal.timestamp.plusDays(1)
            ).first()

        // Then
        assertEquals(1, retrieved.size)
        assertEquals(testMeal.totalCalories, retrieved[0].totalCalories)
    }
}
