package com.example.fitnesstrackerapp.data.model

/**
 * Data model for notification performance insights.
 * 
 * Contains detailed analytics and trends for notification
 * performance optimization and monitoring.
 */
data class NotificationInsights(
    val totalSent: Int = 0,
    val totalDelivered: Int = 0,
    val totalFailed: Int = 0,
    val totalClicked: Int = 0,
    val totalDismissed: Int = 0,
    val averageDeliveryLatencyMs: Double = 0.0,
    val deliverySuccessRate: Double = 0.0,
    val clickThroughRate: Double = 0.0,
    val maxRetryCount: Int = 0,
    val commonErrors: List<ErrorFrequency> = emptyList(),
    val weeklyTrend: Double = 0.0 // Positive means improvement
)
