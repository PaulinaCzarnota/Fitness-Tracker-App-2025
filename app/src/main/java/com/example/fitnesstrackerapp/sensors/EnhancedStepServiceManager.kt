package com.example.fitnesstrackerapp.sensors

/**
 * Enhanced Step Service Manager
 *
 * Advanced manager for the battery-optimized step counter service with:
 * - Intelligent service lifecycle management
 * - Battery optimization detection and handling
 * - Doze mode and app standby awareness
 * - Permission management for sensors and background activity
 * - Service health monitoring and recovery
 * - Integration with WorkoutRepository and GoalRepository
 */

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.fitnesstrackerapp.data.model.StepData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Enhanced manager for battery-optimized step counter service operations
 */
object EnhancedStepServiceManager {
    private const val TAG = "EnhancedStepServiceManager"

    // Service connection and binding
    private var stepService: BatteryOptimizedStepService? = null
    private var isServiceBound = false
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Service state tracking
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _serviceBoundState = MutableStateFlow(false)
    val serviceBoundState: StateFlow<Boolean> = _serviceBoundState.asStateFlow()

    private val _serviceHealthy = MutableStateFlow(true)
    val serviceHealthy: StateFlow<Boolean> = _serviceHealthy.asStateFlow()

    // Service connection for binding
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? BatteryOptimizedStepService.StepServiceBinder
            stepService = binder?.getService()
            isServiceBound = true
            _serviceBoundState.value = true
            _serviceHealthy.value = true
            Log.d(TAG, "Service connected and bound successfully")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            stepService = null
            isServiceBound = false
            _serviceBoundState.value = false
            _serviceHealthy.value = false
            Log.w(TAG, "Service disconnected unexpectedly")
        }
    }

    /**
     * Starts the battery-optimized step counter service
     */
    fun startService(context: Context): Boolean {
        return try {
            if (isServiceRunning(context)) {
                Log.d(TAG, "Service already running")
                return true
            }

            // Check permissions first
            if (!hasRequiredPermissions(context)) {
                Log.w(TAG, "Missing required permissions for step tracking")
                return false
            }

            val serviceIntent = Intent(context, BatteryOptimizedStepService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            _isServiceRunning.value = true
            Log.d(TAG, "Battery-optimized step counter service started")

            // Bind to service for direct communication
            bindToService(context)

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start step counter service", e)
            _isServiceRunning.value = false
            false
        }
    }

    /**
     * Stops the step counter service
     */
    fun stopService(context: Context): Boolean {
        return try {
            unbindFromService(context)

            val serviceIntent = Intent(context, BatteryOptimizedStepService::class.java)
            val result = context.stopService(serviceIntent)

            _isServiceRunning.value = false
            Log.d(TAG, "Step counter service stopped, result: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop step counter service", e)
            false
        }
    }

    /**
     * Binds to the service for direct method calls
     */
    fun bindToService(context: Context): Boolean {
        return try {
            if (isServiceBound) {
                Log.d(TAG, "Service already bound")
                return true
            }

            val serviceIntent = Intent(context, BatteryOptimizedStepService::class.java)
            val result = context.bindService(
                serviceIntent,
                serviceConnection,
                Context.BIND_AUTO_CREATE,
            )

            Log.d(TAG, "Attempting to bind to service, result: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind to service", e)
            false
        }
    }

    /**
     * Unbinds from the service
     */
    fun unbindFromService(context: Context) {
        try {
            if (isServiceBound) {
                context.unbindService(serviceConnection)
                stepService = null
                isServiceBound = false
                _serviceBoundState.value = false
                Log.d(TAG, "Service unbound")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unbind from service", e)
        }
    }

    /**
     * Checks if the step counter service is currently running
     */
    @Suppress("DEPRECATION")
    fun isServiceRunning(context: Context): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)

            for (service in runningServices) {
                if (BatteryOptimizedStepService::class.java.name == service.service.className) {
                    _isServiceRunning.value = true
                    Log.d(TAG, "Battery-optimized step counter service is running")
                    return true
                }
            }

            _isServiceRunning.value = false
            Log.d(TAG, "Step counter service is not running")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if service is running", e)
            false
        }
    }

    /**
     * Restarts the step counter service
     */
    fun restartService(context: Context): Boolean {
        Log.d(TAG, "Restarting step counter service")

        stopService(context)

        // Small delay to ensure clean shutdown
        Thread.sleep(1000)

        return startService(context)
    }

    /**
     * Ensures the step counter service is running and healthy
     */
    fun ensureServiceRunning(context: Context): Boolean {
        return when {
            !isServiceRunning(context) -> {
                Log.d(TAG, "Service not running, starting...")
                startService(context)
            }
            !isServiceBound -> {
                Log.d(TAG, "Service running but not bound, binding...")
                bindToService(context)
            }
            !_serviceHealthy.value -> {
                Log.d(TAG, "Service unhealthy, restarting...")
                restartService(context)
            }
            else -> {
                Log.d(TAG, "Service already running and healthy")
                true
            }
        }
    }

    /**
     * Checks if the device has required permissions for step tracking
     */
    fun hasRequiredPermissions(context: Context): Boolean {
        val requiredPermissions = mutableListOf<String>().apply {
            add(android.Manifest.permission.ACTIVITY_RECOGNITION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(android.Manifest.permission.POST_NOTIFICATIONS)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                add(android.Manifest.permission.FOREGROUND_SERVICE_HEALTH)
            }
        }

        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks if battery optimizations are disabled for the app
     */
    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Not applicable for older versions
        }
    }

    /**
     * Gets current step data from the bound service
     */
    fun getCurrentStepData(): StepData? {
        return stepService?.getCurrentStepData()
    }

    /**
     * Sets daily step goal through the bound service
     */
    fun setDailyGoal(newGoal: Int): Boolean {
        return try {
            stepService?.setDailyGoal(newGoal)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set daily goal", e)
            false
        }
    }

    /**
     * Resets daily steps through the bound service
     */
    fun resetDailySteps(): Boolean {
        return try {
            stepService?.resetDailySteps()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reset daily steps", e)
            false
        }
    }

    /**
     * Monitors service health and performs recovery if needed
     */
    fun monitorServiceHealth(context: Context) {
        serviceScope.launch {
            try {
                val isRunning = isServiceRunning(context)
                val wasHealthy = _serviceHealthy.value

                _serviceHealthy.value = isRunning && (isServiceBound || bindToService(context))

                // If service became unhealthy, attempt recovery
                if (wasHealthy && !_serviceHealthy.value) {
                    Log.w(TAG, "Service health degraded, attempting recovery")
                    restartService(context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error monitoring service health", e)
                _serviceHealthy.value = false
            }
        }
    }

    /**
     * Gets service diagnostic information
     */
    fun getServiceDiagnostics(context: Context): ServiceDiagnostics {
        return ServiceDiagnostics(
            isServiceRunning = isServiceRunning(context),
            isServiceBound = isServiceBound,
            isServiceHealthy = _serviceHealthy.value,
            hasRequiredPermissions = hasRequiredPermissions(context),
            isBatteryOptimizationDisabled = isBatteryOptimizationDisabled(context),
            currentStepData = getCurrentStepData(),
        )
    }

    /**
     * Handles doze mode changes by adjusting service behavior
     */
    fun handleDozeMode(context: Context, isDozeMode: Boolean) {
        Log.d(TAG, "Handling doze mode change: $isDozeMode")

        if (isDozeMode) {
            // In doze mode, ensure service is still running but with optimized settings
            ensureServiceRunning(context)
        } else {
            // Exiting doze mode, resume normal operation
            if (isServiceBound) {
                // Service will automatically adjust sensor settings
                Log.d(TAG, "Resumed from doze mode")
            }
        }
    }

    /**
     * Optimizes service for battery saving
     */
    fun optimizeForBattery(context: Context, enableOptimization: Boolean) {
        Log.d(TAG, "Optimizing service for battery: $enableOptimization")

        // The service automatically handles battery optimization based on doze mode
        // This method could be extended to provide manual optimization controls

        if (enableOptimization) {
            // Force battery optimization mode
            monitorServiceHealth(context)
        } else {
            // Normal operation mode
            ensureServiceRunning(context)
        }
    }

    /**
     * Prepares service for app shutdown
     */
    fun prepareForShutdown(context: Context) {
        Log.d(TAG, "Preparing service for app shutdown")

        // Unbind from service but leave it running
        unbindFromService(context)

        // Service will continue running as foreground service
    }

    /**
     * Initializes service on app startup
     */
    fun initializeOnStartup(context: Context): Boolean {
        Log.d(TAG, "Initializing service on startup")

        return if (hasRequiredPermissions(context)) {
            ensureServiceRunning(context)
        } else {
            Log.w(TAG, "Cannot initialize service without required permissions")
            false
        }
    }
}

/**
 * Data class for service diagnostic information
 */
data class ServiceDiagnostics(
    val isServiceRunning: Boolean,
    val isServiceBound: Boolean,
    val isServiceHealthy: Boolean,
    val hasRequiredPermissions: Boolean,
    val isBatteryOptimizationDisabled: Boolean,
    val currentStepData: StepData?,
)
