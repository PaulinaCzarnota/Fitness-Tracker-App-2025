/**
 * Comprehensive unit tests for AuthRepository using Room in-memory database and JUnit 5.
 *
 * This test class covers:
 * - User registration with password hashing and validation
 * - User login with authentication and session management
 * - Password security and crypto operations
 * - Account locking and failed login attempt tracking
 * - Session persistence and restoration
 * - Error handling and edge cases
 */

package com.example.fitnesstrackerapp.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.auth.SessionManager
import com.example.fitnesstrackerapp.auth.SessionRestoreResult
import com.example.fitnesstrackerapp.data.dao.UserDao
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.User
import com.example.fitnesstrackerapp.security.CryptoManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var cryptoManager: CryptoManager
    private lateinit var sessionManager: SessionManager
    private lateinit var authRepository: AuthRepository

    @BeforeAll
    fun setupDatabase() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
        userDao = database.userDao()
    }

    @BeforeEach
    fun setup() {
        // Use real CryptoManager for integration testing
        cryptoManager = CryptoManager(context)

        // Mock SessionManager for controlled testing
        sessionManager = mockk<SessionManager>(relaxed = true)

        // Create repository with real UserDao and CryptoManager
        authRepository = AuthRepository(
            userDao = userDao,
            passwordManager = cryptoManager,
            sessionManager = sessionManager,
            context = context,
        )
    }

    @AfterEach
    fun cleanup() = runTest {
        database.clearAllTables()
        clearAllMocks()
    }

    @AfterAll
    fun closeDatabase() {
        database.close()
    }

    // region Registration Tests

    @Test
    fun `register creates user with hashed password and salt`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "SecurePassword123!"
        val name = "Test User"

        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs

        // When
        val result = authRepository.register(email, password, name)

        // Then
        assertTrue(result is AuthResult.Success)
        assertEquals("Registration successful", (result as AuthResult.Success).message)

        // Verify user was created with proper security
        val createdUser = userDao.getUserByEmail(email.lowercase())
        assertNotNull(createdUser)
        assertEquals(email.lowercase(), createdUser!!.email)
        assertEquals(name, createdUser.username)
        assertTrue(createdUser.isActive)
        assertFalse(createdUser.isAccountLocked)
        assertEquals(0, createdUser.failedLoginAttempts)

        // Verify password was hashed (not stored as plaintext)
        assertNotEquals(password, createdUser.passwordHash)
        assertTrue(createdUser.passwordHash.isNotBlank())
        assertTrue(createdUser.passwordSalt.isNotBlank())

        // Verify password can be verified with stored hash
        val salt = cryptoManager.hexToBytes(createdUser.passwordSalt)
        val storedHash = cryptoManager.hexToBytes(createdUser.passwordHash)
        assertTrue(cryptoManager.verifyPassword(password, storedHash, salt))

        coVerify { sessionManager.saveUserSession(match { it.email == email.lowercase() }, false) }
    }

    @Test
    fun `register normalizes email to lowercase`() = runTest {
        // Given
        val upperCaseEmail = "TEST@EXAMPLE.COM"
        val password = "SecurePassword123!"
        val name = "Test User"

        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs

        // When
        val result = authRepository.register(upperCaseEmail, password, name)

        // Then
        assertTrue(result is AuthResult.Success)

        val createdUser = userDao.getUserByEmail(upperCaseEmail.lowercase())
        assertNotNull(createdUser)
        assertEquals(upperCaseEmail.lowercase(), createdUser!!.email)
    }

    @Test
    fun `register trims whitespace from inputs`() = runTest {
        // Given
        val email = "  test@example.com  "
        val name = "  Test User  "
        val password = "SecurePassword123!"

        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs

        // When
        val result = authRepository.register(email, password, name)

        // Then
        assertTrue(result is AuthResult.Success)

        val createdUser = userDao.getUserByEmail(email.trim().lowercase())
        assertNotNull(createdUser)
        assertEquals("test@example.com", createdUser!!.email)
        assertEquals("Test User", createdUser.username)
    }

    @Test
    fun `register fails with empty email`() = runTest {
        // When
        val result = authRepository.register("", "password", "name")

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("All fields are required", (result as AuthResult.Error).message)
    }

    @Test
    fun `register fails with empty password`() = runTest {
        // When
        val result = authRepository.register("test@example.com", "", "name")

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("All fields are required", (result as AuthResult.Error).message)
    }

    @Test
    fun `register fails with empty name`() = runTest {
        // When
        val result = authRepository.register("test@example.com", "password", "")

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("All fields are required", (result as AuthResult.Error).message)
    }

    @Test
    fun `register fails when user already exists`() = runTest {
        // Given
        val email = "existing@example.com"
        val existingUser = User(
            email = email,
            username = "Existing User",
            passwordHash = "hash",
            passwordSalt = "salt",
        )
        userDao.insertUser(existingUser)

        // When
        val result = authRepository.register(email, "password", "New User")

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("User with this email already exists", (result as AuthResult.Error).message)
    }

    @Test
    fun `register splits name into firstName and lastName`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "SecurePassword123!"
        val fullName = "John Doe Smith"

        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs

        // When
        val result = authRepository.register(email, password, fullName)

        // Then
        assertTrue(result is AuthResult.Success)

        val createdUser = userDao.getUserByEmail(email)
        assertNotNull(createdUser)
        assertEquals("John", createdUser!!.firstName)
        assertEquals("Doe Smith", createdUser.lastName)
    }

    // endregion

    // region Login Tests

    @Test
    fun `login succeeds with correct credentials`() = runTest {
        // Given
        val email = "user@example.com"
        val password = "SecurePassword123!"
        val name = "Test User"

        // First register user
        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs
        authRepository.register(email, password, name)

        clearAllMocks()
        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs

        // When
        val result = authRepository.login(email, password)

        // Then
        assertTrue(result is AuthResult.Success)
        assertEquals("Login successful", (result as AuthResult.Success).message)

        // Verify session was saved
        coVerify {
            sessionManager.saveUserSession(
                match {
                    it.email == email.lowercase()
                },
                false,
            )
        }

        // Verify user's last login was updated
        val user = userDao.getUserByEmail(email)
        assertNotNull(user?.lastLogin)
    }

    @Test
    fun `login normalizes email to lowercase`() = runTest {
        // Given
        val email = "user@example.com"
        val upperCaseEmail = "USER@EXAMPLE.COM"
        val password = "SecurePassword123!"

        // Register with lowercase
        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs
        authRepository.register(email, password, "Test User")

        clearAllMocks()
        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs

        // When - login with uppercase
        val result = authRepository.login(upperCaseEmail, password)

        // Then
        assertTrue(result is AuthResult.Success)
    }

    @Test
    fun `login fails with incorrect password`() = runTest {
        // Given
        val email = "user@example.com"
        val correctPassword = "CorrectPassword123!"
        val wrongPassword = "WrongPassword123!"

        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs
        authRepository.register(email, correctPassword, "Test User")

        // When
        val result = authRepository.login(email, wrongPassword)

        // Then
        assertTrue(result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("attempts remaining"))

        // Verify failed login attempt was tracked
        val user = userDao.getUserByEmail(email)
        assertEquals(1, user?.failedLoginAttempts)
        assertFalse(user?.isAccountLocked ?: true)
    }

    @Test
    fun `login fails with empty email`() = runTest {
        // When
        val result = authRepository.login("", "password")

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("Email and password are required", (result as AuthResult.Error).message)
    }

    @Test
    fun `login fails with empty password`() = runTest {
        // When
        val result = authRepository.login("user@example.com", "")

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("Email and password are required", (result as AuthResult.Error).message)
    }

    @Test
    fun `login fails with whitespace-only credentials`() = runTest {
        // When
        val result = authRepository.login("   ", "   ")

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("Email and password are required", (result as AuthResult.Error).message)
    }

    @Test
    fun `login fails for non-existent user`() = runTest {
        // When
        val result = authRepository.login("nonexistent@example.com", "password")

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("Invalid email or password", (result as AuthResult.Error).message)
    }

    @Test
    fun `login fails for locked account`() = runTest {
        // Given
        val email = "locked@example.com"
        val user = User(
            email = email,
            username = "Locked User",
            passwordHash = "hash",
            passwordSalt = "salt",
            isActive = true,
            isAccountLocked = true,
            failedLoginAttempts = 5,
        )
        userDao.insertUser(user)

        // When
        val result = authRepository.login(email, "password")

        // Then
        assertTrue(result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("Account is locked"))
    }

    @Test
    fun `login fails for inactive account`() = runTest {
        // Given
        val email = "inactive@example.com"
        val user = User(
            email = email,
            username = "Inactive User",
            passwordHash = "hash",
            passwordSalt = "salt",
            isActive = false,
            isAccountLocked = false,
        )
        userDao.insertUser(user)

        // When
        val result = authRepository.login(email, "password")

        // Then
        assertTrue(result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("Account is deactivated"))
    }

    @Test
    fun `successful login resets failed attempts`() = runTest {
        // Given
        val email = "user@example.com"
        val password = "SecurePassword123!"

        // Register user and simulate failed attempts
        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs
        authRepository.register(email, password, "Test User")

        // Simulate failed attempts by updating user directly
        val user = userDao.getUserByEmail(email)!!
        val userWithFailedAttempts = user.copy(failedLoginAttempts = 3)
        userDao.updateUser(userWithFailedAttempts)

        clearAllMocks()
        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs

        // When - successful login
        val result = authRepository.login(email, password)

        // Then
        assertTrue(result is AuthResult.Success)

        // Verify failed attempts were reset
        val updatedUser = userDao.getUserByEmail(email)
        assertEquals(0, updatedUser?.failedLoginAttempts)
    }

    @Test
    fun `login with rememberMe saves session with rememberMe flag`() = runTest {
        // Given
        val email = "user@example.com"
        val password = "SecurePassword123!"

        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs
        authRepository.register(email, password, "Test User")

        clearAllMocks()
        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs

        // When
        val result = authRepository.login(email, password, rememberMe = true)

        // Then
        assertTrue(result is AuthResult.Success)
        coVerify { sessionManager.saveUserSession(any(), true) }
    }

    // endregion

    // region Account Locking Tests

    @Test
    fun `handleFailedLogin increments failed attempts`() = runTest {
        // Given
        val email = "user@example.com"
        val user = User(
            email = email,
            username = "Test User",
            passwordHash = "hash",
            passwordSalt = "salt",
            failedLoginAttempts = 2,
        )
        userDao.insertUser(user)

        // When
        val result = authRepository.handleFailedLogin(email)

        // Then
        assertTrue(result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("2 attempts remaining"))

        val updatedUser = userDao.getUserByEmail(email)
        assertEquals(3, updatedUser?.failedLoginAttempts)
        assertFalse(updatedUser?.isAccountLocked ?: true)
    }

    @Test
    fun `handleFailedLogin locks account after max attempts`() = runTest {
        // Given
        val email = "user@example.com"
        val user = User(
            email = email,
            username = "Test User",
            passwordHash = "hash",
            passwordSalt = "salt",
            failedLoginAttempts = 4, // One less than max (5)
        )
        userDao.insertUser(user)

        // When
        val result = authRepository.handleFailedLogin(email)

        // Then
        assertTrue(result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("Account locked"))

        val updatedUser = userDao.getUserByEmail(email)
        assertEquals(5, updatedUser?.failedLoginAttempts)
        assertTrue(updatedUser?.isAccountLocked ?: false)
    }

    // endregion

    // region Session Management Tests

    @Test
    fun `restoreSession succeeds with valid session`() = runTest {
        // Given
        val user = User(
            id = 123L,
            email = "user@example.com",
            username = "Test User",
            passwordHash = "hash",
            passwordSalt = "salt",
        )

        coEvery { sessionManager.restoreSession() } returns SessionRestoreResult.Success(user)

        // When
        val result = authRepository.restoreSession()

        // Then
        assertTrue(result is AuthResult.Success)
        assertEquals("Session restored successfully", (result as AuthResult.Success).message)
        assertEquals(user, authRepository.getCurrentUser())
        assertTrue(authRepository.isAuthenticated.value)
    }

    @Test
    fun `restoreSession fails with invalid session`() = runTest {
        // Given
        coEvery { sessionManager.restoreSession() } returns SessionRestoreResult.Failed("Session expired")

        // When
        val result = authRepository.restoreSession()

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("Session expired", (result as AuthResult.Error).message)
    }

    @Test
    fun `logout clears session and state`() = runTest {
        // Given
        val user = User(
            id = 123L,
            email = "user@example.com",
            username = "Test User",
            passwordHash = "hash",
            passwordSalt = "salt",
        )
        // Simulate logged in state
        authRepository.currentUser.value?.let {
            authRepository._currentUser.value = user
            authRepository._isAuthenticated.value = true
        }

        coEvery { sessionManager.clearUserSession() } just Runs

        // When
        authRepository.logout()

        // Then
        coVerify { sessionManager.clearUserSession() }
        assertNull(authRepository.getCurrentUser())
        assertFalse(authRepository.isAuthenticated.value)
        assertNull(authRepository.getCurrentUserId())
    }

    // endregion

    // region Password Management Tests

    @Test
    fun `changePassword succeeds with correct current password`() = runTest {
        // Given
        val email = "user@example.com"
        val currentPassword = "CurrentPassword123!"
        val newPassword = "NewPassword123!"

        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs
        authRepository.register(email, currentPassword, "Test User")

        // When
        val result = authRepository.changePassword(currentPassword, newPassword)

        // Then
        assertTrue(result is AuthResult.Success)
        assertEquals("Password changed successfully", (result as AuthResult.Success).message)

        // Verify new password works
        clearAllMocks()
        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs
        val loginResult = authRepository.login(email, newPassword)
        assertTrue(loginResult is AuthResult.Success)
    }

    @Test
    fun `changePassword fails with incorrect current password`() = runTest {
        // Given
        val email = "user@example.com"
        val currentPassword = "CurrentPassword123!"
        val wrongCurrentPassword = "WrongPassword123!"
        val newPassword = "NewPassword123!"

        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs
        authRepository.register(email, currentPassword, "Test User")

        // When
        val result = authRepository.changePassword(wrongCurrentPassword, newPassword)

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("Current password is incorrect", (result as AuthResult.Error).message)
    }

    @Test
    fun `changePassword fails when user not authenticated`() = runTest {
        // Given - no authenticated user

        // When
        val result = authRepository.changePassword("current", "new")

        // Then
        assertTrue(result is AuthResult.Error)
        assertEquals("User not authenticated", (result as AuthResult.Error).message)
    }

    // endregion

    // region Password Reset Tests

    @Test
    fun `initiatePasswordReset succeeds for existing user`() = runTest {
        // Given
        val email = "user@example.com"
        val user = User(
            email = email,
            username = "Test User",
            passwordHash = "hash",
            passwordSalt = "salt",
        )
        userDao.insertUser(user)

        // When
        val result = authRepository.initiatePasswordReset(email)

        // Then
        assertTrue(result is AuthResult.Success)
        assertEquals("Password reset instructions sent to your email", (result as AuthResult.Success).message)
    }

    @Test
    fun `initiatePasswordReset gives generic response for non-existent user`() = runTest {
        // When
        val result = authRepository.initiatePasswordReset("nonexistent@example.com")

        // Then
        assertTrue(result is AuthResult.Success)
        assertEquals("If this email is registered, you will receive reset instructions", (result as AuthResult.Success).message)
    }

    // endregion

    // region Error Handling Tests

    @Test
    fun `repository handles database exceptions gracefully`() = runTest {
        // Given - Create a scenario that would cause database exception
        val email = "test@example.com"

        // Close the database to simulate error
        database.close()

        // When
        val result = authRepository.register(email, "password", "Test User")

        // Then
        assertTrue(result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.contains("Registration failed"))
    }

    // endregion

    // region State Management Tests

    @Test
    fun `getCurrentUserId returns correct value when authenticated`() = runTest {
        // Given
        val email = "user@example.com"
        val password = "SecurePassword123!"

        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs
        authRepository.register(email, password, "Test User")

        // When
        val userId = authRepository.getCurrentUserId()

        // Then
        assertNotNull(userId)
        assertTrue(userId!! > 0)
    }

    @Test
    fun `getCurrentUserId returns null when not authenticated`() = runTest {
        // When
        val userId = authRepository.getCurrentUserId()

        // Then
        assertNull(userId)
    }

    @Test
    fun `authState flows correctly reflect authentication state`() = runTest {
        // Given
        val email = "user@example.com"
        val password = "SecurePassword123!"

        // Initially not authenticated
        assertFalse(authRepository.isAuthenticated.value)
        assertNull(authRepository.currentUser.value)

        coEvery { sessionManager.saveUserSession(any(), any()) } just Runs

        // When - register user
        authRepository.register(email, password, "Test User")

        // Then - should be authenticated
        assertTrue(authRepository.isAuthenticated.value)
        assertNotNull(authRepository.currentUser.value)
        assertEquals(email.lowercase(), authRepository.currentUser.value?.email)
    }

    // endregion
}
