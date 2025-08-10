package com.example.fitnesstrackerapp.data.entity

/**
 * Enumeration representing notification priority levels.
 *
 * Used to categorize notifications by their importance
 * and determine delivery behavior.
 */
enum class NotificationPriority {
    LOW, // Low priority notifications
    MEDIUM, // Medium priority notifications (default)
    HIGH, // High priority notifications
    URGENT, // Urgent notifications
    CRITICAL, // Critical notifications
}
