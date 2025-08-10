package com.example.fitnesstrackerapp.ui.viewmodel

/**
 * Step Dashboard ViewModel
 *
 * Manages the connection between UI Dashboard and StepCounterService.
 * Provides real-time step data binding and service lifecycle management.
 */

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.model.StepData
import com.example.fitnesstrackerapp.sensors.StepCounterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing step tracking service connection and real-time step data
 */
class StepDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private var stepService: StepCounterService? = null
    private var isServiceBound = false

    // UI State
    private val _stepData = MutableStateFlow(
        StepData(
            steps = 0,
            goal = 10000,
            progress = 0f,
            distance = 0f,
            calories = 0f,
            isTracking = false,
        ),
    )
    val stepData: StateFlow<StepData> = _stepData.asStateFlow()

    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Service connection callback
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG, "StepCounterService connected")
            val binder = service as StepCounterService.StepServiceBinder
            stepService = binder.getService()
            isServiceBound = true
            _isServiceConnected.value = true

            // Start observing step data
            observeStepData()

            // Get initial step data
            updateStepData()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.d(TAG, "StepCounterService disconnected")
            stepService = null
            isServiceBound = false
            _isServiceConnected.value = false
        }
    }

    companion object {
        private const val TAG = "StepDashboardViewModel"
    }

    init {
        startAndBindService()
    }

    /**
     * Starts the step counter service and binds to it
     */
    private fun startAndBindService() {
        val context = getApplication<Application>().applicationContext

        try {
            // Start the service
            val serviceIntent = Intent(context, StepCounterService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            // Bind to the service
            val bindIntent = Intent(context, StepCounterService::class.java)
            val success = context.bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE)

            if (!success) {
                _errorMessage.value = "Failed to bind to step tracking service"
                Log.e(TAG, "Failed to bind to StepCounterService")
            } else {
                Log.d(TAG, "Service binding initiated")
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error starting step tracking service: ${e.message}"
            Log.e(TAG, "Error starting/binding service", e)
        }
    }

    /**
     * Observes real-time step data from the service
     */
    private fun observeStepData() {
        viewModelScope.launch {
            stepService?.let { service ->
                // Observe current steps
                launch {
                    service.currentSteps.collect { steps ->
                        updateStepDataField { it.copy(steps = steps) }
                    }
                }

                // Observe daily goal
                launch {
                    service.dailyGoal.collect { goal ->
                        updateStepDataField { it.copy(goal = goal) }
                    }
                }

                // Observe progress
                launch {
                    service.stepProgress.collect { progress ->
                        updateStepDataField { it.copy(progress = progress) }
                    }
                }

                // Observe calories burned
                launch {
                    service.caloriesBurned.collect { calories ->
                        updateStepDataField { it.copy(calories = calories) }
                    }
                }

                // Observe distance
                launch {
                    service.distanceMeters.collect { distance ->
                        updateStepDataField { it.copy(distance = distance) }
                    }
                }
            }
        }
    }

    /**
     * Updates step data using the provided transform function
     */
    private fun updateStepDataField(transform: (StepData) -> StepData) {
        _stepData.value = transform(_stepData.value)
    }

    /**
     * Gets current step data from service
     */
    private fun updateStepData() {
        stepService?.let { service ->
            val currentData = service.getCurrentStepData()
            _stepData.value = currentData
            Log.d(TAG, "Step data updated: $currentData")
        }
    }

    /**
     * Sets a new daily step goal
     */
    fun setDailyGoal(newGoal: Int) {
        stepService?.setDailyGoal(newGoal)
        Log.d(TAG, "Daily goal set to: $newGoal")
    }

    /**
     * Resets daily steps (for testing/admin purposes)
     */
    fun resetDailySteps() {
        stepService?.resetDailySteps()
        Log.d(TAG, "Daily steps reset")
    }

    /**
     * Gets formatted step progress string
     */
    fun getFormattedProgress(): String {
        val data = _stepData.value
        return "${data.steps} / ${data.goal} steps"
    }

    /**
     * Gets formatted distance string
     */
    fun getFormattedDistance(): String {
        val distanceKm = _stepData.value.distance / 1000f
        return if (distanceKm >= 1.0f) {
            "%.2f km".format(distanceKm)
        } else {
            "%.0f m".format(_stepData.value.distance)
        }
    }

    /**
     * Gets formatted calories string
     */
    fun getFormattedCalories(): String {
        return "%.0f kcal".format(_stepData.value.calories)
    }

    /**
     * Gets progress percentage as int
     */
    fun getProgressPercentage(): Int {
        return _stepData.value.progress.toInt()
    }

    /**
     * Checks if daily goal is achieved
     */
    fun isGoalAchieved(): Boolean {
        val data = _stepData.value
        return data.steps >= data.goal
    }

    /**
     * Gets remaining steps to goal
     */
    fun getRemainingSteps(): Int {
        val data = _stepData.value
        return (data.goal - data.steps).coerceAtLeast(0)
    }

    /**
     * Gets activity level based on step count
     */
    fun getActivityLevel(): String {
        val steps = _stepData.value.steps
        return when {
            steps >= 12000 -> "Highly Active"
            steps >= 10000 -> "Active"
            steps >= 7500 -> "Somewhat Active"
            steps >= 5000 -> "Low Active"
            else -> "Sedentary"
        }
    }

    /**
     * Clears error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Manually refresh step data
     */
    fun refreshStepData() {
        updateStepData()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared, unbinding service")

        // Unbind service
        if (isServiceBound) {
            try {
                getApplication<Application>().applicationContext.unbindService(serviceConnection)
                isServiceBound = false
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding service", e)
            }
        }
    }
}
