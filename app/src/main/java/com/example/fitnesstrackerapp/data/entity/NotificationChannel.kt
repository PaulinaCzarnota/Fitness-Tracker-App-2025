package com.example.fitnesstrackerapp.data.entity

/**
 * Enumeration representing notification delivery channels.
 * 
 * Tracks which delivery channel was used for the notification
 * to help optimize delivery strategies.
 */
enum class NotificationChannel {
    SYSTEM, // Android system notification
    IN_APP, // In-app notification
    PUSH, // Push notification service
    EMAIL, // Email notification
    SMS, // SMS notification (future feature)
    WEBHOOK, // Webhook delivery (future feature)
}
