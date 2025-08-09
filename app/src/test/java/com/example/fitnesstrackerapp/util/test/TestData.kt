package com.example.fitnesstrackerapp.util.test

/**
 * Test data constants for unit tests.
 *
 * Provides commonly used test data values to ensure consistency
 * across all test classes and avoid magic numbers/strings.
 */
object TestData {
    // Email addresses
    const val VALID_EMAIL = "test@example.com"
    const val INVALID_EMAIL = "invalid-email"
    const val ANOTHER_VALID_EMAIL = "another@test.com"

    // Passwords
    const val STRONG_PASSWORD = "StrongPassword123!"
    const val WEAK_PASSWORD = "weak"
    const val ANOTHER_STRONG_PASSWORD = "AnotherStrong456#"

    // Usernames
    const val VALID_USERNAME = "testuser"
    const val INVALID_USERNAME = "a" // Too short
    const val LONG_USERNAME = "thisusernameistoolongandexceedsthelimit"

    // Names
    const val VALID_NAME = "Test User"
    const val FIRST_NAME = "Test"
    const val LAST_NAME = "User"

    // Mock crypto data
    const val MOCK_SALT = "mocksalt12345678"
    const val MOCK_HASH = "mockhash12345678"
    const val MOCK_HEX_STRING = "6d6f636b686173683132333435363738"

    // User profile data
    const val DEFAULT_HEIGHT = 175f // cm
    const val DEFAULT_WEIGHT = 70f // kg
    const val DEFAULT_STEP_GOAL = 10000
    const val DEFAULT_CALORIE_GOAL = 2000

    // Session data
    const val DEFAULT_SESSION_TIMEOUT = 72 * 60 * 60 * 1000L // 3 days in ms
    const val EXTENDED_SESSION_TIMEOUT = 30 * 24 * 60 * 60 * 1000L // 30 days in ms

    // Database IDs
    const val DEFAULT_USER_ID = 1L
    const val ANOTHER_USER_ID = 2L
    const val NON_EXISTENT_USER_ID = 999L

    // Error messages
    const val GENERIC_ERROR_MESSAGE = "An error occurred"
    const val NETWORK_ERROR_MESSAGE = "Network connection failed"
    const val DATABASE_ERROR_MESSAGE = "Database operation failed"
    const val VALIDATION_ERROR_MESSAGE = "Validation failed"

    // Success messages
    const val LOGIN_SUCCESS_MESSAGE = "Login successful"
    const val REGISTRATION_SUCCESS_MESSAGE = "Registration successful"
    const val LOGOUT_SUCCESS_MESSAGE = "Logout successful"
    const val SESSION_RESTORED_MESSAGE = "Session restored"

    // Common test values
    const val EMPTY_STRING = ""
    const val WHITESPACE_STRING = "   "
    const val SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    // Age and date values
    const val VALID_AGE = 25
    const val MIN_AGE = 13
    const val MAX_AGE = 120

    // Failed login attempts
    const val MAX_FAILED_ATTEMPTS = 5
    const val SAFE_FAILED_ATTEMPTS = 2
    const val LOCKOUT_FAILED_ATTEMPTS = 5
}
