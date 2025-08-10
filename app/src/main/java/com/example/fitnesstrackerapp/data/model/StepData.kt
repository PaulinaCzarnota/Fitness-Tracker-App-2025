package com.example.fitnesstrackerapp.data.model

/**
 * Unified data model for step tracking across services.
 *
 * This data class provides a standardized interface for step tracking data
 * communication between different components, services, and UI elements.
 * It encapsulates all essential step tracking metrics in a single model.
 *
 * Key Features:
 * - Comprehensive step tracking metrics
 * - Progress calculation and goal tracking
 * - Distance and calorie estimation
 * - Service state monitoring
 */
data class StepData(
    val steps: Int,
    val goal: Int,
    val progress: Float,
    val distance: Float,
    val calories: Float,
    val isTracking: Boolean,
)
