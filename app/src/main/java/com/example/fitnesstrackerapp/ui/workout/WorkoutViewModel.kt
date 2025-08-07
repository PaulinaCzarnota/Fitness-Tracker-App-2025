package com.example.fitnesstrackerapp.ui.workout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.FitnessTrackerApplication
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * UI state for workout screens
 */
data class WorkoutUiState(
    val isLoading: Boolean = false,
    val weeklyWorkouts: Int = 0,
    val weeklyDuration: Int = 0,
    val weeklyCalories: Int = 0,
    val recentWorkouts: List<Workout> = emptyList(),
    val workouts: List<Workout> = emptyList(),
    val isWorkoutActive: Boolean = false,
    val activeWorkout: Workout? = null,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * WorkoutViewModel handles workout-related UI state and business logic
 *
 * Responsibilities:
 * - Manages workout sessions (start, stop, pause)
 * - Tracks workout history and statistics
 * - Handles workout data persistence
 * - Provides UI state for workout screens
 */
class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FitnessTrackerApplication
    private val workoutRepository = app.workoutRepository
    private val authRepository = app.authRepository

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        loadWorkouts()
        loadWeeklyStats()
    }

    /**
     * Loads all workouts for the current user
     */
    private fun loadWorkouts() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    workoutRepository.getWorkoutsByUserId(currentUserId).collect { workouts ->
                        _uiState.value = _uiState.value.copy(
                            workouts = workouts,
                            recentWorkouts = workouts.take(5),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load workouts: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    /**
     * Loads weekly workout statistics
     */
    private fun loadWeeklyStats() {
        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    val weeklyStats = workoutRepository.getWeeklyStats(currentUserId)
                    _uiState.value = _uiState.value.copy(
                        weeklyWorkouts = weeklyStats.workoutCount,
                        weeklyDuration = weeklyStats.totalDuration,
                        weeklyCalories = weeklyStats.totalCalories
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load weekly stats: ${e.message}"
                )
            }
        }
    }

    /**
     * Starts a new workout session
     */
    fun startWorkout(type: String, duration: Int, distance: Int, notes: String) {
        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    val workout = Workout(
                        userId = currentUserId,
                        workoutType = WorkoutType.valueOf(type),
                        title = "Workout Session",
                        startTime = System.currentTimeMillis(),
                        duration = duration,
                        distance = distance.toFloat(),
                        notes = notes
                    )

                    _uiState.value = _uiState.value.copy(
                        isWorkoutActive = true,
                        activeWorkout = workout,
                        successMessage = "Workout started successfully!"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to start workout: ${e.message}"
                )
            }
        }
    }

    /**
     * Stops the current workout session
     */
    fun stopWorkout() {
        viewModelScope.launch {
            try {
                val activeWorkout = _uiState.value.activeWorkout
                if (activeWorkout != null) {
                    workoutRepository.insertWorkout(activeWorkout)
                    _uiState.value = _uiState.value.copy(
                        isWorkoutActive = false,
                        activeWorkout = null,
                        successMessage = "Workout completed and saved!"
                    )
                    loadWorkouts() // Refresh the workout list
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to save workout: ${e.message}"
                )
            }
        }
    }

    /**
     * Deletes a workout by ID
     */
    fun deleteWorkout(workoutId: Long) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteWorkout(workoutId)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Workout deleted successfully!"
                )
                loadWorkouts() // Refresh the workout list
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete workout: ${e.message}"
                )
            }
        }
    }

    /**
     * Clears any error or success messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}
