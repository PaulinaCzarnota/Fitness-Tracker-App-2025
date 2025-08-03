/**
 * AuthService
 *
 * Purpose:
 * - Wraps Firebase Authentication operations for the FitnessTrackerApp.
 * - Provides coroutine-based suspend functions for registration, login, password reset, and session management.
 * - Designed for use with dependency injection (Hilt) in ViewModels.
 *
 * Methods:
 * - register: Register a new user with email and password.
 * - login: Login an existing user with email and password.
 * - logout: Sign out the current user.
 * - getCurrentUser: Get the currently logged-in user.
 * - sendPasswordResetEmail: Send a password reset email.
 *
 * All methods validate input and throw exceptions for invalid data.
 */

package com.example.fitnesstrackerapp.auth

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    /**
     * Registers a user using Firebase Authentication.
     *
     * @param email The user's email address (must be unique and valid).
     * @param password The user's password (minimum 6 characters).
     * @return FirebaseUser if successful, or null otherwise.
     * @throws IllegalArgumentException if email or password is empty/invalid.
     * @throws Exception if Firebase rejects the request.
     */
    suspend fun register(email: String, password: String): FirebaseUser? {
        require(email.isNotBlank()) { "Email cannot be empty" }
        require(isValidEmail(email)) { "Invalid email format" }
        require(password.isNotBlank()) { "Password cannot be empty" }
        require(password.length >= 6) { "Password must be at least 6 characters" }

        val result: AuthResult = firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .await()
        return result.user
    }

    /**
     * Logs in a user using their email and password.
     *
     * @param email User's registered email.
     * @param password User's password.
     * @return FirebaseUser if login is successful, or null.
     * @throws IllegalArgumentException if email or password is empty/invalid.
     * @throws Exception on invalid credentials or network error.
     */
    suspend fun login(email: String, password: String): FirebaseUser? {
        require(email.isNotBlank()) { "Email cannot be empty" }
        require(isValidEmail(email)) { "Invalid email format" }
        require(password.isNotBlank()) { "Password cannot be empty" }

        val result: AuthResult = firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()
        return result.user
    }

    /**
     * Logs the current user out of the app.
     *
     * This clears the Firebase session immediately.
     */
    fun logout() {
        firebaseAuth.signOut()
    }

    /**
     * Gets the currently logged-in user, if any.
     *
     * @return FirebaseUser or null if no user is logged in.
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Sends a password reset email to the given address.
     *
     * @param email The user's email address.
     * @throws IllegalArgumentException if email is empty or invalid.
     * @throws Exception if the email is unregistered or invalid.
     */
    suspend fun sendPasswordResetEmail(email: String) {
        require(email.isNotBlank()) { "Email cannot be empty" }
        require(isValidEmail(email)) { "Invalid email format" }
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    /**
     * Checks if the provided email has a valid format.
     *
     * @param email The email string to validate.
     * @return True if the email format is valid, false otherwise.
     */
    private fun isValidEmail(email: String): Boolean {
        // Simple regex for email validation
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
