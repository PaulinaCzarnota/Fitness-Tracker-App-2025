/**
 * ExampleInstrumentedTest
 *
 * Instrumented tests that run on a device or emulator.
 * Used to verify runtime behavior and Android-specific functionality.
 *
 * Run with:
 * ./gradlew connectedAndroidTest
 */

package com.example.fitnesstrackerapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    /**
     * Test 1: App context validation
     *
     * Confirms that the package name of the app context is correct.
     */
    @Test
    fun useAppContext() {
        // Get the actual app context at runtime
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Assert the expected package name
        assertEquals("com.example.fitnesstrackerapp", appContext.packageName)
    }

    /**
     * Test 2: ApplicationProvider context validation
     *
     * Confirms ApplicationProvider returns correct app context.
     */
    @Test
    fun applicationProvider_returnsCorrectContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("com.example.fitnesstrackerapp", context.packageName)
    }
}
