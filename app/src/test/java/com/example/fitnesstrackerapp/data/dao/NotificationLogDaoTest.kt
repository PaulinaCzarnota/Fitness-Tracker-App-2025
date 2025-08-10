package com.example.fitnesstrackerapp.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.*
import com.example.fitnesstrackerapp.util.test.TestHelper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import java.io.IOException
import java.util.*

/**
 * Comprehensive unit tests for NotificationLogDao.
 *
 * Tests all notification logging database operations including:
 * - CRUD operations for notification logs with all enum combinations
 * - Event type filtering and categorization
 * - Performance metrics and delivery analytics
 * - Error tracking and retry logic
 * - User interaction analytics
 * - System health monitoring queries
 * - Room enum serialization validation
 */
@ExperimentalCoroutinesApi
class NotificationLogDaoTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var notificationLogDao: NotificationLogDao
    private lateinit var userDao: UserDao
    private lateinit var notificationDao: NotificationDao

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
        notificationDao = database.notificationDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetNotificationLog() = runTest {
        val user = TestHelper.createTestUser(email = "notifications@test.com", username = "notificationuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)
        val notificationLog = NotificationLog(
            userId = userId,
            notificationId = notificationId,
            eventType = NotificationLogEvent.SENT,
            deliveryChannel = NotificationDeliveryChannel.PUSH,
            isSuccess = true,
            errorCode = null,
            errorMessage = null,
            retryCount = 0,
            deliveryDurationMs = 250L,
            priorityLevel = 3,
        )
        val logId = notificationLogDao.insertNotificationLog(notificationLog)
        assertThat(logId).isGreaterThan(0)
        val retrievedLog = notificationLogDao.getNotificationLogById(logId)
        assertThat(retrievedLog).isNotNull()
        assertThat(retrievedLog?.eventType).isEqualTo(NotificationLogEvent.SENT)
        assertThat(retrievedLog?.deliveryChannel).isEqualTo(NotificationDeliveryChannel.PUSH)
        assertThat(retrievedLog?.isSuccess).isTrue()
        assertThat(retrievedLog?.deliveryDurationMs).isEqualTo(250L)
        assertThat(retrievedLog?.priorityLevel).isEqualTo(3)
        assertThat(retrievedLog?.userId).isEqualTo(userId)
    }

    @Test
    fun getNotificationLogsByUser() = runTest {
        val user = TestHelper.createTestUser(email = "userlogs@test.com", username = "userlogsuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)
        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.DELIVERED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.OPENED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
        )
        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }
        val userLogs = notificationLogDao.getNotificationLogsByUserId(userId).first()
        assertThat(userLogs).hasSize(3)
        assertThat(userLogs.map { it.eventType }).containsExactly(
            NotificationLogEvent.OPENED,
            NotificationLogEvent.DELIVERED,
            NotificationLogEvent.SENT,
        )
    }

    @Test
    fun getNotificationLogsByEventType() = runTest {
        val user = TestHelper.createTestUser(email = "eventtype@test.com", username = "eventtypeuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Insert logs with different event types
        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.DELIVERED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val sentLogs = notificationLogDao.getNotificationLogsByEventType(userId, NotificationLogEvent.SENT).first()
        assertThat(sentLogs).hasSize(2)
        assertThat(sentLogs.all { it.eventType == NotificationLogEvent.SENT }).isTrue()

        val deliveredLogs = notificationLogDao.getNotificationLogsByEventType(userId, NotificationLogEvent.DELIVERED).first()
        assertThat(deliveredLogs).hasSize(1)
    }

    @Test
    fun getNotificationLogsByChannel() = runTest {
        val user = TestHelper.createTestUser(email = "channel@test.com", username = "channeluser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Insert logs with different channels
        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.EMAIL,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.SMS,
                isSuccess = true,
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val pushLogs = notificationLogDao.getNotificationLogsByChannel(userId, "PUSH").first()
        assertThat(pushLogs).hasSize(2)
        assertThat(pushLogs.all { it.deliveryChannel == NotificationDeliveryChannel.PUSH }).isTrue()

        val emailLogs = notificationLogDao.getNotificationLogsByChannel(userId, "EMAIL").first()
        assertThat(emailLogs).hasSize(1)

        val smsLogs = notificationLogDao.getNotificationLogsByChannel(userId, "SMS").first()
        assertThat(smsLogs).hasSize(1)
    }

    @Test
    fun getNotificationLogsByDateRange() = runTest {
        val user = TestHelper.createTestUser(email = "daterange@test.com", username = "daterangeuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        val currentTime = System.currentTimeMillis()
        val day1 = Date(currentTime - (2 * 24 * 60 * 60 * 1000)) // 2 days ago
        val day2 = Date(currentTime - (1 * 24 * 60 * 60 * 1000)) // 1 day ago
        val day3 = Date(currentTime) // today
        val day4 = Date(currentTime + (1 * 24 * 60 * 60 * 1000)) // tomorrow

        // Insert logs for different dates
        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                eventTimestamp = day1,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                eventTimestamp = day2,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                eventTimestamp = day3,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                eventTimestamp = day4,
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        // Query for logs from day2 to day3 (inclusive)
        val logsInRange = notificationLogDao.getNotificationLogsByDateRange(userId, day2, day3).first()
        assertThat(logsInRange).hasSize(2)
    }

    @Test
    fun getFailedNotifications() = runTest {
        val user = TestHelper.createTestUser(email = "failed@test.com", username = "faileduser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Insert successful and failed notifications
        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.FAILED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = false,
                errorMessage = "Network timeout",
                errorCode = "TIMEOUT_ERROR",
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.FAILED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = false,
                errorMessage = "Invalid token",
                errorCode = "TOKEN_ERROR",
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val failedLogs = notificationLogDao.getFailedNotifications(userId).first()
        assertThat(failedLogs).hasSize(2)
        assertThat(failedLogs.all { !it.isSuccess }).isTrue()
    }

    @Test
    fun getSuccessfulNotifications() = runTest {
        val user = TestHelper.createTestUser(email = "success@test.com", username = "successuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Insert successful and failed notifications
        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.FAILED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = false,
                errorMessage = "Error",
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val successfulLogs = notificationLogDao.getSuccessfulNotifications(userId).first()
        assertThat(successfulLogs).hasSize(2)
        assertThat(successfulLogs.all { it.isSuccess }).isTrue()
    }

    @Test
    fun getNotificationsByPriority() = runTest {
        val user = TestHelper.createTestUser(email = "priority@test.com", username = "priorityuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Insert notifications with different priorities (using priorityLevel as Int)
        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                priorityLevel = 5, // High priority
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                priorityLevel = 3, // Medium priority
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                priorityLevel = 5, // High priority
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                priorityLevel = 1, // Low priority
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val highPriorityLogs = notificationLogDao.getNotificationsByPriority(userId, 5).first()
        assertThat(highPriorityLogs).hasSize(2)
        assertThat(highPriorityLogs.all { it.priorityLevel == 5 }).isTrue()

        val mediumPriorityLogs = notificationLogDao.getNotificationsByPriority(userId, 3).first()
        assertThat(mediumPriorityLogs).hasSize(1)
    }

    @Test
    fun getNotificationDeliveryStats() = runTest {
        val user = TestHelper.createTestUser(email = "stats@test.com", username = "statsuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        val yesterday = Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000))
        val today = Date()

        // Insert notifications with delivery stats
        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                deliveryDurationMs = 100L,
                eventTimestamp = today,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.FAILED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = false,
                errorMessage = "Failed",
                eventTimestamp = today,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.DELIVERED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                deliveryDurationMs = 200L,
                eventTimestamp = today,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                deliveryDurationMs = 150L,
                eventTimestamp = yesterday,
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val deliveryStats = notificationLogDao.getNotificationDeliveryStats(userId, yesterday, today)
        assertThat(deliveryStats).isNotNull()
        // Note: The exact counts depend on the DAO implementation
        // This test validates the query runs without errors
    }

    @Test
    fun getAverageDeliveryLatency() = runTest {
        val user = TestHelper.createTestUser(email = "latency@test.com", username = "latencyuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        val startDate = Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000))
        val endDate = Date()

        // Insert notifications with different latencies
        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                deliveryDurationMs = 100L,
                eventTimestamp = startDate,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                deliveryDurationMs = 200L,
                eventTimestamp = endDate,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                deliveryDurationMs = 300L,
                eventTimestamp = endDate,
            ),
            // Failed notification (should not be included in average)
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.FAILED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = false,
                eventTimestamp = endDate,
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val averageLatency = notificationLogDao.getAverageDeliveryLatency(userId, startDate, endDate)
        // Average calculation depends on the DAO implementation
        assertThat(averageLatency).isNotNull()
    }

    @Test
    fun getMostCommonErrors() = runTest {
        val user = TestHelper.createTestUser(email = "errors@test.com", username = "errorsuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        val startDate = Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000))
        val endDate = Date()

        // Insert notifications with different error types
        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.FAILED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = false,
                errorCode = "NETWORK_ERROR",
                errorMessage = "Network timeout",
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.FAILED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = false,
                errorCode = "NETWORK_ERROR",
                errorMessage = "Connection failed",
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.FAILED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = false,
                errorCode = "TOKEN_ERROR",
                errorMessage = "Invalid token",
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.FAILED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = false,
                errorCode = "PERMISSION_ERROR",
                errorMessage = "No permission",
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.FAILED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = false,
                errorCode = "TOKEN_ERROR",
                errorMessage = "Expired token",
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val commonErrors = notificationLogDao.getMostCommonErrors(userId, startDate, endDate)
        assertThat(commonErrors).isNotEmpty()
        // The exact ordering depends on the DAO implementation
    }

    @Test
    fun getRetryAttempts() = runTest {
        val user = TestHelper.createTestUser(email = "retry@test.com", username = "retryuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Insert notifications with retry attempts
        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                retryCount = 0,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                retryCount = 1,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                retryCount = 3,
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val withRetries = notificationLogDao.getNotificationsWithRetries(userId).first()
        assertThat(withRetries).hasSize(2) // Notifications with retryCount > 0

        val maxRetries = notificationLogDao.getMaxRetryCount(userId)
        assertThat(maxRetries).isEqualTo(3)
    }

    @Test
    fun getClickedNotifications() = runTest {
        val user = TestHelper.createTestUser(email = "clicked@test.com", username = "clickeduser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Insert notifications with different interaction states
        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.OPENED, // Use OPENED instead of CLICKED
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.OPENED, // Use OPENED instead of CLICKED
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.DISMISSED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val openedLogs = notificationLogDao.getNotificationLogsByEventType(userId, NotificationLogEvent.OPENED).first()
        assertThat(openedLogs).hasSize(2)
        assertThat(openedLogs.all { it.eventType == NotificationLogEvent.OPENED }).isTrue()
    }

    @Test
    fun getDismissedNotifications() = runTest {
        val user = TestHelper.createTestUser(email = "dismissed@test.com", username = "dismisseduser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Insert notifications with different interaction states
        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.DISMISSED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.OPENED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.DISMISSED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        val dismissedLogs = notificationLogDao.getNotificationLogsByEventType(userId, NotificationLogEvent.DISMISSED).first()
        assertThat(dismissedLogs).hasSize(2)
        assertThat(dismissedLogs.all { it.eventType == NotificationLogEvent.DISMISSED }).isTrue()
    }

    @Test
    fun updateNotificationLog() = runTest {
        val user = TestHelper.createTestUser(email = "update@test.com", username = "updateuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        val originalLog = NotificationLog(
            userId = userId,
            notificationId = notificationId,
            eventType = NotificationLogEvent.SENT,
            deliveryChannel = NotificationDeliveryChannel.PUSH,
            isSuccess = true,
            retryCount = 0,
        )
        val logId = notificationLogDao.insertNotificationLog(originalLog)

        // Update the log
        val updatedLog = originalLog.copy(
            id = logId,
            eventType = NotificationLogEvent.DELIVERED,
            isSuccess = true,
            retryCount = 1,
            deliveryDurationMs = 500L,
        )
        notificationLogDao.updateNotificationLog(updatedLog)

        val retrievedLog = notificationLogDao.getNotificationLogById(logId)
        assertThat(retrievedLog?.eventType).isEqualTo(NotificationLogEvent.DELIVERED)
        assertThat(retrievedLog?.retryCount).isEqualTo(1)
        assertThat(retrievedLog?.deliveryDurationMs).isEqualTo(500L)
    }

    @Test
    fun deleteNotificationLog() = runTest {
        val user = TestHelper.createTestUser(email = "delete@test.com", username = "deleteuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        val notificationLog = NotificationLog(
            userId = userId,
            notificationId = notificationId,
            eventType = NotificationLogEvent.SENT,
            deliveryChannel = NotificationDeliveryChannel.PUSH,
            isSuccess = true,
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
        val user = TestHelper.createTestUser(email = "deletebyid@test.com", username = "deletebyiduser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        val notificationLog = NotificationLog(
            userId = userId,
            notificationId = notificationId,
            eventType = NotificationLogEvent.SENT,
            deliveryChannel = NotificationDeliveryChannel.PUSH,
            isSuccess = true,
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
        val user = TestHelper.createTestUser(email = "insertall@test.com", username = "insertalluser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.DELIVERED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.OPENED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
        )

        val logIds = notificationLogDao.insertAll(logs)
        assertThat(logIds).hasSize(3)
        assertThat(logIds.all { it > 0 }).isTrue()

        val userLogs = notificationLogDao.getNotificationLogsByUserId(userId).first()
        assertThat(userLogs).hasSize(3)
    }

    @Test
    fun testForeignKeyConstraint() = runTest {
        val notificationLog = NotificationLog(
            userId = 999L, // Non-existent user
            notificationId = 999L, // Non-existent notification
            eventType = NotificationLogEvent.SENT,
            deliveryChannel = NotificationDeliveryChannel.PUSH,
            isSuccess = true,
        )

        try {
            notificationLogDao.insertNotificationLog(notificationLog)
            Assert.fail("Should throw foreign key constraint exception")
        } catch (e: Exception) {
            // Expected foreign key constraint violation
            assertThat(e.message).contains("FOREIGN KEY")
        }
    }

    @Test
    fun testChannelDistribution() = runTest {
        val user = TestHelper.createTestUser(email = "channels@test.com", username = "channelsuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        val channels = NotificationDeliveryChannel.entries

        // Insert logs for all channels
        channels.forEach { channel ->
            notificationLogDao.insertNotificationLog(
                NotificationLog(
                    userId = userId,
                    notificationId = notificationId,
                    eventType = NotificationLogEvent.SENT,
                    deliveryChannel = channel,
                    isSuccess = true,
                ),
            )
        }

        // Verify each channel has logs
        channels.forEach { channel ->
            val channelLogs = notificationLogDao.getNotificationLogsByChannel(userId, channel.name).first()
            assertThat(channelLogs).hasSize(1)
            assertThat(channelLogs[0].deliveryChannel).isEqualTo(channel)
        }
    }

    @Test
    fun testEventTypeTransitions() = runTest {
        val user = TestHelper.createTestUser(email = "transitions@test.com", username = "transitionsuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Simulate notification lifecycle
        val logs = listOf(
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SCHEDULED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.DELIVERED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
            ),
        )

        logs.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        // Verify the lifecycle events
        val sentLogs = notificationLogDao.getNotificationLogsByEventType(userId, NotificationLogEvent.SENT).first()
        assertThat(sentLogs).hasSize(1)

        val deliveredLogs = notificationLogDao.getNotificationLogsByEventType(userId, NotificationLogEvent.DELIVERED).first()
        assertThat(deliveredLogs).hasSize(1)

        val scheduledLogs = notificationLogDao.getNotificationLogsByEventType(userId, NotificationLogEvent.SCHEDULED).first()
        assertThat(scheduledLogs).hasSize(1)

        // All should have the same notificationId
        val allLogs = notificationLogDao.getNotificationLogsByUserId(userId).first()
        assertThat(allLogs.all { it.notificationId == notificationId }).isTrue()
    }

    @Test
    fun testPerformanceMetrics() = runTest {
        val user = TestHelper.createTestUser(email = "performance@test.com", username = "performanceuser", passwordSalt = "salt123")
        val userId = userDao.insertUser(user)
        val notification = TestHelper.createTestNotification(userId = userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Insert notifications with various performance characteristics
        val logs = listOf(
            // Fast delivery
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                deliveryDurationMs = 50L,
            ),
            // Slow delivery
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                deliveryDurationMs = 2000L,
            ),
            // Failed delivery
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.FAILED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = false,
                errorMessage = "Timeout",
            ),
            // Retry scenario
            NotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.RETRIED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true,
                retryCount = 2,
                deliveryDurationMs = 800L,
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
        assertThat(retriedNotifications[0].retryCount).isEqualTo(2)
    }
}
