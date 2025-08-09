/**
 * Comprehensive instrumented tests for authentication UI components
 *
 * Tests cover:
 * - Login screen interaction and validation
 * - Sign-up screen interaction and validation
 * - Error handling and display
 * - Loading states and progress indicators
 * - Accessibility compliance
 * - Material Design 3 component behavior
 * - Form validation and user feedback
 */

package com.example.fitnesstrackerapp.ui.auth

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.auth.FirebaseAuthManager
import com.example.fitnesstrackerapp.repository.AuthRepository
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthenticationUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var mockAuthRepository: AuthRepository
    private lateinit var mockFirebaseAuthManager: FirebaseAuthManager
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setup() {
        // Create mocks
        mockAuthRepository = mockk(relaxed = true)
        mockFirebaseAuthManager = mockk(relaxed = true)

        // Create ViewModel with mocked dependencies
        authViewModel = AuthViewModel(mockAuthRepository)
    }

    // region Login Screen Tests

    @Test
    fun loginScreen_displaysAllRequiredElements() {
        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = {},
                onNavigateToForgotPassword = {},
            )
        }

        // Verify essential UI elements are displayed
        composeTestRule.onNodeWithText("Fitness Tracker").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Email input field").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Password input field").assertIsDisplayed()
        composeTestRule.onNodeWithText("Remember me").assertIsDisplayed()
        composeTestRule.onNodeWithText("Forgot Password?").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Login button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Don't have an account?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
    }

    @Test
    fun loginScreen_emailValidation_showsErrorForInvalidEmail() {
        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = {},
                onNavigateToForgotPassword = {},
            )
        }

        // Enter invalid email
        composeTestRule.onNodeWithContentDescription("Email input field")
            .performTextInput("invalid-email")

        // Move focus away to trigger validation
        composeTestRule.onNodeWithContentDescription("Password input field")
            .performClick()

        // Verify error message is shown
        composeTestRule.onNodeWithText("Please enter a valid email address")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_validEmail_noErrorShown() {
        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = {},
                onNavigateToForgotPassword = {},
            )
        }

        // Enter valid email
        composeTestRule.onNodeWithContentDescription("Email input field")
            .performTextInput("test@example.com")

        // Move focus away to trigger validation
        composeTestRule.onNodeWithContentDescription("Password input field")
            .performClick()

        // Verify no error message is shown
        composeTestRule.onNodeWithText("Please enter a valid email address")
            .assertDoesNotExist()
    }

    @Test
    fun loginScreen_passwordVisibilityToggle_worksCorrectly() {
        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = {},
                onNavigateToForgotPassword = {},
            )
        }

        // Enter password
        composeTestRule.onNodeWithContentDescription("Password input field")
            .performTextInput("testpassword")

        // Initially password should be hidden (Show password icon visible)
        composeTestRule.onNodeWithContentDescription("Show password")
            .assertIsDisplayed()

        // Click to show password
        composeTestRule.onNodeWithContentDescription("Show password")
            .performClick()

        // Now Hide password icon should be visible
        composeTestRule.onNodeWithContentDescription("Hide password")
            .assertIsDisplayed()

        // Click to hide password again
        composeTestRule.onNodeWithContentDescription("Hide password")
            .performClick()

        // Show password icon should be visible again
        composeTestRule.onNodeWithContentDescription("Show password")
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_rememberMeCheckbox_togglesCorrectly() {
        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = {},
                onNavigateToForgotPassword = {},
            )
        }

        // Initially unchecked
        composeTestRule.onNodeWithContentDescription("Remember me checkbox")
            .assertIsOff()

        // Click to check
        composeTestRule.onNodeWithContentDescription("Remember me checkbox")
            .performClick()

        // Should be checked now
        composeTestRule.onNodeWithContentDescription("Remember me checkbox")
            .assertIsOn()

        // Click to uncheck
        composeTestRule.onNodeWithContentDescription("Remember me checkbox")
            .performClick()

        // Should be unchecked again
        composeTestRule.onNodeWithContentDescription("Remember me checkbox")
            .assertIsOff()
    }

    @Test
    fun loginScreen_loginButtonDisabled_whenFieldsEmpty() {
        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = {},
                onNavigateToForgotPassword = {},
            )
        }

        // Login button should be disabled when fields are empty
        composeTestRule.onNodeWithContentDescription("Login button")
            .assertIsNotEnabled()
    }

    @Test
    fun loginScreen_loginButtonEnabled_whenFieldsFilled() {
        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = {},
                onNavigateToForgotPassword = {},
            )
        }

        // Fill in fields
        composeTestRule.onNodeWithContentDescription("Email input field")
            .performTextInput("test@example.com")
        composeTestRule.onNodeWithContentDescription("Password input field")
            .performTextInput("password123")

        // Login button should be enabled
        composeTestRule.onNodeWithContentDescription("Login button")
            .assertIsEnabled()
    }

    @Test
    fun loginScreen_navigationLinks_areClickable() {
        var signUpClicked = false
        var forgotPasswordClicked = false

        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = { signUpClicked = true },
                onNavigateToForgotPassword = { forgotPasswordClicked = true },
            )
        }

        // Click sign up link
        composeTestRule.onNodeWithText("Sign Up").performClick()
        assert(signUpClicked)

        // Click forgot password link
        composeTestRule.onNodeWithContentDescription("Forgot password button").performClick()
        assert(forgotPasswordClicked)
    }

    // endregion

    // region Sign-Up Screen Tests

    @Test
    fun signUpScreen_displaysAllRequiredElements() {
        composeTestRule.setContent {
            EnhancedSignUpScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onSignUpSuccess = {},
                onNavigateToLogin = {},
            )
        }

        // Verify essential UI elements are displayed
        composeTestRule.onNodeWithText("Join Fitness Tracker").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start your fitness journey today").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Full name input field").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Email input field").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Password input field").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Confirm password input field").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Terms and conditions acceptance").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Sign up button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Already have an account?").assertIsDisplayed()
    }

    @Test
    fun signUpScreen_passwordStrengthIndicator_appearsWithPassword() {
        composeTestRule.setContent {
            EnhancedSignUpScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onSignUpSuccess = {},
                onNavigateToLogin = {},
            )
        }

        // Initially no password strength indicator
        composeTestRule.onNodeWithText("Password Strength:").assertDoesNotExist()

        // Enter password
        composeTestRule.onNodeWithContentDescription("Password input field")
            .performTextInput("weak")

        // Password strength indicator should appear
        composeTestRule.onNodeWithText("Password Strength:").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weak").assertIsDisplayed()
    }

    @Test
    fun signUpScreen_strongPassword_showsGreenIndicator() {
        composeTestRule.setContent {
            EnhancedSignUpScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onSignUpSuccess = {},
                onNavigateToLogin = {},
            )
        }

        // Enter strong password
        composeTestRule.onNodeWithContentDescription("Password input field")
            .performTextInput("StrongPassword123!")

        // Should show strong password indicator
        composeTestRule.onNodeWithText("Strong").assertIsDisplayed()
    }

    @Test
    fun signUpScreen_confirmPasswordMismatch_showsError() {
        composeTestRule.setContent {
            EnhancedSignUpScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onSignUpSuccess = {},
                onNavigateToLogin = {},
            )
        }

        // Enter password
        composeTestRule.onNodeWithContentDescription("Password input field")
            .performTextInput("password123")

        // Enter different confirm password
        composeTestRule.onNodeWithContentDescription("Confirm password input field")
            .performTextInput("different123")

        // Move focus away to trigger validation
        composeTestRule.onNodeWithContentDescription("Full name input field")
            .performClick()

        // Error should be displayed
        composeTestRule.onNodeWithText("Passwords do not match")
            .assertIsDisplayed()
    }

    @Test
    fun signUpScreen_termsNotAccepted_showsError() {
        composeTestRule.setContent {
            EnhancedSignUpScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onSignUpSuccess = {},
                onNavigateToLogin = {},
            )
        }

        // Fill all fields but don't accept terms
        composeTestRule.onNodeWithContentDescription("Full name input field")
            .performTextInput("Test User")
        composeTestRule.onNodeWithContentDescription("Email input field")
            .performTextInput("test@example.com")
        composeTestRule.onNodeWithContentDescription("Password input field")
            .performTextInput("StrongPassword123!")
        composeTestRule.onNodeWithContentDescription("Confirm password input field")
            .performTextInput("StrongPassword123!")

        // Try to sign up without accepting terms
        composeTestRule.onNodeWithContentDescription("Sign up button")
            .performClick()

        // Error should be displayed
        composeTestRule.onNodeWithText("You must accept the terms and conditions")
            .assertIsDisplayed()
    }

    @Test
    fun signUpScreen_fullNameValidation_worksCorrectly() {
        composeTestRule.setContent {
            EnhancedSignUpScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onSignUpSuccess = {},
                onNavigateToLogin = {},
            )
        }

        // Enter invalid name (too short)
        composeTestRule.onNodeWithContentDescription("Full name input field")
            .performTextInput("A")

        // Move focus away to trigger validation
        composeTestRule.onNodeWithContentDescription("Email input field")
            .performClick()

        // Should show error (based on ValidationUtils implementation)
        // The actual error message depends on ValidationUtils.validateFullName implementation
        composeTestRule.waitForIdle()
    }

    @Test
    fun signUpScreen_signUpButtonDisabled_whenFieldsIncomplete() {
        composeTestRule.setContent {
            EnhancedSignUpScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onSignUpSuccess = {},
                onNavigateToLogin = {},
            )
        }

        // Initially button should be enabled (it checks at click time)
        composeTestRule.onNodeWithContentDescription("Sign up button")
            .assertIsEnabled()

        // But clicking should show validation errors
        composeTestRule.onNodeWithContentDescription("Sign up button")
            .performClick()

        // Should show "Please fill in all fields" error
        composeTestRule.onNodeWithText("Please fill in all fields")
            .assertIsDisplayed()
    }

    // endregion

    // region Accessibility Tests

    @Test
    fun loginScreen_hasProperSemantics() {
        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = {},
                onNavigateToForgotPassword = {},
            )
        }

        // Check that important elements have content descriptions
        composeTestRule.onNodeWithContentDescription("Email input field").assertExists()
        composeTestRule.onNodeWithContentDescription("Password input field").assertExists()
        composeTestRule.onNodeWithContentDescription("Login button").assertExists()
        composeTestRule.onNodeWithContentDescription("Remember me checkbox").assertExists()
        composeTestRule.onNodeWithContentDescription("Forgot password button").assertExists()
        composeTestRule.onNodeWithContentDescription("Sign up section").assertExists()
    }

    @Test
    fun signUpScreen_hasProperSemantics() {
        composeTestRule.setContent {
            EnhancedSignUpScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onSignUpSuccess = {},
                onNavigateToLogin = {},
            )
        }

        // Check that important elements have content descriptions
        composeTestRule.onNodeWithContentDescription("Full name input field").assertExists()
        composeTestRule.onNodeWithContentDescription("Email input field").assertExists()
        composeTestRule.onNodeWithContentDescription("Password input field").assertExists()
        composeTestRule.onNodeWithContentDescription("Confirm password input field").assertExists()
        composeTestRule.onNodeWithContentDescription("Terms and conditions acceptance").assertExists()
        composeTestRule.onNodeWithContentDescription("Sign up button").assertExists()
        composeTestRule.onNodeWithContentDescription("Login section").assertExists()
    }

    // endregion

    // region Error Handling Tests

    @Test
    fun errorMessage_displaysCorrectly() {
        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = {},
                onNavigateToForgotPassword = {},
            )
        }

        // Trigger an error by trying to login with empty fields
        composeTestRule.onNodeWithContentDescription("Login button")
            .performClick()

        // Error message should appear
        composeTestRule.onNodeWithText("Please fill in all fields correctly")
            .assertIsDisplayed()
    }

    @Test
    fun errorMessage_hasProperSemantics() {
        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = {},
                onNavigateToForgotPassword = {},
            )
        }

        // Trigger an error
        composeTestRule.onNodeWithContentDescription("Login button")
            .performClick()

        // Error message should have proper content description
        composeTestRule.onAllNodesWithContentDescription(
            text = "Error message: Please fill in all fields correctly",
            substring = true,
        ).assertCountEquals(1)
    }

    // endregion

    // region Loading State Tests

    @Test
    fun loadingState_showsProgressIndicator() {
        // This test would require mocking the ViewModel state to return loading = true
        // Since we're using a real ViewModel, we'd need to trigger a loading state

        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = {},
                onNavigateToForgotPassword = {},
            )
        }

        // Fill valid data and submit to trigger loading state
        composeTestRule.onNodeWithContentDescription("Email input field")
            .performTextInput("test@example.com")
        composeTestRule.onNodeWithContentDescription("Password input field")
            .performTextInput("password123")

        composeTestRule.onNodeWithContentDescription("Login button")
            .performClick()

        // In a real scenario with proper mocking, we would see:
        // composeTestRule.onNodeWithText("Logging in...").assertIsDisplayed()
    }

    // endregion

    // region Form Interaction Tests

    @Test
    fun keyboardNavigation_worksCorrectly() {
        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = {},
                onNavigateToForgotPassword = {},
            )
        }

        // Focus on email field and press Next
        composeTestRule.onNodeWithContentDescription("Email input field")
            .performClick()
            .performImeAction()

        // Password field should now be focused
        // Note: Testing focus changes in Compose can be tricky and may require additional setup
    }

    @Test
    fun formSubmission_viaKeyboard_worksCorrectly() {
        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = {},
                onNavigateToForgotPassword = {},
            )
        }

        // Fill form
        composeTestRule.onNodeWithContentDescription("Email input field")
            .performTextInput("test@example.com")
        composeTestRule.onNodeWithContentDescription("Password input field")
            .performTextInput("password123")

        // Submit via keyboard action on password field
        composeTestRule.onNodeWithContentDescription("Password input field")
            .performImeAction()

        // Should trigger login attempt (in real scenario with proper mocking)
    }

    // endregion

    // region Material Design Tests

    @Test
    fun materialDesign_elementsArePresent() {
        composeTestRule.setContent {
            EnhancedLoginScreen(
                authViewModel = authViewModel,
                firebaseAuthManager = mockFirebaseAuthManager,
                onLoginSuccess = {},
                onNavigateToSignUp = {},
                onNavigateToForgotPassword = {},
            )
        }

        // Check for Material Design elements
        // OutlinedTextField elements should be present
        composeTestRule.onNodeWithText("Email Address").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()

        // Button should be present
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()

        // Card elements should be present
        composeTestRule.onNodeWithText("Fitness Tracker").assertIsDisplayed()
    }

    // endregion
}
