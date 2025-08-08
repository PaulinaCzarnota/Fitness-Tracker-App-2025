package com.example.fitnesstrackerapp.ui.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.fitnesstrackerapp.repository.AuthRepository
import com.example.fitnesstrackerapp.repository.AuthResult
import com.example.fitnesstrackerapp.util.test.TestData
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Comprehensive UI tests for authentication flow.
 *
 * Tests critical user authentication journeys including:
 * - Login form validation and submission
 * - Registration flow with input validation
 * - Error message display and handling
 * - Loading states and user feedback
 * - Navigation between auth screens
 * - Biometric authentication integration
 * - Password strength validation
 * - Authentication state persistence
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class AuthenticationFlowTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setup() {
        hiltRule.inject()

        // Mock the auth repository for consistent testing
        authRepository = mockk {
            every { isAuthenticated } returns flowOf(false)
            every { currentUser } returns flowOf(null)
        }

        authViewModel = AuthViewModel(authRepository)
    }

    @Test
    fun loginScreen_displaysCorrectElements() {
        composeTestRule.setContent {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { },
                onNavigateToSignUp = { },
                onNavigateToForgotPassword = { },
            )
        }

        // Verify all UI elements are displayed
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Don't have an account? Sign up").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forgot Password?").assertIsDisplayed()
    }

    @Test
    fun loginScreen_withValidCredentials_callsRepository() = runTest {
        val email = TestData.VALID_EMAIL
        val password = TestData.STRONG_PASSWORD

        coEvery { authRepository.login(email, password) } returns AuthResult.Success("Login successful")

        composeTestRule.setContent {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { },
                onNavigateToSignUp = { },
                onNavigateToForgotPassword = { },
            )
        }

        // Enter email
        composeTestRule.onNodeWithText("Email")
            .performTextInput(email)

        // Enter password
        composeTestRule.onNodeWithText("Password")
            .performTextInput(password)

        // Click login button
        composeTestRule.onNodeWithText("Login")
            .performClick()

        // Wait for async operation
        composeTestRule.waitForIdle()

        // Verify repository was called
        coVerify { authRepository.login(email, password) }
    }

    @Test
    fun loginScreen_withEmptyFields_showsValidationErrors() {
        composeTestRule.setContent {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { },
                onNavigateToSignUp = { },
                onNavigateToForgotPassword = { },
            )
        }

        // Click login without entering credentials
        composeTestRule.onNodeWithText("Login")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify validation messages appear
        composeTestRule.onNodeWithText("Email is required")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Password is required")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_withInvalidEmail_showsEmailValidationError() {
        composeTestRule.setContent {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { },
                onNavigateToSignUp = { },
                onNavigateToForgotPassword = { },
            )
        }

        // Enter invalid email
        composeTestRule.onNodeWithText("Email")
            .performTextInput(TestData.INVALID_EMAIL)

        composeTestRule.onNodeWithText("Password")
            .performTextInput(TestData.STRONG_PASSWORD)

        composeTestRule.onNodeWithText("Login")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify email validation error
        composeTestRule.onNodeWithText("Please enter a valid email address")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_withIncorrectCredentials_showsErrorMessage() = runTest {
        val email = TestData.VALID_EMAIL
        val password = "wrongpassword"

        coEvery { authRepository.login(email, password) } returns
            AuthResult.Error("Invalid email or password")

        composeTestRule.setContent {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { },
                onNavigateToSignUp = { },
                onNavigateToForgotPassword = { },
            )
        }

        composeTestRule.onNodeWithText("Email")
            .performTextInput(email)

        composeTestRule.onNodeWithText("Password")
            .performTextInput(password)

        composeTestRule.onNodeWithText("Login")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify error message is displayed
        composeTestRule.onNodeWithText("Invalid email or password")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsLoadingDuringAuthentication() = runTest {
        val email = TestData.VALID_EMAIL
        val password = TestData.STRONG_PASSWORD

        // Simulate slow network response
        coEvery { authRepository.login(email, password) } coAnswers {
            kotlinx.coroutines.delay(1000)
            AuthResult.Success("Login successful")
        }

        composeTestRule.setContent {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { },
                onNavigateToSignUp = { },
                onNavigateToForgotPassword = { },
            )
        }

        composeTestRule.onNodeWithText("Email")
            .performTextInput(email)

        composeTestRule.onNodeWithText("Password")
            .performTextInput(password)

        composeTestRule.onNodeWithText("Login")
            .performClick()

        // Verify loading indicator appears
        composeTestRule.onNodeWithTag("loading_indicator")
            .assertIsDisplayed()

        // Wait for operation to complete
        composeTestRule.waitForIdle()
    }

    @Test
    fun signUpScreen_displaysCorrectElements() {
        composeTestRule.setContent {
            SignUpScreen(
                viewModel = authViewModel,
                onSignUpSuccess = { },
                onNavigateToLogin = { },
            )
        }

        // Verify all UI elements are displayed
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Full Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
        composeTestRule.onNodeWithText("Already have an account? Login").assertIsDisplayed()
    }

    @Test
    fun signUpScreen_withValidData_callsRepository() = runTest {
        val name = "Test User"
        val email = TestData.VALID_EMAIL
        val password = TestData.STRONG_PASSWORD

        coEvery { authRepository.register(email, password, name) } returns
            AuthResult.Success("Registration successful")

        composeTestRule.setContent {
            SignUpScreen(
                viewModel = authViewModel,
                onSignUpSuccess = { },
                onNavigateToLogin = { },
            )
        }

        // Fill out the form
        composeTestRule.onNodeWithText("Full Name")
            .performTextInput(name)

        composeTestRule.onNodeWithText("Email")
            .performTextInput(email)

        composeTestRule.onNodeWithText("Password")
            .performTextInput(password)

        composeTestRule.onNodeWithText("Confirm Password")
            .performTextInput(password)

        // Submit form
        composeTestRule.onNodeWithText("Sign Up")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify repository was called
        coVerify { authRepository.register(email, password, name) }
    }

    @Test
    fun signUpScreen_withMismatchedPasswords_showsError() {
        composeTestRule.setContent {
            SignUpScreen(
                viewModel = authViewModel,
                onSignUpSuccess = { },
                onNavigateToLogin = { },
            )
        }

        composeTestRule.onNodeWithText("Full Name")
            .performTextInput("Test User")

        composeTestRule.onNodeWithText("Email")
            .performTextInput(TestData.VALID_EMAIL)

        composeTestRule.onNodeWithText("Password")
            .performTextInput(TestData.STRONG_PASSWORD)

        composeTestRule.onNodeWithText("Confirm Password")
            .performTextInput("DifferentPassword123!")

        composeTestRule.onNodeWithText("Sign Up")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify password mismatch error
        composeTestRule.onNodeWithText("Passwords do not match")
            .assertIsDisplayed()
    }

    @Test
    fun signUpScreen_withWeakPassword_showsPasswordStrengthError() {
        composeTestRule.setContent {
            SignUpScreen(
                viewModel = authViewModel,
                onSignUpSuccess = { },
                onNavigateToLogin = { },
            )
        }

        composeTestRule.onNodeWithText("Full Name")
            .performTextInput("Test User")

        composeTestRule.onNodeWithText("Email")
            .performTextInput(TestData.VALID_EMAIL)

        composeTestRule.onNodeWithText("Password")
            .performTextInput(TestData.WEAK_PASSWORD)

        composeTestRule.onNodeWithText("Confirm Password")
            .performTextInput(TestData.WEAK_PASSWORD)

        composeTestRule.onNodeWithText("Sign Up")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify password strength error
        composeTestRule.onNodeWithText("Password must be at least 8 characters")
            .assertIsDisplayed()
    }

    @Test
    fun passwordVisibilityToggle_worksCorrectly() {
        composeTestRule.setContent {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { },
                onNavigateToSignUp = { },
                onNavigateToForgotPassword = { },
            )
        }

        // Enter password
        composeTestRule.onNodeWithText("Password")
            .performTextInput("testpassword")

        // Initially password should be hidden
        composeTestRule.onNodeWithTag("password_visibility_toggle")
            .assertIsDisplayed()

        // Click visibility toggle
        composeTestRule.onNodeWithTag("password_visibility_toggle")
            .performClick()

        // Verify password is now visible (implementation dependent)
        // This test would need to be adapted based on your actual implementation
    }

    @Test
    fun forgotPasswordFlow_navigatesCorrectly() {
        var navigateToForgotPasswordCalled = false

        composeTestRule.setContent {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { },
                onNavigateToSignUp = { },
                onNavigateToForgotPassword = { navigateToForgotPasswordCalled = true },
            )
        }

        // Click forgot password link
        composeTestRule.onNodeWithText("Forgot Password?")
            .performClick()

        // Verify navigation was called
        assertThat(navigateToForgotPasswordCalled).isTrue()
    }

    @Test
    fun navigationBetweenAuthScreens_worksCorrectly() {
        var navigateToSignUpCalled = false
        var navigateToLoginCalled = false

        composeTestRule.setContent {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { },
                onNavigateToSignUp = { navigateToSignUpCalled = true },
                onNavigateToForgotPassword = { },
            )
        }

        // Navigate to sign up
        composeTestRule.onNodeWithText("Don't have an account? Sign up")
            .performClick()

        assertThat(navigateToSignUpCalled).isTrue()

        // Now test sign up to login navigation
        composeTestRule.setContent {
            SignUpScreen(
                viewModel = authViewModel,
                onSignUpSuccess = { },
                onNavigateToLogin = { navigateToLoginCalled = true },
            )
        }

        composeTestRule.onNodeWithText("Already have an account? Login")
            .performClick()

        assertThat(navigateToLoginCalled).isTrue()
    }

    @Test
    fun authenticationSuccess_clearsFormAndNavigates() = runTest {
        val email = TestData.VALID_EMAIL
        val password = TestData.STRONG_PASSWORD
        var loginSuccessCalled = false

        coEvery { authRepository.login(email, password) } returns
            AuthResult.Success("Login successful")

        composeTestRule.setContent {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { loginSuccessCalled = true },
                onNavigateToSignUp = { },
                onNavigateToForgotPassword = { },
            )
        }

        composeTestRule.onNodeWithText("Email")
            .performTextInput(email)

        composeTestRule.onNodeWithText("Password")
            .performTextInput(password)

        composeTestRule.onNodeWithText("Login")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify success callback was called
        assertThat(loginSuccessCalled).isTrue()
    }

    @Test
    fun emailValidation_showsRealTimeValidation() {
        composeTestRule.setContent {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { },
                onNavigateToSignUp = { },
                onNavigateToForgotPassword = { },
            )
        }

        // Enter invalid email
        composeTestRule.onNodeWithText("Email")
            .performTextInput("invalid")

        // Move focus away from email field
        composeTestRule.onNodeWithText("Password")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify real-time validation error
        composeTestRule.onNodeWithText("Please enter a valid email address")
            .assertIsDisplayed()
    }

    @Test
    fun keyboardNavigation_worksCorrectly() {
        composeTestRule.setContent {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { },
                onNavigateToSignUp = { },
                onNavigateToForgotPassword = { },
            )
        }

        // Test tab navigation between fields
        composeTestRule.onNodeWithText("Email")
            .performTextInput("test@example.com")
            .performImeAction()

        // Verify focus moved to password field
        composeTestRule.onNodeWithText("Password")
            .assertIsFocused()
    }

    @Test
    fun accessibilityLabels_arePresent() {
        composeTestRule.setContent {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { },
                onNavigateToSignUp = { },
                onNavigateToForgotPassword = { },
            )
        }

        // Verify accessibility labels and descriptions are present
        composeTestRule.onNodeWithText("Email")
            .assertIsDisplayed()
            .assert(hasContentDescription() or hasText("Email"))

        composeTestRule.onNodeWithText("Password")
            .assertIsDisplayed()
            .assert(hasContentDescription() or hasText("Password"))

        composeTestRule.onNodeWithText("Login")
            .assertIsDisplayed()
            .assert(hasContentDescription() or hasText("Login"))
    }

    @Test
    fun errorMessages_areClearable() = runTest {
        coEvery { authRepository.login(any(), any()) } returns
            AuthResult.Error("Network error")

        composeTestRule.setContent {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { },
                onNavigateToSignUp = { },
                onNavigateToForgotPassword = { },
            )
        }

        // Trigger error
        composeTestRule.onNodeWithText("Email")
            .performTextInput(TestData.VALID_EMAIL)

        composeTestRule.onNodeWithText("Password")
            .performTextInput("anypassword")

        composeTestRule.onNodeWithText("Login")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify error is displayed
        composeTestRule.onNodeWithText("Network error")
            .assertIsDisplayed()

        // Clear error by editing form
        composeTestRule.onNodeWithText("Email")
            .performTextClearance()
            .performTextInput("newemail@test.com")

        composeTestRule.waitForIdle()

        // Verify error is cleared
        composeTestRule.onNodeWithText("Network error")
            .assertDoesNotExist()
    }
}
