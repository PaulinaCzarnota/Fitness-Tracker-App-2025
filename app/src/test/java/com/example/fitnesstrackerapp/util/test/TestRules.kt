/**
 * Test Rules and Configuration
 *
 * Responsibilities:
 * - Provides test rules for Android components
 * - Sets up test coroutine dispatchers
 * - Manages test database instances
 */

package com.example.fitnesstrackerapp.util.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.TestDispatcher
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
@OptIn(ExperimentalCoroutinesApi::class)
class TestRules {
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
    private val mainDispatcherRule = object : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(dispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }

    private val instantExecutorRule = InstantTaskExecutorRule()

    val ruleChain: TestRule = RuleChain
        .outerRule(instantExecutorRule)
        .around(mainDispatcherRule)
}

abstract class BaseTest {
    protected val testRules = TestRules()
}
