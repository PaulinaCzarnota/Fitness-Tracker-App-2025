package com.example.fitnesstrackerapp

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * ExampleInstrumentedTest
 *
 * A default Android instrumented test that runs on an emulator or physical device.
 * It simply verifies that the app context's package name is correct.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Get the app context using the instrumentation framework
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Check that the package name matches the actual app's package name
        assertEquals("com.example.fitnesstrackerapp", appContext.packageName)
    }
}
