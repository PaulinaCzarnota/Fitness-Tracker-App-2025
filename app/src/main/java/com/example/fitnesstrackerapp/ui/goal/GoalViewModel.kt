package com.example.fitnesstrackerapp.ui.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.entity.Goal
import com.example.fitnesstrackerapp.data.entity.GoalType
import com.example.fitnesstrackerapp.repository.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * UI state for goal management screens
 */
data class GoalUiState(
    val goals: List<Goal> = emptyList(),
    val activeGoals: List<Goal> = emptyList(),
    val achievedGoals: List<Goal> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for managing fitness goals.
 */
class GoalViewModel(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    private var currentUserId: Long? = null

    init {
        // Note: In a real implementation, you'd get the current user ID
        // For now, we'll assume it's set when the user logs in
        loadGoals()
    }

    /**
     * Sets the current user ID and loads their goals
     */
    fun setCurrentUser(userId: Long) {
        currentUserId = userId
        loadGoals()
    }

    /**
     * Creates a new fitness goal
     */
    fun createGoal(
        title: String,
        description: String?,
        goalType: GoalType,
        targetValue: Float,
        unit: String,
        targetDate: Date,
        reminderEnabled: Boolean = true
    ) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            try {
                val goal = Goal(
                    userId = userId,
                    title = title,
                    description = description,
                    goalType = goalType,
                    targetValue = targetValue.toDouble(),
                    unit = unit,
                    targetDate = targetDate,
                    reminderEnabled = reminderEnabled
                )

                goalRepository.insert(goal)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Goal created successfully!"
                )
                loadGoals()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create goal: ${e.message}"
                )
            }
        }
    }

    /**
     * Updates goal progress
     */
    fun updateGoalProgress(goalId: Long, currentValue: Float) {
        viewModelScope.launch {
            try {
                goalRepository.updateProgress(goalId, currentValue.toDouble(), autoComplete = true)

                // Check if goal is achieved
                val goal = goalRepository.getById(goalId)
                if (goal != null && currentValue.toDouble() >= goal.targetValue) {
                    goalRepository.markGoalAsCompleted(goalId, true)
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Congratulations! Goal '${goal.title}' achieved!"
                    )
                }

                loadGoals()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update goal progress: ${e.message}"
                )
            }
        }
    }

    /**
     * Marks a goal as achieved
     */
    fun achieveGoal(goalId: Long) {
        viewModelScope.launch {
            try {
                goalRepository.markGoalAsAchieved(goalId, Date().time)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Goal marked as achieved!"
                )
                loadGoals()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to mark goal as achieved: ${e.message}"
                )
            }
        }
    }

    /**
     * Deletes a goal
     */
    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            try {
                val goal = goalRepository.getById(goalId)
                if (goal != null) {
                    goalRepository.delete(goal)
                }
                _uiState.value = _uiState.value.copy(
                    successMessage = "Goal deleted"
                )
                loadGoals()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete goal: ${e.message}"
                )
            }
        }
    }

    /**
     * Toggles goal reminder
     */
    fun toggleGoalReminder(goalId: Long, enabled: Boolean) {
        currentUserId ?: return

        viewModelScope.launch {
            try {
                // Update the goal's reminder setting
                val goal = goalRepository.getById(goalId)
                if (goal != null) {
                    val updatedGoal = goal.copy(
                        reminderEnabled = enabled,
                        updatedAt = Date()
                    )
                    goalRepository.update(updatedGoal)
                    loadGoals()
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update reminder: ${e.message}"
                )
            }
        }
    }

    /**
     * Clears any UI messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }

    /**
     * Loads goals for the current user
     */
    private fun loadGoals() {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Load all goals
                goalRepository.getAllByUser(userId.toString()).collect { goals ->
                    _uiState.value = _uiState.value.copy(
                        goals = goals,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load goals: ${e.message}"
                )
            }
        }
    }

    // Add missing properties that ViewModels reference
    fun addGoal(goal: Goal) {
        viewModelScope.launch {
            try {
                goalRepository.insert(goal)
                loadGoals()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Goal added successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add goal: ${e.message}"
                )
            }
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            try {
                goalRepository.updateGoal(goal)
                loadGoals()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Goal updated successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update goal: ${e.message}"
                )
            }
        }
    }
}
