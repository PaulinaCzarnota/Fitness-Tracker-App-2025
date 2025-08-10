package com.example.fitnesstrackerapp.util

/**
 * Constants and utility classes for the Fitness Tracker application.
 *
 * This file contains:
 * - Application-wide constants for various features
 * - Step tracking configuration constants
 * - Date utility functions for formatting and calculations
 * - Notification channel and frequency constants
 *
 * These constants help maintain consistency across the application
 * and provide centralized configuration for core functionality.
 */

/**
 * General application constants used throughout the Fitness Tracker app.
 * Contains configuration values for goals, notifications, and WorkManager.
 */
object AppConstants {
    // Goal frequency constants
    const val FREQUENCY_DAILY = "DAILY"
    const val FREQUENCY_WEEKLY = "WEEKLY"
    const val FREQUENCY_MONTHLY = "MONTHLY"
    const val FREQUENCY_ONCE = "ONCE"

    // WorkManager constants
    const val MIN_BACKOFF_MILLIS = 30000L // 30 seconds

    // Notification constants
    const val NOTIFICATION_CHANNEL_REMINDERS = "reminders"
    const val NOTIFICATION_CHANNEL_GOALS = "goals"
    const val NOTIFICATION_CHANNEL_WORKOUTS = "workouts"
}

/**
 * Constants for step tracking functionality
 */
object StepTrackingConstants {
    const val STEP_UPDATE_ACTION = "com.example.fitnesstrackerapp.STEP_UPDATE"
    const val EXTRA_STEPS = "extra_steps"
    const val NOTIFICATION_CHANNEL_ID = "step_tracking_channel"
    const val NOTIFICATION_ID = 1001

    // Step tracking preferences
    const val STEP_PREFS = "step_preferences"
    const val KEY_DAILY_STEPS = "daily_steps"
    const val KEY_STEP_GOAL = "step_goal"
    const val DEFAULT_STEP_GOAL = 10000
}

/**
 * Utility class for date operations
 */
object DateUtil {
    fun formatDate(date: java.util.Date): String {
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return formatter.format(date)
    }

    fun formatTime(date: java.util.Date): String {
        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }

    fun formatDateTime(date: java.util.Date): String {
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }

    fun getStartOfDay(date: java.util.Date): java.util.Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getEndOfDay(date: java.util.Date): java.util.Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        return calendar.time
    }

    fun getStartOfWeek(date: java.util.Date): java.util.Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getStartOfMonth(date: java.util.Date): java.util.Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getStartOfYear(date: java.util.Date): java.util.Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.DAY_OF_YEAR, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.time
    }
}
