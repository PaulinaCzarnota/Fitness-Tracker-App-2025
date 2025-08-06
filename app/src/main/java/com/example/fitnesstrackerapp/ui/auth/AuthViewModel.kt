package com.example.fitnesstrackerapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.entity.User
import com.example.fitnesstrackerapp.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing authentication state and operations.
 *
 * Responsibilities:
 * - Handle user login and registration
 * - Manage authentication state across the app
 * - Provide loading and error states for UI
 * - Handle logout and session management
 */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    /**
     * Data class representing the current authentication state
     */
    data class AuthState(
        val isAuthenticated: Boolean = false,
        val user: User? = null,
        val isLoading: Boolean = false
    )

    /**
     * Data class representing the UI state for authentication screens
     */
    data class UiState(
        val error: String? = null,
        val isLoading: Boolean = false,
        val successMessage: String? = null
    )

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Initialize authentication state from repository
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                _authState.value = _authState.value.copy(
                    isAuthenticated = user != null,
                    user = user
                )
            }
        }

        // Check if user is already logged in
        checkCurrentUser()
    }

    /**
     * Attempts to log in a user with email and password.
     *
     * @param email User's email address
     * @param password User's password
     */
    fun login(email: String, password: String) {
        if (!isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid email address")
            return
        }

        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Password cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = authRepository.login(email, password)
                result.onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Login successful"
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Login failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    /**
     * Registers a new user with email and password.
     *
     * @param email User's email address
     * @param password User's password
     * @param username Optional username (defaults to email prefix)
     */
    fun register(email: String, password: String, username: String? = null) {
        if (!isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid email address")
            return
        }

        if (!isValidPassword(password)) {
            _uiState.value = _uiState.value.copy(
                error = "Password must be at least 6 characters long"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = authRepository.register(email, password, username)
                result.onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Registration successful"
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Registration failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    /**
     * Logs out the current user.
     */
    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _uiState.value = _uiState.value.copy(
                    successMessage = "Logged out successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Logout failed"
                )
            }
        }
    }

    /**
     * Clears any error or success messages from the UI state.
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    /**
     * Checks if there's a currently logged-in user.
     */
    private fun checkCurrentUser() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                _authState.value = _authState.value.copy(
                    isAuthenticated = currentUser != null,
                    user = currentUser
                )
            } catch (e: Exception) {
                // Handle error silently for initialization
                _authState.value = _authState.value.copy(
                    isAuthenticated = false,
                    user = null
                )
            }
        }
    }

    /**
     * Validates email format.
     *
     * @param email Email string to validate
     * @return true if email is valid format
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates password strength.
     *
     * @param password Password string to validate
     * @return true if password meets minimum requirements
     */
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Updates user profile information.
     *
     * @param user Updated user object
     */
    fun updateProfile(user: User) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val result = authRepository.updateUser(user)
                result.onSuccess { updatedUser ->
                    _authState.value = _authState.value.copy(user = updatedUser)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Profile updated successfully"
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Profile update failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
}
