package com.example.fitnesstrackerapp.data.model

/**
 * Data model for notification system performance metrics.
 * 
 * Contains system-level metrics for monitoring notification
 * infrastructure health and performance.
 */
data class NotificationSystemMetrics(
    val dailyVolume: Int = 0,
    val weeklyVolume: Int = 0,
    val averageLatencyMs: Double = 0.0,
    val errorRate: Double = 0.0,
    val retryRate: Double = 0.0,
    val maxRetryCount: Int = 0,
    val topErrors: List<String> = emptyList(),
    val healthScore: Double = 100.0 // 0-100 scale
)
