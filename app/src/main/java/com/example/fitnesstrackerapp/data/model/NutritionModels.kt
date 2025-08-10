package com.example.fitnesstrackerapp.data.model

import androidx.room.ColumnInfo

/**
 * Data model for daily nutrition summaries.
 * Used for tracking nutritional intake patterns over time.
 */
data class DailyNutritionSummary(
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "total_calories") val totalCalories: Double,
    @ColumnInfo(name = "total_protein") val totalProtein: Double,
    @ColumnInfo(name = "total_carbs") val totalCarbs: Double,
    @ColumnInfo(name = "total_fat") val totalFat: Double,
    @ColumnInfo(name = "total_fiber") val totalFiber: Double,
    @ColumnInfo(name = "total_sodium") val totalSodium: Double,
    @ColumnInfo(name = "entry_count") val entryCount: Int,
)

/**
 * Data model for notification analytics and performance metrics.
 * Used for analyzing notification delivery success rates and user engagement.
 */
data class NotificationAnalytics(
    @ColumnInfo(name = "delivery_channel") val deliveryChannel: String,
    @ColumnInfo(name = "total_attempts") val totalAttempts: Int,
    @ColumnInfo(name = "successful_attempts") val successfulAttempts: Int,
    @ColumnInfo(name = "success_rate") val successRate: Double,
)

/**
 * Data model for comprehensive notification performance metrics.
 * Used for monitoring and optimizing notification system performance.
 */
data class NotificationPerformanceMetrics(
    @ColumnInfo(name = "total_events") val totalEvents: Int,
    @ColumnInfo(name = "avg_processing_duration") val avgProcessingDuration: Double?,
    @ColumnInfo(name = "min_processing_duration") val minProcessingDuration: Double?,
    @ColumnInfo(name = "max_processing_duration") val maxProcessingDuration: Double?,
    @ColumnInfo(name = "avg_delivery_duration") val avgDeliveryDuration: Double?,
    @ColumnInfo(name = "successful_events") val successfulEvents: Int,
    @ColumnInfo(name = "failed_events") val failedEvents: Int,
) {
    /**
     * Calculates the success rate as a percentage.
     */
    fun getSuccessRate(): Double {
        return if (totalEvents > 0) (successfulEvents.toDouble() / totalEvents) * 100 else 0.0
    }

    /**
     * Calculates the failure rate as a percentage.
     */
    fun getFailureRate(): Double {
        return if (totalEvents > 0) (failedEvents.toDouble() / totalEvents) * 100 else 0.0
    }
}

/**
 * Data model for user interaction patterns by hour.
 * Used for analyzing when users are most likely to interact with notifications.
 */
data class HourlyInteractionPattern(
    @ColumnInfo(name = "hour") val hour: String,
    @ColumnInfo(name = "interaction_count") val interactionCount: Int,
)

/**
 * Data model for retry statistics.
 * Used for analyzing notification retry patterns and success rates.
 */
data class RetryStatistics(
    @ColumnInfo(name = "total_retries") val totalRetries: Int,
    @ColumnInfo(name = "avg_retry_count") val avgRetryCount: Double?,
    @ColumnInfo(name = "max_retry_count") val maxRetryCount: Int,
    @ColumnInfo(name = "successful_retries") val successfulRetries: Int,
) {
    /**
     * Calculates the retry success rate as a percentage.
     */
    fun getRetrySuccessRate(): Double {
        return if (totalRetries > 0) (successfulRetries.toDouble() / totalRetries) * 100 else 0.0
    }
}

/**
 * Data model for experiment performance metrics.
 * Used for A/B testing and experiment analysis.
 */
data class ExperimentPerformance(
    @ColumnInfo(name = "total_notifications") val totalNotifications: Int,
    @ColumnInfo(name = "delivered_count") val deliveredCount: Int,
    @ColumnInfo(name = "opened_count") val openedCount: Int,
    @ColumnInfo(name = "action_clicked_count") val actionClickedCount: Int,
    @ColumnInfo(name = "avg_processing_duration") val avgProcessingDuration: Double?,
) {
    /**
     * Calculates the delivery rate as a percentage.
     */
    fun getDeliveryRate(): Double {
        return if (totalNotifications > 0) (deliveredCount.toDouble() / totalNotifications) * 100 else 0.0
    }

    /**
     * Calculates the open rate as a percentage.
     */
    fun getOpenRate(): Double {
        return if (deliveredCount > 0) (openedCount.toDouble() / deliveredCount) * 100 else 0.0
    }

    /**
     * Calculates the action click rate as a percentage.
     */
    fun getActionClickRate(): Double {
        return if (deliveredCount > 0) (actionClickedCount.toDouble() / deliveredCount) * 100 else 0.0
    }
}

/**
 * Data model for batch processing statistics.
 * Used for monitoring batch notification processing performance.
 */
data class BatchStatistics(
    @ColumnInfo(name = "total_notifications") val totalNotifications: Int,
    @ColumnInfo(name = "successful_notifications") val successfulNotifications: Int,
    @ColumnInfo(name = "avg_processing_duration") val avgProcessingDuration: Double?,
    @ColumnInfo(name = "batch_start_time") val batchStartTime: Long,
    @ColumnInfo(name = "batch_end_time") val batchEndTime: Long,
) {
    /**
     * Calculates the batch success rate as a percentage.
     */
    fun getSuccessRate(): Double {
        return if (totalNotifications > 0) (successfulNotifications.toDouble() / totalNotifications) * 100 else 0.0
    }

    /**
     * Calculates the total batch processing duration in milliseconds.
     */
    fun getBatchDuration(): Long {
        return batchEndTime - batchStartTime
    }
}

/**
 * Data model for system health metrics.
 * Used for monitoring overall notification system health and performance.
 */
data class SystemHealthMetrics(
    @ColumnInfo(name = "total_events") val totalEvents: Int,
    @ColumnInfo(name = "overall_success_rate") val overallSuccessRate: Double,
    @ColumnInfo(name = "avg_processing_time") val avgProcessingTime: Double?,
    @ColumnInfo(name = "active_users") val activeUsers: Int,
    @ColumnInfo(name = "unique_notifications") val uniqueNotifications: Int,
) {
    /**
     * Determines system health status based on success rate and processing time.
     */
    fun getHealthStatus(): String {
        return when {
            overallSuccessRate >= 95.0 && (avgProcessingTime ?: 0.0) <= 500 -> "Excellent"
            overallSuccessRate >= 90.0 && (avgProcessingTime ?: 0.0) <= 1000 -> "Good"
            overallSuccessRate >= 80.0 && (avgProcessingTime ?: 0.0) <= 2000 -> "Fair"
            overallSuccessRate >= 70.0 -> "Poor"
            else -> "Critical"
        }
    }
}
