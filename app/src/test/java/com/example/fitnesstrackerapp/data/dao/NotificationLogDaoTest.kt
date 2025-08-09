package com.example.fitnesstrackerapp.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.*
import com.example.fitnesstrackerapp.util.test.TestHelper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

/**
 * Comprehensive unit tests for NotificationLogDao.
 *
 * Tests all notification logging database operations including:
 * - CRUD operations for notification logs
 * - Event type filtering and categorization
 * - Performance metrics and delivery analytics
 * - Error tracking and retry logic
 * - User interaction analytics
 * - System health monitoring queries
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class NotificationLogDaoTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var notificationLogDao: NotificationLogDao
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()

        notificationLogDao = database.notificationLogDao()
        userDao = database.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetNotificationLog() = runTest {
        // Create user first
        val user = TestHelper.createTestUser(email = "notifications@test.com", username = "notificationuser")
        val userId = userDao.insertUser(user)

        // Create and insert notification log
        val notificationLog = TestHelper.createTestNotificationLog(
            userId = userId,
            eventType = NotificationEventType.SENT,
            channel = NotificationChannel.PUSH,
            title = "Workout Reminder",
            message = "Time for your workout!",
            priority = NotificationPriority.MEDIUM,
            isSuccessful = true,
            deliveryLatencyMs = 250L,
        )

        val logId = notificationLogDao.insertNotificationLog(notificationLog)
        assertThat(logId).isGreaterThan(0)

        val retrievedLog = notificationLogDao.getNotificationLogById(logId)
        assertThat(retrievedLog).isNotNull()
        assertThat(retrievedLog?.eventType).isEqualTo(NotificationEventType.SENT)
        assertThat(retrievedLog?.channel).isEqualTo(NotificationChannel.PUSH)
        assertThat(retrievedLog?.title).isEqualTo("Workout Reminder")
        assertThat(retrievedLog?.message).isEqualTo("Time for your workout!")
        assertThat(retrievedLog?.priority).isEqualTo(NotificationPriority.MEDIUM)
        assertThat(retrievedLog?.isSuccessful).isTrue()
        assertThat(retrievedLog?.deliveryLatencyMs).isEqualTo(250L)
        assertThat(retrievedLog?.userId).isEqualTo(userId)
    }

    @Test
    fun getNotificationLogsByUser() = runTest {
        val user = TestHelper.createTestUser(email = "userlogs@test.com", username = "userlogsuser")
        val userId = userDao.insertUser(user)

        // Insert multiple notification logs
        val logs = listOf(
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.SENT,
                title = "Workout Reminder",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.DELIVERED,
                title = "Goal Achieved",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.CLICKED,
                title = "Weekly Summary",
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val userLogs = notificationLogDao.getNotificationLogsByUserId(userId).first()
        assertThat(userLogs).hasSize(3)

        // Should be ordered by timestamp DESC
        val titles = userLogs.map { it.title }
        assertThat(titles).containsExactly("Weekly Summary", "Goal Achieved", "Workout Reminder")
    }

    @Test
    fun getNotificationLogsByEventType() = runTest {
        val user = TestHelper.createTestUser(email = "eventtype@test.com", username = "eventtypeuser")
        val userId = userDao.insertUser(user)

        // Insert logs with different event types
        val logs = listOf(
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.SENT,
                title = "Sent Notification 1",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.SENT,
                title = "Sent Notification 2",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.DELIVERED,
                title = "Delivered Notification",
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val sentLogs = notificationLogDao.getNotificationLogsByEventType(userId, NotificationEventType.SENT).first()
        assertThat(sentLogs).hasSize(2)
        assertThat(sentLogs.all { it.eventType == NotificationEventType.SENT }).isTrue()

        val deliveredLogs = notificationLogDao.getNotificationLogsByEventType(userId, NotificationEventType.DELIVERED).first()
        assertThat(deliveredLogs).hasSize(1)
        assertThat(deliveredLogs[0].title).isEqualTo("Delivered Notification")
    }

    @Test
    fun getNotificationLogsByChannel() = runTest {
        val user = TestHelper.createTestUser(email = "channel@test.com", username = "channeluser")
        val userId = userDao.insertUser(user)

        // Insert logs with different channels
        val logs = listOf(
            TestHelper.createTestNotificationLog(
                userId = userId,
                channel = NotificationChannel.PUSH,
                title = "Push Notification 1",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                channel = NotificationChannel.PUSH,
                title = "Push Notification 2",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                channel = NotificationChannel.EMAIL,
                title = "Email Notification",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                channel = NotificationChannel.SMS,
                title = "SMS Notification",
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val pushLogs = notificationLogDao.getNotificationLogsByChannel(userId, NotificationChannel.PUSH).first()
        assertThat(pushLogs).hasSize(2)
        assertThat(pushLogs.all { it.channel == NotificationChannel.PUSH }).isTrue()

        val emailLogs = notificationLogDao.getNotificationLogsByChannel(userId, NotificationChannel.EMAIL).first()
        assertThat(emailLogs).hasSize(1)
        assertThat(emailLogs[0].title).isEqualTo("Email Notification")

        val smsLogs = notificationLogDao.getNotificationLogsByChannel(userId, NotificationChannel.SMS).first()
        assertThat(smsLogs).hasSize(1)
        assertThat(smsLogs[0].title).isEqualTo("SMS Notification")
    }

    @Test
    fun getNotificationLogsByDateRange() = runTest {
        val user = TestHelper.createTestUser(email = "daterange@test.com", username = "daterangeuser")
        val userId = userDao.insertUser(user)

        val currentTime = System.currentTimeMillis()
        val day1 = Date(currentTime - (2 * 24 * 60 * 60 * 1000)) // 2 days ago
        val day2 = Date(currentTime - (1 * 24 * 60 * 60 * 1000)) // 1 day ago
        val day3 = Date(currentTime) // today
        val day4 = Date(currentTime + (1 * 24 * 60 * 60 * 1000)) // tomorrow

        // Insert logs for different dates
        val logs = listOf(
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Day 1 Notification",
            ).copy(timestamp = day1),
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Day 2 Notification",
            ).copy(timestamp = day2),
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Day 3 Notification",
            ).copy(timestamp = day3),
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Day 4 Notification",
            ).copy(timestamp = day4),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        // Query for logs from day2 to day3 (inclusive)
        val logsInRange = notificationLogDao.getNotificationLogsByDateRange(userId, day2, day3).first()
        assertThat(logsInRange).hasSize(2)
        assertThat(logsInRange.map { it.title }).containsExactly("Day 3 Notification", "Day 2 Notification")
    }

    @Test
    fun getFailedNotifications() = runTest {
        val user = TestHelper.createTestUser(email = "failed@test.com", username = "faileduser")
        val userId = userDao.insertUser(user)

        // Insert successful and failed notifications
        val logs = listOf(
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Failed Notification 1",
                isSuccessful = false,
                errorMessage = "Network timeout",
                errorCode = "TIMEOUT_ERROR",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Successful Notification",
                isSuccessful = true,
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Failed Notification 2",
                isSuccessful = false,
                errorMessage = "Invalid token",
                errorCode = "TOKEN_ERROR",
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val failedLogs = notificationLogDao.getFailedNotifications(userId).first()
        assertThat(failedLogs).hasSize(2)
        assertThat(failedLogs.all { !it.isSuccessful }).isTrue()
        assertThat(failedLogs.map { it.title }).containsExactly("Failed Notification 2", "Failed Notification 1")
    }

    @Test
    fun getSuccessfulNotifications() = runTest {
        val user = TestHelper.createTestUser(email = "success@test.com", username = "successuser")
        val userId = userDao.insertUser(user)

        // Insert successful and failed notifications
        val logs = listOf(
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Successful Notification 1",
                isSuccessful = true,
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Failed Notification",
                isSuccessful = false,
                errorMessage = "Error",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Successful Notification 2",
                isSuccessful = true,
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val successfulLogs = notificationLogDao.getSuccessfulNotifications(userId).first()
        assertThat(successfulLogs).hasSize(2)
        assertThat(successfulLogs.all { it.isSuccessful }).isTrue()
        assertThat(successfulLogs.map { it.title }).containsExactly("Successful Notification 2", "Successful Notification 1")
    }

    @Test
    fun getNotificationsByPriority() = runTest {
        val user = TestHelper.createTestUser(email = "priority@test.com", username = "priorityuser")
        val userId = userDao.insertUser(user)

        // Insert notifications with different priorities
        val logs = listOf(
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "High Priority 1",
                priority = NotificationPriority.HIGH,
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Medium Priority",
                priority = NotificationPriority.MEDIUM,
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "High Priority 2",
                priority = NotificationPriority.HIGH,
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Low Priority",
                priority = NotificationPriority.LOW,
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val highPriorityLogs = notificationLogDao.getNotificationsByPriority(userId, NotificationPriority.HIGH).first()
        assertThat(highPriorityLogs).hasSize(2)
        assertThat(highPriorityLogs.all { it.priority == NotificationPriority.HIGH }).isTrue()

        val mediumPriorityLogs = notificationLogDao.getNotificationsByPriority(userId, NotificationPriority.MEDIUM).first()
        assertThat(mediumPriorityLogs).hasSize(1)
        assertThat(mediumPriorityLogs[0].title).isEqualTo("Medium Priority")
    }

    @Test
    fun getNotificationDeliveryStats() = runTest {
        val user = TestHelper.createTestUser(email = "stats@test.com", username = "statsuser")
        val userId = userDao.insertUser(user)

        val yesterday = Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000))
        val today = Date()

        // Insert notifications with delivery stats
        val logs = listOf(
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.SENT,
                isSuccessful = true,
                deliveryLatencyMs = 100L,
            ).copy(timestamp = today),
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.SENT,
                isSuccessful = false,
                deliveryLatencyMs = null,
                errorMessage = "Failed",
            ).copy(timestamp = today),
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.DELIVERED,
                isSuccessful = true,
                deliveryLatencyMs = 200L,
            ).copy(timestamp = today),
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.SENT,
                isSuccessful = true,
                deliveryLatencyMs = 150L,
            ).copy(timestamp = yesterday),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val deliveryStats = notificationLogDao.getNotificationDeliveryStats(userId, yesterday, today)
        assertThat(deliveryStats).isNotNull()
        assertThat(deliveryStats?.totalSent).isEqualTo(3) // 2 from today + 1 from yesterday (successful SENT events)
        assertThat(deliveryStats?.totalDelivered).isEqualTo(1) // 1 DELIVERED event
        assertThat(deliveryStats?.totalFailed).isEqualTo(1) // 1 failed SENT event
    }

    @Test
    fun getAverageDeliveryLatency() = runTest {
        val user = TestHelper.createTestUser(email = "latency@test.com", username = "latencyuser")
        val userId = userDao.insertUser(user)

        val startDate = Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000))
        val endDate = Date()

        // Insert notifications with different latencies
        val logs = listOf(
            TestHelper.createTestNotificationLog(
                userId = userId,
                deliveryLatencyMs = 100L,
                isSuccessful = true,
            ).copy(timestamp = startDate),
            TestHelper.createTestNotificationLog(
                userId = userId,
                deliveryLatencyMs = 200L,
                isSuccessful = true,
            ).copy(timestamp = endDate),
            TestHelper.createTestNotificationLog(
                userId = userId,
                deliveryLatencyMs = 300L,
                isSuccessful = true,
            ).copy(timestamp = endDate),
            // Failed notification (should not be included in average)
            TestHelper.createTestNotificationLog(
                userId = userId,
                deliveryLatencyMs = null,
                isSuccessful = false,
            ).copy(timestamp = endDate),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val averageLatency = notificationLogDao.getAverageDeliveryLatency(userId, startDate, endDate)
        // Average of 100, 200, 300 = 200
        assertThat(averageLatency).isEqualTo(200.0)
    }

    @Test
    fun getMostCommonErrors() = runTest {
        val user = TestHelper.createTestUser(email = "errors@test.com", username = "errorsuser")
        val userId = userDao.insertUser(user)

        // Insert notifications with different error types
        val logs = listOf(
            TestHelper.createTestNotificationLog(
                userId = userId,
                isSuccessful = false,
                errorCode = "NETWORK_ERROR",
                errorMessage = "Network timeout",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                isSuccessful = false,
                errorCode = "NETWORK_ERROR",
                errorMessage = "Connection failed",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                isSuccessful = false,
                errorCode = "TOKEN_ERROR",
                errorMessage = "Invalid token",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                isSuccessful = false,
                errorCode = "PERMISSION_ERROR",
                errorMessage = "No permission",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                isSuccessful = false,
                errorCode = "TOKEN_ERROR",
                errorMessage = "Expired token",
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val commonErrors = notificationLogDao.getMostCommonErrors(userId, 3)
        assertThat(commonErrors).hasSize(3)

        // Should be ordered by frequency DESC
        assertThat(commonErrors[0].errorCode).isEqualTo("NETWORK_ERROR")
        assertThat(commonErrors[0].frequency).isEqualTo(2)
        assertThat(commonErrors[1].errorCode).isEqualTo("TOKEN_ERROR")
        assertThat(commonErrors[1].frequency).isEqualTo(2)
        assertThat(commonErrors[2].errorCode).isEqualTo("PERMISSION_ERROR")
        assertThat(commonErrors[2].frequency).isEqualTo(1)
    }

    @Test
    fun getRetryAttempts() = runTest {
        val user = TestHelper.createTestUser(email = "retry@test.com", username = "retryuser")
        val userId = userDao.insertUser(user)

        // Insert notifications with retry attempts
        val logs = listOf(
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Single Attempt",
                retryCount = 0,
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Two Attempts",
                retryCount = 1,
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Multiple Attempts",
                retryCount = 3,
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val withRetries = notificationLogDao.getNotificationsWithRetries(userId).first()
        assertThat(withRetries).hasSize(2) // Notifications with retryCount > 0
        assertThat(withRetries.map { it.title }).containsExactly("Multiple Attempts", "Two Attempts")

        val maxRetries = notificationLogDao.getMaxRetryCount(userId)
        assertThat(maxRetries).isEqualTo(3)
    }

    @Test
    fun getClickedNotifications() = runTest {
        val user = TestHelper.createTestUser(email = "clicked@test.com", username = "clickeduser")
        val userId = userDao.insertUser(user)

        // Insert notifications with different interaction states
        val logs = listOf(
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.CLICKED,
                title = "Clicked Notification 1",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.SENT,
                title = "Just Sent Notification",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.CLICKED,
                title = "Clicked Notification 2",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.DISMISSED,
                title = "Dismissed Notification",
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val clickedLogs = notificationLogDao.getClickedNotifications(userId).first()
        assertThat(clickedLogs).hasSize(2)
        assertThat(clickedLogs.all { it.eventType == NotificationEventType.CLICKED }).isTrue()
        assertThat(clickedLogs.map { it.title }).containsExactly("Clicked Notification 2", "Clicked Notification 1")
    }

    @Test
    fun getDismissedNotifications() = runTest {
        val user = TestHelper.createTestUser(email = "dismissed@test.com", username = "dismisseduser")
        val userId = userDao.insertUser(user)

        // Insert notifications with different interaction states
        val logs = listOf(
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.DISMISSED,
                title = "Dismissed Notification 1",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.CLICKED,
                title = "Clicked Notification",
            ),
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.DISMISSED,
                title = "Dismissed Notification 2",
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val dismissedLogs = notificationLogDao.getDismissedNotifications(userId).first()
        assertThat(dismissedLogs).hasSize(2)
        assertThat(dismissedLogs.all { it.eventType == NotificationEventType.DISMISSED }).isTrue()
        assertThat(dismissedLogs.map { it.title }).containsExactly("Dismissed Notification 2", "Dismissed Notification 1")
    }

    @Test
    fun updateNotificationLog() = runTest {
        val user = TestHelper.createTestUser(email = "update@test.com", username = "updateuser")
        val userId = userDao.insertUser(user)

        val originalLog = TestHelper.createTestNotificationLog(
            userId = userId,
            title = "Original Title",
            eventType = NotificationEventType.SENT,
            isSuccessful = true,
            retryCount = 0,
        )
        val logId = notificationLogDao.insertNotificationLog(originalLog)

        // Update the log
        val updatedLog = originalLog.copy(
            id = logId,
            title = "Updated Title",
            eventType = NotificationEventType.DELIVERED,
            isSuccessful = true,
            retryCount = 1,
            deliveryLatencyMs = 500L,
        )
        notificationLogDao.updateNotificationLog(updatedLog)

        val retrievedLog = notificationLogDao.getNotificationLogById(logId)
        assertThat(retrievedLog?.title).isEqualTo("Updated Title")
        assertThat(retrievedLog?.eventType).isEqualTo(NotificationEventType.DELIVERED)
        assertThat(retrievedLog?.retryCount).isEqualTo(1)
        assertThat(retrievedLog?.deliveryLatencyMs).isEqualTo(500L)
    }

    @Test
    fun deleteNotificationLog() = runTest {
        val user = TestHelper.createTestUser(email = "delete@test.com", username = "deleteuser")
        val userId = userDao.insertUser(user)

        val notificationLog = TestHelper.createTestNotificationLog(
            userId = userId,
            title = "To Be Deleted",
        )
        val logId = notificationLogDao.insertNotificationLog(notificationLog)

        // Verify log exists
        var retrievedLog = notificationLogDao.getNotificationLogById(logId)
        assertThat(retrievedLog).isNotNull()

        // Delete the log
        notificationLogDao.deleteNotificationLog(notificationLog.copy(id = logId))

        // Verify log is deleted
        retrievedLog = notificationLogDao.getNotificationLogById(logId)
        assertThat(retrievedLog).isNull()
    }

    @Test
    fun deleteNotificationLogById() = runTest {
        val user = TestHelper.createTestUser(email = "deletebyid@test.com", username = "deletebyiduser")
        val userId = userDao.insertUser(user)

        val notificationLog = TestHelper.createTestNotificationLog(
            userId = userId,
            title = "To Be Deleted By ID",
        )
        val logId = notificationLogDao.insertNotificationLog(notificationLog)

        // Verify log exists
        var retrievedLog = notificationLogDao.getNotificationLogById(logId)
        assertThat(retrievedLog).isNotNull()

        // Delete by ID
        notificationLogDao.deleteNotificationLogById(logId)

        // Verify log is deleted
        retrievedLog = notificationLogDao.getNotificationLogById(logId)
        assertThat(retrievedLog).isNull()
    }

    @Test
    fun insertAllNotificationLogs() = runTest {
        val user = TestHelper.createTestUser(email = "insertall@test.com", username = "insertalluser")
        val userId = userDao.insertUser(user)

        val logs = listOf(
            TestHelper.createTestNotificationLog(userId = userId, title = "Bulk Log 1"),
            TestHelper.createTestNotificationLog(userId = userId, title = "Bulk Log 2"),
            TestHelper.createTestNotificationLog(userId = userId, title = "Bulk Log 3"),
        )

        val logIds = notificationLogDao.insertAll(logs)
        assertThat(logIds).hasSize(3)
        assertThat(logIds.all { it > 0 }).isTrue()

        val userLogs = notificationLogDao.getNotificationLogsByUserId(userId).first()
        assertThat(userLogs).hasSize(3)
    }

    @Test
    fun testForeignKeyConstraint() = runTest {
        val notificationLog = TestHelper.createTestNotificationLog(
            userId = 999L, // Non-existent user
            title = "Invalid Log",
        )

        try {
            notificationLogDao.insertNotificationLog(notificationLog)
            Assert.fail("Should throw foreign key constraint exception")
        } catch (e: Exception) {
            // Expected foreign key constraint violation
            assertThat(e.message).containsAnyOf("FOREIGN KEY", "constraint", "no such table")
        }
    }

    @Test
    fun testChannelDistribution() = runTest {
        val user = TestHelper.createTestUser(email = "channels@test.com", username = "channelsuser")
        val userId = userDao.insertUser(user)

        val channels = NotificationChannel.entries

        // Insert logs for all channels
        channels.forEach { channel ->
            notificationLogDao.insertNotificationLog(
                TestHelper.createTestNotificationLog(
                    userId = userId,
                    title = "${channel.name} Notification",
                    channel = channel,
                ),
            )
        }

        // Verify each channel has logs
        channels.forEach { channel ->
            val channelLogs = notificationLogDao.getNotificationLogsByChannel(userId, channel).first()
            assertThat(channelLogs).hasSize(1)
            assertThat(channelLogs[0].channel).isEqualTo(channel)
        }
    }

    @Test
    fun testEventTypeTransitions() = runTest {
        val user = TestHelper.createTestUser(email = "transitions@test.com", username = "transitionsuser")
        val userId = userDao.insertUser(user)

        val timestamp = Date()

        // Simulate notification lifecycle
        val logs = listOf(
            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.SENT,
                title = "Lifecycle Test",
                notificationId = "lifecycle_123",
            ).copy(timestamp = Date(timestamp.time)),

            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.DELIVERED,
                title = "Lifecycle Test",
                notificationId = "lifecycle_123",
            ).copy(timestamp = Date(timestamp.time + 1000)),

            TestHelper.createTestNotificationLog(
                userId = userId,
                eventType = NotificationEventType.CLICKED,
                title = "Lifecycle Test",
                notificationId = "lifecycle_123",
            ).copy(timestamp = Date(timestamp.time + 5000)),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        // Verify the lifecycle events
        val sentLogs = notificationLogDao.getNotificationLogsByEventType(userId, NotificationEventType.SENT).first()
        assertThat(sentLogs).hasSize(1)

        val deliveredLogs = notificationLogDao.getNotificationLogsByEventType(userId, NotificationEventType.DELIVERED).first()
        assertThat(deliveredLogs).hasSize(1)

        val clickedLogs = notificationLogDao.getNotificationLogsByEventType(userId, NotificationEventType.CLICKED).first()
        assertThat(clickedLogs).hasSize(1)

        // All should have the same notificationId
        val allLogs = notificationLogDao.getNotificationLogsByUserId(userId).first()
        assertThat(allLogs.all { it.notificationId == "lifecycle_123" }).isTrue()
    }

    @Test
    fun testPerformanceMetrics() = runTest {
        val user = TestHelper.createTestUser(email = "performance@test.com", username = "performanceuser")
        val userId = userDao.insertUser(user)

        // Insert notifications with various performance characteristics
        val logs = listOf(
            // Fast delivery
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Fast Notification",
                deliveryLatencyMs = 50L,
                isSuccessful = true,
            ),
            // Slow delivery
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Slow Notification",
                deliveryLatencyMs = 2000L,
                isSuccessful = true,
            ),
            // Failed delivery
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Failed Notification",
                deliveryLatencyMs = null,
                isSuccessful = false,
                errorMessage = "Timeout",
            ),
            // Retry scenario
            TestHelper.createTestNotificationLog(
                userId = userId,
                title = "Retry Notification",
                deliveryLatencyMs = 800L,
                retryCount = 2,
                isSuccessful = true,
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val startDate = Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000))
        val endDate = Date()

        // Test various performance queries
        val averageLatency = notificationLogDao.getAverageDeliveryLatency(userId, startDate, endDate)
        assertThat(averageLatency).isNotNull()

        val maxRetries = notificationLogDao.getMaxRetryCount(userId)
        assertThat(maxRetries).isEqualTo(2)

        val retriedNotifications = notificationLogDao.getNotificationsWithRetries(userId).first()
        assertThat(retriedNotifications).hasSize(1)
        assertThat(retriedNotifications[0].title).isEqualTo("Retry Notification")
    }
}
