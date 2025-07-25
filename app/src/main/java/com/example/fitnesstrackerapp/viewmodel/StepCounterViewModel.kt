package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * StepCounterViewModel
 *
 * ViewModel responsible for managing the user's step count in a lifecycle-aware manner.
 * Provides a StateFlow for Compose UI to observe updates efficiently and safely.
 */
class StepCounterViewModel : ViewModel() {

    // Internal mutable flow holding the current step count
    private val _stepCount = MutableStateFlow(0)

    /**
     * Public immutable StateFlow to expose step count to observers.
     * Collected safely in Compose using collectAsStateWithLifecycle().
     */
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    /**
     * Updates the step count to a new value.
     * Typically triggered from a BroadcastReceiver observing STEP_UPDATE.
     *
     * @param steps The number of steps to display.
     */
    fun updateStepCount(steps: Int) {
        _stepCount.value = steps
    }

    /**
     * Resets the step count to zero.
     * This can be called from a reset button in the StepTrackerScreen UI.
     */
    fun resetStepCount() {
        _stepCount.value = 0
    }
}
