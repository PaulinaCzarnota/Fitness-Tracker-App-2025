package com.example.fitnesstrackerapp.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility class for standardized date handling across the app.
 *
 * Features:
 * - Consistent date formatting
 * - Time period calculations
 * - Date range utilities
 * - Time zone handling
 */

object DateUtil {
    private val defaultFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val displayTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    init {
        // Ensure consistent timezone handling
        val timezone = TimeZone.getDefault()
        defaultFormat.timeZone = timezone
        timeFormat.timeZone = timezone
        fullFormat.timeZone = timezone
        displayFormat.timeZone = timezone
        displayTimeFormat.timeZone = timezone
    }

    /**
     * Gets start of day for given date.
     */
    fun getStartOfDay(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    /**
     * Gets end of day for given date.
     */
    fun getEndOfDay(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }

    /**
     * Gets start of week (Sunday) for given date.
     */
    fun getStartOfWeek(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    /**
     * Gets start of month for given date.
     */
    fun getStartOfMonth(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    /**
     * Gets start of year for given date.
     */
    fun getStartOfYear(date: Date = Date()): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    /**
     * Gets a readable string representation of a date.
     */
    fun formatDate(date: Date?): String {
        return date?.let { displayFormat.format(it) } ?: ""
    }

    /**
     * Gets a readable string representation of a time.
     */
    fun formatTime(date: Date?): String {
        return date?.let { displayTimeFormat.format(it) } ?: ""
    }

    /**
     * Gets a full readable string representation of a date and time.
     */
    fun formatDateTime(date: Date?): String {
        return date?.let { "${formatDate(it)} ${formatTime(it)}" } ?: ""
    }

    /**
     * Gets a relative time string (e.g., "2 hours ago", "yesterday").
     */
    fun getRelativeTimeString(date: Date): String {
        val now = Date()
        val diffInMillis = now.time - date.time
        val seconds = diffInMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 365 -> "${days / 365} years ago"
            days > 30 -> "${days / 30} months ago"
            days > 7 -> "${days / 7} weeks ago"
            days > 0 -> if (days == 1L) "yesterday" else "$days days ago"
            hours > 0 -> if (hours == 1L) "1 hour ago" else "$hours hours ago"
            minutes > 0 -> if (minutes == 1L) "1 minute ago" else "$minutes minutes ago"
            else -> "just now"
        }
    }

    /**
     * Gets a date range string (e.g., "Jan 1 - Jan 7, 2024").
     */
    fun getDateRangeString(startDate: Date, endDate: Date): String {
        val startMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(startDate)
        val endMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(endDate)
        val startDay = SimpleDateFormat("d", Locale.getDefault()).format(startDate)
        val endDay = SimpleDateFormat("d", Locale.getDefault()).format(endDate)
        val startYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(startDate)
        val endYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(endDate)

        return if (startYear == endYear) {
            if (startMonth == endMonth) {
                "$startMonth $startDay - $endDay, $startYear"
            } else {
                "$startMonth $startDay - $endMonth $endDay, $startYear"
            }
        } else {
            "$startMonth $startDay, $startYear - $endMonth $endDay, $endYear"
        }
    }

    /**
     * Checks if two dates are on the same day.
     */
    fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Gets the week number for a given date.
     */
    fun getWeekOfYear(date: Date): Int {
        return Calendar.getInstance().apply {
            time = date
            firstDayOfWeek = Calendar.SUNDAY
            minimalDaysInFirstWeek = 1
        }.get(Calendar.WEEK_OF_YEAR)
    }
}
