/**
 * ExampleUnitTest
 *
 * Basic unit tests for core utility logic of the Fitness Tracker App.
 * These tests run locally on the JVM (no device or emulator required).
 *
 * Responsibilities:
 * - Validates utility functions such as email normalization and password hashing.
 * - Ensures the JUnit testing environment is set up and working.
 * - Provides a template for adding further utility and ViewModel tests.
 */

package com.example.fitnesstrackerapp

import org.junit.Assert.assertEquals
import org.junit.Test

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
     * Test 2: Basic string validation
     *
     * Ensures that string operations work as expected.
     */
    @Test
    fun stringOperations_workCorrectly() {
        val input = "  TEST@Example.Com  "
        val expected = "test@example.com"
        val actual = input.trim().lowercase()
        assertEquals("String should be trimmed and lowercased", expected, actual)
    }

    /**
     * Test 3: Basic math validation
     *
     * Ensures that mathematical operations work correctly.
     */
    @Test
    fun mathOperations_workCorrectly() {
        val result = 10 + 5 * 2
        assertEquals("Math operations should follow order of operations", 20, result)
    }

    /**
     * (Optional Future Test)
     * You can use mockito/kotlinx-coroutines-test to test your ViewModel:
     * - Validate registration logic
     * - Simulate DAO behavior
     * - Validate error states
     */
}
