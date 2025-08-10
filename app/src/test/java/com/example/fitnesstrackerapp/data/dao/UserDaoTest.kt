package com.example.fitnesstrackerapp.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.ActivityLevel
import com.example.fitnesstrackerapp.data.entity.Gender
import com.example.fitnesstrackerapp.data.entity.User
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Date

/**
 * Comprehensive unit tests for UserDao.
 *
 * Tests all user-related database operations including:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Authentication and security features
 * - Profile management
 * - Account status tracking
 * - Data integrity and constraints
 */
@ExperimentalCoroutinesApi
@RunWith(org.junit.runners.JUnit4::class)
class UserDaoTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()

        userDao = database.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetUser() = runTest {
        val user = createTestUser(
            email = "test@example.com",
            username = "testuser",
        )

        val userId = userDao.insertUser(user)
        assertThat(userId).isGreaterThan(0)

        val retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser).isNotNull()
        assertThat(retrievedUser?.email).isEqualTo("test@example.com")
        assertThat(retrievedUser?.username).isEqualTo("testuser")
        assertThat(retrievedUser?.isActive).isTrue()
    }

    @Test
    fun getUserByEmail() = runTest {
        val user = createTestUser(
            email = "email@test.com",
            username = "emailuser",
        )

        userDao.insertUser(user)

        val retrievedUser = userDao.getUserByEmail("email@test.com")
        assertThat(retrievedUser).isNotNull()
        assertThat(retrievedUser?.username).isEqualTo("emailuser")
    }

    @Test
    fun getUserByUsername() = runTest {
        val user = createTestUser(
            email = "username@test.com",
            username = "uniqueusername",
        )

        userDao.insertUser(user)

        val retrievedUser = userDao.getUserByUsername("uniqueusername")
        assertThat(retrievedUser).isNotNull()
        assertThat(retrievedUser?.email).isEqualTo("username@test.com")
    }

    @Test
    fun isEmailRegistered() = runTest {
        val user = createTestUser(
            email = "registered@test.com",
            username = "reguser",
        )

        // Email should not be registered initially
        assertThat(userDao.isEmailRegistered("registered@test.com")).isFalse()

        userDao.insertUser(user)

        // Now email should be registered
        assertThat(userDao.isEmailRegistered("registered@test.com")).isTrue()
        assertThat(userDao.isEmailRegistered("notregistered@test.com")).isFalse()
    }

    @Test
    fun isUsernameTaken() = runTest {
        val user = createTestUser(
            email = "taken@test.com",
            username = "takenusername",
        )

        // Username should not be taken initially
        assertThat(userDao.isUsernameTaken("takenusername")).isFalse()

        userDao.insertUser(user)

        // Now username should be taken
        assertThat(userDao.isUsernameTaken("takenusername")).isTrue()
        assertThat(userDao.isUsernameTaken("notaken")).isFalse()
    }

    @Test
    fun getAllActiveUsers() = runTest {
        val activeUser1 = createTestUser(
            email = "active1@test.com",
            username = "active1",
            isActive = true,
        )
        val activeUser2 = createTestUser(
            email = "active2@test.com",
            username = "active2",
            isActive = true,
        )
        val inactiveUser = createTestUser(
            email = "inactive@test.com",
            username = "inactive",
            isActive = false,
        )

        userDao.insertUser(activeUser1)
        userDao.insertUser(activeUser2)
        userDao.insertUser(inactiveUser)

        val activeUsers = userDao.getAllActiveUsers().first()
        assertThat(activeUsers).hasSize(2)
        assertThat(activeUsers.all { it.isActive }).isTrue()
        assertThat(activeUsers.map { it.username }).containsExactly("active2", "active1")
    }

    @Test
    fun getAllUsers() = runTest {
        val user1 = createTestUser(email = "all1@test.com", username = "all1")
        val user2 = createTestUser(email = "all2@test.com", username = "all2")
        val user3 = createTestUser(email = "all3@test.com", username = "all3")

        userDao.insertUser(user1)
        userDao.insertUser(user2)
        userDao.insertUser(user3)

        val allUsers = userDao.getAllUsers().first()
        assertThat(allUsers).hasSize(3)
        assertThat(allUsers.map { it.username }).containsExactly("all3", "all2", "all1")
    }

    @Test
    fun updateUser() = runTest {
        val user = createTestUser(
            email = "update@test.com",
            username = "updateuser",
            firstName = "Original",
        )

        val userId = userDao.insertUser(user)
        val updatedUser = user.copy(
            id = userId,
            firstName = "Updated",
            updatedAt = Date(),
        )

        userDao.updateUser(updatedUser)

        val retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser?.firstName).isEqualTo("Updated")
    }

    @Test
    fun updateHeight() = runTest {
        val user = createTestUser(
            email = "height@test.com",
            username = "heightuser",
            heightCm = 170f,
        )

        val userId = userDao.insertUser(user)
        val newHeight = 175f
        val updatedAt = Date()

        userDao.updateHeight(userId, newHeight, updatedAt)

        val retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser?.heightCm).isEqualTo(newHeight)
        assertThat(retrievedUser?.updatedAt).isEqualTo(updatedAt)
    }

    @Test
    fun updateWeight() = runTest {
        val user = createTestUser(
            email = "weight@test.com",
            username = "weightuser",
            weightKg = 70f,
        )

        val userId = userDao.insertUser(user)
        val newWeight = 75f
        val updatedAt = Date()

        userDao.updateWeight(userId, newWeight, updatedAt)

        val retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser?.weightKg).isEqualTo(newWeight)
        assertThat(retrievedUser?.updatedAt).isEqualTo(updatedAt)
    }

    @Test
    fun updateStepGoal() = runTest {
        val user = createTestUser(
            email = "stepgoal@test.com",
            username = "stepgoaluser",
            dailyStepGoal = 8000,
        )

        val userId = userDao.insertUser(user)
        val newStepGoal = 12000
        val updatedAt = Date()

        userDao.updateStepGoal(userId, newStepGoal, updatedAt)

        val retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser?.dailyStepGoal).isEqualTo(newStepGoal)
        assertThat(retrievedUser?.updatedAt).isEqualTo(updatedAt)
    }

    @Test
    fun updateLastLogin() = runTest {
        val user = createTestUser(
            email = "lastlogin@test.com",
            username = "lastloginuser",
        )

        val userId = userDao.insertUser(user)
        val loginTime = Date()

        userDao.updateLastLogin(userId, loginTime)

        val retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser?.lastLogin).isEqualTo(loginTime)
        assertThat(retrievedUser?.updatedAt).isEqualTo(loginTime)
    }

    @Test
    fun lockAndUnlockAccount() = runTest {
        val user = createTestUser(
            email = "lock@test.com",
            username = "lockuser",
            isAccountLocked = false,
        )

        val userId = userDao.insertUser(user)
        val currentTime = Date()

        // Lock account
        userDao.lockAccount(userId, currentTime)
        var retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser?.isAccountLocked).isTrue()
        assertThat(retrievedUser?.updatedAt).isEqualTo(currentTime)

        // Unlock account
        val unlockTime = Date(currentTime.time + 1000)
        userDao.unlockAccount(userId, unlockTime)
        retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser?.isAccountLocked).isFalse()
        assertThat(retrievedUser?.failedLoginAttempts).isEqualTo(0)
        assertThat(retrievedUser?.updatedAt).isEqualTo(unlockTime)
    }

    @Test
    fun failedLoginAttempts() = runTest {
        val user = createTestUser(
            email = "failed@test.com",
            username = "faileduser",
            failedLoginAttempts = 2,
        )

        val userId = userDao.insertUser(user)
        val currentTime = Date()

        // Increment failed attempts
        userDao.incrementFailedLoginAttempts("failed@test.com", currentTime)
        var retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser?.failedLoginAttempts).isEqualTo(3)

        // Reset failed attempts
        val resetTime = Date(currentTime.time + 1000)
        userDao.resetFailedLoginAttempts(userId, resetTime)
        retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser?.failedLoginAttempts).isEqualTo(0)
        assertThat(retrievedUser?.isAccountLocked).isFalse()
    }

    @Test
    fun activateAndDeactivateUser() = runTest {
        val user = createTestUser(
            email = "activate@test.com",
            username = "activateuser",
            isActive = true,
        )

        val userId = userDao.insertUser(user)
        val currentTimeMillis = System.currentTimeMillis()

        // Deactivate user
        userDao.deactivateUser(userId, currentTimeMillis)
        var retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser?.isActive).isFalse()

        // Activate user
        val activateTimeMillis = currentTimeMillis + 1000
        userDao.activateUser(userId, activateTimeMillis)
        retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser?.isActive).isTrue()
    }

    @Test
    fun getUserCount() = runTest {
        // Initially should have no users
        assertThat(userDao.getUserCount()).isEqualTo(0)

        // Insert users
        userDao.insertUser(createTestUser(email = "count1@test.com", username = "count1"))
        assertThat(userDao.getUserCount()).isEqualTo(1)

        userDao.insertUser(createTestUser(email = "count2@test.com", username = "count2"))
        assertThat(userDao.getUserCount()).isEqualTo(2)
    }

    @Test
    fun getActiveUserCount() = runTest {
        userDao.insertUser(createTestUser(email = "active1@test.com", username = "active1", isActive = true))
        userDao.insertUser(createTestUser(email = "active2@test.com", username = "active2", isActive = true))
        userDao.insertUser(createTestUser(email = "inactive@test.com", username = "inactive", isActive = false))

        assertThat(userDao.getActiveUserCount()).isEqualTo(2)
    }

    @Test
    fun deleteUser() = runTest {
        val user = createTestUser(
            email = "delete@test.com",
            username = "deleteuser",
        )

        val userId = userDao.insertUser(user)

        // Verify user exists
        var retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser).isNotNull()

        // Delete user
        userDao.deleteUser(user.copy(id = userId))

        // Verify user is deleted
        retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser).isNull()
    }

    @Test
    fun deleteUserById() = runTest {
        val user = createTestUser(
            email = "deletebyid@test.com",
            username = "deletebyiduser",
        )

        val userId = userDao.insertUser(user)

        // Verify user exists
        var retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser).isNotNull()

        // Delete by ID
        userDao.deleteUserById(userId)

        // Verify user is deleted
        retrievedUser = userDao.getUserById(userId)
        assertThat(retrievedUser).isNull()
    }

    @Test
    fun emailUniquenessConstraint() = runTest {
        val user1 = createTestUser(
            email = "unique@test.com",
            username = "user1",
        )
        val user2 = createTestUser(
            email = "unique@test.com", // Same email
            username = "user2",
        )

        userDao.insertUser(user1)

        try {
            userDao.insertUser(user2)
            Assert.fail("Should throw exception for duplicate email")
        } catch (e: Exception) {
            // Expected exception for unique constraint violation
            assertThat(e.message).contains("UNIQUE")
        }
    }

    @Test
    fun testUserCalculations() = runTest {
        val user = createTestUser(
            email = "calc@test.com",
            username = "calcuser",
            dateOfBirth = Date(System.currentTimeMillis() - (25L * 365 * 24 * 60 * 60 * 1000)), // 25 years ago
            heightCm = 175f,
            weightKg = 70f,
            gender = Gender.MALE,
            activityLevel = ActivityLevel.MODERATELY_ACTIVE,
        )

        val userId = userDao.insertUser(user)
        val retrievedUser = userDao.getUserById(userId)

        assertThat(retrievedUser).isNotNull()
        // Test BMI calculation
        val expectedBmi = 70f / ((175f / 100f) * (175f / 100f))
        assertThat(retrievedUser!!.getBMI()).isWithin(0.1f).of(expectedBmi)

        // Test age calculation
        assertThat(retrievedUser.getAge()).isEqualTo(25)

        // Test profile completion
        assertThat(retrievedUser.isProfileComplete()).isTrue()

        // Test login capability
        assertThat(retrievedUser.canLogin()).isTrue()
    }

    @Test
    fun getLockedUsers() = runTest {
        val normalUser = createTestUser(
            email = "normal@test.com",
            username = "normal",
            isAccountLocked = false,
            failedLoginAttempts = 2,
        )
        val lockedUser = createTestUser(
            email = "locked@test.com",
            username = "locked",
            isAccountLocked = true,
            failedLoginAttempts = 3,
        )
        val failedUser = createTestUser(
            email = "failed@test.com",
            username = "failed",
            isAccountLocked = false,
            failedLoginAttempts = 5,
        )

        userDao.insertUser(normalUser)
        userDao.insertUser(lockedUser)
        userDao.insertUser(failedUser)

        val lockedUsers = userDao.getLockedUsers().first()
        assertThat(lockedUsers).hasSize(2)
        assertThat(lockedUsers.map { it.username }).containsExactly("locked", "failed")
    }

    private fun createTestUser(
        email: String,
        username: String,
        firstName: String? = null,
        lastName: String? = null,
        dateOfBirth: Date? = null,
        heightCm: Float? = null,
        weightKg: Float? = null,
        gender: Gender? = null,
        activityLevel: ActivityLevel = ActivityLevel.MODERATELY_ACTIVE,
        isActive: Boolean = true,
        isAccountLocked: Boolean = false,
        failedLoginAttempts: Int = 0,
        dailyStepGoal: Int = 10000,
    ) = User(
        email = email,
        username = username,
        passwordHash = "test_hash",
        passwordSalt = "test_salt",
        firstName = firstName,
        lastName = lastName,
        dateOfBirth = dateOfBirth,
        heightCm = heightCm,
        weightKg = weightKg,
        gender = gender,
        activityLevel = activityLevel,
        isActive = isActive,
        isAccountLocked = isAccountLocked,
        failedLoginAttempts = failedLoginAttempts,
        dailyStepGoal = dailyStepGoal,
        createdAt = Date(),
        updatedAt = Date(),
    )
}
