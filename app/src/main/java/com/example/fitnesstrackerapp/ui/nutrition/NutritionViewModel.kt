package com.example.fitnesstrackerapp.ui.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.MealType
import com.example.fitnesstrackerapp.data.model.NutritionSummary
import com.example.fitnesstrackerapp.repository.SimpleNutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * UI state for nutrition tracking screens
 */
data class NutritionUiState(
    val foodEntries: List<FoodEntry> = emptyList(),
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalFiber: Double = 0.0,
    val dailyCalorieGoal: Int = 2000,
    val breakfastCalories: Double = 0.0,
    val lunchCalories: Double = 0.0,
    val dinnerCalories: Double = 0.0,
    val snackCalories: Double = 0.0,
    val nutritionSummary: NutritionSummary? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

/**
 * ViewModel for managing nutrition tracking and food logging
 *
 * Responsibilities:
 * - Manages food entry data and daily nutrition stats
 * - Handles CRUD operations for food entries
 * - Calculates daily nutrition totals
 * - Provides UI state for nutrition screens
 */
class NutritionViewModel(
    private val repository: SimpleNutritionRepository,
    private val userId: Long,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    init {
        loadTodaysFoodEntries()
    }

    /**
     * Adds a new food entry with enhanced nutritional information
     */
    fun addFoodEntry(
        foodName: String,
        calories: Int,
        proteinGrams: Double,
        carbsGrams: Double,
        fatGrams: Double,
        fiberGrams: Double,
        mealType: MealType,
        servingSize: String,
        quantity: Float,
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val entry = FoodEntry(
                    userId = userId,
                    foodName = foodName,
                    servingSize = quantity.toDouble(),
                    servingUnit = servingSize,
                    caloriesPerServing = calories.toDouble(),
                    proteinGrams = proteinGrams,
                    carbsGrams = carbsGrams,
                    fatGrams = fatGrams,
                    fiberGrams = fiberGrams,
                    mealType = mealType,
                    dateConsumed = Date(),
                )
                repository.addFoodEntry(entry)
                loadFoodEntriesForDate(Date())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add food entry: ${e.message}",
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Adds a simple food entry (backward compatibility)
     */
    fun addFoodEntry(name: String, calories: Double, mealType: MealType) {
        addFoodEntry(
            foodName = name,
            calories = calories.toInt(),
            proteinGrams = 0.0,
            carbsGrams = 0.0,
            fatGrams = 0.0,
            fiberGrams = 0.0,
            mealType = mealType,
            servingSize = "serving",
            quantity = 1f,
        )
    }

    /**
     * Loads food entries for the current user and today's date
     */
    private fun loadTodaysFoodEntries() {
        loadFoodEntriesForDate(Date())
    }

    /**
     * Loads food entries for a specific date
     */
    fun loadFoodEntriesForDate(date: Date) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repository.getAllFoodEntriesForUser(userId).collect { entries ->
                    // Filter entries for the specific date (simplified - in real app would use date range query)
                    val todayEntries = entries.filter { 
                        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                        val entryDate = dateFormat.format(it.dateConsumed)
                        val targetDate = dateFormat.format(date)
                        entryDate == targetDate
                    }
                    
                    val totalCalories = todayEntries.sumOf { it.getTotalCalories() }
                    val totalProtein = todayEntries.sumOf { it.getTotalProtein() }
                    val totalCarbs = todayEntries.sumOf { it.getTotalCarbs() }
                    val totalFat = todayEntries.sumOf { it.getTotalFat() }
                    val totalFiber = todayEntries.sumOf { it.getTotalFiber() }
                    
                    val entriesByMeal = todayEntries.groupBy { it.mealType }
                    
                    // Create nutrition summary
                    val nutritionSummary = NutritionSummary(
                        totalCalories = totalCalories,
                        totalProtein = totalProtein,
                        totalCarbs = totalCarbs,
                        totalFat = totalFat,
                        totalFiber = totalFiber,
                        totalSugar = todayEntries.sumOf { it.getTotalSugar() },
                        totalSodium = todayEntries.sumOf { it.getTotalSodium() }
                    )

                    _uiState.value = _uiState.value.copy(
                        foodEntries = todayEntries,
                        totalCalories = totalCalories,
                        totalProtein = totalProtein,
                        totalCarbs = totalCarbs,
                        totalFat = totalFat,
                        totalFiber = totalFiber,
                        breakfastCalories = entriesByMeal[MealType.BREAKFAST]?.sumOf { it.getTotalCalories() } ?: 0.0,
                        lunchCalories = entriesByMeal[MealType.LUNCH]?.sumOf { it.getTotalCalories() } ?: 0.0,
                        dinnerCalories = entriesByMeal[MealType.DINNER]?.sumOf { it.getTotalCalories() } ?: 0.0,
                        snackCalories = entriesByMeal[MealType.SNACK]?.sumOf { it.getTotalCalories() } ?: 0.0,
                        nutritionSummary = nutritionSummary,
                        isLoading = false,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load food entries: ${e.message}",
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Deletes a food entry
     */
    fun deleteFoodEntry(entry: FoodEntry) {
        viewModelScope.launch {
            try {
                repository.deleteFoodEntry(entry)
                loadTodaysFoodEntries()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete food entry: ${e.message}",
                )
            }
        }
    }

    /**
     * Clears the error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Seeds default nutrition data for demonstration
     */
    fun seedDefaultData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Add some sample food entries for today
                val sampleEntries = listOf(
                    FoodEntry(
                        userId = userId,
                        foodName = "Oatmeal with Banana",
                        servingSize = 1.0,
                        servingUnit = "bowl",
                        caloriesPerServing = 250.0,
                        proteinGrams = 8.0,
                        carbsGrams = 45.0,
                        fatGrams = 4.0,
                        fiberGrams = 6.0,
                        sugarGrams = 12.0,
                        mealType = MealType.BREAKFAST,
                        dateConsumed = Date()
                    ),
                    FoodEntry(
                        userId = userId,
                        foodName = "Greek Yogurt",
                        servingSize = 1.0,
                        servingUnit = "cup",
                        caloriesPerServing = 130.0,
                        proteinGrams = 15.0,
                        carbsGrams = 9.0,
                        fatGrams = 0.0,
                        fiberGrams = 0.0,
                        sugarGrams = 9.0,
                        mealType = MealType.BREAKFAST,
                        dateConsumed = Date()
                    ),
                    FoodEntry(
                        userId = userId,
                        foodName = "Chicken Salad",
                        servingSize = 1.5,
                        servingUnit = "cups",
                        caloriesPerServing = 320.0,
                        proteinGrams = 35.0,
                        carbsGrams = 12.0,
                        fatGrams = 8.0,
                        fiberGrams = 4.0,
                        sugarGrams = 6.0,
                        mealType = MealType.LUNCH,
                        dateConsumed = Date()
                    ),
                    FoodEntry(
                        userId = userId,
                        foodName = "Mixed Nuts",
                        servingSize = 0.25,
                        servingUnit = "cup",
                        caloriesPerServing = 170.0,
                        proteinGrams = 6.0,
                        carbsGrams = 6.0,
                        fatGrams = 15.0,
                        fiberGrams = 3.0,
                        sugarGrams = 1.0,
                        mealType = MealType.SNACK,
                        dateConsumed = Date()
                    ),
                    FoodEntry(
                        userId = userId,
                        foodName = "Salmon with Quinoa",
                        servingSize = 1.0,
                        servingUnit = "serving",
                        caloriesPerServing = 450.0,
                        proteinGrams = 40.0,
                        carbsGrams = 35.0,
                        fatGrams = 18.0,
                        fiberGrams = 5.0,
                        sugarGrams = 3.0,
                        mealType = MealType.DINNER,
                        dateConsumed = Date()
                    )
                )
                
                // Add each entry to the repository
                sampleEntries.forEach { entry ->
                    repository.addFoodEntry(entry)
                }
                
                // Reload data to reflect the changes
                loadFoodEntriesForDate(Date())
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to seed default data: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
}
