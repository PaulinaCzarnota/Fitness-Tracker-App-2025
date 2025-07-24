package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * StepCounterViewModel.kt
 *
 * ViewModel responsible for tracking and updating the user's step count.
 * It provides lifecycle-aware observable data for Compose UI.
 */
class StepCounterViewModel : ViewModel() {

    // Backing field for internal step count state
    private val _stepCount = MutableLiveData<Int>(0)

    /**
     * Public LiveData exposing the current step count.
     * UI observes this for real-time updates.
     */
    val stepCount: LiveData<Int> = _stepCount

    /**
     * Updates the step count value.
     * Typically called from a step sensor listener or foreground service.
     *
     * @param steps The new step count value.
     */
    fun updateStepCount(steps: Int) {
        _stepCount.postValue(steps)
    }

    /**
     * Resets the step count to zero.
     * Can be triggered by a user action (e.g., "Reset" button).
     */
    fun resetStepCount() {
        _stepCount.postValue(0)
    }
}
