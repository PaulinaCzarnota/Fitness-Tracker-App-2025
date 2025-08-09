/**
 * Step Service Manager
 *
 * Utility class for managing the lifecycle of StepCounterService.
 * Provides convenient methods for starting, stopping, and checking service status.
 */
package com.example.fitnesstrackerapp.sensors

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Manager class for StepCounterService operations
 */
object StepServiceManager {
    
    private const val TAG = "StepServiceManager"
    
    /**
     * Starts the step counter service as a foreground service
     */
    fun startService(context: Context): Boolean {
        return try {
            val serviceIntent = Intent(context, StepCounterService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            
            Log.d(TAG, "Step counter service started")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start step counter service", e)
            false
        }
    }
    
    /**
     * Stops the step counter service
     */
    fun stopService(context: Context): Boolean {
        return try {
            val serviceIntent = Intent(context, StepCounterService::class.java)
            val result = context.stopService(serviceIntent)
            Log.d(TAG, "Step counter service stopped, result: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop step counter service", e)
            false
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
                if (StepCounterService::class.java.name == service.service.className) {
                    Log.d(TAG, "Step counter service is running")
                    return true
                }
            }
            
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
        stopService(context)
        
        // Small delay to ensure clean shutdown
        Thread.sleep(500)
        
        return startService(context)
    }
    
    /**
     * Ensures the step counter service is running
     * Starts it if not already running
     */
    fun ensureServiceRunning(context: Context): Boolean {
        return if (!isServiceRunning(context)) {
            startService(context)
        } else {
            Log.d(TAG, "Step counter service already running")
            true
        }
    }
}
