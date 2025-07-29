package com.example.fitnesstrackerapp.auth

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthService
 *
 * A singleton service that wraps Firebase Authentication operations.
 * Provides coroutine-based suspend functions for:
 * - User registration and login
 * - Password reset
 * - Session state checks and logout
 *
 * Designed for use with dependency injection (Hilt) in ViewModels.
 */
@Singleton
class AuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    /**
     * Registers a user using Firebase Authentication.
     *
     * @param email The user's email address (must be unique).
     * @param password The user's password (minimum 6 characters).
     * @return FirebaseUser if successful, or null otherwise.
     * @throws Exception if Firebase rejects the request.
     */
    suspend fun register(email: String, password: String): FirebaseUser? {
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
     * @throws Exception on invalid credentials or network error.
     */
    suspend fun login(email: String, password: String): FirebaseUser? {
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
     * @throws Exception if the email is unregistered or invalid.
     */
    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }
}
