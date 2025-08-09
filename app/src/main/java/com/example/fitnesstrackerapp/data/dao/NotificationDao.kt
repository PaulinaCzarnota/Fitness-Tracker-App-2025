package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.Notification
import com.example.fitnesstrackerapp.data.entity.NotificationPriority
import com.example.fitnesstrackerapp.data.entity.NotificationStatus
import com.example.fitnesstrackerapp.data.entity.NotificationType
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Notification Data Access Object for the Fitness Tracker application.
 *
 * This DAO provides comprehensive database operations for Notification entities including
 * notification scheduling, delivery tracking, user interaction management, and analytics.
 * All operations are coroutine-based for optimal performance and UI responsiveness.
 *
 * Key Features:
 * - Notification creation, updates, and deletion with proper scheduling
 * - Status tracking from pending to delivered with user interaction data
 * - Priority-based queries for notification management
 * - Analytics queries for notification effectiveness tracking
 * - Bulk operations for cleanup and maintenance
 * - Recurring notification pattern management
 */
@Dao
interface NotificationDao {
    /**
     * Inserts a new notification into the database.
     *
     * @param notification Notification entity to insert
     * @return The ID of the inserted notification
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    /**
     * Alternative insert method for compatibility.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: Notification): Long

    /**
     * Inserts multiple notifications into the database.
     *
     * @param notifications List of Notification entities to insert
     * @return List of IDs of the inserted notifications
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<Notification>): List<Long>

    /**
     * Updates an existing notification in the database.
     *
     * @param notification Notification entity with updated data
     */
    @Update
    suspend fun updateNotification(notification: Notification)

    /**
     * Alternative update method for compatibility.
     */
    @Update
    suspend fun update(notification: Notification)

    /**
     * Deletes a notification from the database.
     *
     * @param notification Notification entity to delete
     */
    @Delete
    suspend fun deleteNotification(notification: Notification)

    /**
     * Alternative delete method for compatibility.
     */
    @Delete
    suspend fun delete(notification: Notification)

    /**
     * Deletes a notification by its ID.
     *
     * @param notificationId Notification ID to delete
     */
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotificationById(notificationId: Long)

    /**
     * Gets a notification by its ID.
     *
     * @param notificationId Notification ID to search for
     * @return Notification entity or null if not found
     */
    @Query("SELECT * FROM notifications WHERE id = :notificationId LIMIT 1")
    suspend fun getNotificationById(notificationId: Long): Notification?

    /**
     * Gets all notifications for a specific user as a Flow.
     *
     * @param userId User ID
     * @return Flow of list of notifications ordered by scheduled time descending
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY scheduled_time DESC")
    fun getNotificationsByUserId(userId: Long): Flow<List<Notification>>

    /**
     * Gets unread notifications for a user.
     *
     * @param userId User ID
     * @return Flow of list of unread notifications
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND is_read = 0 ORDER BY scheduled_time DESC")
    fun getUnreadNotifications(userId: Long): Flow<List<Notification>>

    /**
     * Gets notifications by type for a user.
     *
     * @param userId User ID
     * @param type Notification type
     * @return Flow of list of notifications of the specified type
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND type = :type ORDER BY scheduled_time DESC")
    fun getNotificationsByType(userId: Long, type: NotificationType): Flow<List<Notification>>

    /**
     * Gets notifications by status for a user.
     *
     * @param userId User ID
     * @param status Notification status
     * @return Flow of list of notifications with the specified status
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND status = :status ORDER BY scheduled_time DESC")
    fun getNotificationsByStatus(userId: Long, status: NotificationStatus): Flow<List<Notification>>

    /**
     * Gets pending notifications that are due for delivery.
     *
     * @param currentTime Current timestamp to check against
     * @return List of notifications ready to be sent
     */
    @Query("SELECT * FROM notifications WHERE status = 'PENDING' AND scheduled_time <= :currentTime ORDER BY priority DESC, scheduled_time ASC")
    suspend fun getPendingNotificationsDue(currentTime: Date): List<Notification>

    /**
     * Gets overdue pending notifications.
     *
     * @param currentTime Current timestamp
     * @return List of overdue notifications
     */
    @Query("SELECT * FROM notifications WHERE status = 'PENDING' AND scheduled_time < :currentTime ORDER BY scheduled_time ASC")
    suspend fun getOverdueNotifications(currentTime: Date): List<Notification>

    /**
     * Gets notifications by priority level.
     *
     * @param userId User ID
     * @param priority Notification priority
     * @return Flow of notifications with specified priority
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND priority = :priority ORDER BY scheduled_time DESC")
    fun getNotificationsByPriority(userId: Long, priority: NotificationPriority): Flow<List<Notification>>

    /**
     * Gets recent notifications with limit.
     *
     * @param userId User ID
     * @param limit Maximum number of notifications to return
     * @return Flow of recent notifications
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    fun getRecentNotifications(userId: Long, limit: Int): Flow<List<Notification>>

    /**
     * Gets notifications within a date range.
     *
     * @param userId User ID
     * @param startDate Start date of range
     * @param endDate End date of range
     * @return Flow of notifications in date range
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND scheduled_time BETWEEN :startDate AND :endDate ORDER BY scheduled_time DESC")
    fun getNotificationsInDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Notification>>

    /**
     * Updates notification status.
     *
     * @param notificationId Notification ID
     * @param status New status
     * @param updatedAt Update timestamp
     */
    @Query("UPDATE notifications SET status = :status, updated_at = :updatedAt WHERE id = :notificationId")
    suspend fun updateNotificationStatus(notificationId: Long, status: NotificationStatus, updatedAt: Date)

    /**
     * Marks notification as sent.
     *
     * @param notificationId Notification ID
     * @param sentTime Time when notification was sent
     */
    @Query("UPDATE notifications SET status = 'SENT', sent_time = :sentTime, updated_at = :sentTime WHERE id = :notificationId")
    suspend fun markNotificationAsSent(notificationId: Long, sentTime: Date)

    /**
     * Marks notification as read.
     *
     * @param notificationId Notification ID
     * @param readTime Time when notification was read
     */
    @Query("UPDATE notifications SET status = 'READ', is_read = 1, read_time = :readTime, updated_at = :readTime WHERE id = :notificationId")
    suspend fun markNotificationAsRead(notificationId: Long, readTime: Date)

    /**
     * Marks notification as clicked.
     *
     * @param notificationId Notification ID
     * @param clickedTime Time when notification was clicked
     */
    @Query("UPDATE notifications SET status = 'CLICKED', is_read = 1, clicked_time = :clickedTime, updated_at = :clickedTime WHERE id = :notificationId")
    suspend fun markNotificationAsClicked(notificationId: Long, clickedTime: Date)

    /**
     * Marks notification as dismissed.
     *
     * @param notificationId Notification ID
     * @param dismissedTime Time when notification was dismissed
     */
    @Query("UPDATE notifications SET status = 'DISMISSED', dismissed_time = :dismissedTime, updated_at = :dismissedTime WHERE id = :notificationId")
    suspend fun markNotificationAsDismissed(notificationId: Long, dismissedTime: Date)

    /**
     * Increments retry count for failed notification.
     *
     * @param notificationId Notification ID
     * @param updatedAt Update timestamp
     */
    @Query("UPDATE notifications SET retry_count = retry_count + 1, updated_at = :updatedAt WHERE id = :notificationId")
    suspend fun incrementRetryCount(notificationId: Long, updatedAt: Date)

    /**
     * Updates notification system ID for cancellation purposes.
     *
     * @param notificationId Notification database ID
     * @param systemNotificationId System notification ID
     */
    @Query("UPDATE notifications SET notification_id = :systemNotificationId WHERE id = :notificationId")
    suspend fun updateSystemNotificationId(notificationId: Long, systemNotificationId: Int)

    /**
     * Gets notifications that can be retried (failed with retry count < max).
     *
     * @return List of notifications eligible for retry
     */
    @Query("SELECT * FROM notifications WHERE status = 'FAILED' AND retry_count < max_retries ORDER BY scheduled_time ASC")
    suspend fun getRetryableNotifications(): List<Notification>

    /**
     * Gets active recurring notifications.
     *
     * @param userId User ID
     * @return Flow of recurring notifications
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND is_recurring = 1 AND status != 'CANCELLED' ORDER BY scheduled_time DESC")
    fun getRecurringNotifications(userId: Long): Flow<List<Notification>>

    /**
     * Gets notification count by type for analytics.
     *
     * @param userId User ID
     * @param type Notification type
     * @return Total count of notifications of specified type
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND type = :type")
    suspend fun getNotificationCountByType(userId: Long, type: NotificationType): Int

    /**
     * Gets notification count by status for analytics.
     *
     * @param userId User ID
     * @param status Notification status
     * @return Count of notifications with specified status
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND status = :status")
    suspend fun getNotificationCountByStatus(userId: Long, status: NotificationStatus): Int

    /**
     * Gets total unread notification count.
     *
     * @param userId User ID
     * @return Number of unread notifications
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND is_read = 0")
    suspend fun getUnreadNotificationCount(userId: Long): Int

    /**
     * Gets notification delivery success rate for analytics.
     *
     * @param userId User ID
     * @return Percentage of successfully delivered notifications
     */
    @Query(
        """
        SELECT
        CASE
            WHEN COUNT(*) = 0 THEN 0.0
            ELSE (CAST(SUM(CASE WHEN status IN ('SENT', 'read', 'DISMISSED', 'CLICKED') THEN 1 ELSE 0 END) AS REAL) / COUNT(*)) * 100
        END
        FROM notifications WHERE user_id = :userId
    """,
    )
    suspend fun getNotificationDeliverySuccessRate(userId: Long): Double

    /**
     * Gets average notification response time in milliseconds.
     *
     * @param userId User ID
     * @return Average time from sent to clicked in milliseconds
     */
    @Query(
        """
        SELECT AVG(clicked_time - sent_time)
        FROM notifications
        WHERE user_id = :userId AND clicked_time IS NOT NULL AND sent_time IS NOT NULL
    """,
    )
    suspend fun getAverageResponseTime(userId: Long): Long?

    /**
     * Deletes old notifications older than specified date.
     *
     * @param userId User ID
     * @param olderThan Cutoff date for deletion
     */
    @Query("DELETE FROM notifications WHERE user_id = :userId AND created_at < :olderThan")
    suspend fun deleteOldNotifications(userId: Long, olderThan: Date)

    /**
     * Deletes notifications by status.
     *
     * @param userId User ID
     * @param status Status to delete
     */
    @Query("DELETE FROM notifications WHERE user_id = :userId AND status = :status")
    suspend fun deleteNotificationsByStatus(userId: Long, status: NotificationStatus)

    /**
     * Deletes all notifications for a user (for account deletion).
     *
     * @param userId User ID
     */
    @Query("DELETE FROM notifications WHERE user_id = :userId")
    suspend fun deleteAllNotificationsForUser(userId: Long)

    /**
     * Searches notifications by title or message content.
     *
     * @param userId User ID
     * @param searchQuery Search query for title or message
     * @return Flow of matching notifications
     */
    @Query(
        """
        SELECT * FROM notifications
        WHERE user_id = :userId
        AND (title LIKE '%' || :searchQuery || '%' OR message LIKE '%' || :searchQuery || '%')
        ORDER BY scheduled_time DESC
    """,
    )
    fun searchNotifications(userId: Long, searchQuery: String): Flow<List<Notification>>

    /**
     * Gets notifications related to specific entity (goal, workout, etc.).
     *
     * @param userId User ID
     * @param entityType Type of related entity
     * @param entityId ID of related entity
     * @return Flow of related notifications
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND related_entity_type = :entityType AND related_entity_id = :entityId ORDER BY scheduled_time DESC")
    fun getNotificationsByRelatedEntity(userId: Long, entityType: String, entityId: Long): Flow<List<Notification>>

    /**
     * Cancels pending notifications for specific entity.
     *
     * @param userId User ID
     * @param entityType Type of related entity
     * @param entityId ID of related entity
     */
    @Query("UPDATE notifications SET status = 'CANCELLED', updated_at = :currentTime WHERE user_id = :userId AND related_entity_type = :entityType AND related_entity_id = :entityId AND status = 'PENDING'")
    suspend fun cancelNotificationsForEntity(userId: Long, entityType: String, entityId: Long, currentTime: Date)

    /**
     * Gets notification statistics for user dashboard.
     *
     * @param userId User ID
     * @return NotificationStats with counts and metrics
     */
    data class NotificationStats(
        val totalNotifications: Int,
        val unreadCount: Int,
        val pendingCount: Int,
        val sentCount: Int,
        val clickedCount: Int,
        val deliverySuccessRate: Double,
    )

    @Query(
        """
        SELECT
        COUNT(*) as totalNotifications,
        SUM(CASE WHEN is_read = 0 THEN 1 ELSE 0 END) as unreadCount,
        SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) as pendingCount,
        SUM(CASE WHEN status = 'SENT' THEN 1 ELSE 0 END) as sentCount,
        SUM(CASE WHEN status = 'CLICKED' THEN 1 ELSE 0 END) as clickedCount,
        CASE
            WHEN COUNT(*) = 0 THEN 0.0
            ELSE (CAST(SUM(CASE WHEN status IN ('SENT', 'read', 'DISMISSED', 'CLICKED') THEN 1 ELSE 0 END) AS REAL) / COUNT(*)) * 100
        END as deliverySuccessRate
        FROM notifications WHERE user_id = :userId
    """,
    )
    suspend fun getNotificationStats(userId: Long): NotificationStats

    /**
     * Gets most recent notification by type.
     *
     * @param userId User ID
     * @param type Notification type
     * @return Most recent notification of specified type
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND type = :type ORDER BY created_at DESC LIMIT 1")
    suspend fun getMostRecentNotificationByType(userId: Long, type: NotificationType): Notification?

    /**
     * Marks all notifications as read for a user.
     *
     * @param userId User ID
     * @param readTime Time when marked as read
     */
    @Query("UPDATE notifications SET is_read = 1, read_time = :readTime, updated_at = :readTime WHERE user_id = :userId AND is_read = 0")
    suspend fun markAllNotificationsAsRead(userId: Long, readTime: Date)

    /**
     * Gets today's notifications for a user.
     *
     * @param userId User ID
     * @param startOfDay Start of current day timestamp
     * @param endOfDay End of current day timestamp
     * @return Flow of today's notifications
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND scheduled_time BETWEEN :startOfDay AND :endOfDay ORDER BY scheduled_time DESC")
    fun getTodaysNotifications(userId: Long, startOfDay: Date, endOfDay: Date): Flow<List<Notification>>

    /**
     * Deletes all notifications (for testing purposes only).
     */
    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()
}
