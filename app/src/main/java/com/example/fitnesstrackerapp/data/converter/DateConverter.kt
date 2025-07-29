package com.example.fitnesstrackerapp.data.converter

import androidx.room.TypeConverter
import java.util.Date

/**
 * Converters to allow Room to reference complex data types.
 * Here we convert between [Date] and its UNIX timestamp representation ([Long]).
 */
class DateConverter {

    /**
     * Converts a UNIX timestamp (stored in the database) into a [Date] object.
     *
     * @param value The timestamp value in milliseconds since epoch, or null.
     * @return A [Date] corresponding to the timestamp, or null if the input was null.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? =
        value?.let { Date(it) }

    /**
     * Converts a [Date] into its UNIX timestamp representation for storage.
     *
     * @param date The [Date] to convert, or null.
     * @return The timestamp in milliseconds since epoch, or null if the input was null.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? =
        date?.time
}
