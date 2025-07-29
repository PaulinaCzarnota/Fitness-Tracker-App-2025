package com.example.fitnesstrackerapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitnesstrackerapp.util.SecurityUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * ExampleInstrumentedTest
 *
 * Instrumented tests that run on a device or emulator.
 * Used to verify runtime behavior and Android-specific functionality.
 *
 * Run with:
 * ./gradlew connectedAndroidTest
 */
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
     * Test 2: Verify email normalization in app context
     *
     * Tests that email normalization behaves as expected on device context.
     */
    @Test
    fun emailNormalization_worksCorrectlyOnDevice() {
        val input = "  TestUser@Mail.Com  "
        val expected = "testuser@mail.com"
        val result = SecurityUtils.normaliseEmail(input)

        assertEquals("Email should be trimmed and lowercased", expected, result)
    }

    /**
     * Test 3: Verify password hashing on device
     *
     * Confirms that SHA-256 hashing produces the correct length.
     */
    @Test
    fun passwordHashing_isCorrectLength() {
        val password = "androidTest123"
        val hash = SecurityUtils.sha256(password)

        assertEquals("Hashed password should be 64 characters", 64, hash.length)
    }

    /**
     * Test 4: ApplicationProvider context validation
     *
     * Confirms ApplicationProvider returns correct app context.
     */
    @Test
    fun applicationProvider_returnsCorrectContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("com.example.fitnesstrackerapp", context.packageName)
    }
}
