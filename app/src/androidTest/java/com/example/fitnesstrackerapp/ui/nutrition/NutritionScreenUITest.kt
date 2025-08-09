/**
 * Comprehensive Espresso UI tests for NutritionScreen
 *
 * Tests cover:
 * - Food entry creation and management
 * - Meal type navigation and filtering
 * - Search functionality
 * - Daily nutrition summary display
 * - Goal tracking and progress indicators
 * - Error handling in UI
 * - Accessibility compliance
 */

package com.example.fitnesstrackerapp.ui.nutrition

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.MealType
import com.example.fitnesstrackerapp.data.model.NutritionSummary
import com.example.fitnesstrackerapp.fake.FakeServiceLocator
import com.example.fitnesstrackerapp.ui.theme.FitnessTrackerAppTheme
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class NutritionScreenUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeServiceLocator: FakeServiceLocator

    @Before
    fun setUp() {
        fakeServiceLocator = FakeServiceLocator()
    }

    @Test
    fun nutritionScreen_displaysCorrectInitialState() {
        // Given
        val userId = 1L

        // When
        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Nutrition Tracking").assertExists()
        composeTestRule.onNodeWithText("Today's Summary").assertExists()
        composeTestRule.onNodeWithText("Breakfast").assertExists()
        composeTestRule.onNodeWithText("Lunch").assertExists()
        composeTestRule.onNodeWithText("Dinner").assertExists()
        composeTestRule.onNodeWithText("Snacks").assertExists()
    }

    @Test
    fun nutritionScreen_addFoodEntry_showsDialog() {
        // Given
        val userId = 1L

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("Add food entry").performClick()

        // Then
        composeTestRule.onNodeWithText("Add Food Entry").assertExists()
        composeTestRule.onNodeWithText("Food Name").assertExists()
        composeTestRule.onNodeWithText("Calories").assertExists()
        composeTestRule.onNodeWithText("Protein (g)").assertExists()
        composeTestRule.onNodeWithText("Carbs (g)").assertExists()
        composeTestRule.onNodeWithText("Fat (g)").assertExists()
    }

    @Test
    fun addFoodEntryDialog_validInput_createsFoodEntry() = runTest {
        // Given
        val userId = 1L

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("Add food entry").performClick()

        // Fill in food entry form
        composeTestRule.onNodeWithText("Food Name").performTextInput("Apple")
        composeTestRule.onNodeWithText("Calories").performTextInput("95")
        composeTestRule.onNodeWithText("Protein (g)").performTextInput("0.5")
        composeTestRule.onNodeWithText("Carbs (g)").performTextInput("25")
        composeTestRule.onNodeWithText("Fat (g)").performTextInput("0.3")

        // Select meal type
        composeTestRule.onNodeWithText("Breakfast").performClick()

        // Save entry
        composeTestRule.onNodeWithText("Save").performClick()

        // Then
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Apple").assertExists()
        composeTestRule.onNodeWithText("95 cal").assertExists()
    }

    @Test
    fun addFoodEntryDialog_invalidInput_showsValidationErrors() {
        // Given
        val userId = 1L

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("Add food entry").performClick()

        // Try to save without filling required fields
        composeTestRule.onNodeWithText("Save").performClick()

        // Then
        composeTestRule.onNodeWithText("Food name is required").assertExists()
        composeTestRule.onNodeWithText("Calories must be a positive number").assertExists()
    }

    @Test
    fun mealTypeFilter_clickingBreakfast_showsOnlyBreakfastItems() = runTest {
        // Given
        val userId = 1L
        val testFoodEntries = listOf(
            createTestFoodEntry(
                foodName = "Oatmeal",
                mealType = MealType.BREAKFAST,
                calories = 300.0
            ),
            createTestFoodEntry(
                foodName = "Sandwich",
                mealType = MealType.LUNCH,
                calories = 450.0
            )
        )

        // Setup fake data
        fakeServiceLocator.foodEntryRepository.addTestData(testFoodEntries)

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // When
        composeTestRule.onNodeWithText("Breakfast").performClick()

        // Then
        composeTestRule.onNodeWithText("Oatmeal").assertExists()
        composeTestRule.onNodeWithText("Sandwich").assertDoesNotExist()
        composeTestRule.onNodeWithText("300 cal").assertExists()
    }

    @Test
    fun searchFunctionality_searchingForFood_filtersResults() = runTest {
        // Given
        val userId = 1L
        val testFoodEntries = listOf(
            createTestFoodEntry(foodName = "Apple", calories = 95.0),
            createTestFoodEntry(foodName = "Apple Pie", calories = 320.0),
            createTestFoodEntry(foodName = "Banana", calories = 105.0),
            createTestFoodEntry(foodName = "Orange", calories = 62.0)
        )

        fakeServiceLocator.foodEntryRepository.addTestData(testFoodEntries)

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("Search foods").performClick()
        composeTestRule.onNodeWithText("Search...").performTextInput("apple")

        // Then
        composeTestRule.onNodeWithText("Apple").assertExists()
        composeTestRule.onNodeWithText("Apple Pie").assertExists()
        composeTestRule.onNodeWithText("Banana").assertDoesNotExist()
        composeTestRule.onNodeWithText("Orange").assertDoesNotExist()
    }

    @Test
    fun dailyNutritionSummary_displaysCorrectValues() = runTest {
        // Given
        val userId = 1L
        val nutritionSummary = NutritionSummary(
            totalCalories = 1850.0,
            totalProtein = 125.0,
            totalCarbs = 230.0,
            totalFats = 65.0,
            totalFiber = 28.0,
            totalSugar = 85.0
        )

        fakeServiceLocator.nutritionRepository.setNutritionSummary(nutritionSummary)

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // Then
        composeTestRule.onNodeWithText("1,850").assertExists() // Calories
        composeTestRule.onNodeWithText("125g").assertExists() // Protein
        composeTestRule.onNodeWithText("230g").assertExists() // Carbs
        composeTestRule.onNodeWithText("65g").assertExists() // Fats
        composeTestRule.onNodeWithText("28g").assertExists() // Fiber
    }

    @Test
    fun goalProgressIndicators_showCorrectProgress() = runTest {
        // Given
        val userId = 1L
        val nutritionSummary = NutritionSummary(
            totalCalories = 1600.0, // 80% of 2000 goal
            totalProtein = 120.0, // 80% of 150 goal
            totalCarbs = 200.0, // 80% of 250 goal
            totalFats = 56.0, // 80% of 70 goal
            totalFiber = 20.0, // 80% of 25 goal
            totalSugar = 40.0
        )

        fakeServiceLocator.nutritionRepository.setNutritionSummary(nutritionSummary)

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // Then - Check progress indicators (80% progress)
        composeTestRule.onNodeWithText("80%").assertExists() // Calories progress
        composeTestRule.onNodeWithContentDescription("Calories progress: 80%").assertExists()
        composeTestRule.onNodeWithContentDescription("Protein progress: 80%").assertExists()
        composeTestRule.onNodeWithContentDescription("Carbs progress: 80%").assertExists()
    }

    @Test
    fun editFoodEntry_longClickOnEntry_showsEditDialog() = runTest {
        // Given
        val userId = 1L
        val testFoodEntry = createTestFoodEntry(
            id = 1L,
            foodName = "Banana",
            calories = 105.0
        )

        fakeServiceLocator.foodEntryRepository.addTestData(listOf(testFoodEntry))

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // When
        composeTestRule.onNodeWithText("Banana").performLongClick()

        // Then
        composeTestRule.onNodeWithText("Edit Food Entry").assertExists()
        composeTestRule.onNodeWithText("Delete").assertExists()
        composeTestRule.onNodeWithText("Cancel").assertExists()
    }

    @Test
    fun deleteFoodEntry_confirmDeletion_removesEntry() = runTest {
        // Given
        val userId = 1L
        val testFoodEntry = createTestFoodEntry(
            id = 1L,
            foodName = "Banana",
            calories = 105.0
        )

        fakeServiceLocator.foodEntryRepository.addTestData(listOf(testFoodEntry))

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // When
        composeTestRule.onNodeWithText("Banana").performLongClick()
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.onNodeWithText("Confirm").performClick()

        // Then
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Banana").assertDoesNotExist()
    }

    @Test
    fun nutritionGoalsSetting_clickGoalsButton_showsGoalsDialog() {
        // Given
        val userId = 1L

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("Set nutrition goals").performClick()

        // Then
        composeTestRule.onNodeWithText("Set Daily Goals").assertExists()
        composeTestRule.onNodeWithText("Daily Calories").assertExists()
        composeTestRule.onNodeWithText("Protein (g)").assertExists()
        composeTestRule.onNodeWithText("Carbs (g)").assertExists()
        composeTestRule.onNodeWithText("Fat (g)").assertExists()
    }

    @Test
    fun setNutritionGoals_validInput_updatesGoals() = runTest {
        // Given
        val userId = 1L

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // When
        composeTestRule.onNodeWithContentDescription("Set nutrition goals").performClick()

        composeTestRule.onNodeWithText("Daily Calories").performTextClearance()
        composeTestRule.onNodeWithText("Daily Calories").performTextInput("2200")

        composeTestRule.onNodeWithText("Protein (g)").performTextClearance()
        composeTestRule.onNodeWithText("Protein (g)").performTextInput("165")

        composeTestRule.onNodeWithText("Save Goals").performClick()

        // Then
        composeTestRule.waitForIdle()
        // Goals should be reflected in the UI
        composeTestRule.onNodeWithText("Goal: 2,200 cal").assertExists()
        composeTestRule.onNodeWithText("Goal: 165g protein").assertExists()
    }

    @Test
    fun macronutrientChart_displaysCorrectProportions() = runTest {
        // Given
        val userId = 1L
        val nutritionSummary = NutritionSummary(
            totalCalories = 2000.0,
            totalProtein = 150.0, // 600 cal = 30%
            totalCarbs = 250.0, // 1000 cal = 50%
            totalFats = 44.4, // 400 cal = 20%
            totalFiber = 25.0,
            totalSugar = 50.0
        )

        fakeServiceLocator.nutritionRepository.setNutritionSummary(nutritionSummary)

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // When - Navigate to macro breakdown
        composeTestRule.onNodeWithText("Macro Breakdown").performClick()

        // Then
        composeTestRule.onNodeWithText("Protein: 30%").assertExists()
        composeTestRule.onNodeWithText("Carbs: 50%").assertExists()
        composeTestRule.onNodeWithText("Fat: 20%").assertExists()
    }

    @Test
    fun weeklyNutritionView_switchesToWeeklyMode() = runTest {
        // Given
        val userId = 1L

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // When
        composeTestRule.onNodeWithText("Weekly View").performClick()

        // Then
        composeTestRule.onNodeWithText("Weekly Nutrition").assertExists()
        composeTestRule.onNodeWithText("Average Daily Calories").assertExists()
        composeTestRule.onNodeWithText("Mon").assertExists()
        composeTestRule.onNodeWithText("Tue").assertExists()
        composeTestRule.onNodeWithText("Wed").assertExists()
    }

    @Test
    fun errorState_networkError_showsErrorMessage() = runTest {
        // Given
        val userId = 1L
        fakeServiceLocator.nutritionRepository.setError("Network error")

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Unable to load nutrition data").assertExists()
        composeTestRule.onNodeWithText("Retry").assertExists()
    }

    @Test
    fun retryButton_onError_retriesDataLoad() = runTest {
        // Given
        val userId = 1L
        fakeServiceLocator.nutritionRepository.setError("Network error")

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // When
        composeTestRule.onNodeWithText("Retry").performClick()

        // Then
        // Clear error and show loading, then success state
        fakeServiceLocator.nutritionRepository.clearError()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Today's Summary").assertExists()
    }

    @Test
    fun accessibilityCompliance_allElementsHaveContentDescriptions() {
        // Given
        val userId = 1L

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // Then - Check key UI elements have accessibility labels
        composeTestRule.onNodeWithContentDescription("Add food entry").assertExists()
        composeTestRule.onNodeWithContentDescription("Search foods").assertExists()
        composeTestRule.onNodeWithContentDescription("Set nutrition goals").assertExists()
        composeTestRule.onNodeWithContentDescription("Calories progress").assertExists()
        composeTestRule.onNodeWithContentDescription("Protein progress").assertExists()
        composeTestRule.onNodeWithContentDescription("Carbohydrates progress").assertExists()
        composeTestRule.onNodeWithContentDescription("Fat progress").assertExists()
    }

    @Test
    fun foodEntryList_scrolling_maintainsState() = runTest {
        // Given
        val userId = 1L
        val manyFoodEntries = (1..20).map { index ->
            createTestFoodEntry(
                id = index.toLong(),
                foodName = "Food Item $index",
                calories = (100 + index * 10).toDouble()
            )
        }

        fakeServiceLocator.foodEntryRepository.addTestData(manyFoodEntries)

        composeTestRule.setContent {
            FitnessTrackerAppTheme {
                NutritionScreen(userId = userId)
            }
        }

        // When
        composeTestRule.onNodeWithText("Food Item 1").assertExists()
        composeTestRule.onNodeWithText("Food Item 20").assertDoesNotExist()

        // Scroll to bottom
        composeTestRule.onNodeWithText("Food Item 1").performScrollTo()
        composeTestRule.onRoot().performTouchInput {
            swipeUp(startY = centerY + 200f, endY = centerY - 200f)
        }

        // Then
        composeTestRule.onNodeWithText("Food Item 20").assertExists()
    }

    @Test
    fun darkMode_nutritionScreen_rendersCorrectly() {
        // Given
        val userId = 1L

        composeTestRule.setContent {
            FitnessTrackerAppTheme(darkTheme = true) {
                NutritionScreen(userId = userId)
            }
        }

        // Then - UI should render without issues in dark mode
        composeTestRule.onNodeWithText("Nutrition Tracking").assertExists()
        composeTestRule.onNodeWithText("Today's Summary").assertExists()
    }

    // Helper function to create test food entries
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
}
