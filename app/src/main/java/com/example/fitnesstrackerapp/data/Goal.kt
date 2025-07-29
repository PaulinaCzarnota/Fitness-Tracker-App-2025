package com.example.fitnesstrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Goal
 *
 * Represents a fitness goal defined by the user, such as:
 * - "Run 10 km this week"
 * - "Burn 3000 calories this month"
 * - "Complete 5 workouts this week"
 *
 * Each goal includes a description, a measurable target, current progress,
 * and a flag to indicate whether the goal has been achieved.
 * This data is stored in the "goal_table" Room table.
 */
@Entity(tableName = "goal_table")
data class Goal(

    /**
     * Primary key for the goal.
     * Automatically generated unique ID for each goal entry.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /**
     * Text description of the goal.
     * Should be short but informative.
     * Example: "Workout 5 times this week"
     */
    val description: String,

    /**
     * The target number to reach (e.g., 5 workouts, 3000 calories).
     * Must be positive (enforce via UI or ViewModel).
     */
    val target: Int,

    /**
     * Tracks the current progress made toward the goal.
     * Must be less than or equal to 'target'.
     * Example: 3 workouts completed so far.
     */
    val current: Int = 0,

    /**
     * Boolean indicating if the goal has been achieved.
     * Automatically updated by logic in ViewModel or business layer.
     */
    val achieved: Boolean = false
)
