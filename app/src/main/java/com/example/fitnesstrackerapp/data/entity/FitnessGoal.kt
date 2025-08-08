/**
 * Fitness Goal Entity
 *
 * Represents a fitness goal with progress tracking capabilities.
 * Maps to the fitness_goals table in the Room database.
 */
package com.example.fitnesstrackerapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "fitness_goals")
data class FitnessGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val description: String = "",
    val type: GoalType,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val unit: String = "",
    val deadline: Date? = null,
    val frequency: String = "ONCE", // DAILY, WEEKLY, MONTHLY, ONCE
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val completedAt: Date? = null,
    val isActive: Boolean = true,
) {
    val progress: Double
        get() = if (targetValue > 0) (currentValue / targetValue * 100).coerceAtMost(100.0) else 0.0

    val name: String
        get() = title
}
