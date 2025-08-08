package com.example.fitnesstrackerapp.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Manages user preferences using DataStore
 */
class UserPreferences(private val context: Context) {

    companion object {
        private val USE_METRIC = booleanPreferencesKey("use_metric")
        private val WORKOUT_REMINDERS_ENABLED = booleanPreferencesKey("workout_reminders_enabled")
        private val BIOMETRIC_LOGIN_ENABLED = booleanPreferencesKey("biometric_login_enabled")
        private val WORKOUT_REMINDER_FREQUENCY = stringPreferencesKey("workout_reminder_frequency")
        private val USER_HEIGHT = stringPreferencesKey("user_height")
        private val USER_WEIGHT = stringPreferencesKey("user_weight")
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
}
