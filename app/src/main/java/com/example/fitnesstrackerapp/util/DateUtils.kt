package com.example.fitnesstrackerapp.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * formatDate
 *
 * Converts a Unix timestamp (in milliseconds) to a formatted, human-readable date string.
 * This is useful for displaying readable dates in your app UI.
 *
 * @param timestamp The Unix timestamp in milliseconds.
 *        Example: System.currentTimeMillis() or value stored in database.
 *
 * @param pattern The desired date format pattern (optional).
 *        Default: "dd MMM yyyy" → outputs like "27 Jul 2025".
 *        You may also use other valid patterns like:
 *            - "yyyy-MM-dd"
 *            - "MMM dd, yyyy"
 *            - "EEEE, MMM d, yyyy"
 *
 * @return A formatted date string using the specified pattern and system locale.
 *
 * @sample
 *     formatDate(1690240200000) → "24 Jul 2023"
 */
fun formatDate(timestamp: Long, pattern: String = "dd MMM yyyy"): String {
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(Date(timestamp))
}
