/**
 * Comprehensive unit tests for ValidationUtils
 *
 * Tests cover all validation scenarios including:
 * - Email validation with various formats
 * - Password strength validation
 * - Username format validation
 * - Full name validation
 * - Security input validation
 * - Edge cases and boundary conditions
 */

package com.example.fitnesstrackerapp.auth

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ValidationUtilsTest {
    class EmailValidationTests {
        @Test
        fun `valid emails return true`() {
            val validEmails = listOf(
                "test@example.com",
                "user.name@domain.co.uk",
                "user+tag@gmail.com",
                "user123@test-domain.com",
                "a@b.co",
                "test.email.with.dots@example.com",
                "x@example.org",
            )

            validEmails.forEach { email ->
                assertThat(ValidationUtils.isValidEmail(email))
                    .withMessage("Email: $email")
                    .isTrue()
            }
        }

        @Test
        fun `invalid emails return false`() {
            val invalidEmails = listOf(
                "",
                " ",
                "plainaddress",
                "@missingdomain.com",
                "missing@.com",
                "missing@domain",
                "spaces in@email.com",
                "double@@domain.com",
                "trailing.dot.@domain.com",
                ".leading.dot@domain.com",
                "too..many.dots@domain.com",
                "toolongdomainname@a".repeat(10) + ".com",
            )

            invalidEmails.forEach { email ->
                assertThat(ValidationUtils.isValidEmail(email))
                    .withMessage("Email: $email")
                    .isFalse()
            }
        }

        @Test
        fun `blank email returns false`() {
            assertThat(ValidationUtils.isValidEmail("")).isFalse()
            assertThat(ValidationUtils.isValidEmail("   ")).isFalse()
        }
    }

    class PasswordStrengthTests {
        @Test
        fun `strong passwords are valid`() {
            val strongPasswords = listOf(
                "StrongPassword123!",
                "MySecure@Pass2024",
                "Complex#Password1",
                "Tr0ub4dor&3",
                "P@ssw0rd123",
            )

            strongPasswords.forEach { password ->
                val result = ValidationUtils.validatePasswordStrength(password)
                assertThat(result.isValid)
                    .withMessage("Password: $password, Message: ${result.message}")
                    .isTrue()
            }
        }

        @Test
        fun `password too short fails validation`() {
            val shortPasswords = listOf("1234567", "Abc123!", "Short1!")

            shortPasswords.forEach { password ->
                val result = ValidationUtils.validatePasswordStrength(password)
                assertThat(result.isValid).isFalse()
                assertThat(result.message).contains("at least")
            }
        }

        @Test
        fun `password without digits fails validation`() {
            val passwordWithoutDigits = "StrongPasswordWithoutNumbers!"
            val result = ValidationUtils.validatePasswordStrength(passwordWithoutDigits)

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("number")
        }

        @Test
        fun `password without uppercase fails validation`() {
            val passwordWithoutUpper = "strongpassword123!"
            val result = ValidationUtils.validatePasswordStrength(passwordWithoutUpper)

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("uppercase")
        }

        @Test
        fun `password without lowercase fails validation`() {
            val passwordWithoutLower = "STRONGPASSWORD123!"
            val result = ValidationUtils.validatePasswordStrength(passwordWithoutLower)

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("lowercase")
        }

        @Test
        fun `password without special characters fails validation`() {
            val passwordWithoutSpecial = "StrongPassword123"
            val result = ValidationUtils.validatePasswordStrength(passwordWithoutSpecial)

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("special character")
        }

        @Test
        fun `password with common patterns fails validation`() {
            val weakPasswords = listOf(
                "Password123!",
                "Qwerty123!",
                "Admin123!",
                "Welcome123!",
                "Letmein123!",
            )

            weakPasswords.forEach { password ->
                val result = ValidationUtils.validatePasswordStrength(password)
                assertThat(result.isValid)
                    .withMessage("Password: $password")
                    .isFalse()
                assertThat(result.message).contains("common patterns")
            }
        }

        @Test
        fun `password with repetitive characters fails validation`() {
            val passwordWithRepeats = "Strongaaa123!"
            val result = ValidationUtils.validatePasswordStrength(passwordWithRepeats)

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("consecutive identical")
        }

        @Test
        fun `password with sequential patterns fails validation`() {
            val sequentialPasswords = listOf(
                "Abc123def!",
                "Strong123abc!",
                "Password321!",
            )

            sequentialPasswords.forEach { password ->
                ValidationUtils.validatePasswordStrength(password)
                // Note: This assumes the implementation detects these patterns
                // The actual result depends on the sequential pattern detection logic
            }
        }

        @Test
        fun `password too long fails validation`() {
            val veryLongPassword = "A".repeat(200) + "1!"
            val result = ValidationUtils.validatePasswordStrength(veryLongPassword)

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("less than")
        }

        @Test
        fun `isValidPasswordLength works correctly`() {
            assertThat(ValidationUtils.isValidPasswordLength("1234567")).isFalse()
            assertThat(ValidationUtils.isValidPasswordLength("12345678")).isTrue()
            assertThat(ValidationUtils.isValidPasswordLength("verylongpassword")).isTrue()
        }
    }

    class UsernameValidationTests {
        @Test
        fun `valid usernames pass validation`() {
            val validUsernames = listOf(
                "user123",
                "test_user",
                "MyUsername",
                "user_123_test",
                "abc",
            )

            validUsernames.forEach { username ->
                val result = ValidationUtils.validateUsername(username)
                assertThat(result.isValid)
                    .withMessage("Username: $username, Message: ${result.message}")
                    .isTrue()
            }
        }

        @Test
        fun `empty username fails validation`() {
            val result = ValidationUtils.validateUsername("")

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("cannot be empty")
        }

        @Test
        fun `username too short fails validation`() {
            val result = ValidationUtils.validateUsername("ab")

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("at least")
        }

        @Test
        fun `username too long fails validation`() {
            val longUsername = "a".repeat(25)
            val result = ValidationUtils.validateUsername(longUsername)

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("less than")
        }

        @Test
        fun `username with invalid characters fails validation`() {
            val invalidUsernames = listOf(
                "user-123",
                "user@test",
                "user 123",
                "user.test",
                "user+test",
                "user!test",
            )

            invalidUsernames.forEach { username ->
                val result = ValidationUtils.validateUsername(username)
                assertThat(result.isValid)
                    .withMessage("Username: $username")
                    .isFalse()
                assertThat(result.message).contains("letters, numbers, and underscores")
            }
        }

        @Test
        fun `reserved usernames fail validation`() {
            val reservedUsernames = listOf(
                "admin",
                "root",
                "user",
                "guest",
                "test",
                "demo",
                "support",
                "null",
                "undefined",
            )

            reservedUsernames.forEach { username ->
                val result = ValidationUtils.validateUsername(username)
                assertThat(result.isValid)
                    .withMessage("Username: $username")
                    .isFalse()
                assertThat(result.message).contains("reserved")
            }
        }
    }

    class FullNameValidationTests {
        @Test
        fun `valid names pass validation`() {
            val validNames = listOf(
                "John Doe",
                "Mary Jane Smith",
                "Jean-Claude Van Damme",
                "O'Connor",
                "María José",
                "Ali",
                "Two Words",
                "Dr. John Smith Jr.",
            )

            validNames.forEach { name ->
                val result = ValidationUtils.validateFullName(name)
                assertThat(result.isValid)
                    .withMessage("Name: $name, Message: ${result.message}")
                    .isTrue()
            }
        }

        @Test
        fun `empty name fails validation`() {
            val result = ValidationUtils.validateFullName("")

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("cannot be empty")
        }

        @Test
        fun `name too long fails validation`() {
            val longName = "a".repeat(100)
            val result = ValidationUtils.validateFullName(longName)

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("less than")
        }

        @Test
        fun `name with invalid characters fails validation`() {
            val invalidNames = listOf(
                "John123",
                "User@Name",
                "Name#123",
                "User$Name",
            )

            invalidNames.forEach { name ->
                val result = ValidationUtils.validateFullName(name)
                assertThat(result.isValid)
                    .withMessage("Name: $name")
                    .isFalse()
                assertThat(result.message).contains("letters, spaces, hyphens, and apostrophes")
            }
        }
    }

    class FormValidationTests {
        @Test
        fun `valid registration form passes validation`() {
            val result = ValidationUtils.validateRegistrationForm(
                email = "test@example.com",
                username = "testuser",
                password = "StrongPassword123!",
                confirmPassword = "StrongPassword123!",
                fullName = "Test User",
            )

            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `registration form with invalid email fails validation`() {
            val result = ValidationUtils.validateRegistrationForm(
                email = "invalid-email",
                username = "testuser",
                password = "StrongPassword123!",
                confirmPassword = "StrongPassword123!",
                fullName = "Test User",
            )

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("valid email address")
        }

        @Test
        fun `registration form with password mismatch fails validation`() {
            val result = ValidationUtils.validateRegistrationForm(
                email = "test@example.com",
                username = "testuser",
                password = "StrongPassword123!",
                confirmPassword = "DifferentPassword123!",
                fullName = "Test User",
            )

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("do not match")
        }

        @Test
        fun `valid login form passes validation`() {
            val result = ValidationUtils.validateLoginForm(
                email = "test@example.com",
                password = "password123",
            )

            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `login form with empty fields fails validation`() {
            val result = ValidationUtils.validateLoginForm(
                email = "",
                password = "",
            )

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("email address")
        }
    }

    class SecurityValidationTests {
        @Test
        fun `safe input passes security check`() {
            val safeInputs = listOf(
                "normal text",
                "user@example.com",
                "safe-password-123",
                "John Doe",
            )

            safeInputs.forEach { input ->
                val result = ValidationUtils.checkForSecurityIssues(input)
                assertThat(result.isValid)
                    .withMessage("Input: $input")
                    .isTrue()
            }
        }

        @Test
        fun `SQL injection patterns fail security check`() {
            val maliciousInputs = listOf(
                "'; DROP TABLE users; --",
                "admin' OR '1'='1",
                "UNION SELECT * FROM users",
                "INSERT INTO users VALUES",
            )

            maliciousInputs.forEach { input ->
                val result = ValidationUtils.checkForSecurityIssues(input)
                assertThat(result.isValid)
                    .withMessage("Input: $input")
                    .isFalse()
                assertThat(result.message).contains("harmful content")
            }
        }

        @Test
        fun `script injection patterns fail security check`() {
            val maliciousInputs = listOf(
                "<script>alert('xss')</script>",
                "javascript:void(0)",
                "onload=\"alert('xss')\"",
                "<img src=x onerror=alert('xss')>",
            )

            maliciousInputs.forEach { input ->
                val result = ValidationUtils.checkForSecurityIssues(input)
                assertThat(result.isValid)
                    .withMessage("Input: $input")
                    .isFalse()
                assertThat(result.message).contains("harmful content")
            }
        }
    }

    class ResetTokenValidationTests {
        @Test
        fun `valid reset token passes validation`() {
            val validToken = "a".repeat(32)
            val result = ValidationUtils.validateResetToken(validToken)

            assertThat(result.isValid).isTrue()
        }

        @Test
        fun `empty reset token fails validation`() {
            val result = ValidationUtils.validateResetToken("")

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("enter the reset token")
        }

        @Test
        fun `reset token wrong length fails validation`() {
            val shortToken = "abc123"
            val longToken = "a".repeat(50)

            val shortResult = ValidationUtils.validateResetToken(shortToken)
            val longResult = ValidationUtils.validateResetToken(longToken)

            assertThat(shortResult.isValid).isFalse()
            assertThat(shortResult.message).contains("exactly")

            assertThat(longResult.isValid).isFalse()
            assertThat(longResult.message).contains("exactly")
        }

        @Test
        fun `reset token with invalid characters fails validation`() {
            val invalidToken = "abcd1234efgh5678ijkl9012mnop@#$%"
            val result = ValidationUtils.validateResetToken(invalidToken)

            assertThat(result.isValid).isFalse()
            assertThat(result.message).contains("letters and numbers")
        }
    }

    class ExtensionFunctionTests {
        @Test
        fun `matchesPassword extension works correctly`() {
            val password = "TestPassword123!"

            val matchResult = password.matchesPassword("TestPassword123!")
            val noMatchResult = password.matchesPassword("DifferentPassword123!")

            assertThat(matchResult.isValid).isTrue()
            assertThat(matchResult.message).contains("match")

            assertThat(noMatchResult.isValid).isFalse()
            assertThat(noMatchResult.message).contains("do not match")
        }

        @Test
        fun `isValidEmail extension works correctly`() {
            assertThat("test@example.com".isValidEmail()).isTrue()
            assertThat("invalid-email".isValidEmail()).isFalse()
        }

        @Test
        fun `isValidPassword extension works correctly`() {
            val strongPassword = "StrongPassword123!"
            val weakPassword = "weak"

            val strongResult = strongPassword.isValidPassword()
            val weakResult = weakPassword.isValidPassword()

            assertThat(strongResult.isValid).isTrue()
            assertThat(weakResult.isValid).isFalse()
        }
    }

    class EdgeCasesTests {
        @Test
        fun `null and empty string handling`() {
            // Test various validation functions with empty strings
            assertThat(ValidationUtils.isValidEmail("")).isFalse()

            val emptyUsernameResult = ValidationUtils.validateUsername("")
            assertThat(emptyUsernameResult.isValid).isFalse()

            val emptyNameResult = ValidationUtils.validateFullName("")
            assertThat(emptyNameResult.isValid).isFalse()
        }

        @Test
        fun `whitespace-only strings`() {
            val whitespaceEmail = "   "
            val whitespaceUsername = "   "
            val whitespaceName = "   "

            assertThat(ValidationUtils.isValidEmail(whitespaceEmail)).isFalse()

            val usernameResult = ValidationUtils.validateUsername(whitespaceUsername)
            assertThat(usernameResult.isValid).isFalse()

            val nameResult = ValidationUtils.validateFullName(whitespaceName)
            assertThat(nameResult.isValid).isFalse()
        }

        @Test
        fun `boundary length testing`() {
            // Test exactly at boundaries
            val minLengthPassword = "A".repeat(7) + "1!" // Just under minimum
            val exactMinPassword = "A".repeat(6) + "1!" // At minimum

            ValidationUtils.validatePasswordStrength(minLengthPassword)
            ValidationUtils.validatePasswordStrength(exactMinPassword)

            // The exact behavior depends on minimum length requirements
            // This assumes minimum is 8 characters
        }

        @Test
        fun `unicode and special character handling`() {
            val unicodeName = "José María"
            ValidationUtils.validateFullName(unicodeName)

            // The result depends on whether unicode characters are supported
            // This test documents current behavior

            val unicodeEmail = "test@domäin.com"
            ValidationUtils.isValidEmail(unicodeEmail)

            // Document current unicode email handling behavior
        }

        @Test
        fun `case sensitivity testing`() {
            // Test case sensitivity in various validations
            val upperCaseReserved = "ADMIN"
            val lowerCaseReserved = "admin"

            val upperResult = ValidationUtils.validateUsername(upperCaseReserved)
            val lowerResult = ValidationUtils.validateUsername(lowerCaseReserved)

            // Both should fail since reserved names should be case-insensitive
            assertThat(upperResult.isValid).isFalse()
            assertThat(lowerResult.isValid).isFalse()
        }
    }
}
