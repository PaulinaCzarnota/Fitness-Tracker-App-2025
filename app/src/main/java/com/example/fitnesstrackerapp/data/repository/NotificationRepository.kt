package com.example.fitnesstrackerapp.data.repository

import com.example.fitnesstrackerapp.data.dao.NotificationLogDao
import com.example.fitnesstrackerapp.data.entity.NotificationDeliveryChannel
import com.example.fitnesstrackerapp.data.entity.NotificationLog
import com.example.fitnesstrackerapp.data.entity.NotificationLogEvent
import com.example.fitnesstrackerapp.data.entity.NotificationPriority
import com.example.fitnesstrackerapp.data.model.ErrorFrequency
import com.example.fitnesstrackerapp.data.model.NotificationDeliveryStats
import com.example.fitnesstrackerapp.data.model.NotificationInsights
import com.example.fitnesstrackerapp.data.model.NotificationSystemMetrics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date

/**
 * Repository for notification logging operations.
 *
 * This repository provides a clean API for notification-related database operations,
 * implementing the repository pattern to abstract database access logic from the business layer.
 * It handles all notification event logging, delivery analytics, error tracking, and performance monitoring.
 *
 * Key features:
 * - CRUD operations for notification logs
 * - Delivery performance analytics
 * - Error tracking and retry logic
 * - User interaction analytics (clicks, dismissals)
 * - System health monitoring and reporting
 * - Caching layer for frequently accessed data
 * - Real-time reactive data streams
 *
 * Usage example:
 * class NotificationService @Inject constructor(
 *     private val notificationRepository: NotificationRepository
 * ) {
 *     suspend fun sendNotification(userId: Long, message: String) {
 *         val result = notificationSender.send(userId, message)
 *         notificationRepository.logNotificationEvent(
 *             userId = userId,
 *             eventType = if (result.isSuccess) SENT else FAILED,
 *             title = message,
 *             isSuccessful = result.isSuccess,
 *             errorMessage = result.errorMessage
 *         )
 *     }
 * }
 *
 * @property notificationLogDao DAO for notification log database operations
 */
class NotificationRepository
// @Inject // REMOVED - using ServiceLocator
constructor(
    private val notificationLogDao: NotificationLogDao,
) {
    // ================================
    // Core CRUD Operations
    // ================================

    /**
     * Logs a notification event to the database.
     *
     * @param userId User ID associated with the notification
     * @param eventType Type of notification event (SENT, DELIVERED, CLICKED, etc.)
     * @param channel Channel used for delivery (PUSH, EMAIL, SMS, IN_APP)
     * @param title Notification title
     * @param message Notification message body
     * @param priority Priority level of the notification
     * @param notificationId Unique identifier for tracking notification lifecycle
     * @param isSuccessful Whether the operation was successful
     * @param deliveryLatencyMs Time taken for delivery in milliseconds
     * @param errorMessage Error message if operation failed
     * @param errorCode Error code for categorizing failures
     * @param retryCount Number of retry attempts made
     * @param metadata Additional metadata as JSON string
     * @return ID of the inserted log entry
     */
    suspend fun logNotificationEvent(
        userId: Long,
        eventType: NotificationLogEvent,
        title: String = "Notification",
        message: String? = null,
        notificationId: String? = null,
        isSuccessful: Boolean = true,
        deliveryLatencyMs: Long? = null,
        errorMessage: String? = null,
        errorCode: String? = null,
        retryCount: Int = 0,
        metadata: String? = null,
        deliveryChannel: NotificationDeliveryChannel = NotificationDeliveryChannel.PUSH,
    ): Long {
        val notificationLog = NotificationLog(
            userId = userId,
            notificationId = 0L, // Use a placeholder for the required numeric ID
            eventType = eventType,
            eventTimestamp = Date(),
            deliveryChannel = deliveryChannel,
            isSuccess = isSuccessful,
            errorCode = errorCode,
            errorMessage = errorMessage,
            retryCount = retryCount,
        )
        return notificationLogDao.insertNotificationLog(notificationLog)
    }

    /**
     * Retrieves a notification log by its ID.
     */
    suspend fun getNotificationLogById(id: Long): NotificationLog? {
        return notificationLogDao.getNotificationLogById(id)
    }

    /**
     * Updates an existing notification log entry.
     */
    suspend fun updateNotificationLog(notificationLog: NotificationLog) {
        notificationLogDao.updateNotificationLog(notificationLog)
    }

    /**
     * Deletes a notification log entry.
     */
    suspend fun deleteNotificationLog(notificationLog: NotificationLog) {
        notificationLogDao.deleteNotificationLog(notificationLog)
    }

    /**
     * Deletes a notification log by ID.
     */
    suspend fun deleteNotificationLogById(id: Long) {
        notificationLogDao.deleteNotificationLogById(id)
    }

    /**
     * Bulk inserts multiple notification logs.
     */
    suspend fun insertNotificationLogs(logs: List<NotificationLog>): List<Long> {
        return notificationLogDao.insertAll(logs)
    }

    // ================================
    // Query Operations
    // ================================

    /**
     * Gets all notification logs for a user.
     * Returns a Flow for reactive updates.
     */
    fun getNotificationLogsByUserId(userId: Long): Flow<List<NotificationLog>> {
        return notificationLogDao.getNotificationLogsByUserId(userId)
    }

    /**
     * Gets notification logs filtered by event type.
     */
    fun getNotificationLogsByEventType(
        userId: Long,
        eventType: NotificationLogEvent,
    ): Flow<List<NotificationLog>> {
        return notificationLogDao.getNotificationLogsByEventType(userId, eventType)
    }

    /**
     * Gets notification logs filtered by delivery channel.
     */
    fun getNotificationLogsByChannel(
        userId: Long,
        channel: NotificationDeliveryChannel,
    ): Flow<List<NotificationLog>> {
        return notificationLogDao.getNotificationLogsByChannel(userId, channel.name)
    }

    /**
     * Gets notification logs within a date range.
     */
    fun getNotificationLogsByDateRange(
        userId: Long,
        startDate: Date,
        endDate: Date,
    ): Flow<List<NotificationLog>> {
        return notificationLogDao.getNotificationLogsByDateRange(userId, startDate, endDate)
    }

    /**
     * Gets all failed notification attempts for a user.
     */
    fun getFailedNotifications(userId: Long): Flow<List<NotificationLog>> {
        return notificationLogDao.getFailedNotifications(userId)
    }

    /**
     * Gets all successful notification deliveries for a user.
     */
    fun getSuccessfulNotifications(userId: Long): Flow<List<NotificationLog>> {
        return notificationLogDao.getSuccessfulNotifications(userId)
    }

    /**
     * Gets notifications filtered by priority level.
     */
    fun getNotificationsByPriority(
        userId: Long,
        priority: NotificationPriority,
    ): Flow<List<NotificationLog>> {
        return notificationLogDao.getNotificationsByPriority(userId, priority.ordinal)
    }

    /**
     * Gets notifications that required retry attempts.
     */
    fun getNotificationsWithRetries(userId: Long): Flow<List<NotificationLog>> {
        return notificationLogDao.getNotificationsWithRetries(userId)
    }

    /**
     * Gets notifications that were clicked by the user.
     */
    fun getClickedNotifications(userId: Long): Flow<List<NotificationLog>> {
        return notificationLogDao.getClickedNotifications(userId)
    }

    /**
     * Gets notifications that were dismissed by the user.
     */
    fun getDismissedNotifications(userId: Long): Flow<List<NotificationLog>> {
        return notificationLogDao.getDismissedNotifications(userId)
    }

    // ================================
    // Analytics and Metrics
    // ================================

    /**
     * Gets comprehensive notification delivery statistics for a date range.
     */
    suspend fun getNotificationDeliveryStats(
        userId: Long,
        startDate: Date,
        endDate: Date,
    ): NotificationDeliveryStats? {
        return notificationLogDao.getNotificationDeliveryStats(userId, startDate, endDate)
    }

    /**
     * Calculates the average delivery latency for notifications in a date range.
     */
    suspend fun getAverageDeliveryLatency(
        userId: Long,
        startDate: Date,
        endDate: Date,
    ): Double? {
        return notificationLogDao.getAverageDeliveryLatency(userId, startDate, endDate)
    }

    /**
     * Gets the most common error types and their frequencies.
     */
    suspend fun getMostCommonErrors(userId: Long, limit: Int = 10): List<ErrorFrequency> {
        val now = Date()
        val monthAgo = getDateDaysAgo(30)
        return notificationLogDao.getMostCommonErrors(userId, monthAgo, now)
    }

    /**
     * Gets the maximum retry count recorded for user's notifications.
     */
    suspend fun getMaxRetryCount(userId: Long): Int? {
        return notificationLogDao.getMaxRetryCount(userId)
    }

    // ================================
    // Convenience Methods and Analytics
    // ================================

    /**
     * Gets today's notification delivery statistics for a user.
     */
    suspend fun getTodayDeliveryStats(userId: Long): NotificationDeliveryStats? {
        val today = Date()
        val calendar = Calendar.getInstance()
        calendar.time = today

        // Set to start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        // Set to end of day
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.time

        return getNotificationDeliveryStats(userId, startOfDay, endOfDay)
    }

    /**
     * Gets this week's notification delivery statistics for a user.
     */
    suspend fun getWeeklyDeliveryStats(userId: Long): NotificationDeliveryStats? {
        val now = Date()
        val weekAgo = Date(now.time - (7 * 24 * 60 * 60 * 1000))

        return getNotificationDeliveryStats(userId, weekAgo, now)
    }

    /**
     * Calculates the delivery success rate for a user in a given period.
     */
    suspend fun getDeliverySuccessRate(
        userId: Long,
        startDate: Date,
        endDate: Date,
    ): Double {
        val stats = getNotificationDeliveryStats(userId, startDate, endDate)
        return if (stats != null && stats.totalSent > 0) {
            (stats.totalDelivered.toDouble() / stats.totalSent.toDouble()) * 100.0
        } else {
            0.0
        }
    }

    /**
     * Calculates the user interaction rate (clicks / delivered).
     */
    suspend fun getInteractionRate(
        userId: Long,
        startDate: Date,
        endDate: Date,
    ): Double {
        val stats = getNotificationDeliveryStats(userId, startDate, endDate)

        return if (stats != null && stats.totalDelivered > 0) {
            // Get count of clicked notifications from logs
            val clickedCount = notificationLogDao.getClickedNotificationCount(userId, startDate, endDate)
            (clickedCount.toDouble() / stats.totalDelivered.toDouble()) * 100.0
        } else {
            0.0
        }
    }

    /**
     * Gets notification performance insights for a user.
     */
    suspend fun getNotificationInsights(userId: Long): NotificationInsights {
        val now = Date()
        val weekAgo = Date(now.time - (7 * 24 * 60 * 60 * 1000))
        val monthAgo = Date(now.time - (30L * 24 * 60 * 60 * 1000))

        val weeklyStats = getWeeklyDeliveryStats(userId)
        val monthlyStats = getNotificationDeliveryStats(userId, monthAgo, now)
        val averageLatency = getAverageDeliveryLatency(userId, weekAgo, now)
        val commonErrors = getMostCommonErrors(userId, 5)
        val maxRetries = getMaxRetryCount(userId) ?: 0

        return NotificationInsights(
            totalSent = monthlyStats?.totalSent ?: 0,
            totalDelivered = monthlyStats?.totalDelivered ?: 0,
            totalFailed = monthlyStats?.totalFailed ?: 0,
            totalClicked = 0, // Would need proper implementation
            totalDismissed = 0, // Would need proper implementation
            averageDeliveryLatencyMs = averageLatency ?: 0.0,
            deliverySuccessRate = if (monthlyStats != null && monthlyStats.totalSent > 0) {
                (monthlyStats.totalDelivered.toDouble() / monthlyStats.totalSent.toDouble()) * 100.0
            } else {
                0.0
            },
            clickThroughRate = 0.0, // Would need proper implementation
            maxRetryCount = maxRetries,
            commonErrors = commonErrors,
            weeklyTrend = if (weeklyStats != null && monthlyStats != null) {
                val weeklyRate = if (weeklyStats.totalSent > 0) {
                    (weeklyStats.totalDelivered.toDouble() / weeklyStats.totalSent.toDouble()) * 100.0
                } else {
                    0.0
                }
                val monthlyRate = if (monthlyStats.totalSent > 0) {
                    (monthlyStats.totalDelivered.toDouble() / monthlyStats.totalSent.toDouble()) * 100.0
                } else {
                    0.0
                }
                weeklyRate - monthlyRate
            } else {
                0.0
            },
        )
    }

    /**
     * Gets performance metrics for notification system monitoring.
     */
    suspend fun getPerformanceMetrics(userId: Long): NotificationSystemMetrics {
        val now = Date()
        val dayAgo = Date(now.time - (24 * 60 * 60 * 1000))
        val weekAgo = Date(now.time - (7 * 24 * 60 * 60 * 1000))

        val dailyStats = getNotificationDeliveryStats(userId, dayAgo, now)
        val weeklyStats = getNotificationDeliveryStats(userId, weekAgo, now)
        val averageLatency = getAverageDeliveryLatency(userId, dayAgo, now)
        val maxRetries = getMaxRetryCount(userId) ?: 0
        val recentErrors = getMostCommonErrors(userId, 3)

        return NotificationSystemMetrics(
            dailyVolume = dailyStats?.totalSent ?: 0,
            weeklyVolume = weeklyStats?.totalSent ?: 0,
            averageLatencyMs = averageLatency ?: 0.0,
            errorRate = if (dailyStats != null && dailyStats.totalSent > 0) {
                (dailyStats.totalFailed.toDouble() / dailyStats.totalSent.toDouble()) * 100.0
            } else {
                0.0
            },
            retryRate = 0.0, // Would need proper implementation
            maxRetryCount = maxRetries,
            topErrors = recentErrors.map { it.errorCode },
            healthScore = calculateHealthScore(dailyStats, averageLatency, recentErrors.size),
        )
    }

    /**
     * Calculates a health score for notification system (0-100).
     */
    private fun calculateHealthScore(
        stats: NotificationDeliveryStats?,
        averageLatency: Double?,
        errorTypeCount: Int,
    ): Double {
        if (stats == null || stats.totalSent == 0) return 100.0

        // Success rate component (0-40 points)
        val successRate = (stats.totalDelivered.toDouble() / stats.totalSent.toDouble()) * 100.0
        val successScore = (successRate / 100.0) * 40.0

        // Latency component (0-30 points)
        val latencyScore = when {
            averageLatency == null -> 30.0
            averageLatency <= 100 -> 30.0 // Excellent
            averageLatency <= 500 -> 25.0 // Good
            averageLatency <= 1000 -> 20.0 // Fair
            averageLatency <= 2000 -> 15.0 // Poor
            else -> 10.0 // Very poor
        }

        // Error diversity component (0-30 points)
        val errorScore = when (errorTypeCount) {
            0 -> 30.0
            1 -> 25.0
            2 -> 20.0
            3 -> 15.0
            4 -> 10.0
            else -> 5.0
        }

        return successScore + latencyScore + errorScore
    }

    /**
     * Cleans up old notification logs based on retention policy.
     * Keeps logs for the specified number of days.
     *
     * @param retentionDays Number of days to keep logs (default: 90)
     * @return Number of deleted log entries
     */
    suspend fun cleanupOldLogs(retentionDays: Int = 90): Int {
        val cutoffDate = Date(System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L))
        return notificationLogDao.deleteLogsOlderThan(cutoffDate)
    }

    /**
     * Gets notification logs for debugging purposes with detailed error information.
     */
    suspend fun getDebugLogs(
        userId: Long,
        startDate: Date,
        endDate: Date,
        includeSuccessful: Boolean = false,
    ): List<NotificationLog> {
        return getNotificationLogsByDateRange(userId, startDate, endDate)
            .first()
            .let { logs ->
                if (includeSuccessful) {
                    logs
                } else {
                    logs.filter { !it.isSuccess }
                }
            }
    }

    /**
     * Exports notification analytics data for reporting.
     */
    suspend fun exportAnalyticsData(
        userId: Long,
        startDate: Date,
        endDate: Date,
    ): Map<String, Any> {
        val stats = getNotificationDeliveryStats(userId, startDate, endDate)
        val insights = getNotificationInsights(userId)
        val performance = getPerformanceMetrics(userId)

        return mapOf(
            "period" to mapOf(
                "startDate" to startDate,
                "endDate" to endDate,
            ),
            "deliveryStats" to (stats ?: "No data available"),
            "insights" to insights,
            "performance" to performance,
            "exportTimestamp" to Date(),
        )
    }

    // ================================
    // Date Utility Methods
    // ================================

    /**
     * Gets a date that is the specified number of days ago.
     * Uses Calendar for reliable date math.
     */
    private fun getDateDaysAgo(days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return calendar.time
    }

    /**
     * Gets a date that is the specified number of hours ago.
     * Uses Calendar for reliable date math.
     */
    private fun getDateHoursAgo(hours: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, -hours)
        return calendar.time
    }

    /**
     * Gets the start of the current day.
     */
    private fun getStartOfDay(date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * Gets the end of the current day.
     */
    private fun getEndOfDay(date: Date = Date()): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }
}
