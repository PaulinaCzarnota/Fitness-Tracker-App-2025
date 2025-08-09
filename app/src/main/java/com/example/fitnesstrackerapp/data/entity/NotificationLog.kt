/**
 * Notification Log entity and related classes for the Fitness Tracker application.
 *
 * This file contains the NotificationLog entity which stores comprehensive notification
 * interaction and delivery history. This is separate from the main Notification entity
 * to track the actual delivery status and user interactions over time.
 *
 * Key Features:
 * - Complete notification delivery tracking
 * - User interaction logging (opens, dismisses, actions)
 * - Error and retry tracking for failed deliveries
 * - Analytics data for notification effectiveness
 * - Performance metrics for notification optimization
 * - Foreign key relationship with Notification and User entities
 */

package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Enumeration representing notification log event types.
 *
 * Tracks the different types of events that can occur in the
 * notification lifecycle for analytics and debugging purposes.
 */
enum class NotificationLogEvent {
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

/**
 * Enumeration representing notification delivery channels.
 *
 * Tracks which delivery channel was used for the notification
 * to help optimize delivery strategies.
 */
enum class NotificationDeliveryChannel {
    SYSTEM, // Android system notification
    IN_APP, // In-app notification
    PUSH, // Push notification service
    EMAIL, // Email notification
    SMS, // SMS notification (future feature)
    WEBHOOK, // Webhook delivery (future feature)
}

/**
 * Entity representing a notification log entry in the Fitness Tracker application.
 *
 * This entity stores detailed logging information about notification events,
 * delivery status, user interactions, and system responses. It provides
 * comprehensive tracking for notification analytics and debugging.
 *
 * Database Features:
 * - Indexed for efficient querying by user, notification, event type, and timestamp
 * - Foreign key constraints ensure data integrity with User and Notification entities
 * - Cascading delete removes logs when parent entities are deleted
 * - Optimized for analytics queries and reporting
 */
@Entity(
    tableName = "notification_logs",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Notification::class,
            parentColumns = ["id"],
            childColumns = ["notification_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["notification_id"]),
        Index(value = ["event_type"]),
        Index(value = ["delivery_channel"]),
        Index(value = ["event_timestamp"]),
        Index(value = ["user_id", "event_type"]),
        Index(value = ["notification_id", "event_type"]),
        Index(value = ["user_id", "event_timestamp"]),
        Index(value = ["delivery_channel", "event_type"]),
        Index(value = ["is_success", "event_type"]),
    ],
)
data class NotificationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "notification_id")
    val notificationId: Long,

    @ColumnInfo(name = "event_type")
    val eventType: NotificationLogEvent,

    @ColumnInfo(name = "event_timestamp")
    val eventTimestamp: Date = Date(),

    @ColumnInfo(name = "delivery_channel")
    val deliveryChannel: NotificationDeliveryChannel,

    @ColumnInfo(name = "is_success")
    val isSuccess: Boolean = true,

    @ColumnInfo(name = "error_code")
    val errorCode: String? = null,

    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,

    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,

    @ColumnInfo(name = "device_token")
    val deviceToken: String? = null,

    @ColumnInfo(name = "platform_response")
    val platformResponse: String? = null, // JSON string with platform-specific data

    @ColumnInfo(name = "user_agent")
    val userAgent: String? = null,

    @ColumnInfo(name = "ip_address")
    val ipAddress: String? = null,

    @ColumnInfo(name = "app_version")
    val appVersion: String? = null,

    @ColumnInfo(name = "os_version")
    val osVersion: String? = null,

    @ColumnInfo(name = "processing_duration_ms")
    val processingDurationMs: Long = 0,

    @ColumnInfo(name = "delivery_duration_ms")
    val deliveryDurationMs: Long? = null,

    @ColumnInfo(name = "interaction_data")
    val interactionData: String? = null, // JSON string with interaction details

    @ColumnInfo(name = "action_taken")
    val actionTaken: String? = null, // Specific action if action button was clicked

    @ColumnInfo(name = "session_id")
    val sessionId: String? = null,

    @ColumnInfo(name = "batch_id")
    val batchId: String? = null, // For bulk notification tracking

    @ColumnInfo(name = "priority_level")
    val priorityLevel: Int = 0, // 0-5, 5 being highest priority

    @ColumnInfo(name = "experiment_id")
    val experimentId: String? = null, // For A/B testing notifications

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
) {
    /**
     * Checks if this log entry represents a successful event.
     * @return true if the event was successful
     */
    fun wasSuccessful(): Boolean = isSuccess

    /**
     * Checks if this log entry represents a user interaction event.
     * @return true if user interacted with the notification
     */
    fun isUserInteraction(): Boolean {
        return eventType in listOf(
            NotificationLogEvent.OPENED,
            NotificationLogEvent.DISMISSED,
            NotificationLogEvent.ACTION_CLICKED,
        )
    }

    /**
     * Checks if this log entry represents an error event.
     * @return true if this is an error or failure event
     */
    fun isError(): Boolean {
        return !isSuccess || eventType in listOf(
            NotificationLogEvent.FAILED,
            NotificationLogEvent.ERROR,
            NotificationLogEvent.EXPIRED,
        )
    }

    /**
     * Gets the time elapsed since this event occurred.
     * @return Duration in milliseconds since the event
     */
    fun getTimeSinceEvent(): Long {
        return Date().time - eventTimestamp.time
    }

    /**
     * Gets formatted event timestamp for display.
     * @return Formatted timestamp string
     */
    fun getFormattedEventTime(): String {
        val formatter = java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS",
            java.util.Locale.getDefault(),
        )
        return formatter.format(eventTimestamp)
    }

    /**
     * Gets formatted processing duration for performance analysis.
     * @return Formatted duration string
     */
    fun getFormattedProcessingDuration(): String {
        return if (processingDurationMs > 0) {
            "${processingDurationMs}ms"
        } else {
            "N/A"
        }
    }

    /**
     * Gets formatted delivery duration for performance analysis.
     * @return Formatted duration string or null if not available
     */
    fun getFormattedDeliveryDuration(): String? {
        return deliveryDurationMs?.let { "${it}ms" }
    }

    /**
     * Gets the total duration from processing to delivery.
     * @return Total duration in milliseconds, or null if delivery time not available
     */
    fun getTotalDuration(): Long? {
        return deliveryDurationMs?.let { processingDurationMs + it }
    }

    /**
     * Gets the performance rating based on processing and delivery times.
     * @return Performance rating from "Excellent" to "Poor"
     */
    fun getPerformanceRating(): String {
        val totalDuration = getTotalDuration() ?: processingDurationMs

        return when {
            totalDuration <= 100 -> "Excellent" // < 100ms
            totalDuration <= 500 -> "Good" // < 500ms
            totalDuration <= 1000 -> "Fair" // < 1s
            totalDuration <= 3000 -> "Slow" // < 3s
            else -> "Poor" // > 3s
        }
    }

    /**
     * Checks if this event indicates a delivery issue that should be retried.
     * @return true if the event suggests a retry is warranted
     */
    fun shouldRetry(): Boolean {
        return eventType == NotificationLogEvent.FAILED &&
            retryCount < MAX_RETRY_COUNT &&
            errorCode != PERMANENT_FAILURE_CODE
    }

    /**
     * Gets the severity level of this log entry.
     * @return Severity level from 1 (info) to 5 (critical)
     */
    fun getSeverityLevel(): Int {
        return when {
            eventType == NotificationLogEvent.ERROR && !isSuccess -> 5 // Critical
            eventType == NotificationLogEvent.FAILED -> 4 // High
            eventType == NotificationLogEvent.EXPIRED -> 3 // Medium
            !isSuccess -> 2 // Low
            else -> 1 // Info
        }
    }

    /**
     * Gets a human-readable description of this log entry.
     * @return Description string for logging or display
     */
    fun getDescription(): String {
        val baseDescription = when (eventType) {
            NotificationLogEvent.SCHEDULED -> "Notification scheduled for delivery"
            NotificationLogEvent.SENT -> "Notification sent to delivery service"
            NotificationLogEvent.DELIVERED -> "Notification delivered to device"
            NotificationLogEvent.DISPLAYED -> "Notification displayed to user"
            NotificationLogEvent.OPENED -> "User opened notification"
            NotificationLogEvent.DISMISSED -> "User dismissed notification"
            NotificationLogEvent.ACTION_CLICKED -> "User clicked action: ${actionTaken ?: "unknown"}"
            NotificationLogEvent.EXPIRED -> "Notification expired before delivery"
            NotificationLogEvent.CANCELLED -> "Notification cancelled"
            NotificationLogEvent.FAILED -> "Notification delivery failed"
            NotificationLogEvent.RETRIED -> "Notification delivery retried"
            NotificationLogEvent.ERROR -> "Error occurred during notification processing"
        }

        return if (!isSuccess && errorMessage != null) {
            "$baseDescription: $errorMessage"
        } else {
            baseDescription
        }
    }

    /**
     * Validates if the log entry data is consistent and valid.
     * @return true if log entry data is valid, false otherwise
     */
    fun isValid(): Boolean {
        return userId > 0 &&
            notificationId > 0 &&
            retryCount >= 0 &&
            retryCount <= MAX_RETRY_COUNT &&
            priorityLevel in 0..5 &&
            processingDurationMs >= 0 &&
            (deliveryDurationMs == null || deliveryDurationMs >= 0) &&
            (!isError() || errorCode != null) // Error events must have error codes
    }

    companion object {
        const val MAX_RETRY_COUNT = 5
        const val PERMANENT_FAILURE_CODE = "PERMANENT_FAILURE"
        const val TEMPORARY_FAILURE_CODE = "TEMPORARY_FAILURE"
        const val RATE_LIMIT_CODE = "RATE_LIMIT_EXCEEDED"
        const val DEVICE_UNREACHABLE_CODE = "DEVICE_UNREACHABLE"
        const val INVALID_TOKEN_CODE = "INVALID_TOKEN"

        // Performance thresholds in milliseconds
        const val EXCELLENT_PERFORMANCE_MS = 100L
        const val GOOD_PERFORMANCE_MS = 500L
        const val FAIR_PERFORMANCE_MS = 1000L
        const val SLOW_PERFORMANCE_MS = 3000L

        // Priority levels
        const val PRIORITY_LOWEST = 0
        const val PRIORITY_LOW = 1
        const val PRIORITY_NORMAL = 2
        const val PRIORITY_HIGH = 3
        const val PRIORITY_URGENT = 4
        const val PRIORITY_CRITICAL = 5
    }
}
