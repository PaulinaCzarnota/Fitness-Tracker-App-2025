package com.example.fitnesstrackerapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.entity.User
import com.example.fitnesstrackerapp.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    data class AuthState(
        val isAuthenticated: Boolean = false,
        val user: User? = null,
        val error: String? = null,
        val isLoading: Boolean = false
    )

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                _authState.value = AuthState(isAuthenticated = user != null, user = user)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            val result = authRepository.login(email, password)
            result.onFailure {
                _authState.value = _authState.value.copy(isLoading = false, error = it.message)
            }
            // On success, the authState will be updated by the collector in init
            _authState.value = _authState.value.copy(isLoading = false)
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            val result = authRepository.register(email, password)
            result.onFailure {
                _authState.value = _authState.value.copy(isLoading = false, error = it.message)
            }
            // On success, the authState will be updated by the collector in init
            _authState.value = _authState.value.copy(isLoading = false)
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
