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
 * A background Android Service that listens to the TYPE_STEP_COUNTER sensor.
 * Tracks the number of steps taken since the service started.
 * Can be extended to send broadcasts or save steps persistently.
 */
class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private var initialStepCount: Int = -1
    private var currentSteps: Int = 0

    override fun onCreate() {
        super.onCreate()

        // Obtain SensorManager and Step Counter Sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("StepCounterService", "Step counter sensor registered.")
        } else {
            Log.e("StepCounterService", "Step Counter Sensor not available on this device.")
        }
    }

    /**
     * Called when a new step count event is received.
     * Calculates relative step count since the service started.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && event.values.isNotEmpty()) {
            val totalSteps = event.values[0].toInt()

            // Set baseline step count on first trigger
            if (initialStepCount == -1) {
                initialStepCount = totalSteps
            }

            currentSteps = totalSteps - initialStepCount

            Log.d("StepCounterService", "Steps since start: $currentSteps")

            // Optional: Broadcast steps to ViewModel/Activity
            val intent = Intent("STEP_UPDATE")
            intent.putExtra("steps", currentSteps)
            sendBroadcast(intent)

            // Optional TODO: Save to Room or DataStore
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used, but required override
    }

    override fun onBind(intent: Intent?): IBinder? {
        // This is a started service (not bound), so return null
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Important: Unregister listener to avoid memory leaks
        sensorManager.unregisterListener(this)
        Log.d("StepCounterService", "Sensor listener unregistered.")
    }
}
