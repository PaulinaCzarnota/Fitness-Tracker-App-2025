package com.example.fitnesstrackerapp.data.model

import androidx.room.ColumnInfo

/**
 * Data model for notification delivery statistics.
 *
 * Contains comprehensive metrics about notification delivery
 * performance for analytics and monitoring purposes.
 */
data class NotificationDeliveryStats(
    @ColumnInfo(name = "total_sent")
    val totalSent: Int = 0,
    @ColumnInfo(name = "total_delivered")
    val totalDelivered: Int = 0,
    @ColumnInfo(name = "total_failed")
    val totalFailed: Int = 0,
    @ColumnInfo(name = "total_clicked")
    val totalClicked: Int = 0,
    @ColumnInfo(name = "total_dismissed")
    val totalDismissed: Int = 0,
    @ColumnInfo(name = "avg_delivery_time")
    val averageDeliveryLatencyMs: Double = 0.0,
) {
    // Computed properties for additional analytics
    val deliverySuccessRate: Double
        get() = if (totalSent > 0) (totalDelivered.toDouble() / totalSent.toDouble()) * 100.0 else 0.0

    val clickThroughRate: Double
        get() = if (totalDelivered > 0) (totalClicked.toDouble() / totalDelivered.toDouble()) * 100.0 else 0.0

    val dismissalRate: Double
        get() = if (totalDelivered > 0) (totalDismissed.toDouble() / totalDelivered.toDouble()) * 100.0 else 0.0
}
