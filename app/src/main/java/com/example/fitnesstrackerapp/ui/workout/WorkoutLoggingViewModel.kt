package com.example.fitnesstrackerapp.ui.workout

/**
 * Enhanced WorkoutLoggingViewModel for detailed workout logging functionality.
 *
 * This ViewModel manages the comprehensive workout logging experience including:
 * - Exercise selection and filtering
 * - Individual set logging with reps, weights, and duration
 * - Personal record tracking
 * - Workout session management
 * - Performance analytics and history
 *
 * Key Features:
 * - Real-time workout tracking
 * - Exercise database management
 * - Set progression and personal records
 * - Rest timer integration
 * - Comprehensive workout analytics
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.entity.EquipmentType
import com.example.fitnesstrackerapp.data.entity.Exercise
import com.example.fitnesstrackerapp.data.entity.MuscleGroup
import com.example.fitnesstrackerapp.data.entity.SetType
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.data.entity.WorkoutSet
import com.example.fitnesstrackerapp.data.entity.WorkoutSetSummary
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import com.example.fitnesstrackerapp.repository.ExerciseRepository
import com.example.fitnesstrackerapp.repository.ExerciseStats
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import com.example.fitnesstrackerapp.repository.WorkoutSetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

/**
 * UI state for detailed workout logging screens.
 */
data class WorkoutLoggingUiState(
    val isLoading: Boolean = false,
    // Workout session state
    val activeWorkout: Workout? = null,
    val isWorkoutActive: Boolean = false,
    val workoutDuration: Long = 0L,
    // Exercise management
    val availableExercises: List<Exercise> = emptyList(),
    val selectedExercises: List<Exercise> = emptyList(),
    val exerciseSearchQuery: String = "",
    val muscleGroupFilter: MuscleGroup? = null,
    val equipmentFilter: EquipmentType? = null,
    // Set logging
    val currentExercise: Exercise? = null,
    val exerciseSets: List<WorkoutSet> = emptyList(),
    val recentSets: List<WorkoutSet> = emptyList(),
    val personalRecords: List<WorkoutSet> = emptyList(),
    val exerciseStats: ExerciseStats? = null,
    // Timer and rest
    val restTimer: Int = 0,
    val isRestTimerActive: Boolean = false,
    val suggestedRestTime: Int = 60, // seconds
    // Workout summary
    val workoutSummary: List<WorkoutSetSummary> = emptyList(),
    val totalVolume: Float = 0f,
    val totalSets: Int = 0,
    // UI state
    val error: String? = null,
    val successMessage: String? = null,
    val isShowingExerciseDialog: Boolean = false,
    val isShowingSetDialog: Boolean = false,
)

/**
 * Data class for set input.
 */
data class SetInput(
    val repetitions: Int = 0,
    val weight: Double = 0.0,
    val duration: Int = 0, // seconds
    val distance: Float = 0f, // km
    val setType: SetType = SetType.NORMAL,
    val rpe: Int? = null,
    val notes: String = "",
)

/**
 * Enhanced ViewModel for comprehensive workout logging.
 */
class WorkoutLoggingViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val workoutSetRepository: WorkoutSetRepository,
    private val userId: Long,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutLoggingUiState())
    val uiState: StateFlow<WorkoutLoggingUiState> = _uiState.asStateFlow()

    // Timer flow for workout duration
    private val _workoutTimer = MutableStateFlow(0L)
    val workoutTimer: StateFlow<Long> = _workoutTimer.asStateFlow()

    // Rest timer flow
    private val _restTimer = MutableStateFlow(0)
    val restTimer: StateFlow<Int> = _restTimer.asStateFlow()

    init {
        loadExercises()
        seedExercisesIfNeeded()
    }

    /**
     * Loads available exercises for the user.
     */
    private fun loadExercises() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                exerciseRepository.getAvailableExercises(userId).collect { exercises ->
                    _uiState.value = _uiState.value.copy(
                        availableExercises = exercises,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load exercises: ${e.message}",
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Seeds exercise database if it's empty.
     */
    private fun seedExercisesIfNeeded() {
        viewModelScope.launch {
            try {
                val exerciseCount = exerciseRepository.getTotalExerciseCount()
                if (exerciseCount == 0) {
                    exerciseRepository.seedExerciseDatabase()
                }
            } catch (e: Exception) {
                // Seeding failed, but not critical
            }
        }
    }

    /**
     * Starts a new workout session.
     */
    fun startWorkout(workoutType: WorkoutType, title: String) {
        viewModelScope.launch {
            try {
                val workout = Workout(
                    userId = userId,
                    workoutType = workoutType,
                    title = title,
                    startTime = Date(),
                )

                val workoutId = workoutRepository.insertWorkout(workout)
                val activeWorkout = workout.copy(id = workoutId)

                _uiState.value = _uiState.value.copy(
                    activeWorkout = activeWorkout,
                    isWorkoutActive = true,
                    selectedExercises = emptyList(),
                    successMessage = "Workout started!",
                )

                // Start workout timer
                startWorkoutTimer()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to start workout: ${e.message}",
                )
            }
        }
    }

    /**
     * Adds an exercise to the current workout.
     */
    fun addExerciseToWorkout(exercise: Exercise) {
        val currentSelected = _uiState.value.selectedExercises
        if (!currentSelected.contains(exercise)) {
            _uiState.value = _uiState.value.copy(
                selectedExercises = currentSelected + exercise,
                isShowingExerciseDialog = false,
            )
        }
    }

    /**
     * Removes an exercise from the current workout.
     */
    fun removeExerciseFromWorkout(exercise: Exercise) {
        viewModelScope.launch {
            try {
                // Delete all sets for this exercise in the current workout
                _uiState.value.activeWorkout?.let { workout ->
                    workoutSetRepository.getSetsByWorkoutAndExercise(workout.id, exercise.id)
                        .first()
                        .forEach { set ->
                            workoutSetRepository.deleteWorkoutSet(set.id)
                        }
                }

                val currentSelected = _uiState.value.selectedExercises
                _uiState.value = _uiState.value.copy(
                    selectedExercises = currentSelected - exercise,
                )

                if (_uiState.value.currentExercise == exercise) {
                    _uiState.value = _uiState.value.copy(
                        currentExercise = null,
                        exerciseSets = emptyList(),
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to remove exercise: ${e.message}",
                )
            }
        }
    }

    /**
     * Selects an exercise for set logging.
     */
    fun selectExercise(exercise: Exercise) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    currentExercise = exercise,
                    isLoading = true,
                )

                // Load sets for this exercise in the current workout
                _uiState.value.activeWorkout?.let { workout ->
                    workoutSetRepository.getSetsByWorkoutAndExercise(workout.id, exercise.id)
                        .collect { sets ->
                            _uiState.value = _uiState.value.copy(
                                exerciseSets = sets,
                                isLoading = false,
                            )
                        }
                }

                // Load recent performance data
                workoutSetRepository.getRecentSetsByExercise(exercise.id, 5)
                    .collect { recentSets ->
                        _uiState.value = _uiState.value.copy(recentSets = recentSets)
                    }

                // Load personal records
                workoutSetRepository.getPersonalRecords(exercise.id)
                    .collect { prs ->
                        _uiState.value = _uiState.value.copy(personalRecords = prs)
                    }

                // Load exercise stats
                val stats = workoutSetRepository.getExerciseStats(exercise.id)
                _uiState.value = _uiState.value.copy(exerciseStats = stats)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load exercise data: ${e.message}",
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Logs a new set for the current exercise.
     */
    fun logSet(setInput: SetInput) {
        viewModelScope.launch {
            try {
                val activeWorkout = _uiState.value.activeWorkout
                val currentExercise = _uiState.value.currentExercise

                if (activeWorkout == null || currentExercise == null) {
                    _uiState.value = _uiState.value.copy(
                        error = "No active workout or exercise selected",
                    )
                    return@launch
                }

                workoutSetRepository.createWorkoutSet(
                    workoutId = activeWorkout.id,
                    exerciseId = currentExercise.id,
                    repetitions = setInput.repetitions,
                    weight = setInput.weight,
                    duration = setInput.duration,
                    distance = setInput.distance,
                    setType = setInput.setType,
                    restTime = setInput.duration.takeIf { it > 0 }
                        ?: _uiState.value.suggestedRestTime,
                    rpe = setInput.rpe,
                    notes = setInput.notes,
                )

                _uiState.value = _uiState.value.copy(
                    successMessage = "Set logged successfully!",
                    isShowingSetDialog = false,
                )

                // Start rest timer if applicable
                if (setInput.setType != SetType.WARM_UP) {
                    startRestTimer(_uiState.value.suggestedRestTime)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to log set: ${e.message}",
                )
            }
        }
    }

    /**
     * Updates an existing set.
     */
    fun updateSet(set: WorkoutSet, setInput: SetInput) {
        viewModelScope.launch {
            try {
                val updatedSet = set.copy(
                    repetitions = setInput.repetitions,
                    weight = setInput.weight,
                    duration = setInput.duration,
                    distance = setInput.distance,
                    setType = setInput.setType,
                    rpe = setInput.rpe,
                    notes = setInput.notes,
                    updatedAt = Date(),
                )

                workoutSetRepository.updateWorkoutSet(updatedSet)

                _uiState.value = _uiState.value.copy(
                    successMessage = "Set updated successfully!",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update set: ${e.message}",
                )
            }
        }
    }

    /**
     * Deletes a set.
     */
    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch {
            try {
                workoutSetRepository.deleteWorkoutSet(set.id)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Set deleted successfully!",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete set: ${e.message}",
                )
            }
        }
    }

    /**
     * Duplicates a set for quick logging.
     */
    fun duplicateSet(set: WorkoutSet) {
        viewModelScope.launch {
            try {
                workoutSetRepository.duplicateSet(set.id)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Set duplicated successfully!",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to duplicate set: ${e.message}",
                )
            }
        }
    }

    /**
     * Completes the current workout.
     */
    fun finishWorkout(notes: String = "") {
        viewModelScope.launch {
            try {
                val activeWorkout = _uiState.value.activeWorkout ?: return@launch
                val endTime = Date()
                val duration = ((endTime.time - activeWorkout.startTime.time) / 1000 / 60).toInt()

                // Get workout summary
                val summary = workoutSetRepository.getWorkoutSummary(activeWorkout.id)
                val totalVolume = workoutSetRepository.getWorkoutVolume(activeWorkout.id)
                val totalCalories = summary.sumOf { (it.totalVolume * 0.5) }.toInt() // Simple calorie calculation

                val completedWorkout = activeWorkout.copy(
                    endTime = endTime,
                    duration = duration,
                    caloriesBurned = totalCalories,
                    notes = notes,
                )

                workoutRepository.updateWorkout(completedWorkout)

                _uiState.value = _uiState.value.copy(
                    isWorkoutActive = false,
                    activeWorkout = null,
                    selectedExercises = emptyList(),
                    currentExercise = null,
                    exerciseSets = emptyList(),
                    workoutSummary = summary,
                    totalVolume = totalVolume,
                    successMessage = "Workout completed!",
                )

                stopWorkoutTimer()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to finish workout: ${e.message}",
                )
            }
        }
    }

    /**
     * Filters exercises by search query.
     */
    fun searchExercises(query: String) {
        _uiState.value = _uiState.value.copy(exerciseSearchQuery = query)

        viewModelScope.launch {
            if (query.isBlank()) {
                exerciseRepository.getAvailableExercises(userId).collect { exercises ->
                    _uiState.value = _uiState.value.copy(availableExercises = exercises)
                }
            } else {
                exerciseRepository.searchExercises(query).collect { exercises ->
                    _uiState.value = _uiState.value.copy(availableExercises = exercises)
                }
            }
        }
    }

    /**
     * Filters exercises by muscle group.
     */
    fun filterByMuscleGroup(muscleGroup: MuscleGroup?) {
        _uiState.value = _uiState.value.copy(muscleGroupFilter = muscleGroup)

        viewModelScope.launch {
            if (muscleGroup == null) {
                exerciseRepository.getAvailableExercises(userId).collect { exercises ->
                    _uiState.value = _uiState.value.copy(availableExercises = exercises)
                }
            } else {
                exerciseRepository.getExercisesByMuscleGroup(muscleGroup).collect { exercises ->
                    _uiState.value = _uiState.value.copy(availableExercises = exercises)
                }
            }
        }
    }

    /**
     * Starts the workout timer.
     */
    private fun startWorkoutTimer() {
        // Timer implementation would go here
        // This is a simplified version
    }

    /**
     * Stops the workout timer.
     */
    private fun stopWorkoutTimer() {
        _workoutTimer.value = 0L
    }

    /**
     * Starts the rest timer.
     */
    private fun startRestTimer(duration: Int) {
        _uiState.value = _uiState.value.copy(
            restTimer = duration,
            isRestTimerActive = true,
        )
    }

    /**
     * Stops the rest timer.
     */
    fun stopRestTimer() {
        _uiState.value = _uiState.value.copy(
            restTimer = 0,
            isRestTimerActive = false,
        )
    }

    /**
     * Shows the exercise selection dialog.
     */
    fun showExerciseDialog() {
        _uiState.value = _uiState.value.copy(isShowingExerciseDialog = true)
    }

    /**
     * Hides the exercise selection dialog.
     */
    fun hideExerciseDialog() {
        _uiState.value = _uiState.value.copy(isShowingExerciseDialog = false)
    }

    /**
     * Shows the set logging dialog.
     */
    fun showSetDialog() {
        _uiState.value = _uiState.value.copy(isShowingSetDialog = true)
    }

    /**
     * Hides the set logging dialog.
     */
    fun hideSetDialog() {
        _uiState.value = _uiState.value.copy(isShowingSetDialog = false)
    }

    /**
     * Clears error messages.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Clears success messages.
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}
