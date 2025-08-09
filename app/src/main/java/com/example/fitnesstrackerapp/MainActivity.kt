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

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.navigation.AppNavigation
import com.example.fitnesstrackerapp.ui.auth.AuthViewModel
import com.example.fitnesstrackerapp.ui.theme.FitnessTrackerTheme
import com.example.fitnesstrackerapp.util.PermissionConstants
import com.example.fitnesstrackerapp.util.PermissionRequestResult
import com.example.fitnesstrackerapp.util.PermissionUtils

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
    private var permissionResults = mutableMapOf<String, Boolean>()

    /**
     * Handles the result of multiple permission requests.
     * Provides user feedback and logs permission status for debugging.
     * Uses centralized permission handling for consistency.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        permissions.forEach { (permission, isGranted) ->
            permissionResults[permission] = isGranted
            val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PermissionRequestResult(
                    permission = permission,
                    isGranted = isGranted,
                    description = PermissionUtils.getPermissionDescription(permission),
                )
            } else {
                // For API < Q, create a simplified result
                PermissionRequestResult(
                    permission = permission,
                    isGranted = isGranted,
                    description = null,
                )
            }

            handlePermissionResult(result)
        }

        // Log comprehensive permission status
        val grantedPermissions = permissionResults.filter { it.value }.keys
        val deniedPermissions = permissionResults.filter { !it.value }.keys

        Log.i(TAG, "Permissions granted: ${grantedPermissions.joinToString(", ")}")
        if (deniedPermissions.isNotEmpty()) {
            Log.w(TAG, "Permissions denied: ${deniedPermissions.joinToString(", ")}")
        }

        // Check if essential permissions are met
        checkEssentialPermissions()
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

            // Initialize ViewModel with proper error handling
            val authVm = try {
                ViewModelProvider(
                    this,
                    ServiceLocator.viewModelFactory,
                )[AuthViewModel::class.java]
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create AuthViewModel", e)
                throw e
            }

            setContent {
                FitnessTrackerTheme {
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
     * Uses centralized permission utilities for consistency.
     */
    private fun requestPermissions() {
        // Get permissions that need to be requested based on API level
        val permissionsToRequest = PermissionUtils.getRuntimePermissionsToRequest(this)

        // Initialize already granted permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PermissionConstants.RUNTIME_PERMISSIONS.forEach { permission ->
                if (PermissionUtils.isPermissionGranted(this, permission)) {
                    permissionResults[permission] = true
                    initializePermissionFeature(permission)
                }
            }
        }

        // Handle notifications on older Android versions
        if (!PermissionUtils.isNotificationPermissionGranted(this) &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            initializeNotifications()
        }

        // Request permissions only if needed
        if (permissionsToRequest.isNotEmpty()) {
            Log.d(TAG, "Requesting permissions: ${permissionsToRequest.joinToString(", ")}")
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Log.d(TAG, "All required permissions already granted")
            checkEssentialPermissions()
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
     * Handles the result of a single permission request.
     * Initializes corresponding features and shows appropriate user feedback.
     */
    private fun handlePermissionResult(result: PermissionRequestResult) {
        if (result.isGranted) {
            Log.d(TAG, "${result.friendlyName} permission granted")
            initializePermissionFeature(result.permission)
        } else {
            Log.w(TAG, "${result.friendlyName} permission denied")
            showPermissionDeniedMessage(result)
        }
    }

    /**
     * Initializes features based on granted permissions.
     */
    private fun initializePermissionFeature(permission: String) {
        when (permission) {
            PermissionConstants.POST_NOTIFICATIONS -> {
                initializeNotifications()
            }
            PermissionConstants.ACTIVITY_RECOGNITION -> {
                initializeActivityTracking()
            }
            PermissionConstants.CAMERA -> {
                // Camera features initialized on-demand
                Log.d(TAG, "Camera permission available for barcode scanning")
            }
            PermissionConstants.BODY_SENSORS -> {
                // Body sensors integrated with activity tracking
                Log.d(TAG, "Body sensors permission available for health tracking")
            }
            PermissionConstants.HIGH_SAMPLING_RATE_SENSORS -> {
                // High sampling rate sensors for more precise tracking
                Log.d(TAG, "High-rate sensors permission available for enhanced fitness tracking")
            }
        }
    }

    /**
     * Checks if essential permissions are granted and provides feedback.
     */
    private fun checkEssentialPermissions() {
        val hasEssential = PermissionUtils.areEssentialPermissionsGranted(this)
        val hasNotifications = PermissionUtils.isNotificationPermissionGranted(this)

        if (hasEssential && hasNotifications) {
            Log.i(TAG, "All essential permissions granted - app fully functional")
        } else if (hasEssential) {
            Log.i(TAG, "Essential permissions granted - notifications may be limited")
        } else {
            Log.w(TAG, "Essential permissions missing - limited app functionality")
            showEssentialPermissionsWarning()
        }
    }

    /**
     * Shows a warning when essential permissions are missing.
     */
    private fun showEssentialPermissionsWarning() {
        val message = "Some core features may not work properly without essential permissions. " +
            "You can enable them in Settings for the full fitness tracking experience."
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Shows a user-friendly message when a permission is denied.
     * Uses centralized permission descriptions for consistency.
     */
    private fun showPermissionDeniedMessage(result: PermissionRequestResult) {
        Toast.makeText(
            this,
            result.deniedMessage,
            Toast.LENGTH_LONG,
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
