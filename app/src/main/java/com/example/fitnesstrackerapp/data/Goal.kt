package com.example.fitnesstrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Goal
 *
 * Represents a fitness goal set by the user. Stored in the Room "goals" table.
 * Goals may include objectives like "Run 5km", "Workout 3 times a week", etc.
 */
@Entity(tableName = "goals")
data class Goal(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Auto-generated primary key for each goal

    val description: String, // Description of the goal (e.g., "Workout 3 times a week")

    val target: Int, // Target value to reach (e.g., 3 sessions)

    val current: Int = 0, // Progress so far (e.g., 2 completed)

    val achieved: Boolean = false // True if goal has been completed
)
