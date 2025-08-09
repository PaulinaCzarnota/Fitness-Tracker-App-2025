package com.example.fitnesstrackerapp.auth

import android.util.Patterns
import java.util.regex.Pattern

/**
 * Utility object for validation functions used throughout the authentication system.
 * Provides comprehensive validation for user inputs, security requirements,
 * and data integrity checks.
 */
object ValidationUtils {
    /**
     * Email validation using Android's built-in pattern matcher
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Basic password length validation
     */
    fun isValidPasswordLength(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    /**
     * Comprehensive password strength validation
     */
    fun validatePasswordStrength(password: String): ValidationResult {
        if (password.length < MIN_PASSWORD_LENGTH) {
            return ValidationResult(
                isValid = false,
                message = "Password must be at least $MIN_PASSWORD_LENGTH characters long",
            )
        }

        if (password.length > MAX_PASSWORD_LENGTH) {
            return ValidationResult(
                isValid = false,
                message = "Password must be less than $MAX_PASSWORD_LENGTH characters long",
            )
        }

        if (!password.any { it.isDigit() }) {
            return ValidationResult(
                isValid = false,
                message = "Password must contain at least one number",
            )
        }

        if (!password.any { it.isUpperCase() }) {
            return ValidationResult(
                isValid = false,
                message = "Password must contain at least one uppercase letter",
            )
        }

        if (!password.any { it.isLowerCase() }) {
            return ValidationResult(
                isValid = false,
                message = "Password must contain at least one lowercase letter",
            )
        }

        if (!password.any { SPECIAL_CHARS.contains(it) }) {
            return ValidationResult(
                isValid = false,
                message = "Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)",
            )
        }

        // Check for common weak patterns
        val lowerPassword = password.lowercase()
        WEAK_PATTERNS.forEach { weakPattern ->
            if (lowerPassword.contains(weakPattern)) {
                return ValidationResult(
                    isValid = false,
                    message = "Password contains common patterns. Please choose a more secure password",
                )
            }
        }

        // Check for repetitive characters (3+ consecutive same characters)
        if (hasRepeatingCharacters(password)) {
            return ValidationResult(
                isValid = false,
                message = "Password should not contain three or more consecutive identical characters",
            )
        }

        // Check for sequential patterns
        if (hasSequentialPattern(password)) {
            return ValidationResult(
                isValid = false,
                message = "Password should not contain sequential patterns (123, abc, etc.)",
            )
        }

        return ValidationResult(true, "Password is strong")
    }

    /**
     * Username validation with format and content checks
     */
    fun validateUsername(username: String): ValidationResult {
        if (username.isBlank()) {
            return ValidationResult(false, "Username cannot be empty")
        }

        if (username.length < MIN_USERNAME_LENGTH) {
            return ValidationResult(
                false,
                "Username must be at least $MIN_USERNAME_LENGTH characters long",
            )
        }

        if (username.length > MAX_USERNAME_LENGTH) {
            return ValidationResult(
                false,
                "Username must be less than $MAX_USERNAME_LENGTH characters long",
            )
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return ValidationResult(
                false,
                "Username can only contain letters, numbers, and underscores",
            )
        }

        // Check for reserved usernames
        if (RESERVED_USERNAMES.contains(username.lowercase())) {
            return ValidationResult(false, "This username is reserved. Please choose another")
        }

        return ValidationResult(true, "Username is valid")
    }

    /**
     * Validates full name format
     */
    fun validateFullName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(false, "Name cannot be empty")
        }

        if (name.length < MIN_NAME_LENGTH) {
            return ValidationResult(
                false,
                "Name must be at least $MIN_NAME_LENGTH characters long",
            )
        }

        if (name.length > MAX_NAME_LENGTH) {
            return ValidationResult(
                false,
                "Name must be less than $MAX_NAME_LENGTH characters long",
            )
        }

        if (!NAME_PATTERN.matcher(name).matches()) {
            return ValidationResult(
                false,
                "Name can only contain letters, spaces, hyphens, and apostrophes",
            )
        }

        return ValidationResult(true, "Name is valid")
    }

    /**
     * Validates registration form data comprehensively
     */
    fun validateRegistrationForm(
        email: String,
        username: String,
        password: String,
        confirmPassword: String,
        fullName: String,
    ): ValidationResult {
        // Validate email
        if (!isValidEmail(email)) {
            return ValidationResult(false, "Please enter a valid email address")
        }

        // Validate full name
        val nameValidation = validateFullName(fullName)
        if (!nameValidation.isValid) {
            return nameValidation
        }

        // Validate username
        val usernameValidation = validateUsername(username)
        if (!usernameValidation.isValid) {
            return usernameValidation
        }

        // Validate password
        val passwordValidation = validatePasswordStrength(password)
        if (!passwordValidation.isValid) {
            return passwordValidation
        }

        // Validate password confirmation
        if (password != confirmPassword) {
            return ValidationResult(false, "Passwords do not match")
        }

        return ValidationResult(true, "Registration form is valid")
    }

    /**
     * Validates login form data
     */
    fun validateLoginForm(email: String, password: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, "Please enter your email address")
        }

        if (!isValidEmail(email)) {
            return ValidationResult(false, "Please enter a valid email address")
        }

        if (password.isBlank()) {
            return ValidationResult(false, "Please enter your password")
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            return ValidationResult(false, "Password is too short")
        }

        return ValidationResult(true, "Login form is valid")
    }

    /**
     * Validates reset token format
     */
    fun validateResetToken(token: String): ValidationResult {
        if (token.isBlank()) {
            return ValidationResult(false, "Please enter the reset token")
        }

        if (token.length != RESET_TOKEN_LENGTH) {
            return ValidationResult(
                false,
                "Reset token must be exactly $RESET_TOKEN_LENGTH characters long",
            )
        }

        if (!RESET_TOKEN_PATTERN.matcher(token).matches()) {
            return ValidationResult(
                false,
                "Reset token can only contain letters and numbers",
            )
        }

        return ValidationResult(true, "Reset token is valid")
    }

    /**
     * Checks for security issues in user input
     */
    fun checkForSecurityIssues(input: String): ValidationResult {
        val lowerInput = input.lowercase()

        // Check for potential SQL injection patterns
        SQL_INJECTION_PATTERNS.forEach { pattern ->
            if (lowerInput.contains(pattern)) {
                return ValidationResult(false, "Input contains potentially harmful content")
            }
        }

        // Check for script injection patterns
        SCRIPT_INJECTION_PATTERNS.forEach { pattern ->
            if (lowerInput.contains(pattern)) {
                return ValidationResult(false, "Input contains potentially harmful content")
            }
        }

        return ValidationResult(true, "Input is safe")
    }

    /**
     * Checks for repetitive characters in password
     */
    private fun hasRepeatingCharacters(password: String): Boolean {
        var count = 1
        for (i in 1 until password.length) {
            if (password[i] == password[i - 1]) {
                count++
                if (count >= 3) return true
            } else {
                count = 1
            }
        }
        return false
    }

    /**
     * Checks for sequential patterns in password
     */
    private fun hasSequentialPattern(password: String): Boolean {
        val lowerPassword = password.lowercase()

        // Check for ascending sequences
        for (i in 0..lowerPassword.length - 3) {
            val char1 = lowerPassword[i]
            val char2 = lowerPassword[i + 1]
            val char3 = lowerPassword[i + 2]

            if ((char2.code == char1.code + 1) && (char3.code == char2.code + 1)) {
                return true
            }
        }

        // Check for descending sequences
        for (i in 0..lowerPassword.length - 3) {
            val char1 = lowerPassword[i]
            val char2 = lowerPassword[i + 1]
            val char3 = lowerPassword[i + 2]

            if ((char2.code == char1.code - 1) && (char3.code == char2.code - 1)) {
                return true
            }
        }

        return false
    }

    // Constants
    private const val MIN_PASSWORD_LENGTH = 8
    private const val MAX_PASSWORD_LENGTH = 128
    private const val MIN_USERNAME_LENGTH = 3
    private const val MAX_USERNAME_LENGTH = 20
    private const val MIN_NAME_LENGTH = 1
    private const val MAX_NAME_LENGTH = 50
    private const val RESET_TOKEN_LENGTH = 32

    private const val SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    private val USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$")
    private val NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'-]+$")
    private val RESET_TOKEN_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$")

    private val WEAK_PATTERNS = listOf(
        "password", "12345", "qwerty", "abc", "admin", "letmein",
        "welcome", "monkey", "dragon", "master", "login", "pass",
        "123456", "654321", "password123", "admin123",
    )

    private val RESERVED_USERNAMES = listOf(
        "admin", "root", "user", "guest", "test", "demo", "support",
        "help", "info", "contact", "mail", "email", "webmaster",
        "postmaster", "hostmaster", "system", "api", "www", "ftp",
        "null", "undefined", "anonymous",
    )

    private val SQL_INJECTION_PATTERNS = listOf(
        "select", "insert", "update", "delete", "drop", "union",
        "script", "javascript", "vbscript", "onload", "onerror",
        "'", "\"", ";", "--", "/*", "*/", "xp_", "sp_",
    )

    private val SCRIPT_INJECTION_PATTERNS = listOf(
        "<script", "</script", "javascript:", "vbscript:", "onload=",
        "onerror=", "onclick=", "onmouseover=", "onfocus=", "onblur=",
    )
}

/**
 * Data class representing validation result
 */
data class ValidationResult(
    val isValid: Boolean,
    val message: String,
)

/**
 * Extension function to validate password match
 */
fun String.matchesPassword(confirmPassword: String): ValidationResult {
    return if (this == confirmPassword) {
        ValidationResult(true, "Passwords match")
    } else {
        ValidationResult(false, "Passwords do not match")
    }
}

/**
 * Extension function for quick email validation
 */
fun String.isValidEmail(): Boolean = ValidationUtils.isValidEmail(this)

/**
 * Extension function for quick password validation
 */
fun String.isValidPassword(): ValidationResult = ValidationUtils.validatePasswordStrength(this)
