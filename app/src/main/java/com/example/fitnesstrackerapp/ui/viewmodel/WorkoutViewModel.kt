package com.example.fitnesstrackerapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel for managing workout data and UI state
 */
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _totalCalories = MutableStateFlow(0)
    val totalCalories: StateFlow<Int> = _totalCalories.asStateFlow()

    private val _totalSteps = MutableStateFlow(0)
    val totalSteps: StateFlow<Int> = _totalSteps.asStateFlow()

    private val _totalDuration = MutableStateFlow(0)
    val totalDuration: StateFlow<Int> = _totalDuration.asStateFlow()

    private var currentUserId: String = "default_user"

    fun setUserId(userId: String) {
        currentUserId = userId
        loadWorkouts()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                workoutRepository.getWorkoutsByUser(currentUserId)
                    .collect { workoutList ->
                        _workouts.value = workoutList
                        updateTotals()
                    }
            } catch (e: Exception) {
                _error.value = "Error loading workouts: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateTotals() {
        viewModelScope.launch {
            try {
                _totalCalories.value = workoutRepository.getTotalCaloriesBurned(currentUserId) ?: 0
                _totalSteps.value = workoutRepository.getTotalSteps(currentUserId) ?: 0
                _totalDuration.value = workoutRepository.getTotalDuration(currentUserId) ?: 0
            } catch (e: Exception) {
                _error.value = "Error calculating totals: ${e.message}"
            }
        }
    }

    fun addWorkout(
        type: String,
        duration: Int,
        distance: Float,
        calories: Int,
        steps: Int,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                val workout = Workout(
                    userId = currentUserId,
                    type = type,
                    duration = duration,
                    distance = distance,
                    calories = calories,
                    steps = steps,
                    notes = notes,
                    date = Date()
                )
                workoutRepository.insertWorkout(workout)
            } catch (e: Exception) {
                _error.value = "Error adding workout: ${e.message}"
            }
        }
    }

    fun updateWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                workoutRepository.updateWorkout(workout)
            } catch (e: Exception) {
                _error.value = "Error updating workout: ${e.message}"
            }
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteWorkout(workout)
            } catch (e: Exception) {
                _error.value = "Error deleting workout: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
