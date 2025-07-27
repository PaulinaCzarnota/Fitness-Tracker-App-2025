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
 * StepCounterService
 *
 * Foreground service that listens to step counter sensor data.
 * Sends broadcasts with relative step count updates every time a step is detected.
 *
 * ⚠️ Note: Works only on devices with a built-in step counter sensor.
 */
class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    // Used to establish baseline on service start
    private var initialStepCount: Int = -1

    // Relative step count since service began
    private var currentSteps: Int = 0

    /**
     * Called when the service is created (once per service lifecycle).
     * Initializes sensor manager and registers sensor listener.
     */
    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor != null) {
            // Register for sensor updates
            sensorManager.registerListener(
                this,
                stepSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            Log.d("StepCounterService", "Step sensor registered.")
        } else {
            Log.e("StepCounterService", "Step Counter Sensor not available on this device.")
        }
    }

    /**
     * Called when sensor values change.
     * Handles step count updates and broadcasts the current step count.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && event.values.isNotEmpty()) {
            val totalSteps = event.values[0].toInt()

            // Record initial step count as baseline
            if (initialStepCount == -1) {
                initialStepCount = totalSteps
            }

            // Calculate relative steps since this service started
            currentSteps = totalSteps - initialStepCount

            Log.d("StepCounterService", "Steps since start: $currentSteps")

            // Send broadcast to UI/ViewModel
            val broadcast = Intent("STEP_UPDATE").apply {
                putExtra("steps", currentSteps)
            }
            sendBroadcast(broadcast)

            // Optional: Store step count persistently here (e.g. using DataStore or Room)
        }
    }

    /**
     * Required method but unused for this service.
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No accuracy handling needed
    }

    /**
     * Required for services — returns null since this is an unbound service.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Called when the service is being stopped. Cleans up listeners.
     */
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        Log.d("StepCounterService", "Step sensor listener unregistered.")
    }
}
