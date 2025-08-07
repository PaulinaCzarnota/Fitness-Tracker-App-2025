/**
 * Sealed class representing authentication operation results.
 *
 * Provides type-safe handling of authentication success and error states
 * throughout the application.
 */

package com.example.fitnesstrackerapp.repository

/**
 * Sealed class for authentication results.
 */
sealed class AuthResult {
    data class Success(val message: String = "Authentication successful") : AuthResult()
    data class Error(val message: String) : AuthResult()
}
