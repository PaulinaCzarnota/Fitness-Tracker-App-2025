package com.example.fitnesstrackerapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.entity.Goal
import com.example.fitnesstrackerapp.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel for managing goal data and UI state
 */
@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    val goals: StateFlow<List<Goal>> = _goals.asStateFlow()

    private val _activeGoals = MutableStateFlow<List<Goal>>(emptyList())
    val activeGoals: StateFlow<List<Goal>> = _activeGoals.asStateFlow()

    private val _completedGoals = MutableStateFlow<List<Goal>>(emptyList())
    val completedGoals: StateFlow<List<Goal>> = _completedGoals.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentUserId: String = "default_user"

    fun setUserId(userId: String) {
        currentUserId = userId
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                goalRepository.getGoalsByUser(currentUserId)
                    .collect { goalList ->
                        _goals.value = goalList
                    }

                goalRepository.getActiveGoals(currentUserId)
                    .collect { activeList ->
                        _activeGoals.value = activeList
                    }

                goalRepository.getCompletedGoals(currentUserId)
                    .collect { completedList ->
                        _completedGoals.value = completedList
                    }
            } catch (e: Exception) {
                _error.value = "Error loading goals: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addGoal(
        title: String,
        description: String,
        targetValue: Float,
        unit: String,
        deadline: Date
    ) {
        viewModelScope.launch {
            try {
                val goal = Goal(
                    userId = currentUserId,
                    title = title,
                    description = description,
                    targetValue = targetValue,
                    unit = unit,
                    deadline = deadline
                )
                goalRepository.insertGoal(goal)
            } catch (e: Exception) {
                _error.value = "Error adding goal: ${e.message}"
            }
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            try {
                goalRepository.updateGoal(goal)
            } catch (e: Exception) {
                _error.value = "Error updating goal: ${e.message}"
            }
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            try {
                goalRepository.deleteGoal(goal)
            } catch (e: Exception) {
                _error.value = "Error deleting goal: ${e.message}"
            }
        }
    }

    fun updateGoalProgress(goalId: Long, currentValue: Float) {
        viewModelScope.launch {
            try {
                goalRepository.updateGoalProgress(goalId, currentValue)
            } catch (e: Exception) {
                _error.value = "Error updating goal progress: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
