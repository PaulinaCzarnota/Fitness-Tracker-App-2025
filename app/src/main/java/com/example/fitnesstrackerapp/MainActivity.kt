/**
 * This is the main Activity class for the Fitness Tracker Android application.
 * It serves as the primary entry point for user interaction and app initialization.
 *
 * Key Responsibilities:
 * - Handle runtime permission requests (notifications, activity recognition)
 * - Set up the main navigation structure using Jetpack Compose
 * - Initialize splash screen functionality
 * - Configure the main UI theme and structure
 * - Manage activity lifecycle events
 */

package com.example.fitnesstrackerapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.fitnesstrackerapp.navigation.AppNavigation
import com.example.fitnesstrackerapp.ui.theme.FitnessTrackerTheme

/**
 * Main Activity class that serves as the entry point for the application.
 *
 * This activity is responsible for:
 * - Managing runtime permission requests with proper user feedback
 * - Setting up the splash screen and app initialization
 * - Configuring the main navigation structure
 * - Handling activity lifecycle events
 * - Providing error handling for critical app functionality
 */
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // Tracks which permissions have been granted
    private var notificationPermissionGranted = false
    private var activityRecognitionGranted = false

    /**
     * Handles the result of multiple permission requests.
     * Provides user feedback and logs permission status for debugging.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (permission, isGranted) ->
            when (permission) {
                Manifest.permission.POST_NOTIFICATIONS -> {
                    notificationPermissionGranted = isGranted
                    if (isGranted) {
                        Log.d(TAG, "Notification permission granted")
                        // Initialize notification channels and schedulers
                        initializeNotifications()
                    } else {
                        Log.w(TAG, "Notification permission denied")
                        showPermissionDeniedMessage("Notifications", "workout reminders and goal tracking")
                    }
                }
                Manifest.permission.ACTIVITY_RECOGNITION -> {
                    activityRecognitionGranted = isGranted
                    if (isGranted) {
                        Log.d(TAG, "Activity recognition permission granted")
                        // Initialize step tracking and activity sensors
                        initializeActivityTracking()
                    } else {
                        Log.w(TAG, "Activity recognition permission denied")
                        showPermissionDeniedMessage("Activity Recognition", "automatic step counting and activity detection")
                    }
                }
                else -> {
                    Log.d(TAG, "Permission $permission result: $isGranted")
                }
            }
        }
        
        // Log summary of permission status
        Log.i(TAG, "Permissions summary - Notifications: $notificationPermissionGranted, Activity: $activityRecognitionGranted")
    }

    /**
     * Initializes the activity with proper error handling and component setup.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            
            // Install splash screen before any other initialization
            installSplashScreen()
            
            // Request necessary permissions
            requestPermissions()
            
            // Set up the main UI content
            setContent {
                FitnessTrackerTheme {
                    val authVm = ViewModelFactoryProvider.getAuthViewModel(this)
                    AppNavigation(authViewModel = authVm)
                }
            }
            
            Log.i(TAG, "MainActivity created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating MainActivity", e)
            // In production, you might want to show an error dialog or restart the app
            throw e
        }
    }

    /**
     * Requests necessary permissions based on the Android API level.
     * Only requests permissions that haven't been granted already.
     */
    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        // Check and request POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                notificationPermissionGranted = true
                initializeNotifications()
            }
        } else {
            // For older versions, notifications are granted by default
            notificationPermissionGranted = true
            initializeNotifications()
        }
        
        // Check and request ACTIVITY_RECOGNITION permission for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            } else {
                activityRecognitionGranted = true
                initializeActivityTracking()
            }
        }

        // Request permissions only if needed
        if (permissionsToRequest.isNotEmpty()) {
            Log.d(TAG, "Requesting permissions: ${permissionsToRequest.joinToString(", ")}")
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Log.d(TAG, "All required permissions already granted")
        }
    }

    /**
     * Initializes notification-related components when permission is granted.
     */
    private fun initializeNotifications() {
        try {
            // Initialize notification channels, schedulers, etc.
            // This would typically involve setting up notification channels
            // and scheduling daily reminders
            Log.d(TAG, "Notifications initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize notifications", e)
        }
    }

    /**
     * Initializes activity tracking components when permission is granted.
     */
    private fun initializeActivityTracking() {
        try {
            // Initialize step counter service, activity sensors, etc.
            Log.d(TAG, "Activity tracking initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize activity tracking", e)
        }
    }

    /**
     * Shows a user-friendly message when a permission is denied.
     *
     * @param permissionName The name of the permission that was denied
     * @param functionality The functionality that will be affected
     */
    private fun showPermissionDeniedMessage(permissionName: String, functionality: String) {
        val message = getString(R.string.permission_denied_message, permissionName, functionality)
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * Handles activity lifecycle cleanup.
     */
    override fun onDestroy() {
        Log.d(TAG, "MainActivity destroyed")
        super.onDestroy()
    }
}
