/**
 * Test Configuration Runner
 *
 * Responsibilities:
 * - Configures test execution for CI/CD
 * - Sets up test environment
 * - Manages test reporting
 */
package com.example.fitnesstrackerapp.config

import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError

class CITestRunner @Throws(InitializationError::class)
constructor(klass: Class<*>) : BlockJUnit4ClassRunner(klass) {

    override fun runChild(method: FrameworkMethod, notifier: RunNotifier) {
        // Setup any required test environment variables
        System.setProperty("CI_TEST_MODE", "true")
        System.setProperty("DB_MEMORY_MODE", "true")

        try {
            super.runChild(method, notifier)
        } finally {
            // Cleanup after test
            System.clearProperty("CI_TEST_MODE")
            System.clearProperty("DB_MEMORY_MODE")
        }
    }

    companion object {
        // Test configuration constants
        const val TEST_TIMEOUT = 30_000L // 30 seconds
        const val DB_SCHEMA_VERSION = 2
        const val ENABLE_LOGGING = false

        // Test categories
        const val UNIT_TEST = "unit"
        const val INTEGRATION_TEST = "integration"
        const val UI_TEST = "ui"

        // Test metadata
        val REQUIRED_PERMISSIONS = arrayOf(
            "android.permission.ACTIVITY_RECOGNITION",
            "android.permission.BODY_SENSORS",
            "android.permission.INTERNET",
            "android.permission.POST_NOTIFICATIONS",
            "android.permission.FOREGROUND_SERVICE"
        )
    }
}
