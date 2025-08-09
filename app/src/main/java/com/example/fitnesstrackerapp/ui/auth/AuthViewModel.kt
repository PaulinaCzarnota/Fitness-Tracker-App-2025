/**
 * ViewModel class for managing authentication UI state and business logic.
 *
 * This ViewModel handles:
 * - User login and registration operations
 * - Authentication state management
 * - Password validation and security
 * - Profile updates and password changes
 * - Error and success message handling
 *
 * @param authRepository Repository for authentication data operations
 */

package com.example.fitnesstrackerapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.auth.BiometricAuthManager
import com.example.fitnesstrackerapp.data.entity.User
import com.example.fitnesstrackerapp.repository.AuthRepository
import com.example.fitnesstrackerapp.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * ViewModel for managing authentication state and operations.
 *
 * @property authRepository The repository for handling authentication data.
 */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    /**
     * Represents the UI state for the authentication screens.
     *
     * @property isAuthenticated Whether the user is currently authenticated.
     * @property user The currently logged-in user, if any.
     * @property isLoading Whether an authentication operation is in progress.
     * @property error An error message to be displayed, if any.
     * @property successMessage A success message to be displayed, if any.
     */
    data class AuthUiState(
        val isAuthenticated: Boolean = false,
        val user: User? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null,
    )

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Observe authentication status from the repository
        viewModelScope.launch {
            authRepository.isAuthenticated.collect { isAuthenticated ->
                _uiState.value = _uiState.value.copy(isAuthenticated = isAuthenticated)
            }
        }
        // Observe user data from the repository
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(user = user)
            }
        }
    }

    /**
     * Logs in a user with the provided email and password.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @param rememberMe Whether to remember the user for automatic login.
     */
    fun login(email: String, password: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.login(email, password, rememberMe)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        successMessage = result.message,
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                }
            }
        }
    }

    /**
     * Registers a new user with the provided details.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @param name The user's full name.
     * @param rememberMe Whether to remember the user for automatic login.
     */
    fun register(email: String, password: String, name: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.register(email, password, name, rememberMe)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        successMessage = result.message,
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                }
            }
        }
    }

    /**
     * Logs out the current user and resets the UI state.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState() // Reset to initial state
        }
    }

    /**
     * Clears any displayed error or success messages from the UI state.
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }

    /**
     * Clears error message only
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Sets an error message to be displayed in the UI
     */
    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }

    /**
     * Updates user profile information
     */
    fun updateProfile(user: User) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = authRepository.updateProfile(user)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.message,
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                }
            }
        }
    }

    /**
     * Changes user password using repository method
     */
    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Validate password strength first
            val validation = validatePasswordStrength(newPassword)
            if (!validation.isValid) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = validation.message,
                )
                return@launch
            }

            when (val result = authRepository.changePassword(currentPassword, newPassword)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.message,
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                }
            }
        }
    }

    /**
     * Initiates password reset process
     */
    fun initiatePasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            if (!isValidEmail(email)) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Please enter a valid email address",
                )
                return@launch
            }

            when (val result = authRepository.initiatePasswordReset(email)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.message,
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                }
            }
        }
    }

    /**
     * Resets password using reset token
     */
    fun resetPassword(email: String, resetToken: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Validate inputs
            if (!isValidEmail(email)) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Please enter a valid email address",
                )
                return@launch
            }

            if (resetToken.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Please enter the reset token",
                )
                return@launch
            }

            val validation = validatePasswordStrength(newPassword)
            if (!validation.isValid) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = validation.message,
                )
                return@launch
            }

            when (val result = authRepository.resetPassword(email, resetToken, newPassword)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.message,
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                }
            }
        }
    }

    /**
     * Attempts to restore user session on app start
     */
    fun restoreSession() {
        viewModelScope.launch {
            when (authRepository.restoreSession()) {
                is AuthResult.Success -> {
                    // Session restored successfully, user state updated by repository
                }
                is AuthResult.Error -> {
                    // Session restore failed, user remains unauthenticated
                    // Don't show error to user for session restore failures
                }
            }
        }
    }

    /**
     * Validates email format
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates password strength
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6 // Minimum 6 characters
    }

    /**
     * Comprehensive password strength validation
     */
    fun validatePasswordStrength(password: String): ValidationResult {
        if (password.length < 8) {
            return ValidationResult(false, "Password must be at least 8 characters long")
        }

        if (!password.any { it.isDigit() }) {
            return ValidationResult(false, "Password must contain at least one number")
        }

        if (!password.any { it.isUpperCase() }) {
            return ValidationResult(false, "Password must contain at least one uppercase letter")
        }

        if (!password.any { it.isLowerCase() }) {
            return ValidationResult(false, "Password must contain at least one lowercase letter")
        }

        val specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        if (!password.any { specialChars.contains(it) }) {
            return ValidationResult(false, "Password must contain at least one special character")
        }

        // Check for common weak patterns
        val weakPatterns = listOf(
            "12345", "password", "qwerty", "abc", "admin",
            "letmein", "welcome", "monkey", "dragon",
        )

        val lowerPassword = password.lowercase()
        if (weakPatterns.any { lowerPassword.contains(it) }) {
            return ValidationResult(false, "Password contains common patterns. Please choose a more secure password")
        }

        return ValidationResult(true, "Password is strong")
    }

    /**
     * Validates username format and requirements
     */
    fun validateUsername(username: String): ValidationResult {
        if (username.isBlank()) {
            return ValidationResult(false, "Username cannot be empty")
        }

        if (username.length < 3) {
            return ValidationResult(false, "Username must be at least 3 characters long")
        }

        if (username.length > 20) {
            return ValidationResult(false, "Username must be less than 20 characters")
        }

        val validUsernamePattern = Pattern.compile("^[a-zA-Z0-9_]+$")
        if (!validUsernamePattern.matcher(username).matches()) {
            return ValidationResult(false, "Username can only contain letters, numbers, and underscores")
        }

        return ValidationResult(true, "Username is valid")
    }

    /**
     * Validates registration form
     */
    fun validateRegistrationForm(
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
    ): ValidationResult {
        // Validate email
        if (!isValidEmail(email)) {
            return ValidationResult(false, "Please enter a valid email address")
        }

        // Validate username
        val usernameValidation = validateUsername(username)
        if (!usernameValidation.isValid) {
            return usernameValidation
        }

        // Validate password
        val passwordValidation = validatePasswordStrength(password)
        if (!passwordValidation.isValid) {
            return passwordValidation
        }

        // Validate password confirmation
        if (password != confirmPassword) {
            return ValidationResult(false, "Passwords do not match")
        }

        return ValidationResult(true, "Registration form is valid")
    }

    /**
     * Validates login form
     */
    fun validateLoginForm(email: String, password: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, "Please enter your email address")
        }

        if (!isValidEmail(email)) {
            return ValidationResult(false, "Please enter a valid email address")
        }

        if (password.isBlank()) {
            return ValidationResult(false, "Please enter your password")
        }

        if (password.length < 6) {
            return ValidationResult(false, "Password is too short")
        }

        return ValidationResult(true, "Login form is valid")
    }

    /**
     * Authenticates using biometric authentication
     */
    fun authenticateWithBiometrics(
        biometricAuthManager: BiometricAuthManager,
        activity: androidx.fragment.app.FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (biometricAuthManager.isBiometricEnabled()) {
            biometricAuthManager.authenticate(
                activity = activity,
                title = "Biometric Authentication",
                subtitle = "Use your fingerprint or face to unlock",
                onSuccess = {
                    // Try to restore session for biometric login
                    restoreSession()
                    onSuccess()
                },
                onError = onError,
            )
        } else {
            onError("Biometric authentication is not enabled or available")
        }
    }

    /**
     * Data class for validation results
     */
    data class ValidationResult(
        val isValid: Boolean,
        val message: String,
    )
}
