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
 * A background service that listens for step count updates using the device's step counter sensor.
 * This service continues collecting data even if the app is in the background.
 */
class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    // Initial total steps at the time the service starts
    private var initialStepCount = -1

    // Relative step count since the service started
    private var currentSteps = 0

    override fun onCreate() {
        super.onCreate()

        // Initialize the sensor manager and get the default step counter sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Register listener if sensor is available
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("StepCounterService", "Step counter sensor registered")
        } ?: Log.e("StepCounterService", "Step Counter Sensor not available on this device.")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && event.values.isNotEmpty()) {
            val totalSteps = event.values[0].toInt()

            // Record initial step count on first reading
            if (initialStepCount == -1) {
                initialStepCount = totalSteps
            }

            // Compute steps taken since service started
            currentSteps = totalSteps - initialStepCount

            Log.d("StepCounterService", "Steps since service started: $currentSteps")

            // TODO: Broadcast step count or persist it if needed
            // Example: sendBroadcast(Intent("STEP_UPDATE").putExtra("steps", currentSteps))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used, but required for interface
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // This is a started service, not a bound service
    }

    override fun onDestroy() {
        super.onDestroy()
        // Always unregister the sensor listener to avoid memory leaks
        sensorManager.unregisterListener(this)
        Log.d("StepCounterService", "Step counter sensor unregistered")
    }
}
