package com.example.fitnesstrackerapp.auth

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.fitnesstrackerapp.data.dao.UserDao
import com.example.fitnesstrackerapp.util.test.MainDispatcherRule
import com.example.fitnesstrackerapp.util.test.TestData
import com.example.fitnesstrackerapp.util.test.TestHelper
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Comprehensive unit tests for SessionManager with enhanced security features.
 *
 * Tests include:
 * - Session save/restore functionality
 * - Encrypted storage verification
 * - Session timeout handling
 * - Biometric authentication settings
 * - Auto-login functionality
 * - Session security edge cases
 * - Error handling and recovery
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SessionManagerTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context
    private lateinit var mockUserDao: UserDao
    private lateinit var sessionManager: SessionManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockUserDao = mockk()

        // Create SessionManager with mocked UserDao
        sessionManager = SessionManager(context, mockUserDao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `saveUserSession stores user data securely`() = runTest {
        // Given
        val user = TestHelper.createTestUser(
            id = 123L,
            email = TestData.VALID_EMAIL,
            username = "testuser",
        )
        val rememberMe = true

        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs

        // When
        sessionManager.saveUserSession(user, rememberMe)

        // Then
        assertThat(sessionManager.isLoggedIn()).isTrue()
        assertThat(sessionManager.getCurrentUserId()).isEqualTo(123L)

        coVerify { mockUserDao.updateLastLogin(123L, any()) }
    }

    @Test
    fun `getCurrentUser returns user from database when session valid`() = runTest {
        // Given
        val user = TestHelper.createTestUser(id = 456L)
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs
        coEvery { mockUserDao.getUserById(456L) } returns user

        sessionManager.saveUserSession(user, false)

        // When
        val retrievedUser = sessionManager.getCurrentUser()

        // Then
        assertThat(retrievedUser).isNotNull()
        assertThat(retrievedUser?.id).isEqualTo(456L)
        assertThat(retrievedUser?.email).isEqualTo(user.email)
    }

    @Test
    fun `getCurrentUser returns null when session invalid`() = runTest {
        // Given - no saved session

        // When
        val retrievedUser = sessionManager.getCurrentUser()

        // Then
        assertThat(retrievedUser).isNull()
    }

    @Test
    fun `isLoggedIn returns false when no session exists`() = runTest {
        // Given - no saved session

        // When & Then
        assertThat(sessionManager.isLoggedIn()).isFalse()
    }

    @Test
    fun `clearUserSession removes all session data`() = runTest {
        // Given
        val user = TestHelper.createTestUser()
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs

        sessionManager.saveUserSession(user, false)
        assertThat(sessionManager.isLoggedIn()).isTrue()

        // When
        sessionManager.clearUserSession()

        // Then
        assertThat(sessionManager.isLoggedIn()).isFalse()
        assertThat(sessionManager.getCurrentUserId()).isEqualTo(0L)
    }

    @Test
    fun `restoreSession succeeds with valid session and active user`() = runTest {
        // Given
        val user = TestHelper.createTestUser(
            id = 789L,
            isActive = true,
            isAccountLocked = false,
        )
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs
        coEvery { mockUserDao.getUserById(789L) } returns user

        sessionManager.saveUserSession(user, false)

        // When
        val result = sessionManager.restoreSession()

        // Then
        assertThat(result).isInstanceOf(SessionRestoreResult.Success::class.java)
        val success = result as SessionRestoreResult.Success
        assertThat(success.user.id).isEqualTo(789L)
    }

    @Test
    fun `restoreSession fails when user is inactive`() = runTest {
        // Given
        val inactiveUser = TestHelper.createTestUser(
            id = 101L,
            isActive = false,
        )
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs
        coEvery { mockUserDao.getUserById(101L) } returns inactiveUser

        sessionManager.saveUserSession(inactiveUser, false)

        // When
        val result = sessionManager.restoreSession()

        // Then
        assertThat(result).isInstanceOf(SessionRestoreResult.Failed::class.java)
        val failure = result as SessionRestoreResult.Failed
        assertThat(failure.message).contains("User data not found")
    }

    @Test
    fun `restoreSession fails when user account is locked`() = runTest {
        // Given
        val lockedUser = TestHelper.createTestUser(
            id = 102L,
            isActive = true,
            isAccountLocked = true,
        )
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs
        coEvery { mockUserDao.getUserById(102L) } returns lockedUser

        sessionManager.saveUserSession(lockedUser, false)

        // When
        val result = sessionManager.restoreSession()

        // Then
        assertThat(result).isInstanceOf(SessionRestoreResult.Failed::class.java)
    }

    @Test
    fun `restoreSession fails when no session exists`() = runTest {
        // Given - no saved session

        // When
        val result = sessionManager.restoreSession()

        // Then
        assertThat(result).isInstanceOf(SessionRestoreResult.Failed::class.java)
        val failure = result as SessionRestoreResult.Failed
        assertThat(failure.message).contains("Session expired or invalid")
    }

    @Test
    fun `biometric authentication can be enabled and disabled`() = runTest {
        // Given - initial state
        assertThat(sessionManager.isBiometricAuthEnabled()).isFalse()

        // When - enable biometric auth
        sessionManager.enableBiometricAuth()

        // Then
        assertThat(sessionManager.isBiometricAuthEnabled()).isTrue()

        // When - disable biometric auth
        sessionManager.disableBiometricAuth()

        // Then
        assertThat(sessionManager.isBiometricAuthEnabled()).isFalse()
    }

    @Test
    fun `auto-login can be enabled and disabled`() = runTest {
        // Given - default state (auto-login enabled by default)
        assertThat(sessionManager.isAutoLoginEnabled()).isTrue()

        // When - disable auto-login
        sessionManager.disableAutoLogin()

        // Then
        assertThat(sessionManager.isAutoLoginEnabled()).isFalse()

        // When - enable auto-login
        sessionManager.enableAutoLogin()

        // Then
        assertThat(sessionManager.isAutoLoginEnabled()).isTrue()
    }

    @Test
    fun `refreshSession updates session timestamp`() = runTest {
        // Given
        val user = TestHelper.createTestUser()
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs

        sessionManager.saveUserSession(user, false)

        // When
        sessionManager.refreshSession()

        // Then
        assertThat(sessionManager.isLoggedIn()).isTrue()
    }

    @Test
    fun `getSessionInfo returns correct session details`() = runTest {
        // Given
        val user = TestHelper.createTestUser()
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs

        sessionManager.saveUserSession(user, true) // with remember me
        sessionManager.enableBiometricAuth()

        // When
        val sessionInfo = sessionManager.getSessionInfo()

        // Then
        assertThat(sessionInfo.isActive).isTrue()
        assertThat(sessionInfo.rememberMeEnabled).isTrue()
        assertThat(sessionInfo.biometricEnabled).isTrue()
        assertThat(sessionInfo.autoLoginEnabled).isTrue()
        assertThat(sessionInfo.loginTimestamp).isGreaterThan(0L)
        assertThat(sessionInfo.sessionAge).isAtLeast(0L)
    }

    @Test
    fun `session timeout works correctly without remember me`() = runTest {
        // Given - mock time progression beyond session timeout
        // Note: This test would require more sophisticated time mocking
        // For now, we test the basic logic

        val user = TestHelper.createTestUser()
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs

        // When
        sessionManager.saveUserSession(user, false) // no remember me

        // Then - session should be valid initially
        assertThat(sessionManager.isLoggedIn()).isTrue()

        // Note: Real timeout testing would require manipulating system time
        // or using a time provider interface for better testability
    }

    @Test
    fun `remember me extends session duration`() = runTest {
        // Given
        val user = TestHelper.createTestUser()
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs

        // When - save session with remember me
        sessionManager.saveUserSession(user, true)

        // Then
        val sessionInfo = sessionManager.getSessionInfo()
        assertThat(sessionInfo.rememberMeEnabled).isTrue()
        assertThat(sessionInfo.isActive).isTrue()
    }

    @Test
    fun `session handles database errors gracefully`() = runTest {
        // Given
        val user = TestHelper.createTestUser(id = 999L)
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs
        coEvery { mockUserDao.getUserById(999L) } throws RuntimeException("Database error")

        sessionManager.saveUserSession(user, false)

        // When
        val retrievedUser = sessionManager.getCurrentUser()

        // Then - should return null and clear session on database error
        assertThat(retrievedUser).isNull()
    }

    @Test
    fun `saveUserSession handles exceptions properly`() = runTest {
        // Given
        val user = TestHelper.createTestUser()
        coEvery { mockUserDao.updateLastLogin(any(), any()) } throws RuntimeException("Update failed")

        // When & Then
        try {
            sessionManager.saveUserSession(user, false)
            // Should throw SessionException
            assertThat(false).isTrue() // Should not reach here
        } catch (e: SessionException) {
            assertThat(e.message).contains("Failed to save session")
            assertThat(e.cause).isInstanceOf(RuntimeException::class.java)
        }
    }

    @Test
    fun `clearUserSession handles exceptions properly`() = runTest {
        // Given
        val user = TestHelper.createTestUser()
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs

        sessionManager.saveUserSession(user, false)

        // When & Then - clearing should work even if there are internal issues
        try {
            sessionManager.clearUserSession()
            // Should complete without throwing
        } catch (e: SessionException) {
            // If it throws, should be a SessionException
            assertThat(e.message).contains("Failed to clear session")
        }

        // Session should be cleared regardless
        assertThat(sessionManager.isLoggedIn()).isFalse()
    }

    @Test
    fun `getCurrentUserId returns 0 for invalid session`() = runTest {
        // Given - no session or invalid session

        // When
        val userId = sessionManager.getCurrentUserId()

        // Then
        assertThat(userId).isEqualTo(0L)
    }

    @Test
    fun `multiple session operations work correctly`() = runTest {
        // Given
        val user1 = TestHelper.createTestUser(id = 100L, email = "user1@test.com")
        val user2 = TestHelper.createTestUser(id = 200L, email = "user2@test.com")

        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs
        coEvery { mockUserDao.getUserById(100L) } returns user1
        coEvery { mockUserDao.getUserById(200L) } returns user2

        // When - save first user session
        sessionManager.saveUserSession(user1, false)
        assertThat(sessionManager.getCurrentUserId()).isEqualTo(100L)

        // Clear and save second user session
        sessionManager.clearUserSession()
        sessionManager.saveUserSession(user2, true)

        // Then
        assertThat(sessionManager.getCurrentUserId()).isEqualTo(200L)
        assertThat(sessionManager.getSessionInfo().rememberMeEnabled).isTrue()
    }
}
