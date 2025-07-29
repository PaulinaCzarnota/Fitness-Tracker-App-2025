package com.example.fitnesstrackerapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.auth.AuthService
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UserViewModel
 *
 * ViewModel that manages user authentication and session state using FirebaseAuth.
 * Delegates operations to [AuthService] for registration, login, logout, and reset password.
 * Exposes a StateFlow<FirebaseUser?> for the UI to observe authentication state.
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    // StateFlow holding the currently authenticated user
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    init {
        // Load session if a user is already authenticated
        _currentUser.value = authService.getCurrentUser()
    }

    /**
     * Registers a user using email and password.
     * Updates [currentUser] if successful.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @param onResult Callback with success state.
     */
    fun register(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val user = authService.register(email.trim(), password.trim())
                _currentUser.value = user
                Log.i("UserViewModel", "Registration successful: ${user?.email}")
                onResult(user != null)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Registration failed", e)
                onResult(false)
            }
        }
    }

    /**
     * Logs in an existing user using email and password.
     * Updates [currentUser] on success.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @param onResult Callback with success state.
     */
    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val user = authService.login(email.trim(), password.trim())
                _currentUser.value = user
                Log.i("UserViewModel", "Login successful: ${user?.email}")
                onResult(user != null)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Login failed", e)
                onResult(false)
            }
        }
    }

    /**
     * Logs out the current user and clears session.
     * Sets [currentUser] to null.
     */
    fun logout() {
        authService.logout()
        _currentUser.value = null
        Log.i("UserViewModel", "User logged out")
    }

    /**
     * Sends a password reset email to the provided address.
     *
     * @param email The email address to send reset link.
     * @param onResult Callback with success state.
     */
    fun sendPasswordReset(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                authService.sendPasswordResetEmail(email.trim())
                Log.i("UserViewModel", "Password reset email sent to $email")
                onResult(true)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Failed to send reset email", e)
                onResult(false)
            }
        }
    }
}
