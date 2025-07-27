package com.example.fitnesstrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Goal
 *
 * Represents a fitness goal set by the user, such as:
 * - "Run 10 km this week"
 * - "Burn 3000 calories this month"
 * - "Do 5 workouts this week"
 *
 * Stored in the Room database table "goal_table".
 */
@Entity(tableName = "goal_table")
data class Goal(

    /**
     * Primary key for the goal.
     * Auto-generated unique ID for each entry.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /**
     * A descriptive label for the goal.
     * Example: "Workout 5 times a week"
     */
    val description: String,

    /**
     * The target value to achieve.
     * Example: 5 workouts
     */
    val target: Int,

    /**
     * Tracks the user's current progress.
     * Example: 3 workouts completed so far.
     */
    val current: Int = 0,

    /**
     * Indicates if the goal has been achieved.
     * Automatically set by logic in ViewModel/UI.
     */
    val achieved: Boolean = false
)
