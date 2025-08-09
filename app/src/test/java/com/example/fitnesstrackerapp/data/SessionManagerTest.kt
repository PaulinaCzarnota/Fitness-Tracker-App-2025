package com.example.fitnesstrackerapp.data

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.example.fitnesstrackerapp.auth.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for SessionManager to verify API surface and functionality.
 * These tests lock the public API surface and ensure consistent session management behavior.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    private lateinit var context: Context
    private lateinit var sessionManager: SessionManager
    
    @Mock
    private lateinit var mockPreferences: SharedPreferences
    
    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setUp() {
        Mockito.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
        sessionManager = SessionManager(context)
    }

    @Test
    fun `login sets user session correctly`() = runTest {
        // Given
        val userId = 123L
        val username = "testuser"
        val isFirstLogin = true

        // When
        sessionManager.login(userId, username, isFirstLogin)

        // Then
        assertTrue("User should be logged in", sessionManager.isLoggedIn())
        assertEquals("User ID should match", userId, sessionManager.getCurrentUserId())
        assertEquals("Username should match", username, sessionManager.getCurrentUsername())
        assertTrue("Should be first login", sessionManager.isFirstLogin())
    }

    @Test
    fun `logout clears user session`() = runTest {
        // Given - Login first
        sessionManager.login(123L, "testuser", false)
        assertTrue("User should be logged in initially", sessionManager.isLoggedIn())

        // When
        sessionManager.logout()

        // Then
        assertFalse("User should be logged out", sessionManager.isLoggedIn())
        assertNull("User ID should be null", sessionManager.getCurrentUserId())
        assertNull("Username should be null", sessionManager.getCurrentUsername())
        assertFalse("Should not be first login after logout", sessionManager.isFirstLogin())
    }

    @Test
    fun `isLoggedIn returns false initially`() {
        // When/Then
        assertFalse("User should not be logged in initially", sessionManager.isLoggedIn())
    }

    @Test
    fun `getCurrentUserId returns null when not logged in`() {
        // When/Then
        assertNull("User ID should be null when not logged in", sessionManager.getCurrentUserId())
    }

    @Test
    fun `getCurrentUsername returns null when not logged in`() {
        // When/Then
        assertNull("Username should be null when not logged in", sessionManager.getCurrentUsername())
    }

    @Test
    fun `isFirstLogin returns false initially`() {
        // When/Then
        assertFalse("Should not be first login initially", sessionManager.isFirstLogin())
    }

    @Test
    fun `setFirstLoginCompleted updates first login status`() = runTest {
        // Given
        sessionManager.login(123L, "testuser", true)
        assertTrue("Should be first login initially", sessionManager.isFirstLogin())

        // When
        sessionManager.setFirstLoginCompleted()

        // Then
        assertFalse("Should no longer be first login", sessionManager.isFirstLogin())
    }

    @Test
    fun `isLoggedInFlow emits correct values`() = runTest {
        // Given
        val initialState = sessionManager.isLoggedInFlow.first()
        assertFalse("Initial flow state should be false", initialState)

        // When - Login
        sessionManager.login(123L, "testuser", false)
        val loggedInState = sessionManager.isLoggedInFlow.first()

        // Then
        assertTrue("Flow should emit true after login", loggedInState)

        // When - Logout
        sessionManager.logout()
        val loggedOutState = sessionManager.isLoggedInFlow.first()

        // Then
        assertFalse("Flow should emit false after logout", loggedOutState)
    }

    @Test
    fun `updateLastActivityTime updates timestamp`() = runTest {
        // Given
        val initialTime = sessionManager.getLastActivityTime()

        // When
        Thread.sleep(10) // Small delay to ensure different timestamp
        sessionManager.updateLastActivityTime()

        // Then
        val updatedTime = sessionManager.getLastActivityTime()
        assertTrue("Last activity time should be updated", updatedTime > initialTime)
    }

    @Test
    fun `getLastActivityTime returns valid timestamp`() {
        // When
        val timestamp = sessionManager.getLastActivityTime()

        // Then
        assertTrue("Timestamp should be positive", timestamp > 0)
        assertTrue("Timestamp should be reasonable (within last hour)", 
                  System.currentTimeMillis() - timestamp < 3600000)
    }

    @Test
    fun `isSessionExpired returns false for recent activity`() = runTest {
        // Given
        sessionManager.login(123L, "testuser", false)
        sessionManager.updateLastActivityTime()

        // When
        val isExpired = sessionManager.isSessionExpired()

        // Then
        assertFalse("Session should not be expired with recent activity", isExpired)
    }

    @Test
    fun `isSessionExpired returns true when not logged in`() {
        // When
        val isExpired = sessionManager.isSessionExpired()

        // Then
        assertTrue("Session should be expired when not logged in", isExpired)
    }

    @Test
    fun `clearSession clears all session data`() = runTest {
        // Given
        sessionManager.login(123L, "testuser", true)
        sessionManager.updateLastActivityTime()

        // When
        sessionManager.clearSession()

        // Then
        assertFalse("Should not be logged in", sessionManager.isLoggedIn())
        assertNull("User ID should be null", sessionManager.getCurrentUserId())
        assertNull("Username should be null", sessionManager.getCurrentUsername())
        assertFalse("Should not be first login", sessionManager.isFirstLogin())
    }

    @Test
    fun `getSessionDuration returns correct duration`() = runTest {
        // Given
        sessionManager.login(123L, "testuser", false)
        System.currentTimeMillis()
        Thread.sleep(100) // Small delay

        // When
        val duration = sessionManager.getSessionDuration()

        // Then
        assertTrue("Duration should be positive", duration > 0)
        assertTrue("Duration should be reasonable", duration < 1000) // Less than 1 second
    }

    @Test
    fun `getSessionDuration returns 0 when not logged in`() {
        // When
        val duration = sessionManager.getSessionDuration()

        // Then
        assertEquals("Duration should be 0 when not logged in", 0L, duration)
    }

    @Test
    fun `refreshSession updates activity time`() = runTest {
        // Given
        sessionManager.login(123L, "testuser", false)
        val initialTime = sessionManager.getLastActivityTime()
        Thread.sleep(10)

        // When
        sessionManager.refreshSession()

        // Then
        val updatedTime = sessionManager.getLastActivityTime()
        assertTrue("Activity time should be updated", updatedTime > initialTime)
    }

    // API Surface Tests - Ensure methods exist and have correct signatures
    @Test
    fun `SessionManager API surface test`() {
        // Test that all expected public methods exist with correct signatures
        try {
            // Session management
            sessionManager.login(1L, "user", true)
            sessionManager.logout()
            sessionManager.isLoggedIn()
            sessionManager.getCurrentUserId()
            sessionManager.getCurrentUsername()
            sessionManager.isFirstLogin()
            sessionManager.setFirstLoginCompleted()
            sessionManager.clearSession()
            
            // Activity tracking
            sessionManager.updateLastActivityTime()
            sessionManager.getLastActivityTime()
            sessionManager.isSessionExpired()
            sessionManager.getSessionDuration()
            sessionManager.refreshSession()
            
            // Reactive flow
            sessionManager.isLoggedInFlow
            
            // Success - API surface is stable
            assertTrue(true)
        } catch (e: NoSuchMethodError) {
            fail("API surface has changed: ${e.message}")
        }
    }

    @Test
    fun `multiple login calls update session correctly`() = runTest {
        // Given
        sessionManager.login(123L, "user1", false)
        assertEquals("user1", sessionManager.getCurrentUsername())

        // When
        sessionManager.login(456L, "user2", true)

        // Then
        assertEquals("Should update to new user ID", 456L, sessionManager.getCurrentUserId())
        assertEquals("Should update to new username", "user2", sessionManager.getCurrentUsername())
        assertTrue("Should be first login for new user", sessionManager.isFirstLogin())
    }

    @Test
    fun `login with same user updates first login status`() = runTest {
        // Given
        sessionManager.login(123L, "testuser", false)
        assertFalse("Should not be first login initially", sessionManager.isFirstLogin())

        // When
        sessionManager.login(123L, "testuser", true)

        // Then
        assertTrue("Should update first login status", sessionManager.isFirstLogin())
    }

    @Test
    fun `isLoggedInFlow is cold flow that remembers state`() = runTest {
        // Given
        sessionManager.login(123L, "testuser", false)

        // When - Multiple subscribers
        val state1 = sessionManager.isLoggedInFlow.first()
        val state2 = sessionManager.isLoggedInFlow.first()

        // Then
        assertTrue("First subscriber should get current state", state1)
        assertTrue("Second subscriber should get same state", state2)
    }
}
