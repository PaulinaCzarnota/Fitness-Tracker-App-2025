package com.example.fitnesstrackerapp.util

/**
 * Permission Constants and Utilities for the Fitness Tracker application.
 *
 * This file centralizes all permission-related constants and provides utility functions
 * for runtime permission requests and checks. It helps maintain consistency across
 * the application and provides a single source of truth for all permission handling.
 *
 * Key Features:
 * - Centralized permission constants
 * - Permission check utilities
 * - Permission request flow helpers
 * - User-friendly permission descriptions
 */

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

/**
 * Constants for all permissions used in the Fitness Tracker app.
 * Centralizes permission strings and provides categorization.
 */
object PermissionConstants {
    // Core fitness and health permissions
    @RequiresApi(Build.VERSION_CODES.Q)
    const val ACTIVITY_RECOGNITION = Manifest.permission.ACTIVITY_RECOGNITION
    const val BODY_SENSORS = Manifest.permission.BODY_SENSORS

    @RequiresApi(Build.VERSION_CODES.S)
    const val HIGH_SAMPLING_RATE_SENSORS = Manifest.permission.HIGH_SAMPLING_RATE_SENSORS

    // Notification permissions
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    const val POST_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS

    // Service and system permissions
    @RequiresApi(Build.VERSION_CODES.P)
    const val FOREGROUND_SERVICE = Manifest.permission.FOREGROUND_SERVICE

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    const val FOREGROUND_SERVICE_HEALTH = Manifest.permission.FOREGROUND_SERVICE_HEALTH
    const val RECEIVE_BOOT_COMPLETED = Manifest.permission.RECEIVE_BOOT_COMPLETED

    @RequiresApi(Build.VERSION_CODES.S)
    const val SCHEDULE_EXACT_ALARM = Manifest.permission.SCHEDULE_EXACT_ALARM

    // Network permissions
    const val INTERNET = Manifest.permission.INTERNET
    const val ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE

    // Optional permissions
    const val CAMERA = Manifest.permission.CAMERA

    // Permission groups for batch requests
    @RequiresApi(Build.VERSION_CODES.Q)
    val ESSENTIAL_PERMISSIONS = arrayOf(
        ACTIVITY_RECOGNITION,
        BODY_SENSORS,
    )

    @RequiresApi(Build.VERSION_CODES.S)
    val SENSOR_PERMISSIONS = arrayOf(
        BODY_SENSORS,
        HIGH_SAMPLING_RATE_SENSORS,
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val NOTIFICATION_PERMISSIONS = arrayOf(
        POST_NOTIFICATIONS,
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    val RUNTIME_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            ACTIVITY_RECOGNITION,
            POST_NOTIFICATIONS,
            CAMERA,
        )
    } else {
        // For API < TIRAMISU, POST_NOTIFICATIONS is not needed
        arrayOf(
            ACTIVITY_RECOGNITION,
            CAMERA,
        )
    }

    // Permission descriptions for user education
    @RequiresApi(Build.VERSION_CODES.Q)
    val PERMISSION_DESCRIPTIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        mapOf(
            ACTIVITY_RECOGNITION to PermissionDescription(
                "Physical Activity",
                "automatic step counting and activity detection",
                "Track your daily steps and physical activities automatically",
            ),
            POST_NOTIFICATIONS to PermissionDescription(
                "Notifications",
                "workout reminders and goal tracking notifications",
                "Send you helpful reminders and celebrate your achievements",
            ),
            BODY_SENSORS to PermissionDescription(
                "Health Sensors",
                "accessing fitness and health sensor data",
                "Use your phone's sensors to track your fitness activities",
            ),
            CAMERA to PermissionDescription(
                "Camera",
                "scanning barcodes for food logging",
                "Quickly log food items by scanning barcodes",
            ),
            HIGH_SAMPLING_RATE_SENSORS to PermissionDescription(
                "High-Rate Sensors",
                "precise fitness and activity tracking",
                "Provide more accurate step counting and movement detection",
            ),
        )
    } else {
        // For API < TIRAMISU, POST_NOTIFICATIONS is not available
        mapOf(
            ACTIVITY_RECOGNITION to PermissionDescription(
                "Physical Activity",
                "automatic step counting and activity detection",
                "Track your daily steps and physical activities automatically",
            ),
            BODY_SENSORS to PermissionDescription(
                "Health Sensors",
                "accessing fitness and health sensor data",
                "Use your phone's sensors to track your fitness activities",
            ),
            CAMERA to PermissionDescription(
                "Camera",
                "scanning barcodes for food logging",
                "Quickly log food items by scanning barcodes",
            ),
        )
    }
}

/**
 * Data class representing permission information for user education.
 */
data class PermissionDescription(
    val friendlyName: String,
    val functionality: String,
    val userBenefit: String,
)

/**
 * Utility class for permission-related operations.
 */
object PermissionUtils {
    /**
     * Checks if a specific permission is granted.
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if all permissions in the given array are granted.
     */
    fun areAllPermissionsGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { isPermissionGranted(context, it) }
    }

    /**
     * Gets the list of permissions that are not yet granted.
     */
    fun getMissingPermissions(context: Context, permissions: Array<String>): List<String> {
        return permissions.filter { !isPermissionGranted(context, it) }
    }

    /**
     * Checks if notification permission is required and granted.
     * On Android 13+ (API 33), explicit permission is required.
     */
    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isPermissionGranted(context, PermissionConstants.POST_NOTIFICATIONS)
        } else {
            true // Granted by default on older versions
        }
    }

    /**
     * Checks if activity recognition permission is required and granted.
     * On Android 10+ (API 29), explicit permission is required.
     */
    fun isActivityRecognitionPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isPermissionGranted(context, PermissionConstants.ACTIVITY_RECOGNITION)
        } else {
            true // Not required on older versions
        }
    }

    /**
     * Gets permissions that need to be requested at runtime based on API level.
     */
    fun getRuntimePermissionsToRequest(context: Context): List<String> {
        val permissionsToRequest = mutableListOf<String>()

        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isPermissionGranted(context, PermissionConstants.POST_NOTIFICATIONS)) {
                permissionsToRequest.add(PermissionConstants.POST_NOTIFICATIONS)
            }
        }

        // Check activity recognition permission for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!isPermissionGranted(context, PermissionConstants.ACTIVITY_RECOGNITION)) {
                permissionsToRequest.add(PermissionConstants.ACTIVITY_RECOGNITION)
            }
        }

        // Camera permission is always runtime (optional)
        if (!isPermissionGranted(context, PermissionConstants.CAMERA)) {
            permissionsToRequest.add(PermissionConstants.CAMERA)
        }

        return permissionsToRequest
    }

    /**
     * Gets the user-friendly description for a permission.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getPermissionDescription(permission: String): PermissionDescription? {
        return PermissionConstants.PERMISSION_DESCRIPTIONS[permission]
    }

    /**
     * Creates a user-friendly message when a permission is denied.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun createPermissionDeniedMessage(permission: String): String {
        val description = getPermissionDescription(permission)
        return if (description != null) {
            "${description.friendlyName} permission is required for ${description.functionality}. " +
                "You can enable it in Settings to ${description.userBenefit.lowercase()}."
        } else {
            "This permission is required for the app to function properly. You can enable it in Settings."
        }
    }

    /**
     * Checks if essential fitness permissions are granted.
     * These are permissions critical for core app functionality.
     */
    fun areEssentialPermissionsGranted(context: Context): Boolean {
        return isActivityRecognitionPermissionGranted(context) &&
            isPermissionGranted(context, PermissionConstants.BODY_SENSORS)
    }
}

/**
 * Permission request result data class for organized handling.
 */
data class PermissionRequestResult(
    val permission: String,
    val isGranted: Boolean,
    val description: PermissionDescription?,
) {
    val friendlyName: String
        get() = description?.friendlyName ?: permission

    val deniedMessage: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PermissionUtils.createPermissionDeniedMessage(permission)
        } else {
            // For API < Q, return a generic message
            "Permission is required for the app to function properly. You can enable it in Settings."
        }
}
