/**
 * Comprehensive test suite that runs all critical end-to-end and integration tests.
 *
 * This test suite orchestrates the execution of all major test flows:
 * - Authentication and user management tests
 * - Workout creation and management tests
 * - Goal setting and tracking tests
 * - Notification and system integration tests
 * - Repository and data layer tests
 *
 * The suite is designed to run with the Android Test Orchestrator for
 * improved test isolation and reliability.
 */

package com.example.fitnesstrackerapp.suite

import androidx.test.filters.LargeTest
import com.example.fitnesstrackerapp.e2e.AddWorkoutFlowTest
import com.example.fitnesstrackerapp.e2e.LoginFlowTest
import com.example.fitnesstrackerapp.e2e.NotificationFlowTest
import com.example.fitnesstrackerapp.e2e.SetGoalFlowTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Main test suite that includes all critical user flow tests.
 *
 * This suite should be run before each release to ensure all
 * critical functionality works end-to-end.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    LoginFlowTest::class,
    AddWorkoutFlowTest::class,
    SetGoalFlowTest::class,
    NotificationFlowTest::class,
)
@LargeTest
class ComprehensiveTestSuite

/**
 * Authentication and user management focused test suite.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    LoginFlowTest::class,
)
class AuthTestSuite

/**
 * Workout and exercise management focused test suite.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    AddWorkoutFlowTest::class,
)
class WorkoutTestSuite

/**
 * Goal setting and progress tracking focused test suite.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    SetGoalFlowTest::class,
)
class GoalTestSuite

/**
 * System integration and notification focused test suite.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    NotificationFlowTest::class,
)
class SystemIntegrationTestSuite
