/**
 * Notification Repository
 *
 * Repository for managing notification and messaging data in the Fitness Tracker App.
 *
 * This class serves as the single source of truth for notification-related data operations,
 * providing a clean API for notification scheduling, delivery tracking, user interaction
 * management, and analytics. It abstracts notification data sources from the rest of the app.
 *
 * Key Features:
 * - Comprehensive CRUD operations for notifications
 * - Notification scheduling and delivery tracking
 * - User interaction monitoring (read, clicked, dismissed)
 * - Priority-based notification management
 * - Recurring notification pattern handling
 * - Notification analytics and engagement metrics
 * - Data validation and business logic enforcement
 *
 * @property notificationDao The Data Access Object for notification database operations.
 */
package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.data.dao.NotificationDao
import com.example.fitnesstrackerapp.data.entity.Notification
import com.example.fitnesstrackerapp.data.entity.NotificationPriority
import com.example.fitnesstrackerapp.data.entity.NotificationStatus
import com.example.fitnesstrackerapp.data.entity.NotificationType
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository for managing notification operations and scheduling.
 *
 * Provides a comprehensive API for notification management, delivery tracking,
 * and user engagement analytics with proper validation and error handling.
 */
class NotificationRepository(private val notificationDao: NotificationDao) {
    // region CRUD Operations

    /**
     * Inserts a new notification into the database.
     *
     * @param notification The notification to insert
     * @return The row ID of the newly inserted notification
     * @throws IllegalArgumentException If the notification data is invalid
     */
    suspend fun insertNotification(notification: Notification): Long {
        require(notification.userId > 0) { "Notification must have a valid user ID" }
        require(notification.title.isNotBlank()) { "Notification title cannot be blank" }
        require(notification.message.isNotBlank()) { "Notification message cannot be blank" }
        require(notification.channelId.isNotBlank()) { "Channel ID cannot be blank" }
        require(notification.isValid()) { "Notification data is invalid" }

        return notificationDao.insertNotification(notification)
    }

    /**
     * Updates an existing notification in the database.
     *
     * @param notification The notification with updated values
     * @throws IllegalArgumentException If the notification ID is invalid
     */
    suspend fun updateNotification(notification: Notification) {
        require(notification.id > 0) { "Notification must have a valid ID" }
        require(notification.isValid()) { "Notification data is invalid" }

        notificationDao.updateNotification(notification)
    }

    /**
     * Deletes a notification from the database.
     *
     * @param notification The notification to delete
     * @throws IllegalArgumentException If the notification ID is invalid
     */
    suspend fun deleteNotification(notification: Notification) {
        require(notification.id > 0) { "Cannot delete notification with invalid ID" }
        notificationDao.deleteNotification(notification)
    }

    /**
     * Deletes a notification by its ID.
     *
     * @param notificationId The ID of the notification to delete
     * @throws IllegalArgumentException If the ID is invalid
     */
    suspend fun deleteNotificationById(notificationId: Long) {
        require(notificationId > 0) { "Notification ID must be positive" }
        notificationDao.deleteNotificationById(notificationId)
    }

    // endregion

    // region Bulk Operations

    /**
     * Inserts multiple notifications into the database.
     *
     * @param notifications The list of notifications to insert
     * @return List of row IDs for the inserted notifications
     * @throws IllegalArgumentException If any notification is invalid
     */
    suspend fun insertAllNotifications(notifications: List<Notification>): List<Long> {
        require(notifications.isNotEmpty()) { "Notifications list cannot be empty" }

        notifications.forEachIndexed { index, notification ->
            require(notification.isValid()) { "Notification at index $index is invalid" }
        }

        return notificationDao.insertAll(notifications)
    }

    /**
     * Deletes all notifications for a specific user.
     *
     * @param userId The ID of the user whose notifications should be deleted
     * @throws IllegalArgumentException If the user ID is invalid
     */
    suspend fun deleteAllNotificationsForUser(userId: Long) {
        require(userId > 0) { "User ID must be positive" }
        notificationDao.deleteAllNotificationsForUser(userId)
    }

    /**
     * Deletes notifications by status.
     *
     * @param userId The ID of the user
     * @param status The status of notifications to delete
     */
    suspend fun deleteNotificationsByStatus(userId: Long, status: NotificationStatus) {
        require(userId > 0) { "User ID must be positive" }
        notificationDao.deleteNotificationsByStatus(userId, status)
    }

    // endregion

    // region Query Operations

    /**
     * Gets a notification by its ID.
     *
     * @param notificationId The ID of the notification
     * @return The notification if found, null otherwise
     */
    suspend fun getNotificationById(notificationId: Long): Notification? {
        require(notificationId > 0) { "Notification ID must be positive" }
        return notificationDao.getNotificationById(notificationId)
    }

    /**
     * Gets all notifications for a specific user.
     *
     * @param userId The ID of the user
     * @return Flow of list of notifications ordered by scheduled time
     */
    fun getNotificationsByUserId(userId: Long): Flow<List<Notification>> {
        require(userId > 0) { "User ID must be positive" }
        return notificationDao.getNotificationsByUserId(userId)
    }

    /**
     * Gets unread notifications for a user.
     *
     * @param userId The ID of the user
     * @return Flow of list of unread notifications
     */
    fun getUnreadNotifications(userId: Long): Flow<List<Notification>> {
        require(userId > 0) { "User ID must be positive" }
        return notificationDao.getUnreadNotifications(userId)
    }

    /**
     * Gets notifications by type for a user.
     *
     * @param userId The ID of the user
     * @param type The notification type to filter by
     * @return Flow of list of notifications of the specified type
     */
    fun getNotificationsByType(userId: Long, type: NotificationType): Flow<List<Notification>> {
        require(userId > 0) { "User ID must be positive" }
        return notificationDao.getNotificationsByType(userId, type)
    }

    /**
     * Gets notifications by status for a user.
     *
     * @param userId The ID of the user
     * @param status The notification status to filter by
     * @return Flow of list of notifications with the specified status
     */
    fun getNotificationsByStatus(userId: Long, status: NotificationStatus): Flow<List<Notification>> {
        require(userId > 0) { "User ID must be positive" }
        return notificationDao.getNotificationsByStatus(userId, status)
    }

    /**
     * Gets notifications by priority level.
     *
     * @param userId The ID of the user
     * @param priority The notification priority to filter by
     * @return Flow of notifications with specified priority
     */
    fun getNotificationsByPriority(userId: Long, priority: NotificationPriority): Flow<List<Notification>> {
        require(userId > 0) { "User ID must be positive" }
        return notificationDao.getNotificationsByPriority(userId, priority)
    }

    /**
     * Gets recent notifications with limit.
     *
     * @param userId The ID of the user
     * @param limit Maximum number of notifications to return
     * @return Flow of recent notifications
     */
    fun getRecentNotifications(userId: Long, limit: Int = 20): Flow<List<Notification>> {
        require(userId > 0) { "User ID must be positive" }
        require(limit > 0) { "Limit must be positive" }

        return notificationDao.getRecentNotifications(userId, limit)
    }

    /**
     * Gets notifications within a date range.
     *
     * @param userId The ID of the user
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return Flow of notifications in date range
     */
    fun getNotificationsInDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Notification>> {
        require(userId > 0) { "User ID must be positive" }
        require(!startDate.after(endDate)) { "Start date cannot be after end date" }

        return notificationDao.getNotificationsInDateRange(userId, startDate, endDate)
    }

    /**
     * Gets today's notifications for a user.
     *
     * @param userId The ID of the user
     * @param startOfDay The start of the current day
     * @param endOfDay The end of the current day
     * @return Flow of today's notifications
     */
    fun getTodaysNotifications(userId: Long, startOfDay: Date, endOfDay: Date): Flow<List<Notification>> {
        require(userId > 0) { "User ID must be positive" }
        require(!startOfDay.after(endOfDay)) { "Start of day cannot be after end of day" }

        return notificationDao.getTodaysNotifications(userId, startOfDay, endOfDay)
    }

    // endregion

    // region Notification Scheduling

    /**
     * Gets pending notifications that are due for delivery.
     *
     * @param currentTime The current timestamp to check against
     * @return List of notifications ready to be sent
     */
    suspend fun getPendingNotificationsDue(currentTime: Date = Date()): List<Notification> {
        return notificationDao.getPendingNotificationsDue(currentTime)
    }

    /**
     * Gets overdue pending notifications.
     *
     * @param currentTime The current timestamp
     * @return List of overdue notifications
     */
    suspend fun getOverdueNotifications(currentTime: Date = Date()): List<Notification> {
        return notificationDao.getOverdueNotifications(currentTime)
    }

    /**
     * Gets notifications that can be retried.
     *
     * @return List of notifications eligible for retry
     */
    suspend fun getRetryableNotifications(): List<Notification> {
        return notificationDao.getRetryableNotifications()
    }

    /**
     * Gets active recurring notifications.
     *
     * @param userId The ID of the user
     * @return Flow of recurring notifications
     */
    fun getRecurringNotifications(userId: Long): Flow<List<Notification>> {
        require(userId > 0) { "User ID must be positive" }
        return notificationDao.getRecurringNotifications(userId)
    }

    // endregion

    // region Status Updates

    /**
     * Updates notification status.
     *
     * @param notificationId The ID of the notification
     * @param status The new status
     * @param updatedAt The update timestamp
     */
    suspend fun updateNotificationStatus(notificationId: Long, status: NotificationStatus, updatedAt: Date = Date()) {
        require(notificationId > 0) { "Notification ID must be positive" }
        notificationDao.updateNotificationStatus(notificationId, status, updatedAt)
    }

    /**
     * Marks notification as sent.
     *
     * @param notificationId The ID of the notification
     * @param sentTime The time when notification was sent
     */
    suspend fun markNotificationAsSent(notificationId: Long, sentTime: Date = Date()) {
        require(notificationId > 0) { "Notification ID must be positive" }
        notificationDao.markNotificationAsSent(notificationId, sentTime)
    }

    /**
     * Marks notification as read.
     *
     * @param notificationId The ID of the notification
     * @param readTime The time when notification was read
     */
    suspend fun markNotificationAsRead(notificationId: Long, readTime: Date = Date()) {
        require(notificationId > 0) { "Notification ID must be positive" }
        notificationDao.markNotificationAsRead(notificationId, readTime)
    }

    /**
     * Marks notification as clicked.
     *
     * @param notificationId The ID of the notification
     * @param clickedTime The time when notification was clicked
     */
    suspend fun markNotificationAsClicked(notificationId: Long, clickedTime: Date = Date()) {
        require(notificationId > 0) { "Notification ID must be positive" }
        notificationDao.markNotificationAsClicked(notificationId, clickedTime)
    }

    /**
     * Marks notification as dismissed.
     *
     * @param notificationId The ID of the notification
     * @param dismissedTime The time when notification was dismissed
     */
    suspend fun markNotificationAsDismissed(notificationId: Long, dismissedTime: Date = Date()) {
        require(notificationId > 0) { "Notification ID must be positive" }
        notificationDao.markNotificationAsDismissed(notificationId, dismissedTime)
    }

    /**
     * Marks all notifications as read for a user.
     *
     * @param userId The ID of the user
     * @param readTime The time when marked as read
     */
    suspend fun markAllNotificationsAsRead(userId: Long, readTime: Date = Date()) {
        require(userId > 0) { "User ID must be positive" }
        notificationDao.markAllNotificationsAsRead(userId, readTime)
    }

    // endregion

    // region System Integration

    /**
     * Increments retry count for failed notification.
     *
     * @param notificationId The ID of the notification
     * @param updatedAt The update timestamp
     */
    suspend fun incrementRetryCount(notificationId: Long, updatedAt: Date = Date()) {
        require(notificationId > 0) { "Notification ID must be positive" }
        notificationDao.incrementRetryCount(notificationId, updatedAt)
    }

    /**
     * Updates notification system ID for cancellation purposes.
     *
     * @param notificationId The notification database ID
     * @param systemNotificationId The system notification ID
     */
    suspend fun updateSystemNotificationId(notificationId: Long, systemNotificationId: Int) {
        require(notificationId > 0) { "Notification ID must be positive" }
        notificationDao.updateSystemNotificationId(notificationId, systemNotificationId)
    }

    // endregion

    // region Entity Relations

    /**
     * Gets notifications related to specific entity.
     *
     * @param userId The ID of the user
     * @param entityType The type of related entity (e.g., "goal", "workout")
     * @param entityId The ID of related entity
     * @return Flow of related notifications
     */
    fun getNotificationsByRelatedEntity(userId: Long, entityType: String, entityId: Long): Flow<List<Notification>> {
        require(userId > 0) { "User ID must be positive" }
        require(entityType.isNotBlank()) { "Entity type cannot be blank" }
        require(entityId > 0) { "Entity ID must be positive" }

        return notificationDao.getNotificationsByRelatedEntity(userId, entityType, entityId)
    }

    /**
     * Cancels pending notifications for specific entity.
     *
     * @param userId The ID of the user
     * @param entityType The type of related entity
     * @param entityId The ID of related entity
     * @param currentTime The current timestamp
     */
    suspend fun cancelNotificationsForEntity(userId: Long, entityType: String, entityId: Long, currentTime: Date = Date()) {
        require(userId > 0) { "User ID must be positive" }
        require(entityType.isNotBlank()) { "Entity type cannot be blank" }
        require(entityId > 0) { "Entity ID must be positive" }

        notificationDao.cancelNotificationsForEntity(userId, entityType, entityId, currentTime)
    }

    // endregion

    // region Analytics and Statistics

    /**
     * Gets notification count by type for analytics.
     *
     * @param userId The ID of the user
     * @param type The notification type
     * @return Total count of notifications of specified type
     */
    suspend fun getNotificationCountByType(userId: Long, type: NotificationType): Int {
        require(userId > 0) { "User ID must be positive" }
        return notificationDao.getNotificationCountByType(userId, type)
    }

    /**
     * Gets notification count by status for analytics.
     *
     * @param userId The ID of the user
     * @param status The notification status
     * @return Count of notifications with specified status
     */
    suspend fun getNotificationCountByStatus(userId: Long, status: NotificationStatus): Int {
        require(userId > 0) { "User ID must be positive" }
        return notificationDao.getNotificationCountByStatus(userId, status)
    }

    /**
     * Gets total unread notification count.
     *
     * @param userId The ID of the user
     * @return Number of unread notifications
     */
    suspend fun getUnreadNotificationCount(userId: Long): Int {
        require(userId > 0) { "User ID must be positive" }
        return notificationDao.getUnreadNotificationCount(userId)
    }

    /**
     * Gets notification delivery success rate.
     *
     * @param userId The ID of the user
     * @return Percentage of successfully delivered notifications
     */
    suspend fun getNotificationDeliverySuccessRate(userId: Long): Double {
        require(userId > 0) { "User ID must be positive" }
        return notificationDao.getNotificationDeliverySuccessRate(userId)
    }

    /**
     * Gets average notification response time.
     *
     * @param userId The ID of the user
     * @return Average time from sent to clicked in milliseconds
     */
    suspend fun getAverageResponseTime(userId: Long): Long? {
        require(userId > 0) { "User ID must be positive" }
        return notificationDao.getAverageResponseTime(userId)
    }

    /**
     * Gets notification statistics for user dashboard.
     *
     * @param userId The ID of the user
     * @return NotificationStats with counts and metrics
     */
    suspend fun getNotificationStats(userId: Long): NotificationDao.NotificationStats {
        require(userId > 0) { "User ID must be positive" }
        return notificationDao.getNotificationStats(userId)
    }

    /**
     * Gets most recent notification by type.
     *
     * @param userId The ID of the user
     * @param type The notification type
     * @return Most recent notification of specified type
     */
    suspend fun getMostRecentNotificationByType(userId: Long, type: NotificationType): Notification? {
        require(userId > 0) { "User ID must be positive" }
        return notificationDao.getMostRecentNotificationByType(userId, type)
    }

    // endregion

    // region Search Operations

    /**
     * Searches notifications by title or message content.
     *
     * @param userId The ID of the user
     * @param searchQuery The search query for title or message
     * @return Flow of matching notifications
     */
    fun searchNotifications(userId: Long, searchQuery: String): Flow<List<Notification>> {
        require(userId > 0) { "User ID must be positive" }
        require(searchQuery.isNotBlank()) { "Search query cannot be blank" }

        return notificationDao.searchNotifications(userId, searchQuery.trim())
    }

    // endregion

    // region Data Maintenance

    /**
     * Deletes old notifications older than specified date.
     *
     * @param userId The ID of the user
     * @param olderThan The cutoff date for deletion
     * @throws IllegalArgumentException If user ID is invalid
     */
    suspend fun deleteOldNotifications(userId: Long, olderThan: Date) {
        require(userId > 0) { "User ID must be positive" }
        notificationDao.deleteOldNotifications(userId, olderThan)
    }

    /**
     * Validates notification data before operations.
     *
     * @param notification The notification to validate
     * @return true if valid, false otherwise
     */
    fun validateNotification(notification: Notification): Boolean {
        return notification.isValid() &&
            notification.userId > 0 &&
            notification.title.isNotBlank() &&
            notification.message.isNotBlank() &&
            notification.channelId.isNotBlank()
    }

    // endregion
}
