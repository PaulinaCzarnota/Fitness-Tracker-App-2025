package com.example.fitnesstrackerapp.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.test.core.app.ApplicationProvider
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for StepTracker to verify API surface and functionality.
 * These tests lock the public API surface and ensure consistent behavior.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class StepTrackerTest {
    private lateinit var context: Context
    private lateinit var stepTracker: StepTracker
    private lateinit var sensorManager: SensorManager
    private lateinit var stepCounterSensor: Sensor
    private lateinit var stepDetectorSensor: Sensor

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        // Mock SensorManager and sensors
        sensorManager = mockk()
        stepCounterSensor = mockk()
        stepDetectorSensor = mockk()

        // Mock context.getSystemService to return our mock SensorManager
        mockkStatic(Context::class)
        every { context.getSystemService(Context.SENSOR_SERVICE) } returns sensorManager
        every { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) } returns stepCounterSensor
        every { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) } returns stepDetectorSensor

        stepTracker = StepTracker(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `isStepTrackingSupported returns true when sensors available`() {
        // Given - sensors are mocked to be available

        // When
        val result = stepTracker.isStepTrackingSupported()

        // Then
        assertTrue(result)
    }

    @Test
    fun `isStepTrackingSupported returns false when no sensors available`() {
        // Given
        every { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) } returns null
        every { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) } returns null
        val tracker = StepTracker(context)

        // When
        val result = tracker.isStepTrackingSupported()

        // Then
        assertFalse(result)
    }

    @Test
    fun `startTracking returns true when sensors available`() {
        // Given
        every { sensorManager.registerListener(any(), any<Sensor>(), any()) } returns true

        // When
        val result = stepTracker.startTracking()

        // Then
        assertTrue(result)
    }

    @Test
    fun `startTracking returns false when no sensors available`() {
        // Given
        every { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) } returns null
        every { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) } returns null
        val tracker = StepTracker(context)

        // When
        val result = tracker.startTracking()

        // Then
        assertFalse(result)
    }

    @Test
    fun `stopTracking calls unregisterListener`() {
        // Given
        every { sensorManager.unregisterListener(any()) } just runs

        // When
        stepTracker.stopTracking()

        // Then
        verify { sensorManager.unregisterListener(stepTracker) }
    }

    @Test
    fun `resetSessionSteps resets count to zero`() = runTest {
        // Given
        stepTracker.addManualSteps(100)

        // When
        stepTracker.resetSessionSteps()

        // Then
        assertEquals(0, stepTracker.stepCount.first())
    }

    @Test
    fun `getSessionStepCount returns current session steps`() {
        // Given
        stepTracker.addManualSteps(150)

        // When
        val result = stepTracker.getSessionStepCount()

        // Then
        assertEquals(150, result)
    }

    @Test
    fun `calculateDistance returns correct distance for steps`() {
        // Given
        val steps = 1000

        // When
        val result = stepTracker.calculateDistance(steps)

        // Then
        assertEquals(760.0f, result, 0.01f) // 1000 * 0.76
    }

    @Test
    fun `calculateDistanceKm returns correct distance in kilometers`() {
        // Given
        val steps = 1000

        // When
        val result = stepTracker.calculateDistanceKm(steps)

        // Then
        assertEquals(0.76f, result, 0.001f) // 760 / 1000
    }

    @Test
    fun `calculateCalories returns correct calories for steps`() {
        // Given
        val steps = 1000

        // When
        val result = stepTracker.calculateCalories(steps)

        // Then
        assertEquals(40.0f, result, 0.01f) // 1000 * 0.04
    }

    @Test
    fun `getStepStats returns current statistics`() {
        // Given
        stepTracker.addManualSteps(500)

        // When
        val result = stepTracker.getStepStats()

        // Then
        assertEquals(500, result.sessionSteps)
        assertEquals(0.38f, result.sessionDistance, 0.01f)
        assertEquals(20.0f, result.sessionCalories, 0.01f)
        assertNotNull(result.totalSteps)
        assertNotNull(result.isTracking)
    }

    @Test
    fun `addManualSteps increases step count`() = runTest {
        // Given
        val initialSteps = stepTracker.stepCount.first()

        // When
        stepTracker.addManualSteps(250)

        // Then
        assertEquals(initialSteps + 250, stepTracker.stepCount.first())
    }

    @Test
    fun `clearCache method exists and executes`() {
        // Given/When - Should not throw exception
        stepTracker.clearCache()

        // Then - Method executed successfully
        assertTrue(true)
    }

    @Test
    fun `setStepLength method exists and accepts parameter`() {
        // Given/When - Should not throw exception
        stepTracker.setStepLength(0.8f)

        // Then - Method executed successfully
        assertTrue(true)
    }

    // Flow tests
    @Test
    fun `stepCount flow emits initial value`() = runTest {
        // When
        val initialValue = stepTracker.stepCount.first()

        // Then
        assertEquals(0, initialValue)
    }

    @Test
    fun `isTracking flow emits initial value`() = runTest {
        // When
        val initialValue = stepTracker.isTracking.first()

        // Then
        assertFalse(initialValue)
    }

    @Test
    fun `totalSteps flow emits initial value`() = runTest {
        // When
        val initialValue = stepTracker.totalSteps.first()

        // Then
        assertEquals(0L, initialValue)
    }

    // API Surface Tests - Ensure methods exist and have correct signatures
    @Test
    fun `StepTracker API surface test`() {
        // Test that all expected public methods exist with correct signatures
        try {
            // Core functionality
            stepTracker.isStepTrackingSupported()
            stepTracker.startTracking()
            stepTracker.stopTracking()
            stepTracker.resetSessionSteps()
            stepTracker.getSessionStepCount()

            // Calculations
            stepTracker.calculateDistance(1000)
            stepTracker.calculateDistanceKm(1000)
            stepTracker.calculateCalories(1000)
            stepTracker.getStepStats()

            // Manual operations
            stepTracker.addManualSteps(100)
            stepTracker.setStepLength(0.8f)
            stepTracker.clearCache()

            // Flows
            assertNotNull(stepTracker.stepCount)
            assertNotNull(stepTracker.isTracking)
            assertNotNull(stepTracker.totalSteps)

            // Success - API surface is stable
            assertTrue(true)
        } catch (e: NoSuchMethodError) {
            fail("API surface has changed: ${e.message}")
        } catch (e: NoSuchFieldError) {
            fail("API surface has changed: ${e.message}")
        }
    }

    // Test StepStats data class
    @Test
    fun `StepStats data class has correct properties`() {
        // Given
        val stats = StepStats(
            sessionSteps = 100,
            totalSteps = 1000L,
            sessionDistance = 0.76f,
            sessionCalories = 40.0f,
            isTracking = true,
        )

        // Then
        assertEquals(100, stats.sessionSteps)
        assertEquals(1000L, stats.totalSteps)
        assertEquals(0.76f, stats.sessionDistance, 0.01f)
        assertEquals(40.0f, stats.sessionCalories, 0.01f)
        assertTrue(stats.isTracking)
    }
}
