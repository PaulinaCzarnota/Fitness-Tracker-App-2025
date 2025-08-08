/**
 * StepCounterViewModel
 *
 * ViewModel for step counting functionality in the Fitness Tracker App.
 *
 * Responsibilities:
 * - Manages step count data and business logic for the UI.
 * - Handles step count updates from sensors and manual resets.
 * - Provides reactive step count updates via StateFlow.
 * - Integrates with StepRepository for data persistence.
 *
 * @property stepRepository Repository for step data operations.
 * @property authRepository Repository for authentication state.
 */

package com.example.fitnesstrackerapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.entity.Step
import com.example.fitnesstrackerapp.repository.AuthRepository
import com.example.fitnesstrackerapp.repository.StepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * UI state for step counter screens
 */
data class StepCounterUiState(
    val todaysSteps: Int = 0,
    val stepGoal: Int = 10000,
    val progressPercentage: Float = 0f,
    val distanceWalked: Float = 0f,
    val caloriesBurned: Float = 0f,
    val weeklySteps: List<Step> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for step counting functionality.
 */
class StepCounterViewModel(
    private val stepRepository: StepRepository,
    private val authRepository: AuthRepository,
    private val userId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(StepCounterUiState())
    val uiState: StateFlow<StepCounterUiState> = _uiState.asStateFlow()

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    private val _stepGoal = MutableStateFlow(10000)
    val stepGoal: StateFlow<Int> = _stepGoal.asStateFlow()

    init {
        // Initialize with current user and load step data
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                if (user != null) {
                    loadStepData(user.id)
                }
            }
        }
    }

    /**
     * Updates the current step count
     */
    fun updateStepCount(steps: Int) {
        viewModelScope.launch {
            _stepCount.value = steps
            _uiState.value = _uiState.value.copy(
                todaysSteps = steps,
                progressPercentage = (steps.toFloat() / _stepGoal.value) * 100f
            )

            // Save to repository
            val step = Step(
                userId = userId,
                count = steps,
                date = Date()
            )
            stepRepository.saveSteps(step)
        }
    }

    /**
     * Sets a new step goal
     */
    fun setStepGoal(goal: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(stepGoal = goal)
                
                // Update progress percentage based on new goal
                val progressPercentage = (_uiState.value.todaysSteps.toFloat() / goal) * 100f
                _uiState.value = _uiState.value.copy(
                    progressPercentage = progressPercentage.coerceAtMost(100f),
                    successMessage = "Step goal updated to $goal steps"
                )

                // Update step goal in StateFlow
                _stepGoal.value = goal

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update step goal: ${e.message}"
                )
            }
        }
    }

    /**
     * Resets the step count
     */
    fun resetStepCount() {
        viewModelScope.launch {
            _stepCount.value = 0
            _uiState.value = _uiState.value.copy(
                todaysSteps = 0,
                progressPercentage = 0f
            )
        }
    }

    /**
     * Clears any UI messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }

    /**
     * Loads step data for the current user
     */
    private fun loadStepData(userId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Load today's steps
                stepRepository.getTodaysSteps(userId).collect { todaysStep ->
                    val todaysSteps = todaysStep?.count ?: 0
                    val stepGoal = todaysStep?.goal ?: 10000
                    val progressPercentage = (todaysSteps.toFloat() / stepGoal) * 100f

                    _uiState.value = _uiState.value.copy(
                        todaysSteps = todaysSteps,
                        stepGoal = stepGoal,
                        progressPercentage = progressPercentage.coerceAtMost(100f),
                        distanceWalked = (todaysStep?.distanceMeters ?: 0f) / 1000f,
                        caloriesBurned = todaysStep?.caloriesBurned ?: 0f,
                        isLoading = false
                    )
                }

                // Load this week's steps for history
                val calendar = Calendar.getInstance()
                val weekEnd = calendar.time
                calendar.add(Calendar.DAY_OF_YEAR, -6) // Last 7 days
                val weekStart = calendar.time

                stepRepository.getStepsInDateRange(userId, weekStart, weekEnd).collect { weeklySteps ->
                    _uiState.value = _uiState.value.copy(
                        weeklySteps = weeklySteps
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load step data: ${e.message}"
                )
            }
        }
    }
}