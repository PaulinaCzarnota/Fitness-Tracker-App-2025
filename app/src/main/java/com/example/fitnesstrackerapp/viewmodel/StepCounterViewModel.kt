package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel to hold and manage step count data.
 * Allows UI to observe step count changes in a lifecycle-aware manner.
 */
class StepCounterViewModel : ViewModel() {

    // Backing property for step count LiveData
    private val _stepCount = MutableLiveData<Int>(0)

    /**
     * Public immutable LiveData exposing the current step count.
     */
    val stepCount: LiveData<Int> = _stepCount

    /**
     * Updates the current step count.
     * Typically called from the sensor event listener or service.
     * @param steps The latest step count value.
     */
    fun updateStepCount(steps: Int) {
        _stepCount.postValue(steps)
    }

    /**
     * Resets the step count to zero.
     * Can be called to start a new count cycle or clear data.
     */
    fun resetStepCount() {
        _stepCount.postValue(0)
    }
}
