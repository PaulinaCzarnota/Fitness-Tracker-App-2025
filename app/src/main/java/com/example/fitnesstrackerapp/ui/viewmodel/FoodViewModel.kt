package com.example.fitnesstrackerapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel for managing nutrition data and UI state
 */
@HiltViewModel
class FoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _foodEntries = MutableStateFlow<List<FoodEntry>>(emptyList())
    val foodEntries: StateFlow<List<FoodEntry>> = _foodEntries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _totalCalories = MutableStateFlow(0)
    val totalCalories: StateFlow<Int> = _totalCalories.asStateFlow()

    private var currentUserId: String = "default_user"

    fun setUserId(userId: String) {
        currentUserId = userId
        loadFoodEntries()
    }

    private fun loadFoodEntries() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                foodRepository.getFoodEntriesByUser(currentUserId)
                    .collect { entries ->
                        _foodEntries.value = entries
                        calculateTotalCalories()
                    }
            } catch (e: Exception) {
                _error.value = "Error loading food entries: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateTotalCalories() {
        _totalCalories.value = _foodEntries.value.sumOf { it.calories }
    }

    fun addFoodEntry(
        name: String,
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float,
        quantity: Float,
        unit: String,
        mealType: String
    ) {
        viewModelScope.launch {
            try {
                val foodEntry = FoodEntry(
                    userId = currentUserId,
                    name = name,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    quantity = quantity,
                    unit = unit,
                    mealType = mealType,
                    date = Date()
                )
                foodRepository.insertFoodEntry(foodEntry)
            } catch (e: Exception) {
                _error.value = "Error adding food entry: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
