package com.example.fitnesstrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class representing a fitness goal, such as "Run 5km" or "Workout 3 times this week".
 * Stored in the Room database in the 'goals' table.
 */
@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,               // Unique identifier for the goal

    val description: String,       // Textual description of the goal (e.g., "Run 5km")

    val target: Int,               // Target value (e.g., 5km, 3 sessions, 10000 steps)

    val current: Int = 0,          // Current progress toward the goal

    val achieved: Boolean = false  // Flag to indicate whether the goal has been achieved
)
