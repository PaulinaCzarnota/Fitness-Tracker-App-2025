package com.example.fitnesstrackerapp.data

import androidx.room.TypeConverter
import com.example.fitnesstrackerapp.data.entity.*
import java.util.Date

/**
 * Type converters for Room database.
 *
 * Handles conversion between complex types and primitive types
 * that can be stored in SQLite database.
 */
class Converters {

    // Date converters
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    // MealType converters
    @TypeConverter
    fun toMealType(value: String?): MealType? {
        return value?.let { MealType.valueOf(it) }
    }

    @TypeConverter
    fun fromMealType(mealType: MealType?): String? {
        return mealType?.name
    }

    // Gender converters
    @TypeConverter
    fun toGender(value: String?): Gender? {
        return value?.let { Gender.valueOf(it) }
    }

    @TypeConverter
    fun fromGender(gender: Gender?): String? {
        return gender?.name
    }

    // ActivityLevel converters
    @TypeConverter
    fun toActivityLevel(value: String?): ActivityLevel? {
        return value?.let { ActivityLevel.valueOf(it) }
    }

    @TypeConverter
    fun fromActivityLevel(activityLevel: ActivityLevel?): String? {
        return activityLevel?.name
    }

    // GoalType converters
    @TypeConverter
    fun toGoalType(value: String?): GoalType? {
        return value?.let { GoalType.valueOf(it) }
    }

    @TypeConverter
    fun fromGoalType(goalType: GoalType?): String? {
        return goalType?.name
    }

    // GoalStatus converters
    @TypeConverter
    fun toGoalStatus(value: String?): GoalStatus? {
        return value?.let { GoalStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromGoalStatus(goalStatus: GoalStatus?): String? {
        return goalStatus?.name
    }

    // WorkoutType converters
    @TypeConverter
    fun toWorkoutType(value: String?): WorkoutType? {
        return value?.let { WorkoutType.valueOf(it) }
    }

    @TypeConverter
    fun fromWorkoutType(workoutType: WorkoutType?): String? {
        return workoutType?.name
    }
}
