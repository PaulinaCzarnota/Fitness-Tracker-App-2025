package com.example.fitnesstrackerapp.ui.viewmodel

/**
 * Unit tests for NutritionViewModel
 *
 * Tests cover:
 * - Food entry creation and management
 * - Daily nutrition tracking
 * - UI state management
 * - Error handling
 */

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.MealType
import com.example.fitnesstrackerapp.repository.SimpleNutritionRepository
import com.example.fitnesstrackerapp.ui.nutrition.NutritionViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.Date

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class NutritionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: SimpleNutritionRepository
    private lateinit var viewModel: NutritionViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val testUserId = 1L

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        
        // Mock repository to return empty list by default
        coEvery { repository.getFoodEntriesForDate(any(), any()) } returns flowOf(emptyList())
        
        viewModel = NutritionViewModel(repository, testUserId)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addFoodEntry should call repository and reload data`() = testScope.runTest {
        // Given
        val foodName = "Apple"
        val calories = 95
        val mealType = MealType.SNACK

        // When
        viewModel.addFoodEntry(foodName, calories.toDouble(), mealType)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.addFoodEntry(any()) }
        coVerify { repository.getFoodEntriesForDate(testUserId, any()) }
    }

    @Test
    fun `addFoodEntry with detailed nutrition should create proper entry`() = testScope.runTest {
        // Given
        val foodName = "Chicken Breast"
        val calories = 165
        val protein = 31.0
        val carbs = 0.0
        val fat = 3.6
        val fiber = 0.0
        val mealType = MealType.LUNCH
        val servingSize = "100g"
        val quantity = 1.5f

        // When
        viewModel.addFoodEntry(foodName, calories, protein, carbs, fat, fiber, mealType, servingSize, quantity)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.addFoodEntry(any()) }
    }

    @Test
    fun `deleteFoodEntry should call repository and reload data`() = testScope.runTest {
        // Given
        val foodEntry = FoodEntry(
            id = 1,
            userId = testUserId,
            foodName = "Test Food",
            servingSize = 1.0,
            servingUnit = "serving",
            caloriesPerServing = 100.0,
            mealType = MealType.BREAKFAST,
            dateConsumed = Date()
        )

        // When
        viewModel.deleteFoodEntry(foodEntry)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.deleteFoodEntry(foodEntry) }
        coVerify { repository.getFoodEntriesForDate(testUserId, any()) }
    }

    @Test
    fun `seedDefaultData should add sample entries`() = testScope.runTest {
        // When
        viewModel.seedDefaultData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 5) { repository.addFoodEntry(any()) }
        coVerify { repository.getFoodEntriesForDate(testUserId, any()) }
    }

    @Test
    fun `clearError should reset error state`() = testScope.runTest {
        // When
        viewModel.clearError()

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(null, uiState.error)
    }

    @Test
    fun `initial state should have loading true`() {
        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.isLoading)
        assertEquals(emptyList<FoodEntry>(), uiState.foodEntries)
        assertEquals(0.0, uiState.totalCalories, 0.01)
    }

    @Test
    fun `food entries should be loaded on initialization`() = testScope.runTest {
        // Given - setup already calls the constructor
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.getFoodEntriesForDate(testUserId, any()) }
    }
}
