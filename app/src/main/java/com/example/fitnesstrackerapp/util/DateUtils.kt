package com.example.fitnesstrackerapp.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Formats a timestamp (in millis) into a readable date string.
 */
fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
