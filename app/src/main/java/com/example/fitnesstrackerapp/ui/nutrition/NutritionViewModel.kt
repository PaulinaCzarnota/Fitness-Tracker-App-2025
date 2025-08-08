package com.example.fitnesstrackerapp.ui.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.MealType
import com.example.fitnesstrackerapp.repository.NutritionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

/**
 * UI state for nutrition tracking screens
 */
data class NutritionUiState(
    val foodEntries: List<FoodEntry> = emptyList(),
    val totalCalories: Double = 0.0,
    val breakfastCalories: Double = 0.0,
    val lunchCalories: Double = 0.0,
    val dinnerCalories: Double = 0.0,
    val snackCalories: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null
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
    private val repository: NutritionRepository,
    private val userId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    init {
        loadTodaysFoodEntries()
    }

    /**
     * Adds a new food entry
     */
    fun addFoodEntry(name: String, calories: Double, mealType: MealType) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val entry = FoodEntry(
                    userId = userId,
                    foodName = name,
                    servingSize = 1.0,
                    servingUnit = "serving",
                    caloriesPerServing = calories,
                    mealType = mealType,
                    dateConsumed = Date()
                )
                repository.addFoodEntry(entry)
                loadTodaysFoodEntries()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add food entry: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Loads food entries for the current user and today's date
     */
    private fun loadTodaysFoodEntries() {
        viewModelScope.launch {
            try {
                repository.getTodaysFoodEntries(userId).collect { entries ->
                    val totalCalories = entries.sumOf { it.calories }
                    val entriesByMeal = entries.groupBy { it.mealType }

                    _uiState.value = _uiState.value.copy(
                        foodEntries = entries,
                        totalCalories = totalCalories,
                        breakfastCalories = entriesByMeal[MealType.BREAKFAST]?.sumOf { it.calories } ?: 0.0,
                        lunchCalories = entriesByMeal[MealType.LUNCH]?.sumOf { it.calories } ?: 0.0,
                        dinnerCalories = entriesByMeal[MealType.DINNER]?.sumOf { it.calories } ?: 0.0,
                        snackCalories = entriesByMeal[MealType.SNACK]?.sumOf { it.calories } ?: 0.0,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load food entries: ${e.message}",
                    isLoading = false
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
                    error = "Failed to delete food entry: ${e.message}"
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
}
