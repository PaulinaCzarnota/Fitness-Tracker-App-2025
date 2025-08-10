package com.example.fitnesstrackerapp.sensors

/**
 * Real-time Step Tracking Service
 *
 * Enhanced foreground service that provides:
 * - Real-time step counting using Sensor.TYPE_STEP_COUNTER with fallback pedometer algorithm
 * - Live notification showing today's steps
 * - Periodic database persistence to StepEntry table
 * - Service binding for UI Dashboard fragment integration
 * - Automatic goal tracking and achievement notifications
 */

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fitnesstrackerapp.MainActivity
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.Step
import com.example.fitnesstrackerapp.data.model.StepData
import com.example.fitnesstrackerapp.repository.StepRepository
import com.example.fitnesstrackerapp.util.StepUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import kotlin.math.abs

class StepCounterService : Service(), SensorEventListener {
    // Sensor components
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    // Step tracking variables
    private var totalStepsFromSensor = 0L
    private var sessionStartSteps = 0L
    private var todaySteps = 0
    private var isStepCounterInitialized = false

    // Fallback pedometer algorithm variables
    private var lastAcceleration = floatArrayOf(0f, 0f, 0f)
    private var lastAccelerationMagnitude = 0f
    private var stepThreshold = 11.0f
    private var lastStepTime = 0L
    private val minStepInterval = 300L // Minimum time between steps in milliseconds

    // Service components
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var stepRepository: StepRepository
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var notificationManager: NotificationManager

    // Live data flows for UI binding
    private val _currentSteps = MutableStateFlow(0)
    val currentSteps: StateFlow<Int> = _currentSteps.asStateFlow()

    private val _dailyGoal = MutableStateFlow(10000)
    val dailyGoal: StateFlow<Int> = _dailyGoal.asStateFlow()

    private val _stepProgress = MutableStateFlow(0f)
    val stepProgress: StateFlow<Float> = _stepProgress.asStateFlow()

    private val _caloriesBurned = MutableStateFlow(0f)
    val caloriesBurned: StateFlow<Float> = _caloriesBurned.asStateFlow()

    private val _distanceMeters = MutableStateFlow(0f)
    val distanceMeters: StateFlow<Float> = _distanceMeters.asStateFlow()

    // Service binder for UI Dashboard integration
    private val binder = StepServiceBinder()

    // Periodic database save
    private val saveToDbRunnable = object : Runnable {
        override fun run() {
            saveStepsToDatabase()
            handler.postDelayed(this, DATABASE_SAVE_INTERVAL)
        }
    }

    inner class StepServiceBinder : Binder() {
        fun getService(): StepCounterService = this@StepCounterService
    }

    companion object {
        private const val TAG = "StepCounterService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "step_counter_channel"
        private const val CHANNEL_NAME = "Step Counter"
        private const val DATABASE_SAVE_INTERVAL = 60000L // Save every minute
        private const val STEP_LENGTH_METERS = 0.76f
        private const val CALORIES_PER_STEP = 0.04f

        const val ACTION_STEP_UPDATE = "com.example.fitnesstrackerapp.STEP_UPDATE"
        const val EXTRA_STEPS = "extra_steps"
        const val EXTRA_GOAL = "extra_goal"
        const val EXTRA_PROGRESS = "extra_progress"
    }

    override fun onCreate() {
        super.onCreate()
        initializeServiceComponents()
        setupSensorManager()
        createNotificationChannel()
        loadTodayStepsFromDatabase()
        Log.d(TAG, "StepCounterService created")
    }

    private fun initializeServiceComponents() {
        // Initialize repository and shared preferences
        val database = AppDatabase.getInstance(this)
        stepRepository = StepRepository(database.stepDao())
        sharedPreferences = getSharedPreferences("fitness_tracker_prefs", MODE_PRIVATE)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Load daily goal from preferences
        val goal = sharedPreferences.getInt("daily_step_goal", 10000)
        _dailyGoal.value = goal
    }

    private fun setupSensorManager() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Primary: Step counter sensor (preferred for accuracy)
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Secondary: Step detector sensor (fallback)
        stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        // Tertiary: Accelerometer for custom pedometer algorithm (fallback)
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        Log.d(
            TAG,
            "Sensors available: Counter=${stepCounterSensor != null}, " +
                "Detector=${stepDetectorSensor != null}, Accelerometer=${accelerometerSensor != null}",
        )
    }

    private fun loadTodayStepsFromDatabase() {
        serviceScope.launch {
            try {
                val userId = getCurrentUserId()
                stepRepository.getTodaysSteps(userId).collect { todaysStep ->
                    todaysStep?.let {
                        todaySteps = it.count
                        updateStepData(todaySteps)
                        Log.d(TAG, "Loaded today's steps from database: $todaySteps")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading today's steps from database", e)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        startForegroundService()
        registerSensorListeners()
        startPeriodicDatabaseSave()
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = createNotification(todaySteps, _dailyGoal.value)
        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "Foreground service started with notification")
    }

    private fun registerSensorListeners() {
        var registered = false

        // Try to register step counter first (most accurate)
        stepCounterSensor?.let { sensor ->
            val success = sensorManager?.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL,
            ) ?: false
            if (success) {
                registered = true
                Log.d(TAG, "Step counter sensor registered")
            }
        }

        // Fallback to step detector
        if (!registered) {
            stepDetectorSensor?.let { sensor ->
                val success = sensorManager?.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL,
                ) ?: false
                if (success) {
                    registered = true
                    Log.d(TAG, "Step detector sensor registered")
                }
            }
        }

        // Final fallback to accelerometer with custom pedometer algorithm
        if (!registered) {
            accelerometerSensor?.let { sensor ->
                val success = sensorManager?.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME,
                ) ?: false
                if (success) {
                    Log.d(TAG, "Accelerometer sensor registered for fallback pedometer")
                }
            }
        }

        if (!registered) {
            Log.w(TAG, "No step sensors available - step tracking disabled")
        }
    }

    private fun startPeriodicDatabaseSave() {
        handler.post(saveToDbRunnable)
        Log.d(TAG, "Periodic database save started")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            when (sensorEvent.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    handleStepCounterSensor(sensorEvent)
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    handleStepDetectorSensor()
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    handleAccelerometerSensor(sensorEvent)
                }
            }
        }
    }

    private fun handleStepCounterSensor(event: SensorEvent) {
        val totalStepsFromSensor = event.values[0].toLong()

        if (!isStepCounterInitialized) {
            // Initialize with existing database steps
            sessionStartSteps = totalStepsFromSensor - todaySteps
            isStepCounterInitialized = true
            Log.d(TAG, "Step counter initialized. Total: $totalStepsFromSensor, Today: $todaySteps")
        }

        // Calculate today's steps
        val newTodaySteps = (totalStepsFromSensor - sessionStartSteps).toInt()
        if (newTodaySteps != todaySteps && newTodaySteps >= 0) {
            todaySteps = newTodaySteps
            updateStepData(todaySteps)
        }
    }

    private fun handleStepDetectorSensor() {
        // Step detector triggers once per step
        todaySteps++
        updateStepData(todaySteps)
    }

    private fun handleAccelerometerSensor(event: SensorEvent) {
        // Fallback pedometer algorithm using accelerometer
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate acceleration magnitude
        val acceleration = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        // Detect step using threshold and timing
        val currentTime = System.currentTimeMillis()
        val accelerationDelta = abs(acceleration - lastAccelerationMagnitude)

        if (accelerationDelta > stepThreshold &&
            currentTime - lastStepTime > minStepInterval
        ) {
            todaySteps++
            updateStepData(todaySteps)
            lastStepTime = currentTime
            Log.d(TAG, "Step detected via accelerometer. Total: $todaySteps")
        }

        lastAccelerationMagnitude = acceleration
    }

    private fun updateStepData(steps: Int) {
        // Update StateFlows for UI binding
        _currentSteps.value = steps

        val distance = StepUtils.calculateDistance(steps)
        val calories = StepUtils.calculateCalories(steps)
        val progress = StepUtils.calculateProgress(steps, _dailyGoal.value)

        _distanceMeters.value = distance
        _caloriesBurned.value = calories
        _stepProgress.value = progress

        // Update notification
        updateNotification(steps, _dailyGoal.value)

        // Broadcast to UI
        broadcastStepUpdate(steps, _dailyGoal.value, progress)

        // Check for goal achievement
        checkGoalAchievement(steps, _dailyGoal.value)
    }

    private fun saveStepsToDatabase() {
        serviceScope.launch {
            try {
                if (todaySteps >= 0) {
                    val userId = getCurrentUserId()
                    val today = getTodayDateAtMidnight()

                    val stepEntry = Step(
                        userId = userId,
                        count = todaySteps,
                        goal = _dailyGoal.value,
                        date = today,
                        caloriesBurned = _caloriesBurned.value,
                        distanceMeters = _distanceMeters.value,
                        activeMinutes = StepUtils.estimateActiveMinutes(todaySteps),
                        createdAt = Date(),
                        updatedAt = Date(),
                    )

                    stepRepository.insertStep(stepEntry)
                    Log.d(TAG, "Steps saved to database: $todaySteps")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving steps to database", e)
            }
        }
    }

    private fun getTodayDateAtMidnight(): Date {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.time
    }

    private fun updateNotification(steps: Int, goal: Int) {
        val notification = createNotification(steps, goal)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun broadcastStepUpdate(steps: Int, goal: Int, progress: Float) {
        val intent = Intent(ACTION_STEP_UPDATE).apply {
            setPackage(packageName) // Explicit intent for security
            putExtra(EXTRA_STEPS, steps)
            putExtra(EXTRA_GOAL, goal)
            putExtra(EXTRA_PROGRESS, progress)
        }
        sendBroadcast(intent)
    }

    private fun checkGoalAchievement(steps: Int, goal: Int) {
        val wasGoalAchieved = sharedPreferences.getBoolean("goal_achieved_today", false)
        val isGoalAchieved = steps >= goal

        if (isGoalAchieved && !wasGoalAchieved) {
            // Goal just achieved - show celebration notification
            showGoalAchievementNotification(steps, goal)
            sharedPreferences.edit().putBoolean("goal_achieved_today", true).apply()
            Log.d(TAG, "Daily step goal achieved! Steps: $steps, Goal: $goal")
        }
    }

    private fun showGoalAchievementNotification(steps: Int, goal: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎉 Goal Achieved!")
            .setContentText("Congratulations! You've reached your daily goal of $goal steps!")
            .setSmallIcon(android.R.drawable.star_on)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: ${sensor?.name}, accuracy: $accuracy")
        when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                // Optimal conditions for step detection
            }
            SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                // May need to adjust algorithm parameters
                stepThreshold = stepThreshold * 0.8f // Make more sensitive
            }
        }
    }

    private fun getCurrentUserId(): Long {
        return sharedPreferences.getLong("current_user_id", 1L)
    }

    private fun createNotification(steps: Int, goal: Int): Notification {
        val progress = if (goal > 0) (steps * 100) / goal else 0
        val progressText = "$steps / $goal steps ($progress%)"

        // Create intent to open app when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Counter Active")
            .setContentText(progressText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setProgress(goal, steps, false)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Shows live step count and daily progress"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroying")

        // Save current steps before stopping
        saveStepsToDatabase()

        // Clean up resources
        handler.removeCallbacks(saveToDbRunnable)
        sensorManager?.unregisterListener(this)
        serviceScope.launch {
            // Final cleanup if needed
        }

        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "Service bound")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Service unbound")
        return super.onUnbind(intent)
    }

    // Public methods for UI Dashboard binding
    fun getCurrentStepData(): StepData {
        return StepData(
            steps = _currentSteps.value,
            goal = _dailyGoal.value,
            progress = _stepProgress.value,
            distance = _distanceMeters.value,
            calories = _caloriesBurned.value,
            isTracking = true,
        )
    }

    fun setDailyGoal(newGoal: Int) {
        _dailyGoal.value = newGoal
        sharedPreferences.edit().putInt("daily_step_goal", newGoal).apply()
        updateStepData(todaySteps) // Recalculate progress
        Log.d(TAG, "Daily goal updated to: $newGoal")
    }

    fun resetDailySteps() {
        todaySteps = 0
        isStepCounterInitialized = false
        sharedPreferences.edit().putBoolean("goal_achieved_today", false).apply()
        updateStepData(todaySteps)
        Log.d(TAG, "Daily steps reset")
    }
}
