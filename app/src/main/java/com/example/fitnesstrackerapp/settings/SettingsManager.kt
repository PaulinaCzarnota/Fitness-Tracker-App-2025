/**
 * Settings Manager
 *
 * Responsibilities:
 * - Manages user preferences and app settings
 * - Handles measurement units and notifications
 * - Provides default values and data validation
 */
package com.example.fitnesstrackerapp.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Manages user preferences and app settings using Jetpack DataStore.
 *
 * Responsibilities:
 * - Provides reactive flows for accessing user preferences.
 * - Handles updating of all user-configurable settings.
 * - Defines default values for all settings.
 * - Consolidates all app preferences into a single manager.
 */
class SettingsManager(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    // User Preferences
    val measurementUnit: Flow<MeasurementUnit> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            MeasurementUnit.valueOf(
                preferences[PreferencesKeys.MEASUREMENT_UNIT] ?: MeasurementUnit.METRIC.name,
            )
        }

    val dailyCalorieTarget: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            preferences[PreferencesKeys.DAILY_CALORIE_TARGET] ?: DEFAULT_CALORIE_TARGET
        }

    val stepGoal: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            preferences[PreferencesKeys.STEP_GOAL] ?: DEFAULT_STEP_GOAL
        }

    val workoutReminders: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            preferences[PreferencesKeys.WORKOUT_REMINDERS] ?: true
        }

    val notificationEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        }

    val notificationTime: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_TIME] ?: "09:00"
        }

    val themeMode: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: "system"
        }

    suspend fun updateMeasurementUnit(unit: MeasurementUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MEASUREMENT_UNIT] = unit.name
        }
    }

    suspend fun updateDailyCalorieTarget(target: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_CALORIE_TARGET] = target
        }
    }

    suspend fun updateStepGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STEP_GOAL] = goal
        }
    }

    suspend fun updateWorkoutReminders(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WORKOUT_REMINDERS] = enabled
        }
    }

    suspend fun updateNotificationTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_TIME] = time
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    private object PreferencesKeys {
        val MEASUREMENT_UNIT = stringPreferencesKey("measurement_unit")
        val DAILY_CALORIE_TARGET = intPreferencesKey("daily_calorie_target")
        val STEP_GOAL = intPreferencesKey("step_goal")
        val WORKOUT_REMINDERS = booleanPreferencesKey("workout_reminders")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_TIME = stringPreferencesKey("notification_time")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    companion object {
        const val DEFAULT_CALORIE_TARGET = 2000
        const val DEFAULT_STEP_GOAL = 10000
    }
}

enum class MeasurementUnit {
    METRIC,
    IMPERIAL,
}
