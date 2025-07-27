package com.example.fitnesstrackerapp

// Import JUnit 4 testing framework and assertion methods
import org.junit.Test
import org.junit.Assert.assertEquals

/**
 * ExampleUnitTest
 *
 * This class demonstrates basic unit-testing capabilities within the Fitness Tracker App project.
 * The tests run locally on the development machine (host JVM) without needing an emulator or device.
 *
 * Extend this test class or add new ones to test additional functionality and logic of the app.
 */
class ExampleUnitTest {

    /**
     * Verifies basic arithmetic correctness.
     *
     * This simple test ensures the testing environment is properly set up
     * and can perform assertions correctly.
     */
    @Test
    fun addition_isCorrect() {
        // Checks that the expression 2 + 2 equals 4.
        assertEquals("Basic addition should return correct result", 4, 2 + 2)
    }
}
