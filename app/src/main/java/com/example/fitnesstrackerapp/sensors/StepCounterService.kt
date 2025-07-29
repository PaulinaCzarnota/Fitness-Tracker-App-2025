package com.example.fitnesstrackerapp.sensors

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fitnesstrackerapp.R

/**
 * StepCounterService
 *
 * Foreground service that listens to the step counter sensor and broadcasts step updates.
 */
class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private var initialStepCount: Int = -1
    private var currentSteps: Int = 0

    companion object {
        const val STEP_UPDATE_ACTION = "com.example.fitnesstrackerapp.STEP_UPDATE"
        const val EXTRA_STEPS = "steps"
        const val ACTION_STOP_SERVICE = "STOP_STEP_SERVICE"
        private const val CHANNEL_ID = "step_counter_channel"
        private const val NOTIFICATION_ID = 101
    }

    override fun onCreate() {
        super.onCreate()

        // Get sensor manager and register for step counter sensor updates
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("StepCounterService", "Step sensor registered.")
        } else {
            Log.e("StepCounterService", "Step sensor not available.")
            stopSelf()
        }

        // Start service as a foreground service with a persistent notification
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle custom intent to stop the service externally
        if (intent?.action == ACTION_STOP_SERVICE) {
            Log.d("StepCounterService", "Received STOP action.")
            stopSelf()
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && event.values.isNotEmpty()) {
            val totalSteps = event.values[0].toInt()

            if (initialStepCount == -1) {
                initialStepCount = totalSteps
            }

            currentSteps = totalSteps - initialStepCount
            Log.d("StepCounterService", "Steps since start: $currentSteps")

            // Send broadcast with updated step count
            val broadcast = Intent(STEP_UPDATE_ACTION).apply {
                putExtra(EXTRA_STEPS, currentSteps)
            }
            sendBroadcast(broadcast)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used in this context
    }

    override fun onBind(intent: Intent?): IBinder? {
        // This is a started service, not a bound service
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the sensor listener when service is stopped
        sensorManager.unregisterListener(this)
        Log.d("StepCounterService", "Sensor unregistered and service destroyed.")
    }

    /**
     * Creates a persistent notification required by Android O+ for background services.
     */
    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks your steps in the background"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking Steps")
            .setContentText("Step tracking is active")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }
}
