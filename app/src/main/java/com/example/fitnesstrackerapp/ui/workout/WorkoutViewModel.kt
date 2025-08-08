package com.example.fitnesstrackerapp.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.entity.*
import com.example.fitnesstrackerapp.repository.ExerciseRepository
import com.example.fitnesstrackerapp.repository.ExerciseStats
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import com.example.fitnesstrackerapp.repository.WorkoutSetRepository
import kotlinx.coroutines.flow.*
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
    val successMessage: String? = null,
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
class WorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val userId: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        loadWorkouts()
    }

    /**
     * Loads user workouts from repository
     */
    private fun loadWorkouts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                workoutRepository.getWorkoutsByUserId(userId).collect { workouts ->
                    _uiState.value = _uiState.value.copy(
                        workouts = workouts,
                        recentWorkouts = workouts.take(5),
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load workouts: ${e.message}",
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Starts a new workout session
     */
    fun startWorkout(type: WorkoutType, title: String) {
        viewModelScope.launch {
            try {
                val workout = Workout(
                    userId = userId,
                    workoutType = type,
                    title = title,
                    startTime = Date(),
                )
                val workoutId = workoutRepository.insertWorkout(workout)
                val activeWorkout = workout.copy(id = workoutId)

                _uiState.value = _uiState.value.copy(
                    isWorkoutActive = true,
                    activeWorkout = activeWorkout,
                    successMessage = "Workout started!",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to start workout: ${e.message}",
                )
            }
        }
    }

    /**
     * Stops the current workout session
     */
    fun stopWorkout(caloriesBurned: Int = 0, distance: Float = 0f, notes: String = "") {
        viewModelScope.launch {
            try {
                val activeWorkout = _uiState.value.activeWorkout
                if (activeWorkout != null) {
                    val endTime = Date()
                    val duration = ((endTime.time - activeWorkout.startTime.time) / 1000 / 60).toInt() // minutes

                    val completedWorkout = activeWorkout.copy(
                        endTime = endTime,
                        duration = duration,
                        caloriesBurned = caloriesBurned,
                        distance = distance,
                        notes = notes,
                    )

                    workoutRepository.updateWorkout(completedWorkout)

                    _uiState.value = _uiState.value.copy(
                        isWorkoutActive = false,
                        activeWorkout = null,
                        successMessage = "Workout completed!",
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to stop workout: ${e.message}",
                )
            }
        }
    }

    /**
     * Deletes a workout
     */
    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteWorkout(workout.id)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Workout deleted!",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete workout: ${e.message}",
                )
            }
        }
    }

    /**
     * Clears error messages
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Clears success messages
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
