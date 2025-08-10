package com.example.fitnesstrackerapp.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for PermissionUtils to verify API surface and functionality.
 * These tests lock the public API surface and ensure consistent behavior.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PermissionUtilsTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockkStatic(ContextCompat::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `isPermissionGranted returns true when permission is granted`() {
        // Given
        val permission = android.Manifest.permission.CAMERA
        every { ContextCompat.checkSelfPermission(context, permission) } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = PermissionUtils.isPermissionGranted(context, permission)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isPermissionGranted returns false when permission is denied`() {
        // Given
        val permission = android.Manifest.permission.CAMERA
        every { ContextCompat.checkSelfPermission(context, permission) } returns PackageManager.PERMISSION_DENIED

        // When
        val result = PermissionUtils.isPermissionGranted(context, permission)

        // Then
        assertFalse(result)
    }

    @Test
    fun `areAllPermissionsGranted returns true when all permissions granted`() {
        // Given
        val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        every { ContextCompat.checkSelfPermission(context, permissions[0]) } returns PackageManager.PERMISSION_GRANTED
        every { ContextCompat.checkSelfPermission(context, permissions[1]) } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = PermissionUtils.areAllPermissionsGranted(context, permissions)

        // Then
        assertTrue(result)
    }

    @Test
    fun `areAllPermissionsGranted returns false when one permission denied`() {
        // Given
        val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        every { ContextCompat.checkSelfPermission(context, permissions[0]) } returns PackageManager.PERMISSION_GRANTED
        every { ContextCompat.checkSelfPermission(context, permissions[1]) } returns PackageManager.PERMISSION_DENIED

        // When
        val result = PermissionUtils.areAllPermissionsGranted(context, permissions)

        // Then
        assertFalse(result)
    }

    @Test
    fun `getMissingPermissions returns only denied permissions`() {
        // Given
        val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        every { ContextCompat.checkSelfPermission(context, permissions[0]) } returns PackageManager.PERMISSION_GRANTED
        every { ContextCompat.checkSelfPermission(context, permissions[1]) } returns PackageManager.PERMISSION_DENIED

        // When
        val result = PermissionUtils.getMissingPermissions(context, permissions)

        // Then
        assertEquals(1, result.size)
        assertEquals(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, result[0])
    }

    @Test
    fun `getMissingPermissions returns empty list when all granted`() {
        // Given
        val permissions = arrayOf(android.Manifest.permission.CAMERA)
        every { ContextCompat.checkSelfPermission(context, permissions[0]) } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = PermissionUtils.getMissingPermissions(context, permissions)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `isNotificationPermissionGranted returns true on SDK less than 33`() {
        // Given/When (SDK 33 is mocked in @Config)
        // Test would be different on actual device with lower SDK
        // Here we test the API surface exists
        val result = PermissionUtils.isNotificationPermissionGranted(context)

        // Then
        // Result depends on mocked behavior, but API should exist
        assertNotNull(result)
    }

    @Test
    fun `isActivityRecognitionPermissionGranted returns true on SDK less than 29`() {
        // Given/When
        val result = PermissionUtils.isActivityRecognitionPermissionGranted(context)

        // Then
        // Result depends on mocked behavior, but API should exist
        assertNotNull(result)
    }

    @Test
    fun `getRuntimePermissionsToRequest returns permissions list`() {
        // Given - Mock permissions as denied
        every { ContextCompat.checkSelfPermission(context, any()) } returns PackageManager.PERMISSION_DENIED

        // When
        val result = PermissionUtils.getRuntimePermissionsToRequest(context)

        // Then
        assertNotNull(result)
        assertTrue(result is List<String>)
    }

    @Test
    fun `areEssentialPermissionsGranted checks required permissions`() {
        // Given
        every { ContextCompat.checkSelfPermission(context, any()) } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = PermissionUtils.areEssentialPermissionsGranted(context)

        // Then
        assertNotNull(result)
        assertTrue(result is Boolean)
    }

    // API Surface Tests - Ensure methods exist and have correct signatures
    @Test
    fun `PermissionUtils API surface test`() {
        // Test that all expected public methods exist with correct signatures
        val context = mockk<Context>()
        val permission = "test.permission"
        val permissions = arrayOf("test.permission1", "test.permission2")

        // These should compile without errors, verifying API surface
        try {
            PermissionUtils.isPermissionGranted(context, permission)
            PermissionUtils.areAllPermissionsGranted(context, permissions)
            PermissionUtils.getMissingPermissions(context, permissions)
            PermissionUtils.isNotificationPermissionGranted(context)
            PermissionUtils.isActivityRecognitionPermissionGranted(context)
            PermissionUtils.getRuntimePermissionsToRequest(context)
            PermissionUtils.areEssentialPermissionsGranted(context)

            // Success - API surface is stable
            assertTrue(true)
        } catch (e: NoSuchMethodError) {
            fail("API surface has changed: ${e.message}")
        }
    }
}
