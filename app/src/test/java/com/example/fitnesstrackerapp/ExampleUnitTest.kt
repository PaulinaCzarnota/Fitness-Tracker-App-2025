package com.example.fitnesstrackerapp

import com.example.fitnesstrackerapp.util.SecurityUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * ExampleUnitTest
 *
 * Basic unit tests for core utility logic of the Fitness Tracker App.
 * These tests run locally on the JVM (no device or emulator required).
 *
 * ➤ You can add more test classes in this package to test your utility classes,
 *   ViewModels (with mocks), and data processing logic.
 */
class ExampleUnitTest {

    /**
     * Test 1: Basic addition validation
     *
     * Ensures that the JUnit testing environment is set up and working.
     */
    @Test
    fun addition_isCorrect() {
        assertEquals("Basic addition should return correct result", 4, 2 + 2)
    }

    /**
     * Test 2: Email normalization logic
     *
     * Ensures SecurityUtils.normaliseEmail() behaves as expected.
     */
    @Test
    fun emailNormalization_isCorrect() {
        val input = "  USER@Example.Com  "
        val expected = "user@example.com"
        val actual = SecurityUtils.normaliseEmail(input)
        assertEquals("Email should be trimmed and lowercased", expected, actual)
    }

    /**
     * Test 3: Password hashing length
     *
     * Ensures that SHA-256 hash output is 64 characters long.
     */
    @Test
    fun sha256_hashLength_isCorrect() {
        val password = "mySecurePassword123"
        val hash = SecurityUtils.sha256(password)
        assertEquals("SHA-256 hash should be 64 characters long", 64, hash.length)
    }

    /**
     * Test 4: SHA-256 produces consistent results
     *
     * Ensures that hashing the same input always produces the same result.
     */
    @Test
    fun sha256_consistency_isCorrect() {
        val password = "repeatable"
        val hash1 = SecurityUtils.sha256(password)
        val hash2 = SecurityUtils.sha256(password)
        assertEquals("Hash should be consistent for same input", hash1, hash2)
    }

    /**
     * (Optional Future Test)
     * You can use mockito/kotlinx-coroutines-test to test your ViewModel:
     * - Validate registration logic
     * - Simulate DAO behavior
     * - Validate error states
     */
}
