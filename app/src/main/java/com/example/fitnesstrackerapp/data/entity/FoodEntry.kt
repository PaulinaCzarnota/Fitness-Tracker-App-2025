package com.example.fitnesstrackerapp.data.entity

/**
 * Entity representing a food entry in the Fitness Tracker application.
 *
 * Features:
 * - Daily food intake logging
 * - Nutritional information tracking
 * - Meal categorization
 * - Calorie and macro tracking
 */

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "food_entries",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class FoodEntry(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val calories: Int,
    val mealType: String, // e.g., Breakfast, Lunch, Dinner, Snack
    val date: Date
)
