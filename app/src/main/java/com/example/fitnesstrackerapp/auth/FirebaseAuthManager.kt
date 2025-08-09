/**
 * Firebase Authentication Manager for the Fitness Tracker App
 *
 * This class provides a comprehensive Firebase authentication implementation with:
 * - Email/password authentication with validation
 * - Google Sign-In integration
 * - Password reset functionality
 * - User profile management
 * - Account linking and verification
 * - Security features and error handling
 *
 * Integrates with the existing local authentication system for offline support.
 */

package com.example.fitnesstrackerapp.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.fitnesstrackerapp.data.dao.UserDao
import com.example.fitnesstrackerapp.data.entity.User
import com.example.fitnesstrackerapp.repository.AuthResult
import com.example.fitnesstrackerapp.security.CryptoManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Firebase Authentication Manager
 *
 * Provides Firebase-based authentication with local database synchronization
 * and comprehensive security features.
 */
class FirebaseAuthManager(
    private val context: Context,
    private val userDao: UserDao,
    private val cryptoManager: CryptoManager,
    private val sessionManager: SessionManager,
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient

    private val _authState = MutableStateFlow<FirebaseAuthState>(FirebaseAuthState.Unauthenticated)
    val authState: StateFlow<FirebaseAuthState> = _authState.asStateFlow()

    private val _currentFirebaseUser = MutableStateFlow<FirebaseUser?>(null)
    val currentFirebaseUser: StateFlow<FirebaseUser?> = _currentFirebaseUser.asStateFlow()

    companion object {
        private const val TAG = "FirebaseAuthManager"
        private const val RC_SIGN_IN = 9001
    }

    init {
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("123456789012-abcdefghijklmnopqrstuvwxyz123456.apps.googleusercontent.com") // Replace with actual client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)

        // Set up authentication state listener
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentFirebaseUser.value = user

            if (user != null) {
                _authState.value = FirebaseAuthState.Authenticated(user)
                Log.d(TAG, "User authenticated: ${user.email}")
            } else {
                _authState.value = FirebaseAuthState.Unauthenticated
                Log.d(TAG, "User unauthenticated")
            }
        }
    }

    /**
     * Registers a new user with Firebase Authentication
     */
    suspend fun registerWithFirebase(
        email: String,
        password: String,
        fullName: String,
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            _authState.value = FirebaseAuthState.Loading

            // Validate input
            if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                _authState.value = FirebaseAuthState.Error("All fields are required")
                return@withContext AuthResult.Error("All fields are required")
            }

            // Create user with Firebase
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Update Firebase profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName.trim())
                    .build()

                firebaseUser.updateProfile(profileUpdates).await()

                // Send email verification
                firebaseUser.sendEmailVerification().await()

                // Create local user record
                val localUser = createLocalUserFromFirebase(firebaseUser, fullName, password)
                val userId = userDao.insertUser(localUser)
                val createdUser = localUser.copy(id = userId)

                // Save session
                sessionManager.saveUserSession(createdUser, false)

                _authState.value = FirebaseAuthState.Authenticated(firebaseUser)
                AuthResult.Success("Registration successful. Please verify your email.")
            } else {
                _authState.value = FirebaseAuthState.Error("Registration failed")
                AuthResult.Error("Registration failed")
            }
        } catch (e: FirebaseAuthException) {
            val errorMessage = handleFirebaseAuthException(e)
            _authState.value = FirebaseAuthState.Error(errorMessage)
            Log.e(TAG, "Firebase registration failed", e)
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            val errorMessage = "Registration failed: ${e.message}"
            _authState.value = FirebaseAuthState.Error(errorMessage)
            Log.e(TAG, "Registration error", e)
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Signs in user with Firebase Authentication
     */
    suspend fun signInWithFirebase(
        email: String,
        password: String,
        rememberMe: Boolean = false,
    ): AuthResult = withContext(Dispatchers.IO) {
        try {
            _authState.value = FirebaseAuthState.Loading

            if (email.isBlank() || password.isBlank()) {
                _authState.value = FirebaseAuthState.Error("Email and password are required")
                return@withContext AuthResult.Error("Email and password are required")
            }

            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Check if email is verified
                if (!firebaseUser.isEmailVerified) {
                    _authState.value = FirebaseAuthState.EmailNotVerified(firebaseUser)
                    return@withContext AuthResult.Error("Please verify your email address before signing in")
                }

                // Sync with local database
                val localUser = syncFirebaseUserToLocal(firebaseUser)
                if (localUser != null) {
                    // Update last login
                    val updatedUser = localUser.copy(lastLogin = Date())
                    userDao.updateUser(updatedUser)

                    // Save session
                    sessionManager.saveUserSession(updatedUser, rememberMe)

                    _authState.value = FirebaseAuthState.Authenticated(firebaseUser)
                    AuthResult.Success("Login successful")
                } else {
                    _authState.value = FirebaseAuthState.Error("Failed to sync user data")
                    AuthResult.Error("Failed to sync user data")
                }
            } else {
                _authState.value = FirebaseAuthState.Error("Sign in failed")
                AuthResult.Error("Sign in failed")
            }
        } catch (e: FirebaseAuthException) {
            val errorMessage = handleFirebaseAuthException(e)
            _authState.value = FirebaseAuthState.Error(errorMessage)
            Log.e(TAG, "Firebase sign in failed", e)
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            val errorMessage = "Sign in failed: ${e.message}"
            _authState.value = FirebaseAuthState.Error(errorMessage)
            Log.e(TAG, "Sign in error", e)
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Signs in with Google using Firebase Authentication
     */
    suspend fun signInWithGoogle(activity: Activity): AuthResult = withContext(Dispatchers.Main) {
        try {
            _authState.value = FirebaseAuthState.Loading

            // Sign out any existing Google account first
            googleSignInClient.signOut().await()

            // Get Google Sign-In intent
            googleSignInClient.signInIntent

            // Note: In a real implementation, you would launch this intent
            // and handle the result in onActivityResult
            // For now, we'll simulate the flow

            return@withContext AuthResult.Error("Google Sign-In requires activity result handling")
        } catch (e: Exception) {
            val errorMessage = "Google Sign-In failed: ${e.message}"
            _authState.value = FirebaseAuthState.Error(errorMessage)
            Log.e(TAG, "Google sign in error", e)
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Handles Google Sign-In result (to be called from activity result)
     */
    suspend fun handleGoogleSignInResult(account: GoogleSignInAccount?): AuthResult = withContext(Dispatchers.IO) {
        try {
            if (account == null) {
                _authState.value = FirebaseAuthState.Error("Google Sign-In was cancelled")
                return@withContext AuthResult.Error("Google Sign-In was cancelled")
            }

            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Check if this is a new user
                val isNewUser = result.additionalUserInfo?.isNewUser ?: false

                if (isNewUser) {
                    // Create local user record for new Google user
                    val localUser = createLocalUserFromFirebase(
                        firebaseUser,
                        account.displayName ?: "Google User",
                        null, // No password for Google sign-in
                    )
                    val userId = userDao.insertUser(localUser)
                    val createdUser = localUser.copy(id = userId)
                    sessionManager.saveUserSession(createdUser, false)
                } else {
                    // Sync existing user
                    syncFirebaseUserToLocal(firebaseUser)?.let { localUser ->
                        val updatedUser = localUser.copy(lastLogin = Date())
                        userDao.updateUser(updatedUser)
                        sessionManager.saveUserSession(updatedUser, false)
                    }
                }

                _authState.value = FirebaseAuthState.Authenticated(firebaseUser)
                AuthResult.Success("Google Sign-In successful")
            } else {
                _authState.value = FirebaseAuthState.Error("Google Sign-In failed")
                AuthResult.Error("Google Sign-In failed")
            }
        } catch (e: FirebaseAuthException) {
            val errorMessage = handleFirebaseAuthException(e)
            _authState.value = FirebaseAuthState.Error(errorMessage)
            Log.e(TAG, "Google Sign-In with Firebase failed", e)
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            val errorMessage = "Google Sign-In failed: ${e.message}"
            _authState.value = FirebaseAuthState.Error(errorMessage)
            Log.e(TAG, "Google Sign-In error", e)
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Sends password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            _authState.value = FirebaseAuthState.Loading

            auth.sendPasswordResetEmail(email.trim()).await()

            _authState.value = FirebaseAuthState.Unauthenticated
            AuthResult.Success("Password reset email sent. Please check your inbox.")
        } catch (e: FirebaseAuthException) {
            val errorMessage = handleFirebaseAuthException(e)
            _authState.value = FirebaseAuthState.Error(errorMessage)
            Log.e(TAG, "Password reset failed", e)
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            val errorMessage = "Password reset failed: ${e.message}"
            _authState.value = FirebaseAuthState.Error(errorMessage)
            Log.e(TAG, "Password reset error", e)
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Changes user password in Firebase
     */
    suspend fun changePassword(newPassword: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                return@withContext AuthResult.Error("User not authenticated")
            }

            firebaseUser.updatePassword(newPassword).await()

            // Update local user record if needed
            syncFirebaseUserToLocal(firebaseUser)

            AuthResult.Success("Password updated successfully")
        } catch (e: FirebaseAuthException) {
            val errorMessage = handleFirebaseAuthException(e)
            Log.e(TAG, "Password change failed", e)
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            val errorMessage = "Password change failed: ${e.message}"
            Log.e(TAG, "Password change error", e)
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Re-authenticates user with their current credentials
     */
    suspend fun reauthenticateUser(currentPassword: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null || firebaseUser.email == null) {
                return@withContext AuthResult.Error("User not authenticated")
            }

            val credential = EmailAuthProvider.getCredential(firebaseUser.email!!, currentPassword)
            firebaseUser.reauthenticate(credential).await()

            AuthResult.Success("Re-authentication successful")
        } catch (e: FirebaseAuthException) {
            val errorMessage = handleFirebaseAuthException(e)
            Log.e(TAG, "Re-authentication failed", e)
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            val errorMessage = "Re-authentication failed: ${e.message}"
            Log.e(TAG, "Re-authentication error", e)
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Resends email verification
     */
    suspend fun resendEmailVerification(): AuthResult = withContext(Dispatchers.IO) {
        try {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                return@withContext AuthResult.Error("User not authenticated")
            }

            if (firebaseUser.isEmailVerified) {
                return@withContext AuthResult.Success("Email is already verified")
            }

            firebaseUser.sendEmailVerification().await()
            AuthResult.Success("Verification email sent")
        } catch (e: FirebaseAuthException) {
            val errorMessage = handleFirebaseAuthException(e)
            Log.e(TAG, "Email verification failed", e)
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            val errorMessage = "Email verification failed: ${e.message}"
            Log.e(TAG, "Email verification error", e)
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Signs out user from Firebase and clears session
     */
    suspend fun signOut(): AuthResult = withContext(Dispatchers.IO) {
        try {
            // Sign out from Firebase
            auth.signOut()

            // Sign out from Google
            googleSignInClient.signOut().await()

            // Clear local session
            sessionManager.clearUserSession()

            _authState.value = FirebaseAuthState.Unauthenticated
            AuthResult.Success("Signed out successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error", e)
            AuthResult.Error("Sign out failed: ${e.message}")
        }
    }

    /**
     * Deletes user account from Firebase and local database
     */
    suspend fun deleteAccount(currentPassword: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                return@withContext AuthResult.Error("User not authenticated")
            }

            // Re-authenticate before deletion
            val reauthResult = reauthenticateUser(currentPassword)
            if (reauthResult !is AuthResult.Success) {
                return@withContext reauthResult
            }

            // Delete from local database first
            firebaseUser.email?.let { email ->
                userDao.getUserByEmail(email)?.let { user ->
                    userDao.deleteUser(user)
                }
            }

            // Delete from Firebase
            firebaseUser.delete().await()

            // Clear session
            sessionManager.clearUserSession()

            _authState.value = FirebaseAuthState.Unauthenticated
            AuthResult.Success("Account deleted successfully")
        } catch (e: FirebaseAuthException) {
            val errorMessage = handleFirebaseAuthException(e)
            Log.e(TAG, "Account deletion failed", e)
            AuthResult.Error(errorMessage)
        } catch (e: Exception) {
            val errorMessage = "Account deletion failed: ${e.message}"
            Log.e(TAG, "Account deletion error", e)
            AuthResult.Error(errorMessage)
        }
    }

    /**
     * Creates a local user record from Firebase user
     */
    private suspend fun createLocalUserFromFirebase(
        firebaseUser: FirebaseUser,
        fullName: String,
        password: String?,
    ): User {
        val nameParts = fullName.split(" ")
        val firstName = nameParts.firstOrNull()
        val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else null

        // For Google sign-in users, we don't store a password
        val (passwordHash, passwordSalt) = if (password != null) {
            val salt = cryptoManager.generateSalt()
            val hash = cryptoManager.hashPassword(password, salt)
            Pair(cryptoManager.bytesToHex(hash), cryptoManager.bytesToHex(salt))
        } else {
            // For OAuth users, use empty hash and salt
            Pair("", "")
        }

        return User(
            email = firebaseUser.email?.lowercase() ?: "",
            username = fullName,
            passwordHash = passwordHash,
            passwordSalt = passwordSalt,
            firstName = firstName,
            lastName = lastName,
            registrationDate = Date(),
            lastLogin = Date(),
            isActive = true,
            isAccountLocked = false,
            failedLoginAttempts = 0,
            isEmailVerified = firebaseUser.isEmailVerified,
            firebaseUid = firebaseUser.uid,
        )
    }

    /**
     * Syncs Firebase user data with local database
     */
    private suspend fun syncFirebaseUserToLocal(firebaseUser: FirebaseUser): User? {
        return try {
            val email = firebaseUser.email?.lowercase() ?: return null
            var localUser = userDao.getUserByEmail(email)

            if (localUser == null) {
                // Create new local user if not exists
                val newUser = createLocalUserFromFirebase(
                    firebaseUser,
                    firebaseUser.displayName ?: "Firebase User",
                    null,
                )
                val userId = userDao.insertUser(newUser)
                newUser.copy(id = userId)
            } else {
                // Update existing local user with Firebase data
                val updatedUser = localUser.copy(
                    isEmailVerified = firebaseUser.isEmailVerified,
                    firebaseUid = firebaseUser.uid,
                    lastLogin = Date(),
                )
                userDao.updateUser(updatedUser)
                updatedUser
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync Firebase user to local", e)
            null
        }
    }

    /**
     * Handles Firebase Auth exceptions and returns user-friendly messages
     */
    private fun handleFirebaseAuthException(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Invalid email address"
            "ERROR_WRONG_PASSWORD" -> "Invalid email or password"
            "ERROR_USER_NOT_FOUND" -> "Invalid email or password"
            "ERROR_USER_DISABLED" -> "This account has been disabled"
            "ERROR_TOO_MANY_REQUESTS" -> "Too many failed attempts. Please try again later"
            "ERROR_OPERATION_NOT_ALLOWED" -> "This sign-in method is not enabled"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists"
            "ERROR_WEAK_PASSWORD" -> "Password is too weak"
            "ERROR_REQUIRES_RECENT_LOGIN" -> "Please sign in again to perform this action"
            "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection"
            "ERROR_USER_TOKEN_EXPIRED" -> "Your session has expired. Please sign in again"
            else -> "Authentication failed: ${exception.message}"
        }
    }

    /**
     * Checks if user is currently authenticated
     */
    fun isAuthenticated(): Boolean {
        return auth.currentUser != null && _authState.value is FirebaseAuthState.Authenticated
    }

    /**
     * Gets the current Firebase user
     */
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }
}

/**
 * Sealed class representing Firebase authentication states
 */
sealed class FirebaseAuthState {
    object Unauthenticated : FirebaseAuthState()
    object Loading : FirebaseAuthState()
    data class Authenticated(val user: FirebaseUser) : FirebaseAuthState()
    data class EmailNotVerified(val user: FirebaseUser) : FirebaseAuthState()
    data class Error(val message: String) : FirebaseAuthState()
}
