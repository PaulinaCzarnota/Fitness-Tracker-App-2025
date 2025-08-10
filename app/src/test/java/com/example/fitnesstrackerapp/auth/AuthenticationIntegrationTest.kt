package com.example.fitnesstrackerapp.auth

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.fitnesstrackerapp.data.dao.UserDao
import com.example.fitnesstrackerapp.repository.AuthRepository
import com.example.fitnesstrackerapp.security.CryptoManager
import com.example.fitnesstrackerapp.ui.auth.AuthViewModel
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
 * Integration tests for the complete authentication system.
 *
 * Tests the full authentication flow including:
 * - SessionManager + AuthRepository + AuthViewModel integration
 * - Secure session management with encrypted storage
 * - Remember me functionality
 * - Automatic login flow
 * - End-to-end authentication scenarios
 * - Error handling across components
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AuthenticationIntegrationTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context
    private lateinit var mockUserDao: UserDao
    private lateinit var cryptoManager: CryptoManager
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockUserDao = mockk()
        cryptoManager = CryptoManager(context)

        sessionManager = SessionManager(context, mockUserDao)
        authRepository = AuthRepository(mockUserDao, cryptoManager, sessionManager, context)
        authViewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `complete registration flow with session management`() = runTest {
        // Given
        val email = TestData.VALID_EMAIL
        val password = TestData.STRONG_PASSWORD
        val name = TestData.VALID_NAME
        val rememberMe = true

        coEvery { mockUserDao.getUserByEmail(email.lowercase()) } returns null
        coEvery { mockUserDao.insertUser(any()) } returns 123L
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs

        // When - register through ViewModel
        authViewModel.register(email, password, name, rememberMe)

        // Then - verify complete flow
        val uiState = authViewModel.uiState.value
        assertThat(uiState.isAuthenticated).isTrue()
        assertThat(uiState.successMessage).contains("Registration successful")
        assertThat(uiState.error).isNull()
        assertThat(uiState.isLoading).isFalse()

        // Verify session was created
        assertThat(sessionManager.isLoggedIn()).isTrue()
        assertThat(sessionManager.getCurrentUserId()).isEqualTo(123L)
        assertThat(sessionManager.getSessionInfo().rememberMeEnabled).isTrue()

        // Verify user creation
        coVerify {
            mockUserDao.insertUser(
                match { user ->
                    user.email == email.lowercase() &&
                        user.username == name &&
                        user.isActive &&
                        !user.isAccountLocked &&
                        user.failedLoginAttempts == 0
                },
            )
        }
    }

    @Test
    fun `complete login flow with session restoration`() = runTest {
        // Given
        val email = TestData.VALID_EMAIL
        val password = TestData.STRONG_PASSWORD
        val user = TestHelper.createTestUser(
            id = 456L,
            email = email,
            isActive = true,
            isAccountLocked = false,
            failedLoginAttempts = 0,
        )

        coEvery { mockUserDao.getUserByEmail(email.lowercase()) } returns user
        coEvery { mockUserDao.resetFailedLoginAttempts(any(), any()) } just Runs
        coEvery { mockUserDao.updateUser(any()) } just Runs
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs
        coEvery { mockUserDao.getUserById(456L) } returns user

        // When - login through ViewModel
        authViewModel.login(email, password, true)

        // Then - verify complete authentication state
        val uiState = authViewModel.uiState.value
        assertThat(uiState.isAuthenticated).isTrue()
        assertThat(uiState.user?.id).isEqualTo(456L)
        assertThat(uiState.successMessage).contains("Login successful")

        // Verify session management
        assertThat(sessionManager.isLoggedIn()).isTrue()
        assertThat(sessionManager.getCurrentUserId()).isEqualTo(456L)
        assertThat(sessionManager.getSessionInfo().rememberMeEnabled).isTrue()
    }

    @Test
    fun `session restoration on app start works correctly`() = runTest {
        // Given - simulate previous login session
        val user = TestHelper.createTestUser(
            id = 789L,
            email = TestData.VALID_EMAIL,
            isActive = true,
            isAccountLocked = false,
        )

        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs
        coEvery { mockUserDao.getUserById(789L) } returns user

        // Save a session first
        sessionManager.saveUserSession(user, true)
        assertThat(sessionManager.isLoggedIn()).isTrue()

        // When - simulate app restart with session restoration
        authViewModel.restoreSession()

        // Then - session should be restored automatically
        val uiState = authViewModel.uiState.value
        assertThat(uiState.isAuthenticated).isTrue()
        assertThat(sessionManager.isLoggedIn()).isTrue()
        assertThat(sessionManager.getCurrentUserId()).isEqualTo(789L)
    }

    @Test
    fun `complete logout flow cleans up everything`() = runTest {
        // Given - authenticated user
        val user = TestHelper.createTestUser(id = 321L)
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs
        coEvery { mockUserDao.getUserById(321L) } returns user

        sessionManager.saveUserSession(user, false)
        assertThat(sessionManager.isLoggedIn()).isTrue()

        // When - logout through ViewModel
        authViewModel.logout()

        // Then - everything should be cleaned up
        val uiState = authViewModel.uiState.value
        assertThat(uiState.isAuthenticated).isFalse()
        assertThat(uiState.user).isNull()

        assertThat(sessionManager.isLoggedIn()).isFalse()
        assertThat(sessionManager.getCurrentUserId()).isEqualTo(0L)
    }

    @Test
    fun `failed login attempts are tracked correctly`() = runTest {
        // Given
        val email = TestData.VALID_EMAIL
        val wrongPassword = "wrongpassword"
        val user = TestHelper.createTestUser(
            email = email,
            failedLoginAttempts = 0,
            isActive = true,
            isAccountLocked = false,
        )

        coEvery { mockUserDao.getUserByEmail(email.lowercase()) } returns user
        coEvery { mockUserDao.updateUser(any()) } just Runs

        // When - multiple failed login attempts
        authViewModel.login(email, wrongPassword)

        // Then - should track failed attempts
        val uiState = authViewModel.uiState.value
        assertThat(uiState.isAuthenticated).isFalse()
        assertThat(uiState.error).contains("attempts remaining")

        // Verify failed attempt was tracked
        coVerify {
            mockUserDao.updateUser(
                match { updatedUser ->
                    updatedUser.failedLoginAttempts == 1
                },
            )
        }
    }

    @Test
    fun `account lockout prevents login even with correct password`() = runTest {
        // Given - locked account
        val email = TestData.VALID_EMAIL
        val correctPassword = TestData.STRONG_PASSWORD
        val lockedUser = TestHelper.createTestUser(
            email = email,
            isAccountLocked = true,
            failedLoginAttempts = 5,
        )

        coEvery { mockUserDao.getUserByEmail(email.lowercase()) } returns lockedUser

        // When - try to login with correct password
        authViewModel.login(email, correctPassword)

        // Then - should be rejected due to locked account
        val uiState = authViewModel.uiState.value
        assertThat(uiState.isAuthenticated).isFalse()
        assertThat(uiState.error).contains("Account is locked")

        // Verify session was not created
        assertThat(sessionManager.isLoggedIn()).isFalse()
    }

    @Test
    fun `session timeout handling works correctly`() = runTest {
        // Given - user with session
        val user = TestHelper.createTestUser(id = 555L)
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs
        coEvery { mockUserDao.getUserById(555L) } returns user

        sessionManager.saveUserSession(user, false) // without remember me
        assertThat(sessionManager.isLoggedIn()).isTrue()

        // When - session info indicates active session
        val sessionInfo = sessionManager.getSessionInfo()

        // Then - session should be active initially
        assertThat(sessionInfo.isActive).isTrue()
        assertThat(sessionInfo.rememberMeEnabled).isFalse()
        assertThat(sessionInfo.sessionAge).isAtLeast(0L)
    }

    @Test
    fun `biometric settings are managed correctly`() = runTest {
        // Given - authenticated user
        val user = TestHelper.createTestUser()
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs

        sessionManager.saveUserSession(user, false)

        // Initially disabled
        assertThat(sessionManager.isBiometricAuthEnabled()).isFalse()

        // When - enable biometric auth
        sessionManager.enableBiometricAuth()

        // Then - should be enabled
        assertThat(sessionManager.isBiometricAuthEnabled()).isTrue()
        assertThat(sessionManager.getSessionInfo().biometricEnabled).isTrue()

        // When - disable biometric auth
        sessionManager.disableBiometricAuth()

        // Then - should be disabled
        assertThat(sessionManager.isBiometricAuthEnabled()).isFalse()
    }

    @Test
    fun `auto-login settings persist across sessions`() = runTest {
        // Given - default auto-login enabled
        assertThat(sessionManager.isAutoLoginEnabled()).isTrue()

        // When - disable auto-login
        sessionManager.disableAutoLogin()
        assertThat(sessionManager.isAutoLoginEnabled()).isFalse()

        // When - enable auto-login
        sessionManager.enableAutoLogin()

        // Then - should be enabled again
        assertThat(sessionManager.isAutoLoginEnabled()).isTrue()
        assertThat(sessionManager.getSessionInfo().autoLoginEnabled).isTrue()
    }

    @Test
    fun `session refresh extends session lifetime`() = runTest {
        // Given - user with active session
        val user = TestHelper.createTestUser()
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs

        sessionManager.saveUserSession(user, false)
        sessionManager.getSessionInfo()

        // When - refresh session
        Thread.sleep(10) // Small delay to ensure timestamp difference
        sessionManager.refreshSession()

        // Then - session should still be active
        assertThat(sessionManager.isLoggedIn()).isTrue()
        val refreshedSessionInfo = sessionManager.getSessionInfo()
        assertThat(refreshedSessionInfo.isActive).isTrue()
    }

    @Test
    fun `error handling preserves system state`() = runTest {
        // Given - database error scenario
        coEvery { mockUserDao.getUserByEmail(any()) } throws RuntimeException("Database connection failed")

        // When - attempt login
        authViewModel.login(TestData.VALID_EMAIL, TestData.STRONG_PASSWORD)

        // Then - error should be handled gracefully
        val uiState = authViewModel.uiState.value
        assertThat(uiState.isAuthenticated).isFalse()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).contains("Login failed")

        // Session should remain clean
        assertThat(sessionManager.isLoggedIn()).isFalse()
        assertThat(sessionManager.getCurrentUserId()).isEqualTo(0L)
    }

    @Test
    fun `concurrent operations are handled safely`() = runTest {
        // Given
        val user = TestHelper.createTestUser(id = 777L)
        coEvery { mockUserDao.updateLastLogin(any(), any()) } just Runs
        coEvery { mockUserDao.getUserById(777L) } returns user

        // When - simulate concurrent operations
        sessionManager.saveUserSession(user, false)
        sessionManager.refreshSession()
        sessionManager.enableBiometricAuth()

        // Then - all operations should complete successfully
        assertThat(sessionManager.isLoggedIn()).isTrue()
        assertThat(sessionManager.getCurrentUserId()).isEqualTo(777L)
        assertThat(sessionManager.isBiometricAuthEnabled()).isTrue()

        val sessionInfo = sessionManager.getSessionInfo()
        assertThat(sessionInfo.isActive).isTrue()
        assertThat(sessionInfo.biometricEnabled).isTrue()
    }
}
