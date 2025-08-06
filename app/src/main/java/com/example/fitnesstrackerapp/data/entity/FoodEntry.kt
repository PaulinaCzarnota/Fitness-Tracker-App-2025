/**
 * FoodEntry Entity Class
 *
 * Responsibilities:
 * - Represents a food entry in the nutrition tracking system
 * - Stores food details like name, calories, meal type, and timestamp
 * - Used by Room database for nutrition data persistence
 */

package com.example.fitnesstrackerapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Data class representing a food entry in the nutrition tracking system
 *
 * @property id Unique identifier for the food entry
 * @property name Name of the food item
 * @property calories Caloric value of the food
 * @property mealType Type of meal (breakfast, lunch, dinner, snack)
 * @property quantity Quantity consumed
 * @property unit Unit of measurement (grams, pieces, etc.)
 * @property createdAt Timestamp when the entry was created
 * @property userId ID of the user who created this entry
 */
@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val calories: Int,
    val mealType: String,
    val quantity: Double = 1.0,
    val unit: String = "serving",
    val createdAt: Date = Date(),
    val userId: Long
)
