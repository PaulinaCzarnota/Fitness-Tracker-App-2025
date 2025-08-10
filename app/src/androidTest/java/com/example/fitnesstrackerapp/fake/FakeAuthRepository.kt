package com.example.fitnesstrackerapp.fake

import com.example.fitnesstrackerapp.data.entity.User
import com.example.fitnesstrackerapp.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

/**
 * Fake AuthRepository for testing purposes
 * Provides a simple implementation that can be used in UI tests
 */
class FakeAuthRepository {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    /**
     * For interface compatibility: exposes the current user as authState.
     * Both properties point to the same MutableStateFlow instance.
     */
    val authState get() = currentUser

    private var shouldFailLogin = false
    private var shouldFailRegister = false

    fun setLoginFailure(shouldFail: Boolean) {
        shouldFailLogin = shouldFail
    }

    fun setRegisterFailure(shouldFail: Boolean) {
        shouldFailRegister = shouldFail
    }

    fun login(email: String, password: String, rememberMe: Boolean = false): AuthResult {
        return if (shouldFailLogin) {
            AuthResult.Error("Login failed")
        } else {
            val user = User(
                id = 1L,
                email = email,
                username = "testuser",
                passwordHash = "hash",
                passwordSalt = "salt",
                firstName = "Test",
                lastName = "User",
                registrationDate = Date(),
                isActive = true,
            )
            _currentUser.value = user
            _isAuthenticated.value = true
            AuthResult.Success("Login successful")
        }
    }

    fun register(email: String, password: String, name: String, rememberMe: Boolean = false): AuthResult {
        return if (shouldFailRegister) {
            AuthResult.Error("Registration failed")
        } else {
            val user = User(
                id = 1L,
                email = email,
                username = name,
                passwordHash = "hash",
                passwordSalt = "salt",
                firstName = name.split(" ").firstOrNull(),
                lastName = if (name.split(" ").size > 1) name.split(" ").drop(1).joinToString(" ") else null,
                registrationDate = Date(),
                isActive = true,
            )
            _currentUser.value = user
            _isAuthenticated.value = true
            AuthResult.Success("Registration successful")
        }
    }

    fun logout() {
        _currentUser.value = null
        _isAuthenticated.value = false
    }

    fun updateProfile(user: User): AuthResult {
        _currentUser.value = user
        return AuthResult.Success("Profile updated")
    }

    fun getCurrentUserId(): Long? = _currentUser.value?.id
}
