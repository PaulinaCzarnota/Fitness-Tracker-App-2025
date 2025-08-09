package com.example.fitnesstrackerapp.data.repository

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
 * Comprehensive unit tests for NotificationRepository.
 *
 * Tests all notification repository operations including:
 * - CRUD operations through repository layer
 * - Analytics and metrics calculations
 * - Performance monitoring functionality
 * - Data transformation and business logic
 * - Repository pattern abstraction
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class NotificationRepositoryTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var repository: NotificationRepository
    private lateinit var testUserId: Long

    @Before
    fun setUp() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()

        repository = NotificationRepository(database.notificationLogDao())

        // Create test user
        val user = TestHelper.createTestUser(email = "repo@test.com", username = "repouser")
        testUserId = database.userDao().insertUser(user)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Test
    fun logNotificationEvent_insertsAndRetrievesCorrectly() = runTest {
        val logId = repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            channel = NotificationChannel.PUSH,
            title = "Test Notification",
            message = "This is a test notification",
            priority = NotificationPriority.HIGH,
            notificationId = "test_123",
            isSuccessful = true,
            deliveryLatencyMs = 150L,
            retryCount = 0,
            metadata = """{"source": "test"}""",
        )

        assertThat(logId).isGreaterThan(0)

        val retrievedLog = repository.getNotificationLogById(logId)
        assertThat(retrievedLog).isNotNull()
        assertThat(retrievedLog?.userId).isEqualTo(testUserId)
        assertThat(retrievedLog?.eventType).isEqualTo(NotificationEventType.SENT)
        assertThat(retrievedLog?.channel).isEqualTo(NotificationChannel.PUSH)
        assertThat(retrievedLog?.title).isEqualTo("Test Notification")
        assertThat(retrievedLog?.message).isEqualTo("This is a test notification")
        assertThat(retrievedLog?.priority).isEqualTo(NotificationPriority.HIGH)
        assertThat(retrievedLog?.notificationId).isEqualTo("test_123")
        assertThat(retrievedLog?.isSuccessful).isTrue()
        assertThat(retrievedLog?.deliveryLatencyMs).isEqualTo(150L)
        assertThat(retrievedLog?.retryCount).isEqualTo(0)
        assertThat(retrievedLog?.metadata).isEqualTo("""{"source": "test"}""")
    }

    @Test
    fun logNotificationEvent_withDefaults() = runTest {
        val logId = repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.DELIVERED,
            title = "Simple Notification",
        )

        val retrievedLog = repository.getNotificationLogById(logId)
        assertThat(retrievedLog).isNotNull()
        assertThat(retrievedLog?.channel).isEqualTo(NotificationChannel.PUSH) // Default
        assertThat(retrievedLog?.priority).isEqualTo(NotificationPriority.MEDIUM) // Default
        assertThat(retrievedLog?.isSuccessful).isTrue() // Default
        assertThat(retrievedLog?.retryCount).isEqualTo(0) // Default
    }

    @Test
    fun updateNotificationLog() = runTest {
        val logId = repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Original Title",
            isSuccessful = true,
        )

        val originalLog = repository.getNotificationLogById(logId)!!
        val updatedLog = originalLog.copy(
            title = "Updated Title",
            eventType = NotificationEventType.DELIVERED,
            deliveryLatencyMs = 300L,
        )

        repository.updateNotificationLog(updatedLog)

        val retrievedLog = repository.getNotificationLogById(logId)
        assertThat(retrievedLog?.title).isEqualTo("Updated Title")
        assertThat(retrievedLog?.eventType).isEqualTo(NotificationEventType.DELIVERED)
        assertThat(retrievedLog?.deliveryLatencyMs).isEqualTo(300L)
    }

    @Test
    fun deleteNotificationLog() = runTest {
        val logId = repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "To Be Deleted",
        )

        val log = repository.getNotificationLogById(logId)!!
        repository.deleteNotificationLog(log)

        val deletedLog = repository.getNotificationLogById(logId)
        assertThat(deletedLog).isNull()
    }

    @Test
    fun deleteNotificationLogById() = runTest {
        val logId = repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "To Be Deleted By ID",
        )

        repository.deleteNotificationLogById(logId)

        val deletedLog = repository.getNotificationLogById(logId)
        assertThat(deletedLog).isNull()
    }

    @Test
    fun insertNotificationLogs_bulkInsert() = runTest {
        val logs = (1..5).map { i ->
            TestHelper.createTestNotificationLog(
                userId = testUserId,
                title = "Bulk Log $i",
            )
        }

        val logIds = repository.insertNotificationLogs(logs)
        assertThat(logIds).hasSize(5)
        assertThat(logIds.all { it > 0 }).isTrue()

        val userLogs = repository.getNotificationLogsByUserId(testUserId).first()
        assertThat(userLogs).hasSize(5)
    }

    @Test
    fun getNotificationLogsByUserId() = runTest {
        // Insert logs for test user
        repeat(3) { i ->
            repository.logNotificationEvent(
                userId = testUserId,
                eventType = NotificationEventType.SENT,
                title = "User Log $i",
            )
        }

        val userLogs = repository.getNotificationLogsByUserId(testUserId).first()
        assertThat(userLogs).hasSize(3)
        assertThat(userLogs.all { it.userId == testUserId }).isTrue()
    }

    @Test
    fun getNotificationLogsByEventType() = runTest {
        // Insert logs with different event types
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Sent Log",
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.DELIVERED,
            title = "Delivered Log",
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Another Sent Log",
        )

        val sentLogs = repository.getNotificationLogsByEventType(testUserId, NotificationEventType.SENT).first()
        assertThat(sentLogs).hasSize(2)
        assertThat(sentLogs.all { it.eventType == NotificationEventType.SENT }).isTrue()

        val deliveredLogs = repository.getNotificationLogsByEventType(testUserId, NotificationEventType.DELIVERED).first()
        assertThat(deliveredLogs).hasSize(1)
        assertThat(deliveredLogs[0].title).isEqualTo("Delivered Log")
    }

    @Test
    fun getNotificationLogsByChannel() = runTest {
        // Insert logs with different channels
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            channel = NotificationChannel.PUSH,
            title = "Push Notification",
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            channel = NotificationChannel.EMAIL,
            title = "Email Notification",
        )

        val pushLogs = repository.getNotificationLogsByChannel(testUserId, NotificationChannel.PUSH).first()
        assertThat(pushLogs).hasSize(1)
        assertThat(pushLogs[0].title).isEqualTo("Push Notification")

        val emailLogs = repository.getNotificationLogsByChannel(testUserId, NotificationChannel.EMAIL).first()
        assertThat(emailLogs).hasSize(1)
        assertThat(emailLogs[0].title).isEqualTo("Email Notification")
    }

    @Test
    fun getNotificationLogsByDateRange() = runTest {
        val now = Date()
        val yesterday = Date(now.time - (24 * 60 * 60 * 1000))
        val twoDaysAgo = Date(now.time - (2 * 24 * 60 * 60 * 1000))

        // Insert logs with different timestamps
        val log1 = TestHelper.createTestNotificationLog(
            userId = testUserId,
            title = "Two Days Ago",
        ).copy(timestamp = twoDaysAgo)
        val log2 = TestHelper.createTestNotificationLog(
            userId = testUserId,
            title = "Yesterday",
        ).copy(timestamp = yesterday)
        val log3 = TestHelper.createTestNotificationLog(
            userId = testUserId,
            title = "Today",
        ).copy(timestamp = now)

        repository.insertNotificationLogs(listOf(log1, log2, log3))

        val logsInRange = repository.getNotificationLogsByDateRange(testUserId, yesterday, now).first()
        assertThat(logsInRange).hasSize(2)
        assertThat(logsInRange.map { it.title }).containsExactly("Today", "Yesterday")
    }

    @Test
    fun getFailedNotifications() = runTest {
        // Insert successful and failed notifications
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Failed Notification",
            isSuccessful = false,
            errorMessage = "Network error",
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Successful Notification",
            isSuccessful = true,
        )

        val failedLogs = repository.getFailedNotifications(testUserId).first()
        assertThat(failedLogs).hasSize(1)
        assertThat(failedLogs[0].title).isEqualTo("Failed Notification")
        assertThat(failedLogs[0].isSuccessful).isFalse()
    }

    @Test
    fun getSuccessfulNotifications() = runTest {
        // Insert successful and failed notifications
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Failed Notification",
            isSuccessful = false,
            errorMessage = "Network error",
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Successful Notification",
            isSuccessful = true,
        )

        val successfulLogs = repository.getSuccessfulNotifications(testUserId).first()
        assertThat(successfulLogs).hasSize(1)
        assertThat(successfulLogs[0].title).isEqualTo("Successful Notification")
        assertThat(successfulLogs[0].isSuccessful).isTrue()
    }

    @Test
    fun getNotificationsByPriority() = runTest {
        // Insert notifications with different priorities
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "High Priority",
            priority = NotificationPriority.HIGH,
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Low Priority",
            priority = NotificationPriority.LOW,
        )

        val highPriorityLogs = repository.getNotificationsByPriority(testUserId, NotificationPriority.HIGH).first()
        assertThat(highPriorityLogs).hasSize(1)
        assertThat(highPriorityLogs[0].title).isEqualTo("High Priority")

        val lowPriorityLogs = repository.getNotificationsByPriority(testUserId, NotificationPriority.LOW).first()
        assertThat(lowPriorityLogs).hasSize(1)
        assertThat(lowPriorityLogs[0].title).isEqualTo("Low Priority")
    }

    @Test
    fun getNotificationsWithRetries() = runTest {
        // Insert notifications with and without retries
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "No Retries",
            retryCount = 0,
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "With Retries",
            retryCount = 2,
        )

        val retriedLogs = repository.getNotificationsWithRetries(testUserId).first()
        assertThat(retriedLogs).hasSize(1)
        assertThat(retriedLogs[0].title).isEqualTo("With Retries")
        assertThat(retriedLogs[0].retryCount).isEqualTo(2)
    }

    @Test
    fun getClickedNotifications() = runTest {
        // Insert notifications with different event types
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.CLICKED,
            title = "Clicked Notification",
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Just Sent",
        )

        val clickedLogs = repository.getClickedNotifications(testUserId).first()
        assertThat(clickedLogs).hasSize(1)
        assertThat(clickedLogs[0].title).isEqualTo("Clicked Notification")
        assertThat(clickedLogs[0].eventType).isEqualTo(NotificationEventType.CLICKED)
    }

    @Test
    fun getDismissedNotifications() = runTest {
        // Insert notifications with different event types
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.DISMISSED,
            title = "Dismissed Notification",
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Just Sent",
        )

        val dismissedLogs = repository.getDismissedNotifications(testUserId).first()
        assertThat(dismissedLogs).hasSize(1)
        assertThat(dismissedLogs[0].title).isEqualTo("Dismissed Notification")
        assertThat(dismissedLogs[0].eventType).isEqualTo(NotificationEventType.DISMISSED)
    }

    @Test
    fun getNotificationDeliveryStats() = runTest {
        val now = Date()
        val yesterday = Date(now.time - (24 * 60 * 60 * 1000))

        // Insert various notification events
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Sent 1",
            isSuccessful = true,
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Sent 2",
            isSuccessful = false,
            errorMessage = "Failed",
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.DELIVERED,
            title = "Delivered 1",
            isSuccessful = true,
        )

        val stats = repository.getNotificationDeliveryStats(testUserId, yesterday, now)
        assertThat(stats).isNotNull()
        assertThat(stats?.totalSent).isEqualTo(1) // Only successful SENT events
        assertThat(stats?.totalDelivered).isEqualTo(1)
        assertThat(stats?.totalFailed).isEqualTo(1)
    }

    @Test
    fun getAverageDeliveryLatency() = runTest {
        val now = Date()
        val yesterday = Date(now.time - (24 * 60 * 60 * 1000))

        // Insert notifications with different latencies
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Fast",
            deliveryLatencyMs = 100L,
            isSuccessful = true,
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Slow",
            deliveryLatencyMs = 300L,
            isSuccessful = true,
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Failed",
            deliveryLatencyMs = null,
            isSuccessful = false,
        )

        val averageLatency = repository.getAverageDeliveryLatency(testUserId, yesterday, now)
        // Average of 100 and 300 = 200 (failed notification excluded)
        assertThat(averageLatency).isEqualTo(200.0)
    }

    @Test
    fun getMostCommonErrors() = runTest {
        // Insert notifications with different error types
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Network Error 1",
            isSuccessful = false,
            errorCode = "NETWORK_ERROR",
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Network Error 2",
            isSuccessful = false,
            errorCode = "NETWORK_ERROR",
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Token Error",
            isSuccessful = false,
            errorCode = "TOKEN_ERROR",
        )

        val commonErrors = repository.getMostCommonErrors(testUserId, 2)
        assertThat(commonErrors).hasSize(2)
        assertThat(commonErrors[0].errorCode).isEqualTo("NETWORK_ERROR")
        assertThat(commonErrors[0].frequency).isEqualTo(2)
        assertThat(commonErrors[1].errorCode).isEqualTo("TOKEN_ERROR")
        assertThat(commonErrors[1].frequency).isEqualTo(1)
    }

    @Test
    fun getMaxRetryCount() = runTest {
        // Insert notifications with different retry counts
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "No Retries",
            retryCount = 0,
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Few Retries",
            retryCount = 2,
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Many Retries",
            retryCount = 5,
        )

        val maxRetries = repository.getMaxRetryCount(testUserId)
        assertThat(maxRetries).isEqualTo(5)
    }

    @Test
    fun getTodayDeliveryStats() = runTest {
        // Insert notification for today
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Today's Notification",
            isSuccessful = true,
        )

        val stats = repository.getTodayDeliveryStats(testUserId)
        assertThat(stats).isNotNull()
        assertThat(stats?.totalSent).isEqualTo(1)
    }

    @Test
    fun getWeeklyDeliveryStats() = runTest {
        // Insert notification for this week
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "This Week's Notification",
            isSuccessful = true,
        )

        val stats = repository.getWeeklyDeliveryStats(testUserId)
        assertThat(stats).isNotNull()
        assertThat(stats?.totalSent).isEqualTo(1)
    }

    @Test
    fun getDeliverySuccessRate() = runTest {
        val now = Date()
        val yesterday = Date(now.time - (24 * 60 * 60 * 1000))

        // Insert successful and failed notifications
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Success 1",
            isSuccessful = true,
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.DELIVERED,
            title = "Success 2",
            isSuccessful = true,
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Failed",
            isSuccessful = false,
        )

        val successRate = repository.getDeliverySuccessRate(testUserId, yesterday, now)
        // 1 successful SENT, 1 DELIVERED, 1 failed SENT = 2/2 = 100%
        assertThat(successRate).isEqualTo(100.0)
    }

    @Test
    fun getNotificationInsights() = runTest {
        // Insert some sample data
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Test Notification",
            isSuccessful = true,
            deliveryLatencyMs = 150L,
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Failed Notification",
            isSuccessful = false,
            errorCode = "NETWORK_ERROR",
        )

        val insights = repository.getNotificationInsights(testUserId)
        assertThat(insights).isNotNull()
        assertThat(insights.totalSent).isEqualTo(1) // Only successful SENT
        assertThat(insights.totalFailed).isEqualTo(1)
        assertThat(insights.averageDeliveryLatencyMs).isEqualTo(150.0)
        assertThat(insights.commonErrors).hasSize(1)
        assertThat(insights.commonErrors[0].errorCode).isEqualTo("NETWORK_ERROR")
    }

    @Test
    fun getPerformanceMetrics() = runTest {
        // Insert sample performance data
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Fast Notification",
            isSuccessful = true,
            deliveryLatencyMs = 100L,
        )
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Failed Notification",
            isSuccessful = false,
            errorCode = "TIMEOUT_ERROR",
            retryCount = 2,
        )

        val metrics = repository.getPerformanceMetrics(testUserId)
        assertThat(metrics).isNotNull()
        assertThat(metrics.dailyVolume).isEqualTo(2) // Both notifications
        assertThat(metrics.averageLatencyMs).isEqualTo(100.0) // Only successful ones
        assertThat(metrics.errorRate).isEqualTo(50.0) // 1 failed out of 2 total
        assertThat(metrics.maxRetryCount).isEqualTo(2)
        assertThat(metrics.topErrors).contains("TIMEOUT_ERROR")
        assertThat(metrics.healthScore).isGreaterThan(0.0)
    }

    @Test
    fun calculateHealthScore_perfectConditions() = runTest {
        // Insert only successful, fast notifications
        repeat(10) { i ->
            repository.logNotificationEvent(
                userId = testUserId,
                eventType = NotificationEventType.SENT,
                title = "Perfect Notification $i",
                isSuccessful = true,
                deliveryLatencyMs = 50L, // Very fast
            )
        }

        val metrics = repository.getPerformanceMetrics(testUserId)
        assertThat(metrics.healthScore).isEqualTo(100.0) // Perfect score
    }

    @Test
    fun calculateHealthScore_poorConditions() = runTest {
        // Insert mostly failed, slow notifications with errors
        repeat(5) { i ->
            repository.logNotificationEvent(
                userId = testUserId,
                eventType = NotificationEventType.SENT,
                title = "Failed Notification $i",
                isSuccessful = false,
                deliveryLatencyMs = 3000L, // Very slow
                errorCode = "ERROR_TYPE_$i", // Different error types
            )
        }

        // Add one successful notification
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "One Success",
            isSuccessful = true,
            deliveryLatencyMs = 100L,
        )

        val metrics = repository.getPerformanceMetrics(testUserId)
        assertThat(metrics.healthScore).isLessThan(50.0) // Poor health score
    }

    @Test
    fun exportAnalyticsData() = runTest {
        val now = Date()
        val yesterday = Date(now.time - (24 * 60 * 60 * 1000))

        // Insert sample data
        repository.logNotificationEvent(
            userId = testUserId,
            eventType = NotificationEventType.SENT,
            title = "Export Test",
            isSuccessful = true,
            deliveryLatencyMs = 200L,
        )

        val exportData = repository.exportAnalyticsData(testUserId, yesterday, now)
        assertThat(exportData).isNotNull()
        assertThat(exportData).containsKey("period")
        assertThat(exportData).containsKey("deliveryStats")
        assertThat(exportData).containsKey("insights")
        assertThat(exportData).containsKey("performance")
        assertThat(exportData).containsKey("exportTimestamp")

        val period = exportData["period"] as Map<*, *>
        assertThat(period["startDate"]).isEqualTo(yesterday)
        assertThat(period["endDate"]).isEqualTo(now)
    }
}
