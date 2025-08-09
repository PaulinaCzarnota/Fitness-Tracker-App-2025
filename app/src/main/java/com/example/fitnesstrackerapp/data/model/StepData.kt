package com.example.fitnesstrackerapp.data.model

/**
 * Unified data class for step tracking across services
 *
 * Used for UI integration and service communication
 */
data class StepData(
    val steps: Int,
    val goal: Int,
    val progress: Float,
    val distance: Float,
    val calories: Float,
    val isTracking: Boolean,
)
