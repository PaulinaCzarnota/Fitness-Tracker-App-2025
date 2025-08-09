package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.NotificationDeliveryChannel
import com.example.fitnesstrackerapp.data.entity.NotificationLog
import com.example.fitnesstrackerapp.data.entity.NotificationLogEvent
import com.example.fitnesstrackerapp.data.model.NotificationAnalytics
import com.example.fitnesstrackerapp.data.model.NotificationPerformanceMetrics
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Notification Log Data Access Object for the Fitness Tracker application.
 *
 * This DAO provides comprehensive database operations for NotificationLog entities including
 * notification delivery tracking, user interaction analytics, error monitoring, and
 * performance metrics collection. All operations are coroutine-based for optimal
 * performance and UI responsiveness.
 *
 * Key Features:
 * - Complete CRUD operations for notification logs
 * - Delivery success and failure tracking
 * - User interaction analytics and engagement metrics
 * - Performance monitoring and optimization data
 * - Error tracking and retry management
 * - A/B testing and experiment support
 * - Comprehensive reporting and analytics queries
 */
@Dao
interface NotificationLogDao {

    // MARK: - Basic CRUD Operations

    /**
     * Inserts a new notification log entry into the database.
     *
     * @param notificationLog NotificationLog entity to insert
     * @return The ID of the inserted notification log entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationLog(notificationLog: NotificationLog): Long

    /**
     * Alternative insert method for compatibility.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notificationLog: NotificationLog): Long

    /**
     * Inserts multiple notification log entries into the database.
     *
     * @param notificationLogs List of NotificationLog entities to insert
     * @return List of IDs of the inserted notification log entries
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notificationLogs: List<NotificationLog>): List<Long>

    /**
     * Updates an existing notification log entry in the database.
     *
     * @param notificationLog NotificationLog entity with updated data
     */
    @Update
    suspend fun updateNotificationLog(notificationLog: NotificationLog)

    /**
     * Alternative update method for compatibility.
     */
    @Update
    suspend fun update(notificationLog: NotificationLog)

    /**
     * Deletes a notification log entry from the database.
     *
     * @param notificationLog NotificationLog entity to delete
     */
    @Delete
    suspend fun deleteNotificationLog(notificationLog: NotificationLog)

    /**
     * Alternative delete method for compatibility.
     */
    @Delete
    suspend fun delete(notificationLog: NotificationLog)

    /**
     * Deletes a notification log entry by its ID.
     *
     * @param notificationLogId Notification log ID to delete
     */
    @Query("DELETE FROM notification_logs WHERE id = :notificationLogId")
    suspend fun deleteNotificationLogById(notificationLogId: Long)

    /**
     * Gets a notification log entry by its ID.
     *
     * @param notificationLogId Notification log ID to retrieve
     * @return NotificationLog entity or null if not found
     */
    @Query("SELECT * FROM notification_logs WHERE id = :notificationLogId LIMIT 1")
    suspend fun getNotificationLogById(notificationLogId: Long): NotificationLog?

    // MARK: - User-based Queries

    /**
     * Gets all notification log entries for a specific user ordered by event timestamp.
     *
     * @param userId User ID
     * @return Flow of list of notification log entries
     */
    @Query("SELECT * FROM notification_logs WHERE user_id = :userId ORDER BY event_timestamp DESC")
    fun getNotificationLogsByUserId(userId: Long): Flow<List<NotificationLog>>

    /**
     * Gets recent notification log entries with limit.
     *
     * @param userId User ID
     * @param limit Maximum number of entries to return
     * @return Flow of recent notification log entries
     */
    @Query("SELECT * FROM notification_logs WHERE user_id = :userId ORDER BY event_timestamp DESC LIMIT :limit")
    fun getRecentNotificationLogs(userId: Long, limit: Int): Flow<List<NotificationLog>>

    /**
     * Gets total notification log entries count for a user.
     *
     * @param userId User ID
     * @return Total number of notification log entries
     */
    @Query("SELECT COUNT(*) FROM notification_logs WHERE user_id = :userId")
    suspend fun getTotalNotificationLogsCount(userId: Long): Int

    // MARK: - Notification-based Queries

    /**
     * Gets all log entries for a specific notification.
     *
     * @param notificationId Notification ID
     * @return Flow of list of notification log entries for the notification
     */
    @Query("SELECT * FROM notification_logs WHERE notification_id = :notificationId ORDER BY event_timestamp ASC")
    fun getNotificationLogsByNotificationId(notificationId: Long): Flow<List<NotificationLog>>

    /**
     * Gets the latest log entry for a specific notification.
     *
     * @param notificationId Notification ID
     * @return Latest NotificationLog entry for the notification
     */
    @Query("SELECT * FROM notification_logs WHERE notification_id = :notificationId ORDER BY event_timestamp DESC LIMIT 1")
    suspend fun getLatestLogForNotification(notificationId: Long): NotificationLog?

    /**
     * Gets notification timeline (all events in chronological order).
     *
     * @param notificationId Notification ID
     * @return List of log entries showing notification lifecycle
     */
    @Query("SELECT * FROM notification_logs WHERE notification_id = :notificationId ORDER BY event_timestamp ASC")
    suspend fun getNotificationTimeline(notificationId: Long): List<NotificationLog>

    // MARK: - Event Type Queries

    /**
     * Gets notification log entries by event type.
     *
     * @param userId User ID
     * @param eventType Event type to filter by
     * @return Flow of notification log entries with specified event type
     */
    @Query("SELECT * FROM notification_logs WHERE user_id = :userId AND event_type = :eventType ORDER BY event_timestamp DESC")
    fun getNotificationLogsByEventType(userId: Long, eventType: NotificationLogEvent): Flow<List<NotificationLog>>

    /**
     * Gets successful notification events.
     *
     * @param userId User ID
     * @return Flow of successful notification log entries
     */
    @Query("SELECT * FROM notification_logs WHERE user_id = :userId AND is_success = 1 ORDER BY event_timestamp DESC")
    fun getSuccessfulNotificationLogs(userId: Long): Flow<List<NotificationLog>>

    /**
     * Gets failed notification events.
     *
     * @param userId User ID
     * @return Flow of failed notification log entries
     */
    @Query("SELECT * FROM notification_logs WHERE user_id = :userId AND is_success = 0 ORDER BY event_timestamp DESC")
    fun getFailedNotificationLogs(userId: Long): Flow<List<NotificationLog>>

    /**
     * Gets user interaction events (opened, dismissed, action clicked).
     *
     * @param userId User ID
     * @return Flow of user interaction log entries
     */
    @Query(
        """
        SELECT * FROM notification_logs
        WHERE user_id = :userId
        AND event_type IN ('OPENED', 'DISMISSED', 'ACTION_CLICKED')
        ORDER BY event_timestamp DESC
    """,
    )
    fun getUserInteractionLogs(userId: Long): Flow<List<NotificationLog>>

    // MARK: - Delivery Channel Queries

    /**
     * Gets notification log entries by delivery channel.
     *
     * @param userId User ID
     * @param deliveryChannel Delivery channel to filter by
     * @return Flow of notification log entries for the delivery channel
     */
    @Query("SELECT * FROM notification_logs WHERE user_id = :userId AND delivery_channel = :deliveryChannel ORDER BY event_timestamp DESC")
    fun getNotificationLogsByDeliveryChannel(userId: Long, deliveryChannel: NotificationDeliveryChannel): Flow<List<NotificationLog>>

    /**
     * Gets delivery success rate by channel.
     *
     * @param userId User ID
     * @param startDate Start date for analysis
     * @param endDate End date for analysis
     * @return List of channel success rates
     */
    @Query(
        """
        SELECT delivery_channel,
        COUNT(*) as total_attempts,
        SUM(CASE WHEN is_success = 1 THEN 1 ELSE 0 END) as successful_attempts,
        CAST(SUM(CASE WHEN is_success = 1 THEN 1 ELSE 0 END) AS FLOAT) / COUNT(*) * 100 as success_rate
        FROM notification_logs
        WHERE user_id = :userId
        AND event_timestamp BETWEEN :startDate AND :endDate
        AND event_type IN ('SENT', 'DELIVERED', 'FAILED')
        GROUP BY delivery_channel
        ORDER BY success_rate DESC
    """,
    )
    suspend fun getDeliverySuccessRateByChannel(userId: Long, startDate: Date, endDate: Date): List<NotificationAnalytics>

    // MARK: - Date-based Queries

    /**
     * Gets notification log entries for a specific date range.
     *
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return Flow of notification log entries in date range
     */
    @Query(
        """
        SELECT * FROM notification_logs
        WHERE user_id = :userId AND event_timestamp BETWEEN :startDate AND :endDate
        ORDER BY event_timestamp DESC
    """,
    )
    fun getNotificationLogsForDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<NotificationLog>>

    /**
     * Gets notification log entries count for a specific date.
     *
     * @param userId User ID
     * @param date Date
     * @return Number of notification log entries for the date
     */
    @Query(
        """
        SELECT COUNT(*) FROM notification_logs
        WHERE user_id = :userId AND DATE(event_timestamp/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
    """,
    )
    suspend fun getNotificationLogsCountForDate(userId: Long, date: Date): Int

    // MARK: - Performance Analytics

    /**
     * Gets average processing duration by event type.
     *
     * @param userId User ID
     * @param startDate Start date for analysis
     * @param endDate End date for analysis
     * @return Average processing duration in milliseconds
     */
    @Query(
        """
        SELECT AVG(processing_duration_ms)
        FROM notification_logs
        WHERE user_id = :userId
        AND event_timestamp BETWEEN :startDate AND :endDate
        AND processing_duration_ms > 0
    """,
    )
    suspend fun getAverageProcessingDuration(userId: Long, startDate: Date, endDate: Date): Double?

    /**
     * Gets performance metrics for notifications.
     *
     * @param userId User ID
     * @param startDate Start date for analysis
     * @param endDate End date for analysis
     * @return Performance metrics summary
     */
    @Query(
        """
        SELECT
        COUNT(*) as total_events,
        AVG(processing_duration_ms) as avg_processing_duration,
        MIN(processing_duration_ms) as min_processing_duration,
        MAX(processing_duration_ms) as max_processing_duration,
        AVG(delivery_duration_ms) as avg_delivery_duration,
        SUM(CASE WHEN is_success = 1 THEN 1 ELSE 0 END) as successful_events,
        SUM(CASE WHEN is_success = 0 THEN 1 ELSE 0 END) as failed_events
        FROM notification_logs
        WHERE user_id = :userId
        AND event_timestamp BETWEEN :startDate AND :endDate
    """,
    )
    suspend fun getPerformanceMetrics(userId: Long, startDate: Date, endDate: Date): NotificationPerformanceMetrics?

    /**
     * Gets slowest performing notifications.
     *
     * @param userId User ID
     * @param limit Number of entries to return
     * @return List of slowest notification log entries
     */
    @Query(
        """
        SELECT * FROM notification_logs
        WHERE user_id = :userId
        AND processing_duration_ms > 0
        ORDER BY processing_duration_ms DESC
        LIMIT :limit
    """,
    )
    suspend fun getSlowestNotifications(userId: Long, limit: Int): List<NotificationLog>

    /**
     * Gets fastest performing notifications.
     *
     * @param userId User ID
     * @param limit Number of entries to return
     * @return List of fastest notification log entries
     */
    @Query(
        """
        SELECT * FROM notification_logs
        WHERE user_id = :userId
        AND processing_duration_ms > 0
        ORDER BY processing_duration_ms ASC
        LIMIT :limit
    """,
    )
    suspend fun getFastestNotifications(userId: Long, limit: Int): List<NotificationLog>

    // MARK: - Error and Retry Analysis

    /**
     * Gets notification log entries that failed and need retry.
     *
     * @param userId User ID
     * @return Flow of notification log entries that should be retried
     */
    @Query(
        """
        SELECT * FROM notification_logs
        WHERE user_id = :userId
        AND event_type = 'FAILED'
        AND retry_count < 5
        AND error_code != 'PERMANENT_FAILURE'
        ORDER BY event_timestamp DESC
    """,
    )
    fun getNotificationLogsForRetry(userId: Long): Flow<List<NotificationLog>>

    /**
     * Gets most common error codes.
     *
     * @param userId User ID
     * @param startDate Start date for analysis
     * @param endDate End date for analysis
     * @return List of error codes with frequency counts
     */
    @Query(
        """
        SELECT error_code, COUNT(*) as frequency
        FROM notification_logs
        WHERE user_id = :userId
        AND event_timestamp BETWEEN :startDate AND :endDate
        AND error_code IS NOT NULL
        GROUP BY error_code
        ORDER BY frequency DESC
    """,
    )
    suspend fun getMostCommonErrors(userId: Long, startDate: Date, endDate: Date): List<Pair<String, Int>>

    /**
     * Gets retry statistics.
     *
     * @param userId User ID
     * @param startDate Start date for analysis
     * @param endDate End date for analysis
     * @return Retry statistics summary
     */
    @Query(
        """
        SELECT
        COUNT(*) as total_retries,
        AVG(retry_count) as avg_retry_count,
        MAX(retry_count) as max_retry_count,
        SUM(CASE WHEN event_type = 'RETRIED' AND is_success = 1 THEN 1 ELSE 0 END) as successful_retries
        FROM notification_logs
        WHERE user_id = :userId
        AND event_timestamp BETWEEN :startDate AND :endDate
        AND retry_count > 0
    """,
    )
    suspend fun getRetryStatistics(userId: Long, startDate: Date, endDate: Date): Map<String, Any>

    // MARK: - User Engagement Analytics

    /**
     * Gets user engagement rate (opened/delivered ratio).
     *
     * @param userId User ID
     * @param startDate Start date for analysis
     * @param endDate End date for analysis
     * @return Engagement rate as percentage
     */
    @Query(
        """
        SELECT
        CAST(SUM(CASE WHEN event_type = 'OPENED' THEN 1 ELSE 0 END) AS FLOAT) /
        NULLIF(SUM(CASE WHEN event_type = 'DELIVERED' THEN 1 ELSE 0 END), 0) * 100 as engagement_rate
        FROM notification_logs
        WHERE user_id = :userId
        AND event_timestamp BETWEEN :startDate AND :endDate
        AND event_type IN ('OPENED', 'DELIVERED')
    """,
    )
    suspend fun getUserEngagementRate(userId: Long, startDate: Date, endDate: Date): Double?

    /**
     * Gets user interaction patterns by hour of day.
     *
     * @param userId User ID
     * @param startDate Start date for analysis
     * @param endDate End date for analysis
     * @return List of interaction counts by hour
     */
    @Query(
        """
        SELECT
        strftime('%H', event_timestamp/1000, 'unixepoch') as hour,
        COUNT(*) as interaction_count
        FROM notification_logs
        WHERE user_id = :userId
        AND event_timestamp BETWEEN :startDate AND :endDate
        AND event_type IN ('OPENED', 'DISMISSED', 'ACTION_CLICKED')
        GROUP BY hour
        ORDER BY hour
    """,
    )
    suspend fun getUserInteractionPatternsByHour(userId: Long, startDate: Date, endDate: Date): List<Pair<String, Int>>

    /**
     * Gets notification response times (time from delivered to opened).
     *
     * @param userId User ID
     * @param startDate Start date for analysis
     * @param endDate End date for analysis
     * @return Average response time in milliseconds
     */
    @Query(
        """
        SELECT AVG(opened.event_timestamp - delivered.event_timestamp) as avg_response_time
        FROM notification_logs delivered
        JOIN notification_logs opened ON delivered.notification_id = opened.notification_id
        WHERE delivered.user_id = :userId
        AND delivered.event_type = 'DELIVERED'
        AND opened.event_type = 'OPENED'
        AND delivered.event_timestamp BETWEEN :startDate AND :endDate
        AND opened.event_timestamp > delivered.event_timestamp
    """,
    )
    suspend fun getAverageResponseTime(userId: Long, startDate: Date, endDate: Date): Long?

    // MARK: - A/B Testing and Experiments

    /**
     * Gets notification log entries for a specific experiment.
     *
     * @param experimentId Experiment ID
     * @return Flow of notification log entries for the experiment
     */
    @Query("SELECT * FROM notification_logs WHERE experiment_id = :experimentId ORDER BY event_timestamp DESC")
    fun getNotificationLogsByExperiment(experimentId: String): Flow<List<NotificationLog>>

    /**
     * Gets experiment performance comparison.
     *
     * @param experimentId Experiment ID
     * @param startDate Start date for analysis
     * @param endDate End date for analysis
     * @return Experiment performance metrics
     */
    @Query(
        """
        SELECT
        COUNT(*) as total_notifications,
        SUM(CASE WHEN event_type = 'DELIVERED' THEN 1 ELSE 0 END) as delivered_count,
        SUM(CASE WHEN event_type = 'OPENED' THEN 1 ELSE 0 END) as opened_count,
        SUM(CASE WHEN event_type = 'ACTION_CLICKED' THEN 1 ELSE 0 END) as action_clicked_count,
        AVG(processing_duration_ms) as avg_processing_duration
        FROM notification_logs
        WHERE experiment_id = :experimentId
        AND event_timestamp BETWEEN :startDate AND :endDate
    """,
    )
    suspend fun getExperimentPerformance(experimentId: String, startDate: Date, endDate: Date): Map<String, Any>

    // MARK: - Cleanup Operations

    /**
     * Deletes notification log entries older than specified date.
     *
     * @param userId User ID
     * @param olderThan Cutoff date
     */
    @Query("DELETE FROM notification_logs WHERE user_id = :userId AND event_timestamp < :olderThan")
    suspend fun deleteOldNotificationLogs(userId: Long, olderThan: Date)

    /**
     * Deletes all notification log entries for a specific notification.
     *
     * @param notificationId Notification ID
     */
    @Query("DELETE FROM notification_logs WHERE notification_id = :notificationId")
    suspend fun deleteAllLogsForNotification(notificationId: Long)

    /**
     * Deletes all notification log entries for a user.
     *
     * @param userId User ID
     */
    @Query("DELETE FROM notification_logs WHERE user_id = :userId")
    suspend fun deleteAllNotificationLogsForUser(userId: Long)

    /**
     * Deletes all notification log entries (for testing purposes only).
     */
    @Query("DELETE FROM notification_logs")
    suspend fun deleteAllNotificationLogs()

    // MARK: - Batch Operations

    /**
     * Gets notification log entries by batch ID.
     *
     * @param batchId Batch ID
     * @return Flow of notification log entries for the batch
     */
    @Query("SELECT * FROM notification_logs WHERE batch_id = :batchId ORDER BY event_timestamp ASC")
    fun getNotificationLogsByBatchId(batchId: String): Flow<List<NotificationLog>>

    /**
     * Gets batch processing statistics.
     *
     * @param batchId Batch ID
     * @return Batch processing summary
     */
    @Query(
        """
        SELECT
        COUNT(*) as total_notifications,
        SUM(CASE WHEN is_success = 1 THEN 1 ELSE 0 END) as successful_notifications,
        AVG(processing_duration_ms) as avg_processing_duration,
        MIN(event_timestamp) as batch_start_time,
        MAX(event_timestamp) as batch_end_time
        FROM notification_logs
        WHERE batch_id = :batchId
    """,
    )
    suspend fun getBatchStatistics(batchId: String): Map<String, Any>

    // MARK: - System Health Monitoring

    /**
     * Gets system health indicators based on notification performance.
     *
     * @param startDate Start date for analysis
     * @param endDate End date for analysis
     * @return System health metrics
     */
    @Query(
        """
        SELECT
        COUNT(*) as total_events,
        CAST(SUM(CASE WHEN is_success = 1 THEN 1 ELSE 0 END) AS FLOAT) / COUNT(*) * 100 as overall_success_rate,
        AVG(processing_duration_ms) as avg_processing_time,
        COUNT(DISTINCT user_id) as active_users,
        COUNT(DISTINCT notification_id) as unique_notifications
        FROM notification_logs
        WHERE event_timestamp BETWEEN :startDate AND :endDate
    """,
    )
    suspend fun getSystemHealthMetrics(startDate: Date, endDate: Date): Map<String, Any>

    /**
     * Gets critical errors that need immediate attention.
     *
     * @param severityThreshold Minimum severity level (1-5)
     * @param hoursBack Hours back from now to check
     * @return List of critical notification log entries
     */
    @Query(
        """
        SELECT * FROM notification_logs
        WHERE priority_level >= :severityThreshold
        AND event_timestamp > datetime('now', '-' || :hoursBack || ' hours')
        AND is_success = 0
        ORDER BY priority_level DESC, event_timestamp DESC
    """,
    )
    suspend fun getCriticalErrors(severityThreshold: Int = 4, hoursBack: Int = 24): List<NotificationLog>
}
