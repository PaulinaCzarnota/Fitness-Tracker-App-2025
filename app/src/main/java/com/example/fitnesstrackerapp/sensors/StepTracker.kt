package com.example.fitnesstrackerapp.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Step tracking utility class for monitoring user steps using device sensors.
 *
 * Responsibilities:
 * - Monitor step counter and step detector sensors
 * - Provide real-time step count updates
 * - Calculate distance and calories based on steps
 * - Handle sensor availability and permissions
 */

class StepTracker(
    private val context: Context
) : SensorEventListener {

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val stepCounterSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    private val stepDetectorSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    }

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _totalSteps = MutableStateFlow(0L)
    val totalSteps: StateFlow<Long> = _totalSteps.asStateFlow()

    private var initialStepCount = 0L
    private var sessionStepCount = 0
    private var isInitialized = false

    companion object {
        private const val STEP_LENGTH_METERS = 0.76f
        private const val CALORIES_PER_STEP = 0.04f
    }

    /**
     * Checks if step tracking sensors are available on the device.
     *
     * @return true if step tracking is supported
     */
    fun isStepTrackingSupported(): Boolean {
        return stepCounterSensor != null || stepDetectorSensor != null
    }

    /**
     * Starts step tracking by registering sensor listeners.
     *
     * @return true if tracking started successfully
     */
    fun startTracking(): Boolean {
        if (!isStepTrackingSupported()) {
            return false
        }

        var success = false

        // Prefer step counter sensor for accuracy
        stepCounterSensor?.let { sensor ->
            success = sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        // Fallback to step detector if step counter is not available
        if (!success && stepDetectorSensor != null) {
            success = sensorManager.registerListener(
                this,
                stepDetectorSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        if (success) {
            _isTracking.value = true
        }

        return success
    }

    /**
     * Stops step tracking by unregistering sensor listeners.
     */
    fun stopTracking() {
        sensorManager.unregisterListener(this)
        _isTracking.value = false
    }

    /**
     * Resets the session step count to zero.
     */
    fun resetSessionSteps() {
        sessionStepCount = 0
        _stepCount.value = 0
    }

    /**
     * Gets the current session step count.
     *
     * @return Number of steps in current session
     */
    fun getSessionStepCount(): Int {
        return sessionStepCount
    }

    /**
     * Calculates distance based on step count.
     *
     * @param steps Number of steps
     * @return Distance in meters
     */
    fun calculateDistance(steps: Int): Float {
        return steps * STEP_LENGTH_METERS
    }

    /**
     * Calculates distance in kilometers based on step count.
     *
     * @param steps Number of steps
     * @return Distance in kilometers
     */
    fun calculateDistanceKm(steps: Int): Float {
        return calculateDistance(steps) / 1000f
    }

    /**
     * Estimates calories burned based on step count.
     *
     * @param steps Number of steps
     * @return Estimated calories burned
     */
    fun calculateCalories(steps: Int): Float {
        return steps * CALORIES_PER_STEP
    }

    /**
     * Gets step tracking statistics.
     *
     * @return StepStats object with current statistics
     */
    fun getStepStats(): StepStats {
        return StepStats(
            sessionSteps = sessionStepCount,
            totalSteps = _totalSteps.value,
            sessionDistance = calculateDistanceKm(sessionStepCount),
            sessionCalories = calculateCalories(sessionStepCount),
            isTracking = _isTracking.value
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    val totalStepsFromSensor = sensorEvent.values[0].toLong()
                    _totalSteps.value = totalStepsFromSensor

                    if (!isInitialized) {
                        initialStepCount = totalStepsFromSensor
                        isInitialized = true
                    }

                    sessionStepCount = (totalStepsFromSensor - initialStepCount).toInt()
                    _stepCount.value = sessionStepCount
                }

                Sensor.TYPE_STEP_DETECTOR -> {
                    // Step detector triggers once per step
                    sessionStepCount++
                    _stepCount.value = sessionStepCount
                    _totalSteps.value = _totalSteps.value + 1
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
        when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                // High accuracy - optimal conditions
            }
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                // Medium accuracy - acceptable conditions
            }
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                // Low accuracy - poor conditions
            }
            SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                // Unreliable - sensor data cannot be trusted
            }
        }
    }

    /**
     * Manually add steps (for testing or manual entry).
     *
     * @param steps Number of steps to add
     */
    fun addManualSteps(steps: Int) {
        sessionStepCount += steps
        _stepCount.value = sessionStepCount
        _totalSteps.value = _totalSteps.value + steps
    }

    /**
     * Sets a custom step length for more accurate distance calculations.
     *
     * @param stepLengthMeters Step length in meters
     */
    fun setStepLength(stepLengthMeters: Float) {
        // This could be stored in preferences for personalized calculations
        // For now, we'll use the default constant
    }
}

/**
 * Data class representing step tracking statistics.
 */
data class StepStats(
    val sessionSteps: Int,
    val totalSteps: Long,
    val sessionDistance: Float, // in kilometers
    val sessionCalories: Float,
    val isTracking: Boolean
)
