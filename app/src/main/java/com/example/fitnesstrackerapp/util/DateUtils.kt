package com.example.fitnesstrackerapp.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * formatDate
 *
 * Converts a timestamp (in milliseconds since epoch) to a human-readable date string.
 *
 * Example:
 *     1690240200000 → "24 Jul 2023"
 *
 * @param timestamp The Unix timestamp in milliseconds.
 * @return A formatted string like "dd MMM yyyy" (e.g., "24 Jul 2023").
 */
fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
