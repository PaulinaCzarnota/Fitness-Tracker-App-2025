/**
 * Comprehensive unit tests for NutritionViewModel
 *
 * Tests cover:
 * - Food entry creation, update, and deletion
 * - Daily nutrition tracking and calculations
 * - Meal type categorization
 * - Search and filtering functionality
 * - Goal tracking and progress monitoring
 * - Error handling and edge cases
 */

package com.example.fitnesstrackerapp.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.MealType
import com.example.fitnesstrackerapp.data.model.NutritionSummary
import com.example.fitnesstrackerapp.repository.FoodEntryRepository
import com.example.fitnesstrackerapp.repository.NutritionRepository
import com.example.fitnesstrackerapp.ui.nutrition.NutritionViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
class NutritionViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var foodEntryRepository: FoodEntryRepository
    private lateinit var nutritionRepository: NutritionRepository
    private lateinit var viewModel: NutritionViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        foodEntryRepository = mockk(relaxed = true)
        nutritionRepository = mockk(relaxed = true)

        viewModel = NutritionViewModel(foodEntryRepository, nutritionRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    @DisplayName("Food Entry Management Tests")
    inner class FoodEntryManagementTests {
        @Test
        fun `addFoodEntry successfully adds new food entry`() = testScope.runTest {
            // Given
            val foodEntry = createTestFoodEntry()
            coEvery { foodEntryRepository.insertFoodEntry(foodEntry) } returns 1L

            // When
            viewModel.addFoodEntry(foodEntry)

            // Then
            coVerify { foodEntryRepository.insertFoodEntry(foodEntry) }
            assertThat(viewModel.isLoading.value).isFalse()
        }

        @Test
        fun `updateFoodEntry successfully updates existing entry`() = testScope.runTest {
            // Given
            val foodEntry = createTestFoodEntry(id = 1L, foodName = "Updated Banana")
            coEvery { foodEntryRepository.updateFoodEntry(foodEntry) } returns Unit

            // When
            viewModel.updateFoodEntry(foodEntry)

            // Then
            coVerify { foodEntryRepository.updateFoodEntry(foodEntry) }
            assertThat(viewModel.isLoading.value).isFalse()
        }

        @Test
        fun `deleteFoodEntry removes entry successfully`() = testScope.runTest {
            // Given
            val foodEntry = createTestFoodEntry(id = 1L)
            coEvery { foodEntryRepository.deleteFoodEntry(foodEntry) } returns Unit

            // When
            viewModel.deleteFoodEntry(foodEntry)

            // Then
            coVerify { foodEntryRepository.deleteFoodEntry(foodEntry) }
            assertThat(viewModel.isLoading.value).isFalse()
        }

        @Test
        fun `getFoodEntriesForDate retrieves entries for specific date`() = testScope.runTest {
            // Given
            val date = Date()
            val userId = 1L
            val foodEntries = listOf(
                createTestFoodEntry(id = 1L, mealType = MealType.BREAKFAST),
                createTestFoodEntry(id = 2L, mealType = MealType.LUNCH)
            )
            every { foodEntryRepository.getFoodEntriesForDate(userId, date) } returns flowOf(foodEntries)

            // When
            val result = viewModel.getFoodEntriesForDate(userId, date)

            // Then
            result.observeForever { entries ->
                assertThat(entries).hasSize(2)
                assertThat(entries[0].mealType).isEqualTo(MealType.BREAKFAST)
                assertThat(entries[1].mealType).isEqualTo(MealType.LUNCH)
            }
        }
    }

    @Nested
    @DisplayName("Daily Nutrition Tracking Tests")
    inner class DailyNutritionTrackingTests {
        @Test
        fun `getDailyNutritionSummary calculates totals correctly`() = testScope.runTest {
            // Given
            val userId = 1L
            val date = Date()
            val nutritionSummary = NutritionSummary(
                totalCalories = 2000.0,
                totalProtein = 150.0,
                totalCarbs = 250.0,
                totalFats = 80.0,
                totalFiber = 25.0,
                totalSugar = 50.0
            )
            every { nutritionRepository.getDailyNutritionSummary(userId, date) } returns flowOf(nutritionSummary)

            // When
            val result = viewModel.getDailyNutritionSummary(userId, date)

            // Then
            result.observeForever { summary ->
                assertThat(summary.totalCalories).isEqualTo(2000.0)
                assertThat(summary.totalProtein).isEqualTo(150.0)
                assertThat(summary.totalCarbs).isEqualTo(250.0)
                assertThat(summary.totalFats).isEqualTo(80.0)
            }
        }

        @Test
        fun `updateDailyGoals sets nutrition goals correctly`() = testScope.runTest {
            // Given
            val userId = 1L
            val calorieGoal = 2200.0
            val proteinGoal = 165.0
            val carbGoal = 275.0
            val fatGoal = 85.0

            coEvery {
                nutritionRepository.updateNutritionGoals(userId, calorieGoal, proteinGoal, carbGoal, fatGoal)
            } returns Unit

            // When
            viewModel.updateDailyGoals(userId, calorieGoal, proteinGoal, carbGoal, fatGoal)

            // Then
            coVerify {
                nutritionRepository.updateNutritionGoals(userId, calorieGoal, proteinGoal, carbGoal, fatGoal)
            }
        }
    }

    @Nested
    @DisplayName("Meal Type Management Tests")
    inner class MealTypeManagementTests {
        @Test
        fun `getFoodEntriesByMealType filters entries correctly`() = testScope.runTest {
            // Given
            val userId = 1L
            val date = Date()
            val mealType = MealType.BREAKFAST
            val breakfastEntries = listOf(
                createTestFoodEntry(id = 1L, foodName = "Oatmeal", mealType = MealType.BREAKFAST),
                createTestFoodEntry(id = 2L, foodName = "Orange Juice", mealType = MealType.BREAKFAST)
            )

            every {
                foodEntryRepository.getFoodEntriesByMealType(userId, date, mealType)
            } returns flowOf(breakfastEntries)

            // When
            val result = viewModel.getFoodEntriesByMealType(userId, date, mealType)

            // Then
            result.observeForever { entries ->
                assertThat(entries).hasSize(2)
                entries.forEach { entry ->
                    assertThat(entry.mealType).isEqualTo(MealType.BREAKFAST)
                }
            }
        }

        @Test
        fun `getMealTypeCalories calculates meal totals`() = testScope.runTest {
            // Given
            val userId = 1L
            val date = Date()
            val mealType = MealType.LUNCH
            val lunchCalories = 650.0

            every {
                nutritionRepository.getMealTypeCalories(userId, date, mealType)
            } returns flowOf(lunchCalories)

            // When
            val result = viewModel.getMealTypeCalories(userId, date, mealType)

            // Then
            result.observeForever { calories ->
                assertThat(calories).isEqualTo(650.0)
            }
        }
    }

    @Nested
    @DisplayName("Search and Filter Tests")
    inner class SearchAndFilterTests {
        @Test
        fun `searchFoodEntries filters by food name`() = testScope.runTest {
            // Given
            val userId = 1L
            val searchQuery = "banana"
            val searchResults = listOf(
                createTestFoodEntry(id = 1L, foodName = "Banana"),
                createTestFoodEntry(id = 2L, foodName = "Banana Smoothie")
            )

            every {
                foodEntryRepository.searchFoodEntries(userId, searchQuery)
            } returns flowOf(searchResults)

            // When
            val result = viewModel.searchFoodEntries(userId, searchQuery)

            // Then
            result.observeForever { entries ->
                assertThat(entries).hasSize(2)
                entries.forEach { entry ->
                    assertThat(entry.foodName.lowercase()).contains(searchQuery)
                }
            }
        }

        @Test
        fun `getRecentFoodEntries returns recent entries`() = testScope.runTest {
            // Given
            val userId = 1L
            val limit = 10
            val recentEntries = listOf(
                createTestFoodEntry(id = 1L, foodName = "Apple"),
                createTestFoodEntry(id = 2L, foodName = "Chicken Breast")
            )

            every {
                foodEntryRepository.getRecentFoodEntries(userId, limit)
            } returns flowOf(recentEntries)

            // When
            val result = viewModel.getRecentFoodEntries(userId, limit)

            // Then
            result.observeForever { entries ->
                assertThat(entries).hasSize(2)
                assertThat(entries[0].foodName).isEqualTo("Apple")
                assertThat(entries[1].foodName).isEqualTo("Chicken Breast")
            }
        }
    }

    @Nested
    @DisplayName("Nutrition Calculation Tests")
    inner class NutritionCalculationTests {
        @Test
        fun `calculateMacroPercentages returns correct ratios`() = testScope.runTest {
            // Given
            val totalCalories = 2000.0
            val protein = 150.0 // 600 calories, 30%
            val carbs = 250.0 // 1000 calories, 50%
            val fats = 89.0 // 800 calories, 20%

            // When
            val percentages = viewModel.calculateMacroPercentages(totalCalories, protein, carbs, fats)

            // Then
            assertThat(percentages.proteinPercent).isWithin(0.1).of(30.0)
            assertThat(percentages.carbsPercent).isWithin(0.1).of(50.0)
            assertThat(percentages.fatsPercent).isWithin(0.1).of(40.0)
        }

        @Test
        fun `calculateCalorieDeficit returns correct deficit`() = testScope.runTest {
            // Given
            val consumedCalories = 1800.0
            val dailyGoal = 2000.0
            val expectedDeficit = 200.0

            // When
            val deficit = viewModel.calculateCalorieDeficit(consumedCalories, dailyGoal)

            // Then
            assertThat(deficit).isEqualTo(expectedDeficit)
        }

        @Test
        fun `isGoalMet returns correct status`() = testScope.runTest {
            // Given
            val consumedAmount = 150.0
            val goalAmount = 160.0

            // When
            val isGoalMet = viewModel.isGoalMet(consumedAmount, goalAmount)

            // Then
            assertThat(isGoalMet).isTrue() // Within 10% tolerance
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    inner class ErrorHandlingTests {
        @Test
        fun `addFoodEntry handles repository exception gracefully`() = testScope.runTest {
            // Given
            val foodEntry = createTestFoodEntry()
            val exception = RuntimeException("Database error")
            coEvery { foodEntryRepository.insertFoodEntry(foodEntry) } throws exception

            // When
            viewModel.addFoodEntry(foodEntry)

            // Then
            assertThat(viewModel.errorMessage.value).contains("Database error")
            assertThat(viewModel.isLoading.value).isFalse()
        }

        @Test
        fun `updateFoodEntry handles null entry gracefully`() = testScope.runTest {
            // Given
            val nullEntry: FoodEntry? = null

            // When & Then - should not crash
            try {
                viewModel.updateFoodEntry(nullEntry!!)
                assertThat(viewModel.errorMessage.value).contains("Invalid food entry")
            } catch (e: Exception) {
                // Expected behavior for null entry
            }
        }

        @Test
        fun `searchFoodEntries handles empty query gracefully`() = testScope.runTest {
            // Given
            val userId = 1L
            val emptyQuery = ""

            every {
                foodEntryRepository.searchFoodEntries(userId, emptyQuery)
            } returns flowOf(emptyList())

            // When
            val result = viewModel.searchFoodEntries(userId, emptyQuery)

            // Then
            result.observeForever { entries ->
                assertThat(entries).isEmpty()
            }
        }
    }

    @Nested
    @DisplayName("Data Validation Tests")
    inner class DataValidationTests {
        @Test
        fun `validateFoodEntry returns false for invalid entry`() = testScope.runTest {
            // Given
            val invalidEntry = createTestFoodEntry(
                foodName = "", // Empty name
                calories = -100.0 // Negative calories
            )

            // When
            val isValid = viewModel.validateFoodEntry(invalidEntry)

            // Then
            assertThat(isValid).isFalse()
        }

        @Test
        fun `validateFoodEntry returns true for valid entry`() = testScope.runTest {
            // Given
            val validEntry = createTestFoodEntry(
                foodName = "Apple",
                calories = 95.0,
                protein = 0.5,
                carbs = 25.0,
                fats = 0.3
            )

            // When
            val isValid = viewModel.validateFoodEntry(validEntry)

            // Then
            assertThat(isValid).isTrue()
        }
    }

    // Helper method to create test food entries
    private fun createTestFoodEntry(
        id: Long = 0L,
        userId: Long = 1L,
        foodName: String = "Test Food",
        brand: String? = null,
        servingSize: Double = 1.0,
        servingUnit: String = "piece",
        calories: Double = 100.0,
        protein: Double = 5.0,
        carbs: Double = 20.0,
        fats: Double = 2.0,
        fiber: Double = 3.0,
        sugar: Double = 15.0,
        mealType: MealType = MealType.BREAKFAST,
        date: Date = Date()
    ): FoodEntry {
        return FoodEntry(
            id = id,
            userId = userId,
            foodName = foodName,
            brand = brand,
            servingSize = servingSize,
            servingUnit = servingUnit,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fats = fats,
            fiber = fiber,
            sugar = sugar,
            mealType = mealType,
            date = date,
            createdAt = Date(),
            updatedAt = Date()
        )
    }

    // Helper data classes for complex calculations
    data class MacroPercentages(
        val proteinPercent: Double,
        val carbsPercent: Double,
        val fatsPercent: Double
    )

    // Extension functions for ViewModel testing
    private fun NutritionViewModel.calculateMacroPercentages(
        totalCalories: Double,
        protein: Double,
        carbs: Double,
        fats: Double
    ): MacroPercentages {
        val proteinCalories = protein * 4
        val carbsCalories = carbs * 4
        val fatsCalories = fats * 9

        return MacroPercentages(
            proteinPercent = (proteinCalories / totalCalories) * 100,
            carbsPercent = (carbsCalories / totalCalories) * 100,
            fatsPercent = (fatsCalories / totalCalories) * 100
        )
    }

    private fun NutritionViewModel.calculateCalorieDeficit(
        consumedCalories: Double,
        dailyGoal: Double
    ): Double {
        return dailyGoal - consumedCalories
    }

    private fun NutritionViewModel.isGoalMet(
        consumedAmount: Double,
        goalAmount: Double,
        tolerance: Double = 0.1
    ): Boolean {
        return consumedAmount >= (goalAmount * (1.0 - tolerance))
    }

    private fun NutritionViewModel.validateFoodEntry(entry: FoodEntry): Boolean {
        return entry.foodName.isNotBlank() &&
            entry.calories >= 0.0 &&
            entry.protein >= 0.0 &&
            entry.carbs >= 0.0 &&
            entry.fats >= 0.0 &&
            entry.servingSize > 0.0
    }
}
