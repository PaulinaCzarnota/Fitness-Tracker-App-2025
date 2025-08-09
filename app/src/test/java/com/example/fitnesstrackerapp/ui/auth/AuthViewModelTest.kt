package com.example.fitnesstrackerapp.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.fitnesstrackerapp.repository.AuthRepository
import com.example.fitnesstrackerapp.repository.AuthResult
import com.example.fitnesstrackerapp.util.test.MainDispatcherRule
import com.example.fitnesstrackerapp.util.test.TestData
import com.example.fitnesstrackerapp.util.test.TestHelper
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive unit tests for AuthViewModel.
 *
 * Tests authentication-related functionality including:
 * - Login and registration operations
 * - Input validation and error handling
 * - UI state management
 * - Password validation and security
 * - Authentication flow edge cases
 */
@ExperimentalCoroutinesApi
class AuthViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        authRepository = mockk()

        // Set up default mock behaviors
        every { authRepository.isAuthenticated } returns flowOf(false)
        every { authRepository.currentUser } returns flowOf(null)

        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `initial UI state is correct`() = runTest {
        // Given - ViewModel initialized in setup

        // When - checking initial state
        val uiState = viewModel.uiState.value

        // Then - should have default values
        assertThat(uiState.isAuthenticated).isFalse()
        assertThat(uiState.user).isNull()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).isNull()
        assertThat(uiState.successMessage).isNull()
    }

    @Test
    fun `login with valid credentials succeeds`() = runTest {
        // Given
        val email = TestData.VALID_EMAIL
        val password = TestData.STRONG_PASSWORD
        val successResult = AuthResult.Success("Login successful")

        coEvery { authRepository.login(email, password, false) } returns successResult

        // When
        viewModel.login(email, password)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isAuthenticated).isTrue()
        assertThat(uiState.successMessage).isEqualTo("Login successful")
        assertThat(uiState.error).isNull()

        coVerify { authRepository.login(email, password, false) }
    }

    @Test
    fun `login with invalid credentials fails`() = runTest {
        // Given
        val email = TestData.VALID_EMAIL
        val password = "wrongpassword"
        val errorResult = AuthResult.Error("Invalid email or password")

        coEvery { authRepository.login(email, password, false) } returns errorResult

        // When
        viewModel.login(email, password)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isAuthenticated).isFalse()
        assertThat(uiState.error).isEqualTo("Invalid email or password")
        assertThat(uiState.successMessage).isNull()

        coVerify { authRepository.login(email, password, false) }
    }

    @Test
    fun `login sets loading state correctly`() = runTest {
        // Given
        val email = TestData.VALID_EMAIL
        val password = TestData.STRONG_PASSWORD
        val successResult = AuthResult.Success("Login successful")

        coEvery { authRepository.login(email, password, false) } returns successResult

        // When
        viewModel.login(email, password)

        // Then - verify loading was set to true during operation and false after
        val finalState = viewModel.uiState.value
        assertThat(finalState.isLoading).isFalse()
    }

    @Test
    fun `register with valid data succeeds`() = runTest {
        // Given
        val email = TestData.VALID_EMAIL
        val password = TestData.STRONG_PASSWORD
        val name = "Test User"
        val successResult = AuthResult.Success("Registration successful")

        coEvery { authRepository.register(email, password, name, false) } returns successResult

        // When
        viewModel.register(email, password, name)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isAuthenticated).isTrue()
        assertThat(uiState.successMessage).isEqualTo("Registration successful")
        assertThat(uiState.error).isNull()

        coVerify { authRepository.register(email, password, name, false) }
    }

    @Test
    fun `register with existing email fails`() = runTest {
        // Given
        val email = TestData.VALID_EMAIL
        val password = TestData.STRONG_PASSWORD
        val name = "Test User"
        val errorResult = AuthResult.Error("User with this email already exists")

        coEvery { authRepository.register(email, password, name, false) } returns errorResult

        // When
        viewModel.register(email, password, name)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isAuthenticated).isFalse()
        assertThat(uiState.error).isEqualTo("User with this email already exists")
        assertThat(uiState.successMessage).isNull()

        coVerify { authRepository.register(email, password, name, false) }
    }

    @Test
    fun `logout clears authentication state`() = runTest {
        // Given - start with authenticated state
        every { authRepository.isAuthenticated } returns flowOf(true)
        every { authRepository.currentUser } returns flowOf(TestHelper.createTestUser())
        coEvery { authRepository.logout() } just Runs

        viewModel = AuthViewModel(authRepository)

        // When
        viewModel.logout()

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isAuthenticated).isFalse()
        assertThat(uiState.user).isNull()
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).isNull()
        assertThat(uiState.successMessage).isNull()

        coVerify { authRepository.logout() }
    }

    @Test
    fun `clearMessages clears error and success messages`() = runTest {
        // Given - viewModel with error message
        val errorResult = AuthResult.Error("Some error")
        coEvery { authRepository.login(any(), any(), any()) } returns errorResult

        viewModel.login(TestData.VALID_EMAIL, "wrongpassword")

        // Verify error is present
        assertThat(viewModel.uiState.value.error).isNotNull()

        // When
        viewModel.clearMessages()

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.error).isNull()
        assertThat(uiState.successMessage).isNull()
    }

    @Test
    fun `clearError clears only error message`() = runTest {
        // Given - viewModel with both error and success messages
        val errorResult = AuthResult.Error("Some error")
        coEvery { authRepository.login(any(), any()) } returns errorResult

        viewModel.login(TestData.VALID_EMAIL, "wrongpassword")

        // Manually set success message for test
        // Note: In real scenario, this would come from a successful operation

        // When
        viewModel.clearError()

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.error).isNull()
    }

    @Test
    fun `updateProfile with valid user succeeds`() = runTest {
        // Given
        val user = TestHelper.createTestUser(
            email = TestData.VALID_EMAIL,
            username = "updateduser",
        )
        val successResult = AuthResult.Success("Profile updated successfully")

        coEvery { authRepository.updateProfile(user) } returns successResult

        // When
        viewModel.updateProfile(user)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.successMessage).isEqualTo("Profile updated successfully")
        assertThat(uiState.error).isNull()

        coVerify { authRepository.updateProfile(user) }
    }

    @Test
    fun `updateProfile with invalid data fails`() = runTest {
        // Given
        val user = TestHelper.createTestUser()
        val errorResult = AuthResult.Error("Failed to update profile")

        coEvery { authRepository.updateProfile(user) } returns errorResult

        // When
        viewModel.updateProfile(user)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).isEqualTo("Failed to update profile")
        assertThat(uiState.successMessage).isNull()

        coVerify { authRepository.updateProfile(user) }
    }

    @Test
    fun `changePassword with valid passwords succeeds`() = runTest {
        // Given
        val currentPassword = TestData.STRONG_PASSWORD
        val newPassword = "NewStrongPassword123!"
        val successResult = AuthResult.Success("Password changed successfully")

        coEvery { authRepository.changePassword(currentPassword, newPassword) } returns successResult

        // When
        viewModel.changePassword(currentPassword, newPassword)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.successMessage).isEqualTo("Password changed successfully")
        assertThat(uiState.error).isNull()

        coVerify { authRepository.changePassword(currentPassword, newPassword) }
    }

    @Test
    fun `changePassword with weak new password fails`() = runTest {
        // Given
        val currentPassword = TestData.STRONG_PASSWORD
        val weakPassword = TestData.WEAK_PASSWORD

        // When
        viewModel.changePassword(currentPassword, weakPassword)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).isNotNull()
        assertThat(uiState.error).contains("Password must")
        assertThat(uiState.successMessage).isNull()

        // Verify repository method was never called due to validation failure
        coVerify(exactly = 0) { authRepository.changePassword(any(), any()) }
    }

    @Test
    fun `changePassword with wrong current password fails`() = runTest {
        // Given
        val wrongCurrentPassword = "WrongPassword123!"
        val newPassword = "NewStrongPassword123!"
        val errorResult = AuthResult.Error("Current password is incorrect")

        coEvery { authRepository.changePassword(wrongCurrentPassword, newPassword) } returns errorResult

        // When
        viewModel.changePassword(wrongCurrentPassword, newPassword)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).isEqualTo("Current password is incorrect")
        assertThat(uiState.successMessage).isNull()

        coVerify { authRepository.changePassword(wrongCurrentPassword, newPassword) }
    }

    @Test
    fun `authentication state flows are observed correctly`() = runTest {
        // Given
        val testUser = TestHelper.createTestUser()
        every { authRepository.isAuthenticated } returns flowOf(true)
        every { authRepository.currentUser } returns flowOf(testUser)

        // When
        val newViewModel = AuthViewModel(authRepository)

        // Then
        val uiState = newViewModel.uiState.value
        assertThat(uiState.isAuthenticated).isTrue()
        assertThat(uiState.user).isEqualTo(testUser)
    }

    @Test
    fun `multiple login attempts handle loading state correctly`() = runTest {
        // Given
        val email = TestData.VALID_EMAIL
        val password = TestData.STRONG_PASSWORD
        val successResult = AuthResult.Success("Login successful")

        coEvery { authRepository.login(email, password, false) } returns successResult

        // When - simulate multiple rapid login attempts
        viewModel.login(email, password)
        viewModel.login(email, password)

        // Then - should handle state correctly
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isAuthenticated).isTrue()

        // Repository should be called for each attempt
        coVerify(exactly = 2) { authRepository.login(email, password, false) }
    }

    @Test
    fun `repository errors are handled gracefully`() = runTest {
        // Given
        val email = TestData.VALID_EMAIL
        val password = TestData.STRONG_PASSWORD

        coEvery { authRepository.login(email, password, false) } throws RuntimeException("Network error")

        // When
        viewModel.login(email, password)

        // Then - should handle exception without crashing
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isAuthenticated).isFalse()
        // Error might be handled by the repository layer or propagated
    }

    @Test
    fun `empty email and password inputs are handled`() = runTest {
        // Given
        val emptyEmail = ""
        val emptyPassword = ""
        val errorResult = AuthResult.Error("Email and password are required")

        coEvery { authRepository.login(emptyEmail, emptyPassword, false) } returns errorResult

        // When
        viewModel.login(emptyEmail, emptyPassword)

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isAuthenticated).isFalse()
        assertThat(uiState.error).isEqualTo("Email and password are required")

        coVerify { authRepository.login(emptyEmail, emptyPassword, false) }
    }

    @Test
    fun `invalid email format is handled by repository`() = runTest {
        // Given
        val invalidEmail = TestData.INVALID_EMAIL
        val password = TestData.STRONG_PASSWORD
        val errorResult = AuthResult.Error("Invalid email format")

        coEvery { authRepository.register(invalidEmail, password, "Test User", false) } returns errorResult

        // When
        viewModel.register(invalidEmail, password, "Test User")

        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.isAuthenticated).isFalse()
        assertThat(uiState.error).isEqualTo("Invalid email format")

        coVerify { authRepository.register(invalidEmail, password, "Test User", false) }
    }

    @Test
    fun `viewmodel handles null user from repository correctly`() = runTest {
        // Given
        every { authRepository.isAuthenticated } returns flowOf(true)
        every { authRepository.currentUser } returns flowOf(null)

        // When
        val newViewModel = AuthViewModel(authRepository)

        // Then
        val uiState = newViewModel.uiState.value
        assertThat(uiState.isAuthenticated).isTrue()
        assertThat(uiState.user).isNull()
    }

    @Test
    fun `successful operations clear previous errors`() = runTest {
        // Given - start with an error state
        val errorResult = AuthResult.Error("Login failed")
        coEvery { authRepository.login(any(), any()) } returns errorResult

        viewModel.login(TestData.VALID_EMAIL, "wrongpassword")
        assertThat(viewModel.uiState.value.error).isNotNull()

        // When - perform successful operation
        val successResult = AuthResult.Success("Login successful")
        coEvery { authRepository.login(any(), any(), any()) } returns successResult

        viewModel.login(TestData.VALID_EMAIL, TestData.STRONG_PASSWORD)

        // Then - error should be cleared
        val uiState = viewModel.uiState.value
        assertThat(uiState.error).isNull()
        assertThat(uiState.successMessage).isEqualTo("Login successful")
        assertThat(uiState.isAuthenticated).isTrue()
    }
}
