package com.example.fitnesstrackerapp.data

import androidx.room.TypeConverter import java.util.Date

/**
 * Room database type converters for custom data types.
 *
 * Provides conversion between complex types and primitive types that Room can store:
 * - Date <-> Long (timestamp)
 * - List<String> <-> String (comma-separated)
 * - List<Float> <-> String (comma-separated)
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun fromFloatList(value: List<Float>?): String? {
        return value?.joinToString(",")
    }

    @TypeConverter
    fun toFloatList(value: String?): List<Float>? {
        return value?.split(",")?.mapNotNull {
            try {
                it.trim().toFloat()
            } catch (e: NumberFormatException) {
                null
            }
        }
    }
}
