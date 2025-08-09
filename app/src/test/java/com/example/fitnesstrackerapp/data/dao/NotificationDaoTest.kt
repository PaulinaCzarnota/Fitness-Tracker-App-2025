package com.example.fitnesstrackerapp.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.*
import com.example.fitnesstrackerapp.util.test.TestHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

/**
 * Unit tests for NotificationDao.
 *
 * Tests all notification-related database operations including
 * CRUD operations, status updates, analytics queries, and
 * relationship management with other entities.
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class NotificationDaoTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var notificationDao: NotificationDao
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()

        notificationDao = database.notificationDao()
        userDao = database.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetNotification() = runTest {
        // Create user first
        val user = TestHelper.createTestUser(email = "notify@test.com", username = "notifyuser")
        val userId = userDao.insertUser(user)

        // Create notification
        val notification = TestHelper.createTestNotification(
            userId = userId,
            type = NotificationType.WORKOUT_REMINDER,
            title = "Workout Time",
            message = "Time for your daily workout!",
        )

        val notificationId = notificationDao.insertNotification(notification)
        Assert.assertTrue("Notification ID should be valid", notificationId > 0)

        val retrievedNotification = notificationDao.getNotificationById(notificationId)
        Assert.assertNotNull("Notification should exist", retrievedNotification)
        Assert.assertEquals("Title should match", "Workout Time", retrievedNotification?.title)
        Assert.assertEquals("Type should match", NotificationType.WORKOUT_REMINDER, retrievedNotification?.type)
        Assert.assertEquals("User ID should match", userId, retrievedNotification?.userId)
    }

    @Test
    fun getNotificationsByUserId() = runTest {
        val user = TestHelper.createTestUser(email = "user@test.com", username = "testuser")
        val userId = userDao.insertUser(user)

        // Insert multiple notifications
        val notifications = listOf(
            TestHelper.createTestNotification(userId = userId, title = "Notification 1", type = NotificationType.WORKOUT_REMINDER),
            TestHelper.createTestNotification(userId = userId, title = "Notification 2", type = NotificationType.GOAL_ACHIEVEMENT),
            TestHelper.createTestNotification(userId = userId, title = "Notification 3", type = NotificationType.DAILY_MOTIVATION),
        )

        notifications.forEach { notification ->
            notificationDao.insertNotification(notification)
        }

        val userNotifications = notificationDao.getNotificationsByUserId(userId).first()
        Assert.assertEquals("Should have 3 notifications", 3, userNotifications.size)
        Assert.assertTrue(
            "Should contain all notification types",
            userNotifications.map { it.type }.containsAll(
                listOf(NotificationType.WORKOUT_REMINDER, NotificationType.GOAL_ACHIEVEMENT, NotificationType.DAILY_MOTIVATION),
            ),
        )
    }

    @Test
    fun getNotificationsByType() = runTest {
        val user = TestHelper.createTestUser(email = "type@test.com", username = "typeuser")
        val userId = userDao.insertUser(user)

        // Insert notifications of different types
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, type = NotificationType.WORKOUT_REMINDER, title = "Workout 1"),
        )
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, type = NotificationType.WORKOUT_REMINDER, title = "Workout 2"),
        )
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, type = NotificationType.GOAL_ACHIEVEMENT, title = "Goal Achieved"),
        )

        val workoutNotifications = notificationDao.getNotificationsByType(userId, NotificationType.WORKOUT_REMINDER).first()
        Assert.assertEquals("Should have 2 workout notifications", 2, workoutNotifications.size)
        Assert.assertTrue(
            "All should be workout reminders",
            workoutNotifications.all { it.type == NotificationType.WORKOUT_REMINDER },
        )

        val goalNotifications = notificationDao.getNotificationsByType(userId, NotificationType.GOAL_ACHIEVEMENT).first()
        Assert.assertEquals("Should have 1 goal notification", 1, goalNotifications.size)
    }

    @Test
    fun getUnreadNotifications() = runTest {
        val user = TestHelper.createTestUser(email = "unread@test.com", username = "unreaduser")
        val userId = userDao.insertUser(user)

        // Insert notifications with different read states
        val unreadNotification = TestHelper.createTestNotification(userId = userId, title = "Unread")
        val readNotificationId = notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Read"),
        )

        notificationDao.insertNotification(unreadNotification)
        notificationDao.markNotificationAsRead(readNotificationId, Date())

        val unreadNotifications = notificationDao.getUnreadNotifications(userId).first()
        Assert.assertEquals("Should have 1 unread notification", 1, unreadNotifications.size)
        Assert.assertEquals("Should be the unread notification", "Unread", unreadNotifications[0].title)
        Assert.assertFalse("Should not be read", unreadNotifications[0].isRead)
    }

    @Test
    fun updateNotificationStatus() = runTest {
        val user = TestHelper.createTestUser(email = "status@test.com", username = "statususer")
        val userId = userDao.insertUser(user)

        val notification = TestHelper.createTestNotification(userId = userId, title = "Status Test")
        val notificationId = notificationDao.insertNotification(notification)

        // Test marking as sent
        val sentTime = Date()
        notificationDao.markNotificationAsSent(notificationId, sentTime)

        var updatedNotification = notificationDao.getNotificationById(notificationId)
        Assert.assertEquals("Status should be SENT", NotificationStatus.SENT, updatedNotification?.status)
        Assert.assertEquals("Sent time should match", sentTime.time, updatedNotification?.sentTime?.time ?: 0)

        // Test marking as clicked
        val clickedTime = Date(System.currentTimeMillis() + 1000)
        notificationDao.markNotificationAsClicked(notificationId, clickedTime)

        updatedNotification = notificationDao.getNotificationById(notificationId)
        Assert.assertEquals("Status should be CLICKED", NotificationStatus.CLICKED, updatedNotification?.status)
        Assert.assertEquals("Clicked time should match", clickedTime.time, updatedNotification?.clickedTime?.time ?: 0)
        Assert.assertTrue("Should be marked as read", updatedNotification?.isRead == true)
    }

    @Test
    fun getPendingNotificationsDue() = runTest {
        val user = TestHelper.createTestUser(email = "pending@test.com", username = "pendinguser")
        val userId = userDao.insertUser(user)

        val currentTime = Date()
        val pastTime = Date(currentTime.time - 60000) // 1 minute ago
        val futureTime = Date(currentTime.time + 60000) // 1 minute from now

        // Insert notifications with different scheduled times
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Due Now", scheduledTime = pastTime),
        )
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Future", scheduledTime = futureTime),
        )

        val dueNotifications = notificationDao.getPendingNotificationsDue(currentTime)
        Assert.assertEquals("Should have 1 due notification", 1, dueNotifications.size)
        Assert.assertEquals("Should be the past notification", "Due Now", dueNotifications[0].title)
    }

    @Test
    fun getNotificationsByPriority() = runTest {
        val user = TestHelper.createTestUser(email = "priority@test.com", username = "priorityuser")
        val userId = userDao.insertUser(user)

        // Insert notifications with different priorities
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "High Priority", priority = NotificationPriority.HIGH),
        )
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Low Priority", priority = NotificationPriority.LOW),
        )
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Default Priority", priority = NotificationPriority.DEFAULT),
        )

        val highPriorityNotifications = notificationDao.getNotificationsByPriority(userId, NotificationPriority.HIGH).first()
        Assert.assertEquals("Should have 1 high priority notification", 1, highPriorityNotifications.size)
        Assert.assertEquals("Should be high priority", NotificationPriority.HIGH, highPriorityNotifications[0].priority)
    }

    @Test
    fun getNotificationStats() = runTest {
        val user = TestHelper.createTestUser(email = "stats@test.com", username = "statsuser")
        val userId = userDao.insertUser(user)

        // Insert notifications with different statuses
        val sentNotificationId = notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Sent"),
        )
        val clickedNotificationId = notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Clicked"),
        )
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Pending"),
        )

        // Update statuses
        notificationDao.markNotificationAsSent(sentNotificationId, Date())
        notificationDao.markNotificationAsClicked(clickedNotificationId, Date())

        val stats = notificationDao.getNotificationStats(userId)
        Assert.assertEquals("Total should be 3", 3, stats.totalNotifications)
        Assert.assertEquals("Pending should be 1", 1, stats.pendingCount)
        Assert.assertEquals("Sent should be 1", 1, stats.sentCount)
        Assert.assertEquals("Clicked should be 1", 1, stats.clickedCount)
        Assert.assertTrue("Delivery success rate should be > 0", stats.deliverySuccessRate > 0)
    }

    @Test
    fun getNotificationsByRelatedEntity() = runTest {
        val user = TestHelper.createTestUser(email = "related@test.com", username = "relateduser")
        val userId = userDao.insertUser(user)

        val relatedEntityId = 123L
        val entityType = "goal"

        // Insert notifications with and without related entity
        notificationDao.insertNotification(
            TestHelper.createTestNotification(
                userId = userId,
                title = "Goal Related",
                relatedEntityId = relatedEntityId,
                relatedEntityType = entityType,
            ),
        )
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Not Related"),
        )

        val relatedNotifications = notificationDao.getNotificationsByRelatedEntity(userId, entityType, relatedEntityId).first()
        Assert.assertEquals("Should have 1 related notification", 1, relatedNotifications.size)
        Assert.assertEquals("Should be goal related", "Goal Related", relatedNotifications[0].title)
        Assert.assertEquals("Entity ID should match", relatedEntityId, relatedNotifications[0].relatedEntityId)
    }

    @Test
    fun deleteOldNotifications() = runTest {
        val user = TestHelper.createTestUser(email = "old@test.com", username = "olduser")
        val userId = userDao.insertUser(user)

        val oldDate = Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L) // 7 days ago
        val newDate = Date()

        // Insert old and new notifications
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Old").copy(createdAt = oldDate),
        )
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "New").copy(createdAt = newDate),
        )

        // Delete old notifications
        val cutoffDate = Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L) // 3 days ago
        notificationDao.deleteOldNotifications(userId, cutoffDate)

        val remainingNotifications = notificationDao.getNotificationsByUserId(userId).first()
        Assert.assertEquals("Should have 1 notification remaining", 1, remainingNotifications.size)
        Assert.assertEquals("Should be the new notification", "New", remainingNotifications[0].title)
    }

    @Test
    fun incrementRetryCount() = runTest {
        val user = TestHelper.createTestUser(email = "retry@test.com", username = "retryuser")
        val userId = userDao.insertUser(user)

        val notification = TestHelper.createTestNotification(userId = userId, title = "Retry Test")
        val notificationId = notificationDao.insertNotification(notification)

        // Increment retry count
        val currentTime = Date()
        notificationDao.incrementRetryCount(notificationId, currentTime)

        val updatedNotification = notificationDao.getNotificationById(notificationId)
        Assert.assertEquals("Retry count should be 1", 1, updatedNotification?.retryCount)
        Assert.assertEquals("Updated time should match", currentTime.time, updatedNotification?.updatedAt?.time ?: 0)
    }

    @Test
    fun getRetryableNotifications() = runTest {
        val user = TestHelper.createTestUser(email = "retryable@test.com", username = "retryableuser")
        val userId = userDao.insertUser(user)

        // Create failed notification that can be retried
        val retryableNotificationId = notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Retryable"),
        )
        notificationDao.updateNotificationStatus(retryableNotificationId, NotificationStatus.FAILED, Date())

        // Create failed notification that has exceeded max retries
        val maxRetriedNotificationId = notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Max Retried").copy(retryCount = 3, maxRetries = 3),
        )
        notificationDao.updateNotificationStatus(maxRetriedNotificationId, NotificationStatus.FAILED, Date())

        val retryableNotifications = notificationDao.getRetryableNotifications()
        Assert.assertEquals("Should have 1 retryable notification", 1, retryableNotifications.size)
        Assert.assertEquals("Should be the retryable one", "Retryable", retryableNotifications[0].title)
    }

    @Test
    fun markAllNotificationsAsRead() = runTest {
        val user = TestHelper.createTestUser(email = "markall@test.com", username = "markalluser")
        val userId = userDao.insertUser(user)

        // Insert multiple unread notifications
        notificationDao.insertNotification(TestHelper.createTestNotification(userId = userId, title = "Unread 1"))
        notificationDao.insertNotification(TestHelper.createTestNotification(userId = userId, title = "Unread 2"))
        notificationDao.insertNotification(TestHelper.createTestNotification(userId = userId, title = "Unread 3"))

        // Verify all are unread
        var unreadCount = notificationDao.getUnreadNotificationCount(userId)
        Assert.assertEquals("Should have 3 unread notifications", 3, unreadCount)

        // Mark all as read
        val readTime = Date()
        notificationDao.markAllNotificationsAsRead(userId, readTime)

        // Verify all are now read
        unreadCount = notificationDao.getUnreadNotificationCount(userId)
        Assert.assertEquals("Should have 0 unread notifications", 0, unreadCount)

        val allNotifications = notificationDao.getNotificationsByUserId(userId).first()
        Assert.assertTrue(
            "All notifications should be read",
            allNotifications.all { it.isRead },
        )
    }

    @Test
    fun searchNotifications() = runTest {
        val user = TestHelper.createTestUser(email = "search@test.com", username = "searchuser")
        val userId = userDao.insertUser(user)

        // Insert notifications with different titles and messages
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Workout Reminder", message = "Time to exercise"),
        )
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Goal Achievement", message = "You reached your workout goal!"),
        )
        notificationDao.insertNotification(
            TestHelper.createTestNotification(userId = userId, title = "Daily Tip", message = "Remember to stay hydrated"),
        )

        // Search by title
        val workoutNotifications = notificationDao.searchNotifications(userId, "Workout").first()
        Assert.assertEquals("Should find 1 workout notification in title", 1, workoutNotifications.size)

        // Search by message content
        val exerciseNotifications = notificationDao.searchNotifications(userId, "exercise").first()
        Assert.assertEquals("Should find 1 notification with 'exercise' in message", 1, exerciseNotifications.size)

        // Search by partial match
        val goalNotifications = notificationDao.searchNotifications(userId, "goal").first()
        Assert.assertEquals("Should find 1 notification with 'goal'", 1, goalNotifications.size)
    }

    @Test
    fun testNotificationForeignKeyConstraint() = runTest {
        val notification = TestHelper.createTestNotification(
            userId = 999L, // Non-existent user
            title = "Invalid Notification",
        )

        try {
            notificationDao.insertNotification(notification)
            Assert.fail("Should throw foreign key constraint exception")
        } catch (e: Exception) {
            // Expected foreign key constraint violation
            Assert.assertTrue(
                "Exception should be constraint related",
                e.message?.contains("FOREIGN KEY") == true ||
                    e.message?.contains("constraint") == true,
            )
        }
    }

    @Test
    fun testNotificationCascadeDelete() = runTest {
        // Create user and notification
        val user = TestHelper.createTestUser(email = "cascade@test.com", username = "cascadeuser")
        val userId = userDao.insertUser(user)

        val notification = TestHelper.createTestNotification(userId = userId, title = "Cascade Test")
        val notificationId = notificationDao.insertNotification(notification)

        // Verify notification exists
        var retrievedNotification = notificationDao.getNotificationById(notificationId)
        Assert.assertNotNull("Notification should exist before user deletion", retrievedNotification)

        // Delete user (should cascade delete notification)
        userDao.deleteUserById(userId)

        // Verify notification is also deleted
        retrievedNotification = notificationDao.getNotificationById(notificationId)
        Assert.assertNull("Notification should be deleted after user deletion", retrievedNotification)
    }
}
