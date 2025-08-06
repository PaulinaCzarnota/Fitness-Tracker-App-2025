package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.auth.AuthService
import com.example.fitnesstrackerapp.data.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Repository for handling authentication operations.
 *
 * Responsibilities:
 * - Manage user authentication state
 * - Delegate user registration and login to AuthService
 * - Provide the current user's authentication state
 */
class AuthRepository(
    private val authService: AuthService
) {

    private val _authState = MutableStateFlow<User?>(null)
    val authState: StateFlow<User?> = _authState.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Load the current user's state on initialization
        loadCurrentUser()
    }

    /**
     * Registers a new user.
     *
     * @param email User's email address
     * @param password User's password
     * @param username Optional username
     * @return Result containing the created User or an error
     */
    suspend fun register(email: String, password: String, username: String? = null): Result<User> {
        val result = authService.registerUser(email, password, username)
        if (result.isSuccess) {
            _authState.value = result.getOrNull()
        }
        return result
    }

    /**
     * Authenticates a user.
     *
     * @param email User's email address
     * @param password User's password
     * @return Result containing the authenticated User or an error
     */
    suspend fun login(email: String, password: String): Result<User> {
        val result = authService.loginUser(email, password)
        if (result.isSuccess) {
            _authState.value = result.getOrNull()
        }
        return result
    }

    /**
     * Logs out the current user.
     */
    suspend fun logout() {
        authService.logoutUser()
        _authState.value = null
    }

    /**
     * Gets the currently authenticated user.
     *
     * @return Current user or null if not authenticated
     */
    fun getCurrentUser(): User? {
        return _authState.value
    }

    /**
     * Checks if a user is currently authenticated.
     *
     * @return true if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return _authState.value != null
    }

    /**
     * Loads the current user from the AuthService.
     */
    private fun loadCurrentUser() {
        repositoryScope.launch {
            val result = authService.getCurrentUser()
            _authState.value = result.getOrNull()
        }
    }
}
