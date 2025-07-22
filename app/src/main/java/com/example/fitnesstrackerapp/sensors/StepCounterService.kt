package com.example.fitnesstrackerapp.sensors

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log

/**
 * Background service that continuously listens to step counter sensor updates.
 * It can be used to collect step data even when the app is not in the foreground.
 */
class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var initialStepCount = -1
    private var currentSteps = 0

    override fun onCreate() {
        super.onCreate()

        // Initialize sensor manager and fetch the step counter sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Register listener if sensor is available
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: Log.e("StepCounterService", "Step counter sensor not available")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()

            // Set initial step count on first reading
            if (initialStepCount == -1) {
                initialStepCount = totalSteps
            }

            // Calculate steps since service started
            currentSteps = totalSteps - initialStepCount

            Log.d("StepCounterService", "Steps since start: $currentSteps")

            // Optionally: broadcast the step count to UI or store in local database
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed for this use case
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Service is not bound, return null
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister sensor listener to prevent memory leaks
        sensorManager.unregisterListener(this)
    }
}
