/**
 * Notification entity and related classes for the Fitness Tracker application.
 *
 * This file contains the Notification entity which stores comprehensive notification data
 * including notification types, scheduling information, delivery status, and user preferences.
 * The entity uses Room database annotations for optimal storage and retrieval performance.
 *
 * Key Features:
 * - Multiple notification types (workout reminders, goal updates, motivational tips)
 * - Scheduling and delivery tracking with timestamps
 * - Priority levels and notification channels management
 * - Read/unread status tracking for user engagement
 * - Custom notification content with titles, messages, and actions
 * - Foreign key relationship with User entity for data integrity
 */

package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Enumeration representing different types of notifications in the fitness app.
 *
 * Each type corresponds to specific app functionality and has different
 * priority levels and handling requirements.
 */
enum class NotificationType {
    WORKOUT_REMINDER,
    GOAL_ACHIEVEMENT,
    GOAL_DEADLINE_APPROACHING,
    DAILY_MOTIVATION,
    STEP_MILESTONE,
    WEEKLY_PROGRESS,
    NUTRITION_REMINDER,
    HYDRATION_REMINDER,
    REST_DAY_REMINDER,
    ACHIEVEMENT_UNLOCKED,
    WORKOUT_STREAK,
    INACTIVE_USER_ENGAGEMENT,
}

/**
 * Enumeration representing notification priority levels.
 *
 * Priority affects how notifications are displayed and whether they
 * interrupt the user's current activity.
 */
enum class NotificationPriority {
    LOW, // Background notifications, tips
    DEFAULT, // Standard reminders
    HIGH, // Important deadlines, achievements
    URGENT, // Critical health or safety notifications
}

/**
 * Enumeration representing notification delivery status.
 *
 * Tracks the lifecycle of notifications from creation to user interaction.
 */
enum class NotificationStatus {
    PENDING, // Scheduled but not yet sent
    SENT, // Delivered to the system
    READ, // User has seen the notification
    DISMISSED, // User has dismissed the notification
    CLICKED, // User has clicked on the notification
    FAILED, // Failed to deliver
    CANCELLED, // Cancelled before delivery
}

/**
 * Entity representing a notification in the Fitness Tracker application.
 *
 * This entity stores comprehensive notification information including type, content,
 * scheduling, delivery status, and user interaction data. All notifications are
 * associated with a specific user through foreign key relationship.
 *
 * Database Features:
 * - Indexed for efficient querying by user, type, status, and scheduling dates
 * - Foreign key constraint ensures data integrity with User entity
 * - Cascading delete removes notifications when user is deleted
 */
@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["type"]),
        Index(value = ["status"]),
        Index(value = ["scheduled_time"]),
        Index(value = ["is_read"]),
        Index(value = ["user_id", "type"]),
        Index(value = ["user_id", "status"]),
        Index(value = ["user_id", "scheduled_time"]),
    ],
)
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "type")
    val type: NotificationType,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "message")
    val message: String,
    @ColumnInfo(name = "priority")
    val priority: NotificationPriority = NotificationPriority.DEFAULT,
    @ColumnInfo(name = "status")
    val status: NotificationStatus = NotificationStatus.PENDING,
    @ColumnInfo(name = "scheduled_time")
    val scheduledTime: Date,
    @ColumnInfo(name = "sent_time")
    val sentTime: Date? = null,
    @ColumnInfo(name = "read_time")
    val readTime: Date? = null,
    @ColumnInfo(name = "dismissed_time")
    val dismissedTime: Date? = null,
    @ColumnInfo(name = "clicked_time")
    val clickedTime: Date? = null,
    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,
    @ColumnInfo(name = "is_recurring")
    val isRecurring: Boolean = false,
    @ColumnInfo(name = "recurrence_pattern")
    val recurrencePattern: String? = null, // JSON string for recurrence rules
    @ColumnInfo(name = "channel_id")
    val channelId: String, // Android notification channel ID
    @ColumnInfo(name = "notification_id")
    val notificationId: Int? = null, // System notification ID for cancellation
    @ColumnInfo(name = "action_data")
    val actionData: String? = null, // JSON string for additional action data
    @ColumnInfo(name = "related_entity_id")
    val relatedEntityId: Long? = null, // ID of related goal, workout, etc.
    @ColumnInfo(name = "related_entity_type")
    val relatedEntityType: String? = null, // Type of related entity (goal, workout, etc.)
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    @ColumnInfo(name = "max_retries")
    val maxRetries: Int = 3,
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
) {
    /**
     * Checks if the notification is overdue for delivery.
     * @return true if scheduled time has passed and notification is still pending
     */
    fun isOverdue(): Boolean {
        return status == NotificationStatus.PENDING && scheduledTime.before(Date())
    }

    /**
     * Checks if the notification can be retried.
     * @return true if retry count hasn't exceeded max retries
     */
    fun canRetry(): Boolean {
        return retryCount < maxRetries && status == NotificationStatus.FAILED
    }

    /**
     * Gets the time elapsed since notification was sent.
     * @return Elapsed time in milliseconds, or null if not sent
     */
    fun getTimeSinceSent(): Long? {
        return sentTime?.let { Date().time - it.time }
    }

    /**
     * Checks if the notification was interacted with (clicked or dismissed).
     * @return true if user has interacted with the notification
     */
    fun hasUserInteracted(): Boolean {
        return clickedTime != null || dismissedTime != null
    }

    /**
     * Gets the response time from sent to clicked.
     * @return Response time in milliseconds, or null if not clicked
     */
    fun getResponseTime(): Long? {
        return if (sentTime != null && clickedTime != null) {
            clickedTime.time - sentTime.time
        } else {
            null
        }
    }

    /**
     * Checks if notification is active (sent but not dismissed/clicked).
     * @return true if notification is currently active
     */
    fun isActive(): Boolean {
        return status == NotificationStatus.SENT && !hasUserInteracted()
    }

    /**
     * Gets formatted scheduled time string.
     * @return Formatted date and time string
     */
    fun getFormattedScheduledTime(): String {
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault())
        return formatter.format(scheduledTime)
    }

    /**
     * Gets time until scheduled delivery.
     * @return Milliseconds until scheduled time (negative if overdue)
     */
    fun getTimeUntilDelivery(): Long {
        return scheduledTime.time - Date().time
    }

    /**
     * Checks if notification is scheduled for today.
     * @return true if scheduled date is today
     */
    fun isScheduledForToday(): Boolean {
        val today = java.util.Calendar.getInstance()
        val scheduled = java.util.Calendar.getInstance().apply { time = scheduledTime }

        return today.get(java.util.Calendar.YEAR) == scheduled.get(java.util.Calendar.YEAR) &&
            today.get(java.util.Calendar.DAY_OF_YEAR) == scheduled.get(java.util.Calendar.DAY_OF_YEAR)
    }

    /**
     * Gets notification urgency level based on priority and time.
     * @return Urgency level string
     */
    fun getUrgencyLevel(): String {
        val hoursUntilDelivery = getTimeUntilDelivery() / (1000 * 60 * 60)

        return when {
            priority == NotificationPriority.URGENT -> "Critical"
            priority == NotificationPriority.HIGH && hoursUntilDelivery <= 1 -> "Urgent"
            priority == NotificationPriority.HIGH -> "High"
            hoursUntilDelivery <= 0 && status == NotificationStatus.PENDING -> "Overdue"
            hoursUntilDelivery <= 6 -> "Soon"
            else -> "Normal"
        }
    }

    /**
     * Validates if the notification data is consistent and valid.
     * @return true if notification data is valid, false otherwise
     */
    fun isValid(): Boolean {
        return title.isNotBlank() &&
            message.isNotBlank() &&
            channelId.isNotBlank() &&
            retryCount in 0..maxRetries &&
            (relatedEntityType == null || relatedEntityId != null) &&
            scheduledTime.after(Date(0)) // Not epoch time
    }

    companion object {
        const val DEFAULT_MAX_RETRIES = 3
        const val MAX_TITLE_LENGTH = 100
        const val MAX_MESSAGE_LENGTH = 500
    }
}
