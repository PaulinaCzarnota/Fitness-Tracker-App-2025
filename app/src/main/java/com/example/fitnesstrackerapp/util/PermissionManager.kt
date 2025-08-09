/**
 * Runtime Permission Manager for the Fitness Tracker application.
 *
 * This class provides a centralized way to handle runtime permission requests
 * throughout the application. It offers both individual and batch permission
 * request capabilities with user-friendly feedback and proper error handling.
 *
 * Key Features:
 * - Centralized permission request handling
 * - Batch permission requests with individual results
 * - User education before requesting permissions
 * - Graceful degradation when permissions are denied
 * - Integration with activity lifecycle
 */

package com.example.fitnesstrackerapp.util

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Centralized permission manager for handling runtime permissions.
 * Integrates with activity lifecycle and provides comprehensive permission handling.
 */
class PermissionManager(
    private val activity: ComponentActivity,
    private val onPermissionResult: (PermissionRequestResult) -> Unit = {},
    private val onAllPermissionsHandled: (List<PermissionRequestResult>) -> Unit = {},
) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "PermissionManager"
    }

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var pendingPermissions = mutableSetOf<String>()
    private var currentResults = mutableListOf<PermissionRequestResult>()

    init {
        activity.lifecycle.addObserver(this)
        initializePermissionLauncher()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Log.d(TAG, "PermissionManager initialized for ${activity.javaClass.simpleName}")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        activity.lifecycle.removeObserver(this)
        Log.d(TAG, "PermissionManager destroyed")
    }

    /**
     * Initializes the permission launcher for handling multiple permissions.
     */
    private fun initializePermissionLauncher() {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                handlePermissionResults(permissions)
            }
        }
    }

    /**
     * Processes the results of permission requests.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        currentResults.clear()

        permissions.forEach { (permission, isGranted) ->
            val result = PermissionRequestResult(
                permission = permission,
                isGranted = isGranted,
                description = PermissionUtils.getPermissionDescription(permission),
            )

            currentResults.add(result)
            onPermissionResult(result)

            if (isGranted) {
                Log.d(TAG, "${result.friendlyName} permission granted")
            } else {
                Log.w(TAG, "${result.friendlyName} permission denied")
            }
        }

        pendingPermissions.clear()
        onAllPermissionsHandled(currentResults.toList())
    }

    /**
     * Requests a single permission with user education.
     */
    fun requestPermission(
        permission: String,
        showEducation: Boolean = true,
    ) {
        requestPermissions(arrayOf(permission), showEducation)
    }

    /**
     * Requests multiple permissions with optional user education.
     */
    fun requestPermissions(
        permissions: Array<String>,
        showEducation: Boolean = true,
    ) {
        val permissionsToRequest = permissions.filter { permission ->
            !PermissionUtils.isPermissionGranted(activity, permission)
        }

        if (permissionsToRequest.isEmpty()) {
            Log.d(TAG, "All requested permissions already granted")
            // Create granted results for already granted permissions
            val grantedResults = permissions.map { permission ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    PermissionRequestResult(
                        permission = permission,
                        isGranted = true,
                        description = PermissionUtils.getPermissionDescription(permission),
                    )
                } else {
                    // For API < Q, create a simplified result
                    PermissionRequestResult(
                        permission = permission,
                        isGranted = true,
                        description = null,
                    )
                }
            }
            onAllPermissionsHandled(grantedResults)
            return
        }

        if (showEducation) {
            showPermissionEducation(permissionsToRequest) {
                launchPermissionRequest(permissionsToRequest.toTypedArray())
            }
        } else {
            launchPermissionRequest(permissionsToRequest.toTypedArray())
        }
    }

    /**
     * Requests essential fitness permissions required for core functionality.
     */
    fun requestEssentialPermissions(showEducation: Boolean = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(PermissionConstants.ESSENTIAL_PERMISSIONS, showEducation)
        }
    }

    /**
     * Requests all runtime permissions used by the app.
     */
    fun requestAllRuntimePermissions(showEducation: Boolean = true) {
        val permissionsToRequest = PermissionUtils.getRuntimePermissionsToRequest(activity)
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(permissionsToRequest.toTypedArray(), showEducation)
        } else {
            Log.d(TAG, "All runtime permissions already granted")
        }
    }

    /**
     * Shows educational information about why permissions are needed.
     */
    private fun showPermissionEducation(
        permissions: List<String>,
        onProceed: () -> Unit,
    ) {
        // For now, just proceed directly
        // In a real app, you might show a dialog explaining the permissions
        Log.d(TAG, "Permission education would be shown for: ${permissions.joinToString(", ")}")
        onProceed()
    }

    /**
     * Launches the actual permission request.
     */
    private fun launchPermissionRequest(permissions: Array<String>) {
        pendingPermissions.addAll(permissions)
        Log.d(TAG, "Requesting permissions: ${permissions.joinToString(", ")}")
        permissionLauncher.launch(permissions)
    }

    /**
     * Checks if all essential permissions are granted.
     */
    fun areEssentialPermissionsGranted(): Boolean {
        return PermissionUtils.areEssentialPermissionsGranted(activity)
    }

    /**
     * Checks if a specific permission is granted.
     */
    fun isPermissionGranted(permission: String): Boolean {
        return PermissionUtils.isPermissionGranted(activity, permission)
    }

    /**
     * Gets the list of missing essential permissions.
     */
    fun getMissingEssentialPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PermissionUtils.getMissingPermissions(activity, PermissionConstants.ESSENTIAL_PERMISSIONS)
        } else {
            // For API < Q, return empty list as most permissions are granted at install time
            emptyList()
        }
    }

    /**
     * Opens the app settings page for manual permission management.
     */
    fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(intent)
            Log.d(TAG, "Opened app settings for manual permission management")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open app settings", e)
        }
    }

    /**
     * Creates a user-friendly summary of current permission status.
     */
    fun getPermissionStatusSummary(): PermissionStatusSummary {
        val essential = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PermissionConstants.ESSENTIAL_PERMISSIONS
        } else {
            // For API < Q, use a smaller set of essential permissions
            emptyArray<String>()
        }
        val grantedEssential = essential.filter { PermissionUtils.isPermissionGranted(activity, it) }
        val deniedEssential = essential.filter { !PermissionUtils.isPermissionGranted(activity, it) }

        val optional = PermissionConstants.RUNTIME_PERMISSIONS.filterNot { it in essential }
        val grantedOptional = optional.filter { PermissionUtils.isPermissionGranted(activity, it) }
        val deniedOptional = optional.filter { !PermissionUtils.isPermissionGranted(activity, it) }

        return PermissionStatusSummary(
            grantedEssential = grantedEssential,
            deniedEssential = deniedEssential,
            grantedOptional = grantedOptional,
            deniedOptional = deniedOptional,
            isFullyFunctional = deniedEssential.isEmpty(),
            hasNotifications = PermissionUtils.isNotificationPermissionGranted(activity),
        )
    }
}

/**
 * Data class representing the overall permission status of the app.
 */
data class PermissionStatusSummary(
    val grantedEssential: List<String>,
    val deniedEssential: List<String>,
    val grantedOptional: List<String>,
    val deniedOptional: List<String>,
    val isFullyFunctional: Boolean,
    val hasNotifications: Boolean,
) {
    val totalGranted: Int = grantedEssential.size + grantedOptional.size
    val totalDenied: Int = deniedEssential.size + deniedOptional.size
    val grantedPercentage: Float = if (totalGranted + totalDenied == 0) {
        100f
    } else {
        (totalGranted.toFloat() / (totalGranted + totalDenied)) * 100f
    }
}
