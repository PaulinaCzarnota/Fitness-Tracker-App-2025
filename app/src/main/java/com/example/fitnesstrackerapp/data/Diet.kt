package com.example.fitnesstrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Diet
 *
 * Represents a single food intake log for diet/nutrition tracking.
 * Stored in the 'diets' table of the Room database.
 */
@Entity(tableName = "diets")
data class Diet(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Auto-generated unique ID for each entry

    val food: String, // Description of the food item (e.g., "Banana")

    val calories: Int, // Caloric value of the food item

    val date: Long = System.currentTimeMillis() // Time of consumption (default = now)
)
