package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * StepCounterViewModel
 *
 * A lifecycle-aware [ViewModel] that manages step count data.
 * Exposes a [StateFlow] for real-time UI updates using Jetpack Compose.
 *
 * This ViewModel is intended to be updated by a step sensor listener,
 * such as a foreground service or BroadcastReceiver receiving sensor events.
 */
class StepCounterViewModel : ViewModel() {

    /**
     * Internal mutable state for the current step count.
     * Starts at 0 and is updated from sensor data.
     */
    private val _stepCount = MutableStateFlow(0)

    /**
     * Public read-only state for observing the current step count in the UI.
     * UI should observe this using `collectAsStateWithLifecycle()` in Compose.
     */
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    /**
     * Updates the current step count.
     * Should be called with new step data from a sensor or service.
     *
     * @param steps The new step count value.
     */
    fun updateStepCount(steps: Int) {
        _stepCount.value = steps
    }

    /**
     * Resets the step count back to zero.
     * Typically triggered by a "Reset" action in the UI.
     */
    fun resetStepCount() {
        _stepCount.value = 0
    }
}
