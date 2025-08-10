package com.example.fitnesstrackerapp.data.model

import androidx.room.ColumnInfo

/**
 * Data model for error frequency analytics.
 *
 * Tracks the frequency of specific error codes
 * for notification failure analysis.
 */
data class ErrorFrequency(
    @ColumnInfo(name = "error_code")
    val errorCode: String,
    @ColumnInfo(name = "frequency")
    val count: Int = 0,
) {
    // Computed properties for additional analytics
    val errorMessage: String? get() = null
    val percentage: Double get() = 0.0
    val lastOccurrence: java.util.Date? get() = null
}
