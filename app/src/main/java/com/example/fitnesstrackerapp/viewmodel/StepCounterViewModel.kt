package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * StepCounterViewModel
 *
 * ViewModel that holds and manages the current step count in a lifecycle-aware manner.
 * This ViewModel allows the UI to observe real-time updates using StateFlow.
 * Step count data is typically updated by a BroadcastReceiver receiving
 * data from a foreground StepCounterService.
 */
class StepCounterViewModel : ViewModel() {

    // Mutable state for internal use — step count initialized to 0
    private val _stepCount = MutableStateFlow(0)

    /**
     * Public immutable view of step count for external UI observers.
     * Composable UIs observe this using `collectAsStateWithLifecycle()`.
     */
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    /**
     * Update the step count with the latest value.
     *
     * Typically called from a BroadcastReceiver or a service listener.
     *
     * @param steps New step count value, received from step sensor or service.
     */
    fun updateStepCount(steps: Int) {
        _stepCount.value = steps
    }

    /**
     * Resets the current step count to 0.
     *
     * Typically triggered by a UI "Reset" button to start a new daily count.
     */
    fun resetStepCount() {
        _stepCount.value = 0
    }
}
