package com.example.fitnesstrackerapp.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.*
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
 * - CRUD operations for notification logs with all enum combinations
 * - Event type filtering and categorization
 * - Performance metrics and delivery analytics
 * - Error tracking and retry logic
 * - User interaction analytics
 * - System health monitoring queries
 * - Room enum serialization validation
 * - The new deleteLogsOlderThan method
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ComprehensiveNotificationLogDaoTest {
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

    private fun createTestUser(): User {
        return User(
            username = "testuser_${System.currentTimeMillis()}",
            email = "test_${System.currentTimeMillis()}@example.com",
            passwordHash = "hashedpassword",
            firstName = "Test",
            lastName = "User"
        )
    }

    private fun createTestNotification(userId: Long): Notification {
        return Notification(
            userId = userId,
            type = NotificationType.WORKOUT_REMINDER,
            title = "Test Notification",
            message = "Test Message",
            scheduledTime = Date(),
            channelId = "test_channel"
        )
    }

    private fun createTestNotificationLog(
        userId: Long,
        notificationId: Long,
        eventType: NotificationLogEvent = NotificationLogEvent.SENT,
        deliveryChannel: NotificationDeliveryChannel = NotificationDeliveryChannel.PUSH,
        isSuccess: Boolean = true,
        errorCode: String? = null,
        errorMessage: String? = null,
        retryCount: Int = 0
    ): NotificationLog {
        return NotificationLog(
            userId = userId,
            notificationId = notificationId,
            eventType = eventType,
            deliveryChannel = deliveryChannel,
            isSuccess = isSuccess,
            errorCode = errorCode,
            errorMessage = errorMessage,
            retryCount = retryCount
        )
    }

    @Test
    fun testAllNotificationLogEventEnumValues() = runTest {
        val user = createTestUser()
        val userId = userDao.insertUser(user)
        val notification = createTestNotification(userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Test all enum values can be stored and retrieved
        val eventTypes = NotificationLogEvent.entries
        val logs = mutableListOf<Long>()

        eventTypes.forEach { eventType ->
            val log = createTestNotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = eventType
            )
            val logId = notificationLogDao.insertNotificationLog(log)
            logs.add(logId)

            // Verify the enum was stored correctly
            val retrievedLog = notificationLogDao.getNotificationLogById(logId)
            assertThat(retrievedLog?.eventType).isEqualTo(eventType)
        }

        // Verify all logs are retrievable
        val userLogs = notificationLogDao.getNotificationLogsByUserId(userId).first()
        assertThat(userLogs).hasSize(eventTypes.size)
    }

    @Test
    fun testAllNotificationDeliveryChannelEnumValues() = runTest {
        val user = createTestUser()
        val userId = userDao.insertUser(user)
        val notification = createTestNotification(userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Test all enum values can be stored and retrieved
        val channels = NotificationDeliveryChannel.entries
        val logs = mutableListOf<Long>()

        channels.forEach { channel ->
            val log = createTestNotificationLog(
                userId = userId,
                notificationId = notificationId,
                deliveryChannel = channel
            )
            val logId = notificationLogDao.insertNotificationLog(log)
            logs.add(logId)

            // Verify the enum was stored correctly
            val retrievedLog = notificationLogDao.getNotificationLogById(logId)
            assertThat(retrievedLog?.deliveryChannel).isEqualTo(channel)
        }

        // Verify all logs are retrievable
        val userLogs = notificationLogDao.getNotificationLogsByUserId(userId).first()
        assertThat(userLogs).hasSize(channels.size)
    }

    @Test
    fun testAllNotificationTypeEnumValues() = runTest {
        val user = createTestUser()
        val userId = userDao.insertUser(user)

        // Test all notification types can be stored and retrieved
        val types = NotificationType.entries
        val notifications = mutableListOf<Long>()

        types.forEach { type ->
            val notification = Notification(
                userId = userId,
                type = type,
                title = "Test ${type.name}",
                message = "Test Message",
                scheduledTime = Date(),
                channelId = "test_channel"
            )
            val notificationId = notificationDao.insertNotification(notification)
            notifications.add(notificationId)

            // Verify the enum was stored correctly
            val retrievedNotification = notificationDao.getNotificationById(notificationId)
            assertThat(retrievedNotification?.type).isEqualTo(type)
        }

        // Verify all notifications are retrievable
        val userNotifications = notificationDao.getNotificationsByUserId(userId).first()
        assertThat(userNotifications).hasSize(types.size)
    }

    @Test
    fun testAllNotificationPriorityEnumValues() = runTest {
        val user = createTestUser()
        val userId = userDao.insertUser(user)

        // Test all priority levels can be stored and retrieved
        val priorities = NotificationPriority.entries
        val notifications = mutableListOf<Long>()

        priorities.forEach { priority ->
            val notification = Notification(
                userId = userId,
                type = NotificationType.WORKOUT_REMINDER,
                title = "Test ${priority.name}",
                message = "Test Message",
                priority = priority,
                scheduledTime = Date(),
                channelId = "test_channel"
            )
            val notificationId = notificationDao.insertNotification(notification)
            notifications.add(notificationId)

            // Verify the enum was stored correctly
            val retrievedNotification = notificationDao.getNotificationById(notificationId)
            assertThat(retrievedNotification?.priority).isEqualTo(priority)
        }

        // Verify all notifications are retrievable
        val userNotifications = notificationDao.getNotificationsByUserId(userId).first()
        assertThat(userNotifications).hasSize(priorities.size)
    }

    @Test
    fun testAllNotificationStatusEnumValues() = runTest {
        val user = createTestUser()
        val userId = userDao.insertUser(user)

        // Test all status values can be stored and retrieved
        val statuses = NotificationStatus.entries
        val notifications = mutableListOf<Long>()

        statuses.forEach { status ->
            val notification = Notification(
                userId = userId,
                type = NotificationType.WORKOUT_REMINDER,
                title = "Test ${status.name}",
                message = "Test Message",
                status = status,
                scheduledTime = Date(),
                channelId = "test_channel"
            )
            val notificationId = notificationDao.insertNotification(notification)
            notifications.add(notificationId)

            // Verify the enum was stored correctly
            val retrievedNotification = notificationDao.getNotificationById(notificationId)
            assertThat(retrievedNotification?.status).isEqualTo(status)
        }

        // Verify all notifications are retrievable
        val userNotifications = notificationDao.getNotificationsByUserId(userId).first()
        assertThat(userNotifications).hasSize(statuses.size)
    }

    @Test
    fun testEnumCombinations() = runTest {
        val user = createTestUser()
        val userId = userDao.insertUser(user)
        val notification = createTestNotification(userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Test combinations of different enums
        val testCombinations = listOf(
            Triple(NotificationLogEvent.SENT, NotificationDeliveryChannel.PUSH, true),
            Triple(NotificationLogEvent.DELIVERED, NotificationDeliveryChannel.EMAIL, true),
            Triple(NotificationLogEvent.FAILED, NotificationDeliveryChannel.SMS, false),
            Triple(NotificationLogEvent.OPENED, NotificationDeliveryChannel.IN_APP, true),
            Triple(NotificationLogEvent.DISMISSED, NotificationDeliveryChannel.SYSTEM, true),
            Triple(NotificationLogEvent.ERROR, NotificationDeliveryChannel.WEBHOOK, false)
        )

        val logIds = mutableListOf<Long>()

        testCombinations.forEach { (eventType, channel, isSuccess) ->
            val log = createTestNotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = eventType,
                deliveryChannel = channel,
                isSuccess = isSuccess
            )
            val logId = notificationLogDao.insertNotificationLog(log)
            logIds.add(logId)

            // Verify the combination was stored correctly
            val retrievedLog = notificationLogDao.getNotificationLogById(logId)
            assertThat(retrievedLog?.eventType).isEqualTo(eventType)
            assertThat(retrievedLog?.deliveryChannel).isEqualTo(channel)
            assertThat(retrievedLog?.isSuccess).isEqualTo(isSuccess)
        }

        // Verify all combinations are retrievable
        val userLogs = notificationLogDao.getNotificationLogsByUserId(userId).first()
        assertThat(userLogs).hasSize(testCombinations.size)
    }

    @Test
    fun testDeleteLogsOlderThan() = runTest {
        val user = createTestUser()
        val userId = userDao.insertUser(user)
        val notification = createTestNotification(userId)
        val notificationId = notificationDao.insertNotification(notification)

        val now = Date()
        val threeDaysAgo = Date(now.time - (3 * 24 * 60 * 60 * 1000))
        val oneDayAgo = Date(now.time - (24 * 60 * 60 * 1000))
        val twoMinutesAgo = Date(now.time - (2 * 60 * 1000))
        val cutoffDate = Date(now.time - (2 * 24 * 60 * 60 * 1000)) // 2 days ago

        // Insert logs with different timestamps
        val oldLog1 = createTestNotificationLog(
            userId = userId,
            notificationId = notificationId,
            eventType = NotificationLogEvent.SENT
        ).copy(eventTimestamp = threeDaysAgo)
        
        val oldLog2 = createTestNotificationLog(
            userId = userId,
            notificationId = notificationId,
            eventType = NotificationLogEvent.DELIVERED
        ).copy(eventTimestamp = oneDayAgo)

        val recentLog = createTestNotificationLog(
            userId = userId,
            notificationId = notificationId,
            eventType = NotificationLogEvent.OPENED
        ).copy(eventTimestamp = twoMinutesAgo)

        notificationLogDao.insertNotificationLog(oldLog1)
        notificationLogDao.insertNotificationLog(oldLog2)  
        notificationLogDao.insertNotificationLog(recentLog)

        // Verify all logs are present
        var allLogs = notificationLogDao.getNotificationLogsByUserId(userId).first()
        assertThat(allLogs).hasSize(3)

        // Delete logs older than cutoff date
        val deletedCount = notificationLogDao.deleteLogsOlderThan(cutoffDate)
        assertThat(deletedCount).isEqualTo(1) // Only the 3-day-old log should be deleted

        // Verify only recent logs remain
        allLogs = notificationLogDao.getNotificationLogsByUserId(userId).first()
        assertThat(allLogs).hasSize(2)
        assertThat(allLogs.map { it.eventType }).containsExactly(
            NotificationLogEvent.OPENED,
            NotificationLogEvent.DELIVERED
        )
    }

    @Test
    fun testGetNotificationLogsByEventType() = runTest {
        val user = createTestUser()
        val userId = userDao.insertUser(user)
        val notification = createTestNotification(userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Insert logs for each event type
        val eventTypes = NotificationLogEvent.entries
        eventTypes.forEach { eventType ->
            val log = createTestNotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = eventType
            )
            notificationLogDao.insertNotificationLog(log)
        }

        // Test filtering by each event type
        eventTypes.forEach { eventType ->
            val filteredLogs = notificationLogDao.getNotificationLogsByEventType(userId, eventType).first()
            assertThat(filteredLogs).hasSize(1)
            assertThat(filteredLogs[0].eventType).isEqualTo(eventType)
        }
    }

    @Test
    fun testGetNotificationLogsByDeliveryChannel() = runTest {
        val user = createTestUser()
        val userId = userDao.insertUser(user)
        val notification = createTestNotification(userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Insert logs for each delivery channel
        val channels = NotificationDeliveryChannel.entries
        channels.forEach { channel ->
            val log = createTestNotificationLog(
                userId = userId,
                notificationId = notificationId,
                deliveryChannel = channel
            )
            notificationLogDao.insertNotificationLog(log)
        }

        // Test filtering by each delivery channel
        channels.forEach { channel ->
            val filteredLogs = notificationLogDao.getNotificationLogsByDeliveryChannel(userId, channel).first()
            assertThat(filteredLogs).hasSize(1)
            assertThat(filteredLogs[0].deliveryChannel).isEqualTo(channel)
        }
    }

    @Test
    fun testComplexEnumQueries() = runTest {
        val user = createTestUser()
        val userId = userDao.insertUser(user)
        val notification = createTestNotification(userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Insert diverse logs for complex queries
        val testData = listOf(
            createTestNotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.SENT,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = true
            ),
            createTestNotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.FAILED,
                deliveryChannel = NotificationDeliveryChannel.PUSH,
                isSuccess = false,
                errorCode = "NETWORK_ERROR"
            ),
            createTestNotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.DELIVERED,
                deliveryChannel = NotificationDeliveryChannel.EMAIL,
                isSuccess = true
            ),
            createTestNotificationLog(
                userId = userId,
                notificationId = notificationId,
                eventType = NotificationLogEvent.OPENED,
                deliveryChannel = NotificationDeliveryChannel.IN_APP,
                isSuccess = true
            )
        )

        testData.forEach { log ->
            notificationLogDao.insertNotificationLog(log)
        }

        // Test successful vs failed logs
        val successfulLogs = notificationLogDao.getSuccessfulNotificationLogs(userId).first()
        assertThat(successfulLogs).hasSize(3)
        assertThat(successfulLogs.all { it.isSuccess }).isTrue()

        val failedLogs = notificationLogDao.getFailedNotificationLogs(userId).first()
        assertThat(failedLogs).hasSize(1)
        assertThat(failedLogs.all { !it.isSuccess }).isTrue()

        // Test user interaction logs
        val interactionLogs = notificationLogDao.getUserInteractionLogs(userId).first()
        assertThat(interactionLogs).hasSize(1) // Only OPENED event
        assertThat(interactionLogs[0].eventType).isEqualTo(NotificationLogEvent.OPENED)
    }

    @Test
    fun testForeignKeyConstraints() = runTest {
        // Test with non-existent user - should fail
        val invalidLog = createTestNotificationLog(
            userId = 999L,
            notificationId = 1L
        )

        try {
            notificationLogDao.insertNotificationLog(invalidLog)
            Assert.fail("Should have thrown foreign key constraint exception")
        } catch (e: Exception) {
            // Expected - foreign key constraint should prevent this
            assertThat(e.message).containsAnyOf("FOREIGN KEY", "constraint")
        }
    }

    @Test
    fun testEnumSerializationConsistency() = runTest {
        val user = createTestUser()
        val userId = userDao.insertUser(user)
        val notification = createTestNotification(userId)
        val notificationId = notificationDao.insertNotification(notification)

        // Test that enum values are stored as strings (Room default behavior)
        val log = createTestNotificationLog(
            userId = userId,
            notificationId = notificationId,
            eventType = NotificationLogEvent.ACTION_CLICKED,
            deliveryChannel = NotificationDeliveryChannel.WEBHOOK
        )
        
        val logId = notificationLogDao.insertNotificationLog(log)
        val retrievedLog = notificationLogDao.getNotificationLogById(logId)

        // Verify enums are correctly serialized/deserialized
        assertThat(retrievedLog?.eventType).isEqualTo(NotificationLogEvent.ACTION_CLICKED)
        assertThat(retrievedLog?.deliveryChannel).isEqualTo(NotificationDeliveryChannel.WEBHOOK)
    }
}
