package com.example.fitnesstrackerapp.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.fitnesstrackerapp.data.dao.UserDao
import com.example.fitnesstrackerapp.data.entity.User
import com.example.fitnesstrackerapp.repository.AuthRepository
import com.example.fitnesstrackerapp.repository.AuthResult
import com.example.fitnesstrackerapp.security.CryptoManager
import com.example.fitnesstrackerapp.util.test.MainDispatcherRule
import com.example.fitnesstrackerapp.util.test.TestData
import com.example.fitnesstrackerapp.util.test.TestHelper
import com.google.common.truth.Truth.assertThat
import io.mockk.Awaits
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive unit tests for authentication security edge cases.
 *
 * Tests include:
 * - Account lockout mechanisms
 * - Failed login attempt tracking
 * - Password security validation
 * - Session security edge cases
 * - Brute force attack protection
 * - Account status validation
 * - Input validation and sanitization
 * - Concurrent authentication attempts
 */
@ExperimentalCoroutinesApi
class AuthenticationSecurityTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mockUserDao: UserDao
    private lateinit var mockCryptoManager: CryptoManager
    private lateinit var mockSessionManager: SessionManager
    private lateinit var authRepository: AuthRepository

    @Before
    fun setup() {
        mockUserDao = mockk()
        mockCryptoManager = mockk()
        mockSessionManager = mockk()

        // Setup common crypto manager behavior
        every { mockCryptoManager.generateSalt() } returns TestData.MOCK_SALT.toByteArray()
        every { mockCryptoManager.hashPassword(any(), any()) } returns TestData.MOCK_HASH.toByteArray()
        every { mockCryptoManager.bytesToHex(any()) } returns TestData.MOCK_HEX_STRING
        every { mockCryptoManager.hexToBytes(any()) } returns TestData.MOCK_HASH.toByteArray()

        authRepository = AuthRepository(
            userDao = mockUserDao,
            passwordManager = mockCryptoManager,
            sessionManager = mockSessionManager,
            context = TestHelper.createMockContext(),
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `login fails for locked account`() = runTest {
        // Given
        val lockedUser = TestHelper.createTestUser(
            email = TestData.VALID_EMAIL,
            isAccountLocked = true,
            failedLoginAttempts = 5,
        )

        coEvery { mockUserDao.getUserByEmail(TestData.VALID_EMAIL) } returns lockedUser

        // When
        val result = authRepository.login(TestData.VALID_EMAIL, TestData.STRONG_PASSWORD)

        // Then
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        val error = result as AuthResult.Error
        assertThat(error.message).contains("Account is locked")
        assertThat(error.message).contains("too many failed attempts")
    }

    @Test
    fun `login fails for inactive account`() = runTest {
        // Given
        val inactiveUser = TestHelper.createTestUser(
            email = TestData.VALID_EMAIL,
            isActive = false,
            isAccountLocked = false,
        )

        coEvery { mockUserDao.getUserByEmail(TestData.VALID_EMAIL) } returns inactiveUser

        // When
        val result = authRepository.login(TestData.VALID_EMAIL, TestData.STRONG_PASSWORD)

        // Then
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        val error = result as AuthResult.Error
        assertThat(error.message).contains("Account is deactivated")
    }

    @Test
    fun `handleFailedLogin increments failed attempts`() = runTest {
        // Given
        val user = TestHelper.createTestUser(
            email = TestData.VALID_EMAIL,
            failedLoginAttempts = 2,
        )

        coEvery { mockUserDao.getUserByEmail(TestData.VALID_EMAIL) } returns user
        coEvery { mockUserDao.updateUser(any()) } just Runs

        // When
        val result = authRepository.handleFailedLogin(TestData.VALID_EMAIL)

        // Then
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        val error = result as AuthResult.Error
        assertThat(error.message).contains("2 attempts remaining")

        coVerify {
            mockUserDao.updateUser(
                match { updatedUser ->
                    updatedUser.failedLoginAttempts == 3 && !updatedUser.isAccountLocked
                },
            )
        }
    }

    @Test
    fun `handleFailedLogin locks account after max attempts`() = runTest {
        // Given
        val user = TestHelper.createTestUser(
            email = TestData.VALID_EMAIL,
            failedLoginAttempts = 4, // One less than max
        )

        coEvery { mockUserDao.getUserByEmail(TestData.VALID_EMAIL) } returns user
        coEvery { mockUserDao.updateUser(any()) } just Runs

        // When
        val result = authRepository.handleFailedLogin(TestData.VALID_EMAIL)

        // Then
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        val error = result as AuthResult.Error
        assertThat(error.message).contains("Account locked")

        coVerify {
            mockUserDao.updateUser(
                match { updatedUser ->
                    updatedUser.failedLoginAttempts == 5 && updatedUser.isAccountLocked
                },
            )
        }
    }

    @Test
    fun `successful login resets failed attempts`() = runTest {
        // Given
        val user = TestHelper.createTestUser(
            email = TestData.VALID_EMAIL,
            failedLoginAttempts = 3,
            isActive = true,
            isAccountLocked = false,
        )

        coEvery { mockUserDao.getUserByEmail(TestData.VALID_EMAIL) } returns user
        every { mockCryptoManager.verifyPassword(any(), any(), any()) } returns true
        coEvery { mockUserDao.resetFailedLoginAttempts(any(), any()) } just Runs
        coEvery { mockUserDao.updateUser(any()) } just Runs
        coEvery { mockSessionManager.saveUserSession(any(), any()) } just Awaits

        // When
        val result = authRepository.login(TestData.VALID_EMAIL, TestData.STRONG_PASSWORD)

        // Then
        assertThat(result).isInstanceOf(AuthResult.Success::class.java)
        coVerify { mockUserDao.resetFailedLoginAttempts(user.id, any()) }
    }

    @Test
    fun `login with empty email fails`() = runTest {
        // When
        val result = authRepository.login("", TestData.STRONG_PASSWORD)

        // Then
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        val error = result as AuthResult.Error
        assertThat(error.message).contains("Email and password are required")
    }

    @Test
    fun `login with empty password fails`() = runTest {
        // When
        val result = authRepository.login(TestData.VALID_EMAIL, "")

        // Then
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        val error = result as AuthResult.Error
        assertThat(error.message).contains("Email and password are required")
    }

    @Test
    fun `login with whitespace-only inputs fails`() = runTest {
        // When
        val result = authRepository.login("   ", "   ")

        // Then
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        val error = result as AuthResult.Error
        assertThat(error.message).contains("Email and password are required")
    }

    @Test
    fun `registration fails with existing email`() = runTest {
        // Given
        val existingUser = TestHelper.createTestUser(email = TestData.VALID_EMAIL)
        coEvery { mockUserDao.getUserByEmail(TestData.VALID_EMAIL) } returns existingUser

        // When
        val result = authRepository.register(TestData.VALID_EMAIL, TestData.STRONG_PASSWORD, "Test User")

        // Then
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        val error = result as AuthResult.Error
        assertThat(error.message).contains("User with this email already exists")
    }

    @Test
    fun `registration fails with empty fields`() = runTest {
        // When - empty email
        val result1 = authRepository.register("", TestData.STRONG_PASSWORD, "Test User")
        assertThat(result1).isInstanceOf(AuthResult.Error::class.java)
        assertThat((result1 as AuthResult.Error).message).contains("All fields are required")

        // When - empty password
        val result2 = authRepository.register(TestData.VALID_EMAIL, "", "Test User")
        assertThat(result2).isInstanceOf(AuthResult.Error::class.java)
        assertThat((result2 as AuthResult.Error).message).contains("All fields are required")

        // When - empty name
        val result3 = authRepository.register(TestData.VALID_EMAIL, TestData.STRONG_PASSWORD, "")
        assertThat(result3).isInstanceOf(AuthResult.Error::class.java)
        assertThat((result3 as AuthResult.Error).message).contains("All fields are required")
    }

    @Test
    fun `registration normalizes email to lowercase`() = runTest {
        // Given
        val uppercaseEmail = "USER@EXAMPLE.COM"
        coEvery { mockUserDao.getUserByEmail(uppercaseEmail.lowercase()) } returns null
        coEvery { mockUserDao.insertUser(any()) } returns 123L
        coEvery { mockSessionManager.saveUserSession(any(), any()) } just Awaits

        // When
        val result = authRepository.register(uppercaseEmail, TestData.STRONG_PASSWORD, "Test User")

        // Then
        assertThat(result).isInstanceOf(AuthResult.Success::class.java)
        coVerify {
            mockUserDao.insertUser(
                match { user ->
                    user.email == uppercaseEmail.lowercase()
                },
            )
        }
    }

    @Test
    fun `registration trims whitespace from inputs`() = runTest {
        // Given
        val emailWithSpaces = "  user@example.com  "
        val nameWithSpaces = "  Test User  "

        coEvery { mockUserDao.getUserByEmail("user@example.com") } returns null
        coEvery { mockUserDao.insertUser(any()) } returns 123L
        coEvery { mockSessionManager.saveUserSession(any(), any()) } just Awaits

        // When
        val result = authRepository.register(emailWithSpaces, TestData.STRONG_PASSWORD, nameWithSpaces)

        // Then
        assertThat(result).isInstanceOf(AuthResult.Success::class.java)
        coVerify {
            mockUserDao.insertUser(
                match { user ->
                    user.email == "user@example.com" &&
                        user.username == "Test User"
                },
            )
        }
    }

    @Test
    fun `registration handles database insert failure`() = runTest {
        // Given
        coEvery { mockUserDao.getUserByEmail(any()) } returns null
        coEvery { mockUserDao.insertUser(any()) } throws RuntimeException("Database insert failed")

        // When
        val result = authRepository.register(TestData.VALID_EMAIL, TestData.STRONG_PASSWORD, "Test User")

        // Then
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        val error = result as AuthResult.Error
        assertThat(error.message).contains("Registration failed")
        assertThat(error.message).contains("Database insert failed")
    }

    @Test
    fun `login handles database query failure gracefully`() = runTest {
        // Given
        coEvery { mockUserDao.getUserByEmail(any()) } throws RuntimeException("Database connection failed")

        // When
        val result = authRepository.login(TestData.VALID_EMAIL, TestData.STRONG_PASSWORD)

        // Then
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        val error = result as AuthResult.Error
        assertThat(error.message).contains("Login failed")
        assertThat(error.message).contains("Database connection failed")
    }

    @Test
    fun `password verification failure is handled securely`() = runTest {
        // Given
        val user = TestHelper.createTestUser(
            email = TestData.VALID_EMAIL,
            isActive = true,
            isAccountLocked = false,
        )

        coEvery { mockUserDao.getUserByEmail(TestData.VALID_EMAIL) } returns user
        every { mockCryptoManager.verifyPassword(any(), any(), any()) } returns false
        coEvery { mockUserDao.updateUser(any()) } just Runs

        // When
        val result = authRepository.login(TestData.VALID_EMAIL, "wrongpassword")

        // Then
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        val error = result as AuthResult.Error
        assertThat(error.message).contains("4 attempts remaining") // Shows attempt tracking
    }

    @Test
    fun `crypto manager exceptions are handled gracefully`() = runTest {
        // Given
        val user = TestHelper.createTestUser(
            email = TestData.VALID_EMAIL,
            isActive = true,
            isAccountLocked = false,
        )

        coEvery { mockUserDao.getUserByEmail(TestData.VALID_EMAIL) } returns user
        every { mockCryptoManager.hexToBytes(any()) } throws RuntimeException("Crypto operation failed")

        // When
        val result = authRepository.login(TestData.VALID_EMAIL, TestData.STRONG_PASSWORD)

        // Then
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        val error = result as AuthResult.Error
        assertThat(error.message).contains("Login failed")
    }

    @Test
    fun `session manager exceptions are handled in login`() = runTest {
        // Given
        val user = TestHelper.createTestUser(
            email = TestData.VALID_EMAIL,
            isActive = true,
            isAccountLocked = false,
        )

        coEvery { mockUserDao.getUserByEmail(TestData.VALID_EMAIL) } returns user
        every { mockCryptoManager.verifyPassword(any(), any(), any()) } returns true
        coEvery { mockUserDao.resetFailedLoginAttempts(any(), any()) } just Runs
        coEvery { mockUserDao.updateUser(any()) } just Runs
        coEvery { mockSessionManager.saveUserSession(any(), any()) } throws SessionException("Session save failed")

        // When
        val result = authRepository.login(TestData.VALID_EMAIL, TestData.STRONG_PASSWORD)

        // Then
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        val error = result as AuthResult.Error
        assertThat(error.message).contains("Login failed")
    }

    @Test
    fun `concurrent login attempts are handled safely`() = runTest {
        // Given
        val user = TestHelper.createTestUser(
            email = TestData.VALID_EMAIL,
            failedLoginAttempts = 0,
            isActive = true,
            isAccountLocked = false,
        )

        coEvery { mockUserDao.getUserByEmail(TestData.VALID_EMAIL) } returns user
        every { mockCryptoManager.verifyPassword(any(), any(), any()) } returns false
        coEvery { mockUserDao.updateUser(any()) } just Runs

        // When - simulate multiple failed attempts
        val result1 = authRepository.handleFailedLogin(TestData.VALID_EMAIL)
        val result2 = authRepository.handleFailedLogin(TestData.VALID_EMAIL)
        val result3 = authRepository.handleFailedLogin(TestData.VALID_EMAIL)

        // Then - all should be handled individually
        assertThat(result1).isInstanceOf(AuthResult.Error::class.java)
        assertThat(result2).isInstanceOf(AuthResult.Error::class.java)
        assertThat(result3).isInstanceOf(AuthResult.Error::class.java)

        // Should increment failed attempts for each call
        coVerify(exactly = 3) { mockUserDao.updateUser(any()) }
    }

    @Test
    fun `account lockout persists across login attempts`() = runTest {
        // Given
        val lockedUser = TestHelper.createTestUser(
            email = TestData.VALID_EMAIL,
            isAccountLocked = true,
            failedLoginAttempts = 5,
        )

        coEvery { mockUserDao.getUserByEmail(TestData.VALID_EMAIL) } returns lockedUser

        // When - try to login with correct password
        val result = authRepository.login(TestData.VALID_EMAIL, TestData.STRONG_PASSWORD)

        // Then - should fail due to locked account
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        val error = result as AuthResult.Error
        assertThat(error.message).contains("Account is locked")

        // Verify no password verification was attempted
        verify(exactly = 0) { mockCryptoManager.verifyPassword(any(), any(), any()) }
    }

    @Test
    fun `user with null values is handled safely`() = runTest {
        // Given
        val userWithNulls = User(
            id = 123L,
            email = TestData.VALID_EMAIL,
            username = "testuser",
            passwordHash = "hash",
            passwordSalt = "salt",
            firstName = null, // null values
            lastName = null,
            dateOfBirth = null,
            heightCm = null,
            weightKg = null,
            gender = null,
        )

        coEvery { mockUserDao.getUserByEmail(TestData.VALID_EMAIL) } returns userWithNulls
        every { mockCryptoManager.verifyPassword(any(), any(), any()) } returns true
        coEvery { mockUserDao.resetFailedLoginAttempts(any(), any()) } just Runs
        coEvery { mockUserDao.updateUser(any()) } just Runs
        coEvery { mockSessionManager.saveUserSession(any(), any()) } just Awaits

        // When
        val result = authRepository.login(TestData.VALID_EMAIL, TestData.STRONG_PASSWORD)

        // Then - should handle null values gracefully
        assertThat(result).isInstanceOf(AuthResult.Success::class.java)
    }

    @Test
    fun `registration with special characters in name is handled`() = runTest {
        // Given
        val nameWithSpecialChars = "José María O'Connor-Smith"
        coEvery { mockUserDao.getUserByEmail(any()) } returns null
        coEvery { mockUserDao.insertUser(any()) } returns 123L
        coEvery { mockSessionManager.saveUserSession(any(), any()) } just Awaits

        // When
        val result = authRepository.register(TestData.VALID_EMAIL, TestData.STRONG_PASSWORD, nameWithSpecialChars)

        // Then
        assertThat(result).isInstanceOf(AuthResult.Success::class.java)
        coVerify {
            mockUserDao.insertUser(
                match { user ->
                    user.username == nameWithSpecialChars
                },
            )
        }
    }

    @Test
    fun `login with case-insensitive email matching works`() = runTest {
        // Given
        val user = TestHelper.createTestUser(
            email = "user@example.com", // lowercase in database
            isActive = true,
            isAccountLocked = false,
        )

        // Mock will be called with lowercase version
        coEvery { mockUserDao.getUserByEmail("user@example.com") } returns user
        every { mockCryptoManager.verifyPassword(any(), any(), any()) } returns true
        coEvery { mockUserDao.resetFailedLoginAttempts(any(), any()) } just Runs
        coEvery { mockUserDao.updateUser(any()) } just Runs
        coEvery { mockSessionManager.saveUserSession(any(), any()) } just Awaits

        // When - login with uppercase email
        val result = authRepository.login("USER@EXAMPLE.COM", TestData.STRONG_PASSWORD)

        // Then
        assertThat(result).isInstanceOf(AuthResult.Success::class.java)
        coVerify { mockUserDao.getUserByEmail("user@example.com") }
    }
}
