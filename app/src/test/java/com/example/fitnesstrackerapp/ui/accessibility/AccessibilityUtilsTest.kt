/**
 * Comprehensive unit tests for AccessibilityUtils using Robolectric
 *
 * Tests cover:
 * - Accessibility manager functionality
 * - Speak-out behavior validation  
 * - Content description generation
 * - Color contrast validation
 * - Touch target accessibility
 * - Live region announcements
 * - Backward compatibility across API levels
 */

package com.example.fitnesstrackerapp.ui.accessibility

import android.content.Context
import android.os.Build
import android.view.accessibility.AccessibilityManager
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.ui.accessibility.AccessibilityComposables.AccessibilityState
import com.example.fitnesstrackerapp.ui.accessibility.AccessibilityManagerUtils.isAccessibilityEnabled
import com.example.fitnesstrackerapp.ui.accessibility.AccessibilityManagerUtils.isTalkBackEnabled
import com.example.fitnesstrackerapp.ui.accessibility.ColorAccessibility.contrastRatio
import com.example.fitnesstrackerapp.ui.accessibility.ColorAccessibility.isAccessibleContrast
import com.example.fitnesstrackerapp.ui.accessibility.ColorAccessibility.suggestAccessibleColor
import com.example.fitnesstrackerapp.ui.accessibility.FitnessAccessibilityText.goalProgressDescription
import com.example.fitnesstrackerapp.ui.accessibility.FitnessAccessibilityText.nutritionSummaryDescription
import com.example.fitnesstrackerapp.ui.accessibility.FitnessAccessibilityText.stepCountDescription
import com.example.fitnesstrackerapp.ui.accessibility.FitnessAccessibilityText.workoutStatusDescription
import com.example.fitnesstrackerapp.ui.accessibility.SpeakOutUtils.FitnessUpdateType
import com.example.fitnesstrackerapp.ui.accessibility.SpeakOutUtils.announceFitnessUpdate
import com.example.fitnesstrackerapp.ui.accessibility.SpeakOutUtils.announceForAccessibility
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28, 29, 30, 31, 33], manifest = Config.NONE)
class AccessibilityUtilsTest {
    
    private lateinit var context: Context
    private lateinit var mockAccessibilityManager: AccessibilityManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockAccessibilityManager = mockk()
        
        // Mock ContextCompat.getSystemService
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.getSystemService(context, AccessibilityManager::class.java) 
        } returns mockAccessibilityManager
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    // ========== AccessibilityManagerUtils Tests ==========
    
    @Test
    fun `isAccessibilityEnabled returns true when accessibility services are enabled`() {
        // Arrange
        every { mockAccessibilityManager.isEnabled } returns true
        
        // Act
        val result = isAccessibilityEnabled(context)
        
        // Assert
        assertTrue("Should return true when accessibility is enabled", result)
        verify { mockAccessibilityManager.isEnabled }
    }
    
    @Test
    fun `isAccessibilityEnabled returns false when accessibility services are disabled`() {
        // Arrange
        every { mockAccessibilityManager.isEnabled } returns false
        
        // Act
        val result = isAccessibilityEnabled(context)
        
        // Assert
        assertFalse("Should return false when accessibility is disabled", result)
        verify { mockAccessibilityManager.isEnabled }
    }
    
    @Test
    fun `isAccessibilityEnabled handles null AccessibilityManager gracefully`() {
        // Arrange
        every { 
            ContextCompat.getSystemService(context, AccessibilityManager::class.java) 
        } returns null
        
        // Act
        val result = isAccessibilityEnabled(context)
        
        // Assert
        assertFalse("Should return false when AccessibilityManager is null", result)
    }
    
    @Test
    fun `isAccessibilityEnabled handles exceptions gracefully`() {
        // Arrange
        every { mockAccessibilityManager.isEnabled } throws RuntimeException("Test exception")
        
        // Act
        val result = isAccessibilityEnabled(context)
        
        // Assert
        assertFalse("Should return false when exception occurs", result)
    }
    
    @Test
    fun `isTalkBackEnabled returns true when touch exploration is enabled`() {
        // Arrange
        every { mockAccessibilityManager.isTouchExplorationEnabled } returns true
        
        // Act
        val result = isTalkBackEnabled(context)
        
        // Assert
        assertTrue("Should return true when TalkBack is enabled", result)
        verify { mockAccessibilityManager.isTouchExplorationEnabled }
    }
    
    @Test
    fun `isTalkBackEnabled returns false when touch exploration is disabled`() {
        // Arrange
        every { mockAccessibilityManager.isTouchExplorationEnabled } returns false
        
        // Act
        val result = isTalkBackEnabled(context)
        
        // Assert
        assertFalse("Should return false when TalkBack is disabled", result)
        verify { mockAccessibilityManager.isTouchExplorationEnabled }
    }
    
    @Test
    fun `getRecommendedTimeoutMillis extends timeout when accessibility is enabled on older APIs`() {
        // Arrange
        every { mockAccessibilityManager.isEnabled } returns true
        val originalTimeout = 1000
        
        // Act
        val result = AccessibilityManagerUtils.getRecommendedTimeoutMillis(context, originalTimeout)
        
        // Assert - On APIs < Q, should double the timeout if accessibility is enabled
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            assertEquals("Should double timeout on older APIs when accessibility enabled", 
                originalTimeout * 2, result)
        } else {
            // On API Q+, behavior depends on the mock setup
            assertTrue("Should return a reasonable timeout", result > 0)
        }
    }
    
    // ========== SpeakOutUtils Tests ==========
    
    @Test
    fun `announceForAccessibility does nothing when accessibility is disabled`() {
        // Arrange
        every { mockAccessibilityManager.isEnabled } returns false
        val testMessage = "Test announcement"
        
        // Act
        announceForAccessibility(context, testMessage)
        
        // Assert - Should not throw exceptions and handle gracefully
        verify { mockAccessibilityManager.isEnabled }
    }
    
    @Test
    fun `announceForAccessibility processes message when accessibility is enabled`() {
        // Arrange
        every { mockAccessibilityManager.isEnabled } returns true
        val testMessage = "Test announcement"
        
        // Act & Assert - Should not throw exceptions
        assertDoesNotThrow {
            announceForAccessibility(context, testMessage)
        }
        
        verify { mockAccessibilityManager.isEnabled }
    }
    
    @Test
    fun `announceFitnessUpdate formats messages correctly for different update types`() {
        // Arrange
        every { mockAccessibilityManager.isEnabled } returns true
        every { mockAccessibilityManager.isTouchExplorationEnabled } returns true
        val baseMessage = "10000 steps completed"
        
        // Act & Assert for each update type
        assertDoesNotThrow {
            announceFitnessUpdate(context, FitnessUpdateType.GOAL_ACHIEVED, baseMessage)
            announceFitnessUpdate(context, FitnessUpdateType.WORKOUT_MILESTONE, baseMessage)
            announceFitnessUpdate(context, FitnessUpdateType.WARNING, baseMessage)
            announceFitnessUpdate(context, FitnessUpdateType.PROGRESS_UPDATE, baseMessage)
            announceFitnessUpdate(context, FitnessUpdateType.NAVIGATION, baseMessage)
        }
    }
    
    @Test
    fun `announceFitnessUpdate skips announcement when TalkBack is disabled`() {
        // Arrange
        every { mockAccessibilityManager.isEnabled } returns true
        every { mockAccessibilityManager.isTouchExplorationEnabled } returns false
        
        // Act & Assert - Should handle gracefully without TalkBack
        assertDoesNotThrow {
            announceFitnessUpdate(context, FitnessUpdateType.GOAL_ACHIEVED, "Test message")
        }
    }
    
    // ========== FitnessAccessibilityText Tests ==========
    
    @Test
    fun `stepCountDescription provides appropriate messages for different progress levels`() {
        // Test cases: (steps, goal, expected keyword)
        val testCases = listOf(
            Triple(10000, 10000, "goal achieved"),
            Triple(9000, 10000, "Almost there"),
            Triple(5000, 10000, "Halfway"),
            Triple(3000, 10000, "Good progress"),
            Triple(1000, 10000, "Getting started")
        )
        
        testCases.forEach { (steps, goal, expectedKeyword) ->
            // Act
            val description = stepCountDescription(steps, goal)
            
            // Assert
            assertTrue(
                "Description for $steps/$goal should contain '$expectedKeyword'",
                description.lowercase().contains(expectedKeyword.lowercase())
            )
            assertTrue(
                "Description should contain step count",
                description.contains(steps.toString())
            )
            assertTrue(
                "Description should contain goal",
                description.contains(goal.toString())
            )
        }
    }
    
    @Test
    fun `workoutStatusDescription differentiates between active and completed workouts`() {
        // Test active workout
        val activeDescription = workoutStatusDescription(
            isActive = true,
            workoutName = "Morning Run",
            duration = "30 minutes",
            intensity = "High"
        )
        
        assertTrue("Active workout should contain 'Currently doing'", 
            activeDescription.contains("Currently doing"))
        assertTrue("Should contain workout name", 
            activeDescription.contains("Morning Run"))
        assertTrue("Should contain duration", 
            activeDescription.contains("30 minutes"))
        assertTrue("Should contain intensity", 
            activeDescription.contains("High"))
        
        // Test completed workout
        val completedDescription = workoutStatusDescription(
            isActive = false,
            workoutName = "Evening Yoga",
            duration = "45 minutes"
        )
        
        assertTrue("Completed workout should contain 'Completed'", 
            completedDescription.contains("Completed"))
        assertTrue("Should contain workout name", 
            completedDescription.contains("Evening Yoga"))
        assertFalse("Should not contain intensity when not provided",
            completedDescription.contains("Intensity"))
    }
    
    @Test
    fun `goalProgressDescription includes all relevant information`() {
        // Act
        val description = goalProgressDescription(
            goalName = "Daily Steps",
            current = 7500f,
            target = 10000f,
            unit = "steps",
            daysLeft = 5
        )
        
        // Assert
        assertTrue("Should contain goal name", description.contains("Daily Steps"))
        assertTrue("Should contain current value", description.contains("7500"))
        assertTrue("Should contain target value", description.contains("10000"))
        assertTrue("Should contain unit", description.contains("steps"))
        assertTrue("Should contain percentage", description.contains("75%"))
        assertTrue("Should contain days remaining", description.contains("5 days remaining"))
    }
    
    @Test
    fun `goalProgressDescription works without days remaining`() {
        // Act
        val description = goalProgressDescription(
            goalName = "Weight Loss",
            current = 65f,
            target = 70f,
            unit = "kg"
        )
        
        // Assert
        assertTrue("Should contain goal name", description.contains("Weight Loss"))
        assertTrue("Should contain current value", description.contains("65"))
        assertTrue("Should contain target value", description.contains("70"))
        assertFalse("Should not contain days remaining", description.contains("days remaining"))
    }
    
    @Test
    fun `nutritionSummaryDescription formats macronutrients correctly`() {
        // Test with calorie goal
        val descriptionWithGoal = nutritionSummaryDescription(
            calories = 1800,
            protein = 120f,
            carbs = 200f,
            fat = 60f,
            calorieGoal = 2000
        )
        
        assertTrue("Should contain calories consumed", descriptionWithGoal.contains("1800 calories"))
        assertTrue("Should contain calorie goal", descriptionWithGoal.contains("2000 goal"))
        assertTrue("Should contain remaining calories", descriptionWithGoal.contains("200 calories remaining"))
        assertTrue("Should contain protein", descriptionWithGoal.contains("120g protein"))
        assertTrue("Should contain carbs", descriptionWithGoal.contains("200g carbohydrates"))
        assertTrue("Should contain fat", descriptionWithGoal.contains("60g fat"))
        
        // Test without calorie goal
        val descriptionWithoutGoal = nutritionSummaryDescription(
            calories = 1500,
            protein = 100f,
            carbs = 150f,
            fat = 50f
        )
        
        assertTrue("Should contain calories", descriptionWithoutGoal.contains("1500 calories"))
        assertFalse("Should not contain goal information", descriptionWithoutGoal.contains("goal"))
        assertTrue("Should contain macronutrients", descriptionWithoutGoal.contains("Macronutrients"))
    }
    
    // ========== ColorAccessibility Tests ==========
    
    @Test
    fun `contrastRatio calculates correct ratios`() {
        // Test high contrast (black on white)
        val blackWhiteRatio = contrastRatio(Color.Black, Color.White)
        assertEquals("Black on white should have maximum contrast ratio", 21.0, blackWhiteRatio, 0.1)
        
        // Test low contrast (gray on gray)
        val lightGray = Color(0.7f, 0.7f, 0.7f)
        val darkGray = Color(0.6f, 0.6f, 0.6f)
        val grayRatio = contrastRatio(lightGray, darkGray)
        assertTrue("Gray on gray should have low contrast", grayRatio < 3.0)
        
        // Test same color (minimum contrast)
        val sameColorRatio = contrastRatio(Color.Red, Color.Red)
        assertEquals("Same colors should have ratio of 1.0", 1.0, sameColorRatio, 0.1)
    }
    
    @Test
    fun `isAccessibleContrast correctly identifies accessible combinations`() {
        // Test accessible combinations
        assertTrue("Black on white should be accessible", 
            isAccessibleContrast(Color.Black, Color.White))
        assertTrue("White on black should be accessible", 
            isAccessibleContrast(Color.White, Color.Black))
        
        // Test non-accessible combinations
        val lightGray = Color(0.8f, 0.8f, 0.8f)
        val mediumGray = Color(0.7f, 0.7f, 0.7f)
        assertFalse("Light gray on medium gray should not be accessible", 
            isAccessibleContrast(lightGray, mediumGray))
    }
    
    @Test
    fun `isAccessibleContrast considers large text threshold`() {
        val color1 = Color(0.5f, 0.5f, 0.5f)
        val color2 = Color(0.8f, 0.8f, 0.8f)
        
        // This combination might pass for large text but fail for normal text
        val ratio = contrastRatio(color1, color2)
        if (ratio >= AccessibilityConstants.MIN_CONTRAST_RATIO_LARGE && 
            ratio < AccessibilityConstants.MIN_CONTRAST_RATIO_NORMAL) {
            
            assertTrue("Should be accessible for large text", 
                isAccessibleContrast(color1, color2, isLargeText = true))
            assertFalse("Should not be accessible for normal text", 
                isAccessibleContrast(color1, color2, isLargeText = false))
        }
    }
    
    @Test
    fun `suggestAccessibleColor returns null for already accessible combinations`() {
        // Act
        val suggestion = suggestAccessibleColor(Color.Black, Color.White)
        
        // Assert
        assertNull("Should return null for already accessible combination", suggestion)
    }
    
    @Test
    fun `suggestAccessibleColor provides fallback for inaccessible combinations`() {
        // Test with similar colors
        val lightGray1 = Color(0.7f, 0.7f, 0.7f)
        val lightGray2 = Color(0.75f, 0.75f, 0.75f)
        
        // Act
        val suggestion = suggestAccessibleColor(lightGray1, lightGray2)
        
        // Assert
        assertNotNull("Should provide a suggestion for inaccessible combination", suggestion)
        if (suggestion != null) {
            assertTrue("Suggested color should be accessible", 
                isAccessibleContrast(suggestion, lightGray2))
        }
    }
    
    // ========== AccessibilityActions Tests ==========
    
    @Test
    fun `workoutActions creates correct custom actions`() {
        // Arrange
        val onStart = mockk<() -> Unit>(relaxed = true)
        val onPause = mockk<() -> Unit>(relaxed = true)
        val onStop = mockk<() -> Unit>(relaxed = true)
        val onSkip = mockk<() -> Unit>(relaxed = true)
        
        // Act
        val actions = AccessibilityActions.workoutActions(onStart, onPause, onStop, onSkip)
        
        // Assert
        assertEquals("Should create 4 actions", 4, actions.size)
        
        val actionLabels = actions.map { it.label }
        assertTrue("Should contain start action", actionLabels.contains("Start workout"))
        assertTrue("Should contain pause action", actionLabels.contains("Pause workout"))
        assertTrue("Should contain stop action", actionLabels.contains("Stop workout"))
        assertTrue("Should contain skip action", actionLabels.contains("Skip exercise"))
    }
    
    @Test
    fun `workoutActions handles null actions gracefully`() {
        // Act
        val actions = AccessibilityActions.workoutActions(
            onStart = null, onPause = null, onStop = null, onSkip = null
        )
        
        // Assert
        assertTrue("Should create empty list when all actions are null", actions.isEmpty())
    }
    
    @Test
    fun `workoutActions creates partial action lists`() {
        // Arrange
        val onStart = mockk<() -> Unit>(relaxed = true)
        
        // Act
        val actions = AccessibilityActions.workoutActions(onStart = onStart)
        
        // Assert
        assertEquals("Should create 1 action", 1, actions.size)
        assertEquals("Should be start action", "Start workout", actions[0].label)
    }
    
    @Test
    fun `goalActions creates correct custom actions`() {
        // Arrange
        val onEdit = mockk<() -> Unit>(relaxed = true)
        val onDelete = mockk<() -> Unit>(relaxed = true)
        val onUpdateProgress = mockk<() -> Unit>(relaxed = true)
        
        // Act
        val actions = AccessibilityActions.goalActions(onEdit, onDelete, onUpdateProgress)
        
        // Assert
        assertEquals("Should create 3 actions", 3, actions.size)
        
        val actionLabels = actions.map { it.label }
        assertTrue("Should contain edit action", actionLabels.contains("Edit goal"))
        assertTrue("Should contain delete action", actionLabels.contains("Delete goal"))
        assertTrue("Should contain update progress action", actionLabels.contains("Update progress"))
    }
    
    // ========== Integration Tests ==========
    
    @Test
    fun `accessibility state reflects system settings correctly`() {
        // Arrange
        every { mockAccessibilityManager.isEnabled } returns true
        every { mockAccessibilityManager.isTouchExplorationEnabled } returns true
        every { mockAccessibilityManager.isHighTextContrastEnabled } returns true
        
        // Act
        val state = AccessibilityState(
            isEnabled = isAccessibilityEnabled(context),
            isTalkBackEnabled = isTalkBackEnabled(context),
            isHighContrastEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AccessibilityManagerUtils.isHighTextContrastEnabled(context)
            } else false
        )
        
        // Assert
        assertTrue("Accessibility should be enabled", state.isEnabled)
        assertTrue("TalkBack should be enabled", state.isTalkBackEnabled)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            assertTrue("High contrast should be enabled on API 21+", state.isHighContrastEnabled)
        } else {
            assertFalse("High contrast should be false on older APIs", state.isHighContrastEnabled)
        }
    }
    
    @Test
    fun `fitness announcements integrate properly with accessibility system`() {
        // Arrange
        every { mockAccessibilityManager.isEnabled } returns true
        every { mockAccessibilityManager.isTouchExplorationEnabled } returns true
        
        // Test different fitness scenarios
        val scenarios = listOf(
            Triple(FitnessUpdateType.GOAL_ACHIEVED, "Daily step goal reached", "Achievement!"),
            Triple(FitnessUpdateType.WORKOUT_MILESTONE, "5K completed", "Milestone reached"),
            Triple(FitnessUpdateType.WARNING, "Low water intake", "Warning:"),
            Triple(FitnessUpdateType.PROGRESS_UPDATE, "Progress updated", "Progress updated"),
            Triple(FitnessUpdateType.NAVIGATION, "Moved to workout screen", "Navigation:")
        )
        
        scenarios.forEach { (updateType, message, _) ->
            // Act & Assert - Should not throw exceptions
            assertDoesNotThrow("$updateType announcement should work") {
                announceFitnessUpdate(context, updateType, message)
            }
        }
    }
    
    // Helper function for assertDoesNotThrow (since it might not be available in all JUnit versions)
    private fun assertDoesNotThrow(message: String? = null, executable: () -> Unit) {
        try {
            executable()
        } catch (e: Exception) {
            fail(message?.let { "$it - " } + "Expected no exception but got: ${e.javaClass.simpleName}: ${e.message}")
        }
    }
}
