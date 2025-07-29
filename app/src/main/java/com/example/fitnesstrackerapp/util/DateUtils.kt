package com.example.fitnesstrackerapp.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * formatDate
 *
 * Converts a Unix timestamp (in milliseconds) into a human-readable date string.
 * Used to display readable dates from stored timestamps in the UI.
 *
 * @param timestamp The Unix timestamp in milliseconds.
 *        Example: System.currentTimeMillis() or timestamp retrieved from Room.
 *
 * @param pattern The date format pattern (default: "dd MMM yyyy").
 *        Other valid patterns:
 *          - "yyyy-MM-dd"
 *          - "MMM dd, yyyy"
 *          - "EEEE, MMM d, yyyy"
 *
 * @return A formatted date string using the provided pattern and system locale.
 *
 * @sample
 *     formatDate(1722037200000) → "26 Jul 2024"
 */
fun formatDate(timestamp: Long, pattern: String = "dd MMM yyyy"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(Date(timestamp))
}
