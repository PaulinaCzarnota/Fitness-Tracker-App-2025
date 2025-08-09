/**
 * Workout Management ViewModel
 *
 * This ViewModel handles comprehensive workout management operations including:
 * - Creating new workouts with MET-based calorie calculations
 * - Updating existing workouts with live calorie recalculation
 * - Deleting workouts with proper cleanup
 * - LiveData-based reactive UI updates
 * - Error handling and user feedback
 */

package com.example.fitnesstrackerapp.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import com.example.fitnesstrackerapp.util.MetTableCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * UI state for workout management operations.
 */
data class WorkoutManagementUiState(
    val isLoading: Boolean = false,
    val workouts: List<Workout> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,

    // Filter and search state
    val searchQuery: String = "",
    val selectedWorkoutTypes: Set<String> = emptySet(),
    val dateRange: Pair<Date?, Date?> = Pair(null, null),

    // Statistics
    val totalWorkouts: Int = 0,
    val totalDuration: Int = 0,
    val totalCalories: Int = 0,
    val averageDuration: Float = 0f,
)

/**
 * ViewModel for managing workout CRUD operations.
 */
class WorkoutManagementViewModel(
    private val workoutRepository: WorkoutRepository,
    private val userId: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutManagementUiState())
    val uiState: StateFlow<WorkoutManagementUiState> = _uiState.asStateFlow()

    init {
        loadWorkouts()
        loadStatistics()
    }

    /**
     * Loads all workouts for the user with LiveData observation.
     */
    private fun loadWorkouts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                workoutRepository.getWorkoutsByUserId(userId).collect { workouts ->
                    _uiState.value = _uiState.value.copy(
                        workouts = workouts,
                        totalWorkouts = workouts.size,
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
     * Loads workout statistics.
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                val totalDuration = workoutRepository.getTotalWorkoutDuration(userId)
                val averageDuration = workoutRepository.getAverageWorkoutDuration(userId)

                _uiState.value = _uiState.value.copy(
                    totalDuration = totalDuration,
                    averageDuration = averageDuration,
                )
            } catch (e: Exception) {
                // Statistics loading failed, but not critical
            }
        }
    }

    /**
     * Creates a new workout with MET-based calorie calculation.
     */
    fun createWorkout(workoutData: WorkoutData) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // Calculate calories using MET tables
                val estimatedCalories = MetTableCalculator.calculateWorkoutCalories(
                    workoutType = workoutData.workoutType,
                    durationMinutes = workoutData.duration,
                    weightKg = 70.0, // Default weight, should be user's actual weight
                    intensity = workoutData.intensity,
                    distance = workoutData.distance.takeIf { it > 0 },
                    avgHeartRate = workoutData.avgHeartRate,
                )

                // Create workout entity
                val workout = Workout(
                    userId = userId,
                    workoutType = workoutData.workoutType,
                    title = workoutData.title,
                    startTime = Date(),
                    endTime = Date(System.currentTimeMillis() + (workoutData.duration * 60 * 1000L)),
                    duration = workoutData.duration,
                    distance = workoutData.distance,
                    caloriesBurned = estimatedCalories,
                    avgHeartRate = workoutData.avgHeartRate,
                    maxHeartRate = workoutData.maxHeartRate,
                    notes = workoutData.notes.takeIf { it.isNotBlank() },
                    createdAt = Date(),
                    updatedAt = Date(),
                )

                workoutRepository.insertWorkout(workout)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Workout created successfully! Estimated $estimatedCalories calories burned.",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create workout: ${e.message}",
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Updates an existing workout.
     */
    fun updateWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                workoutRepository.updateWorkout(workout)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Workout updated successfully!",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update workout: ${e.message}",
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Deletes a workout.
     */
    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                workoutRepository.deleteWorkout(workout.id)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Workout deleted successfully!",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete workout: ${e.message}",
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Searches workouts by query string.
     */
    fun searchWorkouts(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        if (query.isBlank()) {
            loadWorkouts()
        } else {
            viewModelScope.launch {
                try {
                    workoutRepository.getWorkoutsByUserId(userId).collect { allWorkouts ->
                        val filteredWorkouts = allWorkouts.filter { workout ->
                            workout.title.contains(query, ignoreCase = true) ||
                                workout.workoutType.name.contains(query, ignoreCase = true) ||
                                workout.notes?.contains(query, ignoreCase = true) == true
                        }

                        _uiState.value = _uiState.value.copy(
                            workouts = filteredWorkouts,
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to search workouts: ${e.message}",
                    )
                }
            }
        }
    }

    /**
     * Filters workouts by date range.
     */
    fun filterByDateRange(startDate: Date?, endDate: Date?) {
        _uiState.value = _uiState.value.copy(
            dateRange = Pair(startDate, endDate),
        )

        if (startDate == null || endDate == null) {
            loadWorkouts()
        } else {
            viewModelScope.launch {
                try {
                    workoutRepository.getWorkoutsByDateRange(userId, startDate, endDate)
                        .collect { filteredWorkouts ->
                            _uiState.value = _uiState.value.copy(
                                workouts = filteredWorkouts,
                            )
                        }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to filter workouts: ${e.message}",
                    )
                }
            }
        }
    }

    /**
     * Filters workouts by workout types.
     */
    fun filterByWorkoutTypes(workoutTypes: Set<String>) {
        _uiState.value = _uiState.value.copy(
            selectedWorkoutTypes = workoutTypes,
        )

        if (workoutTypes.isEmpty()) {
            loadWorkouts()
        } else {
            viewModelScope.launch {
                try {
                    workoutRepository.getWorkoutsByUserId(userId).collect { allWorkouts ->
                        val filteredWorkouts = allWorkouts.filter { workout ->
                            workoutTypes.contains(workout.workoutType.name)
                        }

                        _uiState.value = _uiState.value.copy(
                            workouts = filteredWorkouts,
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to filter workouts: ${e.message}",
                    )
                }
            }
        }
    }

    /**
     * Gets workout statistics for a specific period.
     */
    fun getWorkoutStatistics(
        startDate: Date,
        endDate: Date,
        callback: (WorkoutStatistics) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                workoutRepository.getWorkoutsByDateRange(userId, startDate, endDate)
                    .first() // Get the first emission
                    .let { workouts ->
                        val stats = WorkoutStatistics(
                            totalWorkouts = workouts.size,
                            totalDuration = workouts.sumOf { it.duration },
                            totalCalories = workouts.sumOf { it.caloriesBurned },
                            totalDistance = workouts.sumOf { it.distance.toDouble() }.toFloat(),
                            averageDuration = if (workouts.isNotEmpty()) {
                                workouts.sumOf { it.duration }.toFloat() / workouts.size
                            } else {
                                0f
                            },
                            averageCalories = if (workouts.isNotEmpty()) {
                                workouts.sumOf { it.caloriesBurned }.toFloat() / workouts.size
                            } else {
                                0f
                            },
                            workoutsByType = workouts.groupingBy { it.workoutType.name }.eachCount(),
                        )
                        callback(stats)
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to calculate statistics: ${e.message}",
                )
            }
        }
    }

    /**
     * Recalculates calories for all workouts using current MET tables.
     * This is useful when MET values are updated or user weight changes.
     */
    fun recalculateAllCalories(userWeight: Double) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    successMessage = null,
                    error = null,
                )

                val currentWorkouts = _uiState.value.workouts
                var updatedCount = 0

                currentWorkouts.forEach { workout ->
                    try {
                        val newCalories = MetTableCalculator.calculateWorkoutCalories(
                            workoutType = workout.workoutType,
                            durationMinutes = workout.duration,
                            weightKg = userWeight,
                            distance = workout.distance.takeIf { it > 0 },
                            avgHeartRate = workout.avgHeartRate,
                        )

                        if (newCalories != workout.caloriesBurned) {
                            val updatedWorkout = workout.copy(
                                caloriesBurned = newCalories,
                                updatedAt = Date(),
                            )
                            workoutRepository.updateWorkout(updatedWorkout)
                            updatedCount++
                        }
                    } catch (e: Exception) {
                        // Continue with other workouts if one fails
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = if (updatedCount > 0) {
                        "Updated calorie calculations for $updatedCount workouts"
                    } else {
                        "All workouts already have accurate calorie calculations"
                    },
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to recalculate calories: ${e.message}",
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Clears the current error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Clears the current success message.
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /**
     * Resets all filters and search.
     */
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedWorkoutTypes = emptySet(),
            dateRange = Pair(null, null),
        )
        loadWorkouts()
    }
}

/**
 * Data class for workout statistics.
 */
data class WorkoutStatistics(
    val totalWorkouts: Int,
    val totalDuration: Int, // in minutes
    val totalCalories: Int,
    val totalDistance: Float, // in kilometers
    val averageDuration: Float, // in minutes
    val averageCalories: Float,
    val workoutsByType: Map<String, Int>,
)
