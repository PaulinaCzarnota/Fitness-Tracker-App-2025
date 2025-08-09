package com.example.fitnesstrackerapp.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Enhanced user preferences manager with comprehensive notification settings.
 *
 * Manages all user preferences including fitness goals, notification settings,
 * reminder schedules, and personalization options using DataStore.
 */
class UserPreferences(private val context: Context) {
    companion object {
        // Basic user preferences
        private val USE_METRIC = booleanPreferencesKey("use_metric")
        private val USER_HEIGHT = stringPreferencesKey("user_height")
        private val USER_WEIGHT = stringPreferencesKey("user_weight")
        private val BIOMETRIC_LOGIN_ENABLED = booleanPreferencesKey("biometric_login_enabled")

        // Notification preferences
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val WORKOUT_REMINDERS_ENABLED = booleanPreferencesKey("workout_reminders_enabled")
        private val HYDRATION_REMINDERS_ENABLED = booleanPreferencesKey("hydration_reminders_enabled")
        private val GOAL_REMINDERS_ENABLED = booleanPreferencesKey("goal_reminders_enabled")
        private val MOTIVATIONAL_MESSAGES_ENABLED = booleanPreferencesKey("motivational_messages_enabled")

        // Timing preferences
        private val WORKOUT_REMINDER_TIME = stringPreferencesKey("workout_reminder_time")
        private val HYDRATION_REMINDER_INTERVAL = intPreferencesKey("hydration_reminder_interval")
        private val MOTIVATIONAL_MESSAGE_TIME = stringPreferencesKey("motivational_message_time")
        private val WORKOUT_REMINDER_DAYS = stringSetPreferencesKey("workout_reminder_days")

        // Goal and tracking preferences
        private val DAILY_WATER_GOAL = intPreferencesKey("daily_water_goal")
        private val WORKOUT_REMINDER_FREQUENCY = stringPreferencesKey("workout_reminder_frequency")
    }

    /**
     * Whether to use metric units
     */
    val useMetric: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_METRIC] ?: true
    }

    /**
     * Whether workout reminders are enabled
     */
    val workoutRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[WORKOUT_REMINDERS_ENABLED] ?: true
    }

    /**
     * Whether biometric login is enabled
     */
    val biometricLoginEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BIOMETRIC_LOGIN_ENABLED] ?: false
    }

    /**
     * Workout reminder frequency
     */
    val workoutReminderFrequency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[WORKOUT_REMINDER_FREQUENCY] ?: "daily"
    }

    /**
     * User height
     */
    val userHeight: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_HEIGHT] ?: ""
    }

    /**
     * User weight
     */
    val userWeight: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_WEIGHT] ?: ""
    }

    // ==============================================
    // Enhanced Notification Preferences
    // ==============================================

    /**
     * Whether notifications are enabled globally
     */
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }

    /**
     * Whether hydration reminders are enabled
     */
    val hydrationRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HYDRATION_REMINDERS_ENABLED] ?: true
    }

    /**
     * Whether goal reminders are enabled
     */
    val goalRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[GOAL_REMINDERS_ENABLED] ?: true
    }

    /**
     * Whether motivational messages are enabled
     */
    val motivationalMessagesEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MOTIVATIONAL_MESSAGES_ENABLED] ?: true
    }

    /**
     * Workout reminder time (HH:mm format)
     */
    val workoutReminderTime: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[WORKOUT_REMINDER_TIME] ?: "09:00"
    }

    /**
     * Hydration reminder interval in hours
     */
    val hydrationReminderInterval: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[HYDRATION_REMINDER_INTERVAL] ?: 2
    }

    /**
     * Motivational message time (HH:mm format)
     */
    val motivationalMessageTime: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[MOTIVATIONAL_MESSAGE_TIME] ?: "08:00"
    }

    /**
     * Days when workout reminders should be sent
     */
    val workoutReminderDays: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[WORKOUT_REMINDER_DAYS] ?: setOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    }

    /**
     * Daily water intake goal in milliliters
     */
    val dailyWaterGoal: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DAILY_WATER_GOAL] ?: 2000
    }

    /**
     * Set whether to use metric units
     */
    suspend fun setUseMetric(useMetric: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_METRIC] = useMetric
        }
    }

    /**
     * Set workout reminders enabled
     */
    suspend fun setWorkoutRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WORKOUT_REMINDERS_ENABLED] = enabled
        }
    }

    /**
     * Set biometric login enabled
     */
    suspend fun setBiometricLoginEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_LOGIN_ENABLED] = enabled
        }
    }

    /**
     * Set workout reminder frequency
     */
    suspend fun setWorkoutReminderFrequency(frequency: String) {
        context.dataStore.edit { preferences ->
            preferences[WORKOUT_REMINDER_FREQUENCY] = frequency
        }
    }

    /**
     * Update user height
     */
    suspend fun updateHeight(height: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_HEIGHT] = height
        }
    }

    /**
     * Update user weight
     */
    suspend fun updateWeight(weight: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_WEIGHT] = weight
        }
    }

    // ==============================================
    // Enhanced Notification Preference Setters
    // ==============================================

    /**
     * Set global notifications enabled/disabled
     */
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    /**
     * Set hydration reminders enabled/disabled
     */
    suspend fun setHydrationRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HYDRATION_REMINDERS_ENABLED] = enabled
        }
    }

    /**
     * Set goal reminders enabled/disabled
     */
    suspend fun setGoalRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[GOAL_REMINDERS_ENABLED] = enabled
        }
    }

    /**
     * Set motivational messages enabled/disabled
     */
    suspend fun setMotivationalMessagesEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MOTIVATIONAL_MESSAGES_ENABLED] = enabled
        }
    }

    /**
     * Set workout reminder time
     */
    suspend fun setWorkoutReminderTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[WORKOUT_REMINDER_TIME] = time
        }
    }

    /**
     * Set hydration reminder interval in hours
     */
    suspend fun setHydrationReminderInterval(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[HYDRATION_REMINDER_INTERVAL] = hours.coerceIn(1, 12)
        }
    }

    /**
     * Set motivational message time
     */
    suspend fun setMotivationalMessageTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[MOTIVATIONAL_MESSAGE_TIME] = time
        }
    }

    /**
     * Set workout reminder days
     */
    suspend fun setWorkoutReminderDays(days: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[WORKOUT_REMINDER_DAYS] = days
        }
    }

    /**
     * Set daily water goal in milliliters
     */
    suspend fun setDailyWaterGoal(goalMl: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_WATER_GOAL] = goalMl.coerceIn(500, 5000)
        }
    }

    /**
     * Updates all notification preferences at once for efficiency.
     */
    suspend fun updateNotificationPreferences(
        notificationsEnabled: Boolean? = null,
        workoutRemindersEnabled: Boolean? = null,
        hydrationRemindersEnabled: Boolean? = null,
        goalRemindersEnabled: Boolean? = null,
        motivationalMessagesEnabled: Boolean? = null,
        workoutReminderTime: String? = null,
        hydrationReminderInterval: Int? = null,
        motivationalMessageTime: String? = null,
        workoutReminderDays: Set<String>? = null,
        dailyWaterGoal: Int? = null,
    ) {
        context.dataStore.edit { preferences ->
            notificationsEnabled?.let { preferences[NOTIFICATIONS_ENABLED] = it }
            workoutRemindersEnabled?.let { preferences[WORKOUT_REMINDERS_ENABLED] = it }
            hydrationRemindersEnabled?.let { preferences[HYDRATION_REMINDERS_ENABLED] = it }
            goalRemindersEnabled?.let { preferences[GOAL_REMINDERS_ENABLED] = it }
            motivationalMessagesEnabled?.let { preferences[MOTIVATIONAL_MESSAGES_ENABLED] = it }
            workoutReminderTime?.let { preferences[WORKOUT_REMINDER_TIME] = it }
            hydrationReminderInterval?.let { preferences[HYDRATION_REMINDER_INTERVAL] = it.coerceIn(1, 12) }
            motivationalMessageTime?.let { preferences[MOTIVATIONAL_MESSAGE_TIME] = it }
            workoutReminderDays?.let { preferences[WORKOUT_REMINDER_DAYS] = it }
            dailyWaterGoal?.let { preferences[DAILY_WATER_GOAL] = it.coerceIn(500, 5000) }
        }
    }
}
