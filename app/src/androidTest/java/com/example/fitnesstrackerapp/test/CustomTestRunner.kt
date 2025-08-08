/**
 * Custom Test Runner
 *
 * Responsibilities:
 * - Configures test environment
 * - Handles database initialization for tests
 * - Manages test lifecycle
 */

package com.example.fitnesstrackerapp.test

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class CustomTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        classLoader: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(
            classLoader,
            TestApplication::class.java.name,
            context
        )
    }
}

class TestApplication : Application()
