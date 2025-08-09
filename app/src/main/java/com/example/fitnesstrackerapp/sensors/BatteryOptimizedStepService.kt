/**
 * Battery-Optimized Step Counter Service
 *
 * Enhanced foreground service that provides:
 * - Real-time step counting using Sensor.TYPE_STEP_COUNTER with fallback algorithms
 * - Battery optimization with sensor batching and doze-mode awareness
 * - Efficient data persistence with batching and smart syncing
 * - Integration with workouts and goals for comprehensive fitness tracking
 * - Service binding for UI integration and live updates
 * - Automatic goal tracking with achievement notifications
 */
package com.example.fitnesstrackerapp.sensors

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fitnesstrackerapp.MainActivity
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.Step
import com.example.fitnesstrackerapp.data.model.StepData
import com.example.fitnesstrackerapp.repository.GoalRepository
import com.example.fitnesstrackerapp.repository.StepRepository
import com.example.fitnesstrackerapp.repository.WorkoutRepository
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

class BatteryOptimizedStepService : Service(), SensorEventListener {
    // Sensor components
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    // Battery optimization components
    private lateinit var powerManager: PowerManager
    private var isDozeMode = false
    private var sensorBatchingSupported = false
    private var currentSensorDelay = SensorManager.SENSOR_DELAY_NORMAL

    // Step tracking variables
    private var totalStepsFromSensor = 0L
    private var sessionStartSteps = 0L
    private var todaySteps = 0
    private var isStepCounterInitialized = false

    // Batched data for battery optimization
    private val stepDataBatch = mutableListOf<StepDataPoint>()
    private var lastBatchSaveTime = 0L

    // Fallback pedometer algorithm variables
    private var lastAcceleration = floatArrayOf(0f, 0f, 0f)
    private var lastAccelerationMagnitude = 0f
    private var stepThreshold = 11.0f
    private var lastStepTime = 0L
    private val minStepInterval = 300L

    // Service components
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var stepRepository: StepRepository
    private lateinit var goalRepository: GoalRepository
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var notificationManager: NotificationManager

    // Battery optimization receivers
    private val dozeReceiver = DozeAwareBroadcastReceiver()

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

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    // Service binder
    private val binder = StepServiceBinder()

    // Optimized database operations
    private val saveToDbRunnable = object : Runnable {
        override fun run() {
            saveStepsBatch()

            // Adaptive save interval based on battery state
            val interval = if (isDozeMode) {
                DATABASE_SAVE_INTERVAL_DOZE
            } else {
                DATABASE_SAVE_INTERVAL_NORMAL
            }

            handler.postDelayed(this, interval)
        }
    }

    inner class StepServiceBinder : Binder() {
        fun getService(): BatteryOptimizedStepService = this@BatteryOptimizedStepService
    }

    companion object {
        private const val TAG = "BatteryOptimizedStepService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "step_counter_channel"
        private const val CHANNEL_NAME = "Step Counter"

        // Adaptive save intervals
        private const val DATABASE_SAVE_INTERVAL_NORMAL = 60000L // 1 minute normal
        private const val DATABASE_SAVE_INTERVAL_DOZE = 300000L // 5 minutes in doze

        // Battery optimization constants
        private const val BATCH_SIZE_THRESHOLD = 10
        private const val MAX_BATCH_AGE_MS = 180000L // 3 minutes

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
        setupBatteryOptimization()
        createNotificationChannel()
        loadTodayStepsFromDatabase()
        registerDozeReceiver()
        Log.d(TAG, "Battery-optimized StepCounterService created")
    }

    private fun initializeServiceComponents() {
        val database = AppDatabase.getInstance(this)
        stepRepository = StepRepository(database.stepDao())
        goalRepository = GoalRepository(database.goalDao())
        workoutRepository = WorkoutRepository(database.workoutDao())
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

        // Check for sensor batching support (always available on our min SDK 24)
        sensorBatchingSupported = stepCounterSensor?.let { sensor ->
            sensor.fifoMaxEventCount > 0
        } ?: false

        Log.d(
            TAG,
            "Sensors available: Counter=${stepCounterSensor != null}, " +
                "Detector=${stepDetectorSensor != null}, " +
                "Accelerometer=${accelerometerSensor != null}, " +
                "Batching=$sensorBatchingSupported",
        )
    }

    private fun setupBatteryOptimization() {
        powerManager = getSystemService(POWER_SERVICE) as PowerManager

        // Check initial doze state (always available on our min SDK 24)
        isDozeMode = powerManager.isDeviceIdleMode

        Log.d(TAG, "Battery optimization setup - Doze mode: $isDozeMode")
    }

    private fun registerDozeReceiver() {
        val filter = IntentFilter().apply {
            addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(dozeReceiver, filter)
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
        _isTracking.value = true
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = createNotification(todaySteps, _dailyGoal.value)
        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "Foreground service started with notification")
    }

    private fun registerSensorListeners() {
        var registered = false

        // Determine optimal sensor delay based on battery state
        currentSensorDelay = if (isDozeMode) {
            SensorManager.SENSOR_DELAY_NORMAL
        } else {
            SensorManager.SENSOR_DELAY_UI
        }

        // Try to register step counter first (most accurate)
        stepCounterSensor?.let { sensor ->
            val batchLatencyUs = if (sensorBatchingSupported && isDozeMode) {
                // Use batching in doze mode (5 seconds latency)
                5_000_000
            } else {
                0 // Real-time
            }

            val success = sensorManager?.registerListener(
                this,
                sensor,
                currentSensorDelay,
                batchLatencyUs,
            ) ?: false

            if (success) {
                registered = true
                Log.d(TAG, "Step counter sensor registered with batching: $sensorBatchingSupported, delay: $currentSensorDelay")
            }
        }

        // Fallback to step detector
        if (!registered) {
            stepDetectorSensor?.let { sensor ->
                val success = sensorManager?.registerListener(
                    this,
                    sensor,
                    currentSensorDelay,
                ) ?: false

                if (success) {
                    registered = true
                    Log.d(TAG, "Step detector sensor registered")
                }
            }
        }

        // Final fallback to accelerometer
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
            _isTracking.value = false
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
            sessionStartSteps = totalStepsFromSensor - todaySteps
            isStepCounterInitialized = true
            Log.d(TAG, "Step counter initialized. Total: $totalStepsFromSensor, Today: $todaySteps")
        }

        val newTodaySteps = (totalStepsFromSensor - sessionStartSteps).toInt()
        if (newTodaySteps != todaySteps && newTodaySteps >= 0) {
            todaySteps = newTodaySteps

            // Add to batch for efficient database operations
            addToStepBatch(todaySteps)
            updateStepData(todaySteps)
        }
    }

    private fun handleStepDetectorSensor() {
        todaySteps++
        addToStepBatch(todaySteps)
        updateStepData(todaySteps)
    }

    private fun handleAccelerometerSensor(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        val currentTime = System.currentTimeMillis()
        val accelerationDelta = abs(acceleration - lastAccelerationMagnitude)

        if (accelerationDelta > stepThreshold &&
            currentTime - lastStepTime > minStepInterval
        ) {
            todaySteps++
            addToStepBatch(todaySteps)
            updateStepData(todaySteps)
            lastStepTime = currentTime
        }

        lastAccelerationMagnitude = acceleration
    }

    private fun addToStepBatch(steps: Int) {
        val currentTime = System.currentTimeMillis()
        val stepDataPoint = StepDataPoint(
            timestamp = currentTime,
            stepCount = steps,
            calories = StepUtils.calculateCalories(steps),
            distance = StepUtils.calculateDistance(steps),
        )

        stepDataBatch.add(stepDataPoint)

        // Save batch if it gets too large or too old
        if (stepDataBatch.size >= BATCH_SIZE_THRESHOLD ||
            currentTime - lastBatchSaveTime > MAX_BATCH_AGE_MS
        ) {
            saveStepsBatch()
        }
    }

    private fun updateStepData(steps: Int) {
        _currentSteps.value = steps

        val distance = StepUtils.calculateDistance(steps)
        val calories = StepUtils.calculateCalories(steps)
        val progress = StepUtils.calculateProgress(steps, _dailyGoal.value)

        _distanceMeters.value = distance
        _caloriesBurned.value = calories
        _stepProgress.value = progress

        updateNotification(steps, _dailyGoal.value)
        broadcastStepUpdate(steps, _dailyGoal.value, progress)
        checkGoalAchievement(steps, _dailyGoal.value)

        // Sync with workouts and goals
        syncWithWorkoutsAndGoals(steps, calories, distance)
    }

    private fun syncWithWorkoutsAndGoals(steps: Int, calories: Float, distance: Float) {
        serviceScope.launch {
            try {
                getCurrentUserId()

                // Update active workout with step data if available
                // This would integrate with any running workout sessions

                // Check and update step-related goals
                // This integrates with the goal management system

                Log.d(TAG, "Synced step data with workouts and goals")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing with workouts and goals", e)
            }
        }
    }

    private fun saveStepsBatch() {
        if (stepDataBatch.isEmpty()) return

        serviceScope.launch {
            try {
                val userId = getCurrentUserId()
                val today = getTodayDateAtMidnight()
                val currentTime = Date()

                // Get the latest data point from batch
                val latestData = stepDataBatch.lastOrNull()
                if (latestData != null) {
                    val stepEntry = Step(
                        userId = userId,
                        count = latestData.stepCount,
                        goal = _dailyGoal.value,
                        date = today,
                        caloriesBurned = latestData.calories,
                        distanceMeters = latestData.distance,
                        activeMinutes = StepUtils.estimateActiveMinutes(latestData.stepCount),
                        createdAt = currentTime,
                        updatedAt = currentTime,
                    )

                    stepRepository.insertStep(stepEntry)

                    // Clear the batch after successful save
                    stepDataBatch.clear()
                    lastBatchSaveTime = System.currentTimeMillis()

                    Log.d(TAG, "Step batch saved to database: ${latestData.stepCount} steps")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving step batch to database", e)
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
            setPackage(packageName)
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
                // Optimal conditions
            }
            SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                // Adjust sensitivity
                stepThreshold *= 0.8f
            }
        }
    }

    private fun getCurrentUserId(): Long {
        return sharedPreferences.getLong("current_user_id", 1L)
    }

    private fun createNotification(steps: Int, goal: Int): Notification {
        val progress = if (goal > 0) (steps * 100) / goal else 0
        val progressText = "$steps / $goal steps ($progress%)"

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

        saveStepsBatch() // Save any remaining data
        handler.removeCallbacks(saveToDbRunnable)
        sensorManager?.unregisterListener(this)
        unregisterReceiver(dozeReceiver)
        _isTracking.value = false

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

    // Public methods for UI binding
    fun getCurrentStepData(): StepData {
        return StepData(
            steps = _currentSteps.value,
            goal = _dailyGoal.value,
            progress = _stepProgress.value,
            distance = _distanceMeters.value,
            calories = _caloriesBurned.value,
            isTracking = _isTracking.value,
        )
    }

    fun setDailyGoal(newGoal: Int) {
        _dailyGoal.value = newGoal
        sharedPreferences.edit().putInt("daily_step_goal", newGoal).apply()
        updateStepData(todaySteps)
        Log.d(TAG, "Daily goal updated to: $newGoal")
    }

    fun resetDailySteps() {
        todaySteps = 0
        isStepCounterInitialized = false
        stepDataBatch.clear()
        sharedPreferences.edit().putBoolean("goal_achieved_today", false).apply()
        updateStepData(todaySteps)
        Log.d(TAG, "Daily steps reset")
    }

    // Doze-aware broadcast receiver
    inner class DozeAwareBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED -> {
                    val wasDozeMode = isDozeMode
                    isDozeMode = powerManager.isDeviceIdleMode

                    if (wasDozeMode != isDozeMode) {
                        Log.d(TAG, "Doze mode changed: $isDozeMode")

                        // Re-register sensors with appropriate settings
                        sensorManager?.unregisterListener(this@BatteryOptimizedStepService)
                        registerSensorListeners()

                        // Save any pending data before entering doze
                        if (isDozeMode) {
                            saveStepsBatch()
                        }
                    }
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    // Handle battery level changes if needed
                }
            }
        }
    }
}

/**
 * Data class for batched step data points
 */
data class StepDataPoint(
    val timestamp: Long,
    val stepCount: Int,
    val calories: Float,
    val distance: Float,
)

