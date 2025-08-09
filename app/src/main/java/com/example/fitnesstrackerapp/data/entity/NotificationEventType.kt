package com.example.fitnesstrackerapp.data.entity

/**
 * Enumeration representing notification event types.
 * 
 * Used to track different types of notification events
 * throughout the notification lifecycle.
 */
enum class NotificationEventType {
    SCHEDULED, // Notification was scheduled
    SENT, // Successfully sent to system
    DELIVERED, // Delivered to device
    DISPLAYED, // Shown to user
    OPENED, // User tapped notification
    DISMISSED, // User dismissed notification
    ACTION_CLICKED, // User clicked action button
    EXPIRED, // Notification expired
    CANCELLED, // Notification was cancelled
    FAILED, // Delivery failed
    RETRIED, // Delivery was retried
    ERROR, // An error occurred
}
