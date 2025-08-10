package com.example.fitnesstrackerapp.data

/**
 * Type Converters for Room Database in the Fitness Tracker application.
 *
 * This file contains all type converters required for Room database to handle
 * complex data types that cannot be directly stored in SQLite. Each converter
 * provides bidirectional conversion between complex types and primitive types.
 *
 * Key Features:
 * - Date/timestamp conversion for all date fields
 * - Enum converters for all custom enum types
 * - Null-safe conversion handling
 * - Optimized for database storage and retrieval performance
 * - Consistent error handling and validation
 */

import androidx.room.TypeConverter
import com.example.fitnesstrackerapp.data.entity.ActivityLevel
import com.example.fitnesstrackerapp.data.entity.DifficultyLevel
import com.example.fitnesstrackerapp.data.entity.EquipmentType
import com.example.fitnesstrackerapp.data.entity.ExerciseType
import com.example.fitnesstrackerapp.data.entity.Gender
import com.example.fitnesstrackerapp.data.entity.GoalStatus
import com.example.fitnesstrackerapp.data.entity.GoalType
import com.example.fitnesstrackerapp.data.entity.MealType
import com.example.fitnesstrackerapp.data.entity.MuscleGroup
import com.example.fitnesstrackerapp.data.entity.NotificationChannel
import com.example.fitnesstrackerapp.data.entity.NotificationDeliveryChannel
import com.example.fitnesstrackerapp.data.entity.NotificationEventType
import com.example.fitnesstrackerapp.data.entity.NotificationLogEvent
import com.example.fitnesstrackerapp.data.entity.NotificationPriority
import com.example.fitnesstrackerapp.data.entity.NotificationStatus
import com.example.fitnesstrackerapp.data.entity.NotificationType
import com.example.fitnesstrackerapp.data.entity.SetType
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import java.util.Date

/**
 * Handles conversion between complex types and primitive types that can be stored
 * in SQLite database. All converters are null-safe and handle edge cases gracefully.
 *
 * Supported Conversions:
 * - Date ↔ Long (timestamp)
 * - All custom enums ↔ String (enum name)
 * - Complex objects ↔ JSON strings (if needed)
 */
class Converters {
    /**
     * Converts timestamp to Date object.
     * @param timestamp Unix timestamp in milliseconds
     * @return Date object or null if timestamp is null
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    /**
     * Converts Date object to timestamp.
     * @param date Date object to convert
     * @return Unix timestamp in milliseconds or null if date is null
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    /**
     * Converts string to MealType enum.
     * @param value String representation of MealType
     * @return MealType enum or null if value is null/invalid
     */
    @TypeConverter
    fun toMealType(value: String?): MealType? {
        return value?.let {
            try {
                MealType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null // Handle invalid enum values gracefully
            }
        }
    }

    /**
     * Converts MealType enum to string.
     * @param mealType MealType enum to convert
     * @return String representation or null if mealType is null
     */
    @TypeConverter
    fun fromMealType(mealType: MealType?): String? {
        return mealType?.name
    }

    /**
     * Converts string to Gender enum.
     * @param value String representation of Gender
     * @return Gender enum or null if value is null/invalid
     */
    @TypeConverter
    fun toGender(value: String?): Gender? {
        return value?.let {
            try {
                Gender.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts Gender enum to string.
     * @param gender Gender enum to convert
     * @return String representation or null if gender is null
     */
    @TypeConverter
    fun fromGender(gender: Gender?): String? {
        return gender?.name
    }

    /**
     * Converts string to ActivityLevel enum.
     * @param value String representation of ActivityLevel
     * @return ActivityLevel enum or null if value is null/invalid
     */
    @TypeConverter
    fun toActivityLevel(value: String?): ActivityLevel? {
        return value?.let {
            try {
                ActivityLevel.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts ActivityLevel enum to string.
     * @param activityLevel ActivityLevel enum to convert
     * @return String representation or null if activityLevel is null
     */
    @TypeConverter
    fun fromActivityLevel(activityLevel: ActivityLevel?): String? {
        return activityLevel?.name
    }

    /**
     * Converts string to GoalType enum.
     * @param value String representation of GoalType
     * @return GoalType enum or null if value is null/invalid
     */
    @TypeConverter
    fun toGoalType(value: String?): GoalType? {
        return value?.let {
            try {
                GoalType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts GoalType enum to string.
     * @param goalType GoalType enum to convert
     * @return String representation or null if goalType is null
     */
    @TypeConverter
    fun fromGoalType(goalType: GoalType?): String? {
        return goalType?.name
    }

    /**
     * Converts string to GoalStatus enum.
     * @param value String representation of GoalStatus
     * @return GoalStatus enum or null if value is null/invalid
     */
    @TypeConverter
    fun toGoalStatus(value: String?): GoalStatus? {
        return value?.let {
            try {
                GoalStatus.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts GoalStatus enum to string.
     * @param goalStatus GoalStatus enum to convert
     * @return String representation or null if goalStatus is null
     */
    @TypeConverter
    fun fromGoalStatus(goalStatus: GoalStatus?): String? {
        return goalStatus?.name
    }

    /**
     * Converts string to WorkoutType enum.
     * @param value String representation of WorkoutType
     * @return WorkoutType enum or null if value is null/invalid
     */
    @TypeConverter
    fun toWorkoutType(value: String?): WorkoutType? {
        return value?.let {
            try {
                WorkoutType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts WorkoutType enum to string.
     * @param workoutType WorkoutType enum to convert
     * @return String representation or null if workoutType is null
     */
    @TypeConverter
    fun fromWorkoutType(workoutType: WorkoutType?): String? {
        return workoutType?.name
    }

    /**
     * Converts string to NotificationType enum.
     * @param value String representation of NotificationType
     * @return NotificationType enum or null if value is null/invalid
     */
    @TypeConverter
    fun toNotificationType(value: String?): NotificationType? {
        return value?.let {
            try {
                NotificationType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts NotificationType enum to string.
     * @param notificationType NotificationType enum to convert
     * @return String representation or null if notificationType is null
     */
    @TypeConverter
    fun fromNotificationType(notificationType: NotificationType?): String? {
        return notificationType?.name
    }

    /**
     * Converts string to NotificationPriority enum.
     * @param value String representation of NotificationPriority
     * @return NotificationPriority enum or null if value is null/invalid
     */
    @TypeConverter
    fun toNotificationPriority(value: String?): NotificationPriority? {
        return value?.let {
            try {
                NotificationPriority.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts NotificationPriority enum to string.
     * @param notificationPriority NotificationPriority enum to convert
     * @return String representation or null if notificationPriority is null
     */
    @TypeConverter
    fun fromNotificationPriority(notificationPriority: NotificationPriority?): String? {
        return notificationPriority?.name
    }

    /**
     * Converts string to NotificationStatus enum.
     * @param value String representation of NotificationStatus
     * @return NotificationStatus enum or null if value is null/invalid
     */
    @TypeConverter
    fun toNotificationStatus(value: String?): NotificationStatus? {
        return value?.let {
            try {
                NotificationStatus.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts NotificationStatus enum to string.
     * @param notificationStatus NotificationStatus enum to convert
     * @return String representation or null if notificationStatus is null
     */
    @TypeConverter
    fun fromNotificationStatus(notificationStatus: NotificationStatus?): String? {
        return notificationStatus?.name
    }

    /**
     * Converts string to MuscleGroup enum.
     * @param value String representation of MuscleGroup
     * @return MuscleGroup enum or null if value is null/invalid
     */
    @TypeConverter
    fun toMuscleGroup(value: String?): MuscleGroup? {
        return value?.let {
            try {
                MuscleGroup.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts MuscleGroup enum to string.
     * @param muscleGroup MuscleGroup enum to convert
     * @return String representation or null if muscleGroup is null
     */
    @TypeConverter
    fun fromMuscleGroup(muscleGroup: MuscleGroup?): String? {
        return muscleGroup?.name
    }

    /**
     * Converts string to EquipmentType enum.
     * @param value String representation of EquipmentType
     * @return EquipmentType enum or null if value is null/invalid
     */
    @TypeConverter
    fun toEquipmentType(value: String?): EquipmentType? {
        return value?.let {
            try {
                EquipmentType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts EquipmentType enum to string.
     * @param equipmentType EquipmentType enum to convert
     * @return String representation or null if equipmentType is null
     */
    @TypeConverter
    fun fromEquipmentType(equipmentType: EquipmentType?): String? {
        return equipmentType?.name
    }

    /**
     * Converts string to ExerciseType enum.
     * @param value String representation of ExerciseType
     * @return ExerciseType enum or null if value is null/invalid
     */
    @TypeConverter
    fun toExerciseType(value: String?): ExerciseType? {
        return value?.let {
            try {
                ExerciseType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts ExerciseType enum to string.
     * @param exerciseType ExerciseType enum to convert
     * @return String representation or null if exerciseType is null
     */
    @TypeConverter
    fun fromExerciseType(exerciseType: ExerciseType?): String? {
        return exerciseType?.name
    }

    /**
     * Converts string to DifficultyLevel enum.
     * @param value String representation of DifficultyLevel
     * @return DifficultyLevel enum or null if value is null/invalid
     */
    @TypeConverter
    fun toDifficultyLevel(value: String?): DifficultyLevel? {
        return value?.let {
            try {
                DifficultyLevel.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts DifficultyLevel enum to string.
     * @param difficultyLevel DifficultyLevel enum to convert
     * @return String representation or null if difficultyLevel is null
     */
    @TypeConverter
    fun fromDifficultyLevel(difficultyLevel: DifficultyLevel?): String? {
        return difficultyLevel?.name
    }

    /**
     * Converts string to SetType enum.
     * @param value String representation of SetType
     * @return SetType enum or null if value is null/invalid
     */
    @TypeConverter
    fun toSetType(value: String?): SetType? {
        return value?.let {
            try {
                SetType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts SetType enum to string.
     * @param setType SetType enum to convert
     * @return String representation or null if setType is null
     */
    @TypeConverter
    fun fromSetType(setType: SetType?): String? {
        return setType?.name
    }

    // ================================
    // Notification-related TypeConverters
    // ================================

    /**
     * Converts string to NotificationEventType enum.
     * @param value String representation of NotificationEventType
     * @return NotificationEventType enum or null if value is null/invalid
     */
    @TypeConverter
    fun toNotificationEventType(value: String?): NotificationEventType? {
        return value?.let {
            try {
                NotificationEventType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts NotificationEventType enum to string.
     * @param notificationEventType NotificationEventType enum to convert
     * @return String representation or null if notificationEventType is null
     */
    @TypeConverter
    fun fromNotificationEventType(notificationEventType: NotificationEventType?): String? {
        return notificationEventType?.name
    }

    /**
     * Converts string to NotificationChannel enum.
     * @param value String representation of NotificationChannel
     * @return NotificationChannel enum or null if value is null/invalid
     */
    @TypeConverter
    fun toNotificationChannel(value: String?): NotificationChannel? {
        return value?.let {
            try {
                NotificationChannel.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts NotificationChannel enum to string.
     * @param notificationChannel NotificationChannel enum to convert
     * @return String representation or null if notificationChannel is null
     */
    @TypeConverter
    fun fromNotificationChannel(notificationChannel: NotificationChannel?): String? {
        return notificationChannel?.name
    }

    /**
     * Converts string to NotificationLogEvent enum.
     * @param value String representation of NotificationLogEvent
     * @return NotificationLogEvent enum or null if value is null/invalid
     */
    @TypeConverter
    fun toNotificationLogEvent(value: String?): NotificationLogEvent? {
        return value?.let {
            try {
                NotificationLogEvent.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts NotificationLogEvent enum to string.
     * @param notificationLogEvent NotificationLogEvent enum to convert
     * @return String representation or null if notificationLogEvent is null
     */
    @TypeConverter
    fun fromNotificationLogEvent(notificationLogEvent: NotificationLogEvent?): String? {
        return notificationLogEvent?.name
    }

    /**
     * Converts string to NotificationDeliveryChannel enum.
     * @param value String representation of NotificationDeliveryChannel
     * @return NotificationDeliveryChannel enum or null if value is null/invalid
     */
    @TypeConverter
    fun toNotificationDeliveryChannel(value: String?): NotificationDeliveryChannel? {
        return value?.let {
            try {
                NotificationDeliveryChannel.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Converts NotificationDeliveryChannel enum to string.
     * @param notificationDeliveryChannel NotificationDeliveryChannel enum to convert
     * @return String representation or null if notificationDeliveryChannel is null
     */
    @TypeConverter
    fun fromNotificationDeliveryChannel(notificationDeliveryChannel: NotificationDeliveryChannel?): String? {
        return notificationDeliveryChannel?.name
    }
}
