package com.example.fitnesstrackerapp.di

import com.example.fitnesstrackerapp.auth.AuthService
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * FirebaseModule
 *
 * Dagger Hilt module to provide Firebase-related dependencies.
 * This module is installed into the SingletonComponent scope,
 * meaning all provided instances are shared across the app.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /**
     * Provides a singleton instance of [FirebaseAuth].
     *
     * This is required for Firebase-based authentication features such as:
     * - User registration/login
     * - Session persistence
     * - Account management
     *
     * @return Singleton FirebaseAuth instance
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    /**
     * Provides a singleton instance of [AuthService] which wraps FirebaseAuth operations.
     *
     * This service allows abstraction and unit testing by hiding direct
     * FirebaseAuth access behind a clean interface.
     *
     * @param firebaseAuth FirebaseAuth injected by Hilt
     * @return Singleton AuthService instance
     */
    @Provides
    @Singleton
    fun provideAuthService(firebaseAuth: FirebaseAuth): AuthService {
        return AuthService(firebaseAuth)
    }

    // ───────────────────────────────────────────────────────────
    // Optional: Add more Firebase services when needed below
    // Uncomment to use Firestore, Storage, Cloud Messaging, etc.
    // ───────────────────────────────────────────────────────────

    /*
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    */

}
