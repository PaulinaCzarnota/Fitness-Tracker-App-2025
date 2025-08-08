/**
 * Entity representing a food entry for nutrition tracking.
 *
 * Tracks daily food intake including nutritional information
 * for comprehensive diet and health monitoring.
 */

package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Food entry entity for tracking nutrition intake.
 */
@Entity(
    tableName = "food_entries",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["meal_type"]),
        Index(value = ["date_consumed"]),
        Index(value = ["food_name"])
    ]
)
data class FoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "food_name")
    val foodName: String,

    @ColumnInfo(name = "brand_name")
    val brandName: String? = null,

    @ColumnInfo(name = "serving_size")
    val servingSize: Double,

    @ColumnInfo(name = "serving_unit")
    val servingUnit: String, // grams, cups, pieces, etc.

    @ColumnInfo(name = "calories_per_serving")
    val caloriesPerServing: Double,

    @ColumnInfo(name = "protein_grams")
    val proteinGrams: Double = 0.0,

    @ColumnInfo(name = "carbs_grams")
    val carbsGrams: Double = 0.0,

    @ColumnInfo(name = "fat_grams")
    val fatGrams: Double = 0.0,

    @ColumnInfo(name = "fiber_grams")
    val fiberGrams: Double = 0.0,

    @ColumnInfo(name = "sugar_grams")
    val sugarGrams: Double = 0.0,

    @ColumnInfo(name = "sodium_mg")
    val sodiumMg: Double = 0.0,

    @ColumnInfo(name = "meal_type")
    val mealType: MealType,

    @ColumnInfo(name = "date_consumed")
    val dateConsumed: Date = Date(),

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "logged_at")
    val loggedAt: Date = Date()
) {
    /**
     * Calculate total calories for this entry.
     */
    fun getTotalCalories(): Double {
        return caloriesPerServing * servingSize
    }

    /**
     * Get calories as alias for compatibility
     */
    val calories: Double
        get() = getTotalCalories()

    /**
     * Get protein as alias for compatibility
     */
    val protein: Double
        get() = proteinGrams * servingSize

    /**
     * Get carbs as alias for compatibility
     */
    val carbs: Double
        get() = carbsGrams * servingSize

    /**
     * Get fat as alias for compatibility
     */
    val fat: Double
        get() = fatGrams * servingSize

    /**
     * Get name as alias for compatibility
     */
    val name: String
        get() = foodName

    /**
     * Get date as alias for compatibility
     */
    val date: Date
        get() = dateConsumed

    /**
     * Calculates calories per 100g for comparison purposes.
     *
     * @return Calories per 100g or null if calculation not possible
     */
    fun getCaloriesPer100g(): Double? {
        return if (servingSize > 0 && servingUnit == "grams") {
            (caloriesPerServing / servingSize) * 100.0
        } else {
            null
        }
    }

    /**
     * Calculates protein percentage of total calories.
     *
     * @return Protein percentage (0-100)
     */
    fun getProteinPercentage(): Double {
        return if (getTotalCalories() > 0) {
            ((proteinGrams * 4.0) / getTotalCalories()) * 100.0
        } else {
            0.0
        }
    }

    /**
     * Calculates carbohydrate percentage of total calories.
     *
     * @return Carbohydrate percentage (0-100)
     */
    fun getCarbsPercentage(): Double {
        return if (getTotalCalories() > 0) {
            ((carbsGrams * 4.0) / getTotalCalories()) * 100.0
        } else {
            0.0
        }
    }

    /**
     * Calculates fat percentage of total calories.
     *
     * @return Fat percentage (0-100)
     */
    fun getFatPercentage(): Double {
        return if (getTotalCalories() > 0) {
            ((fatGrams * 9.0) / getTotalCalories()) * 100.0
        } else {
            0.0
        }
    }

    /**
     * Gets macronutrient breakdown as a formatted string.
     *
     * @return Formatted macronutrient string
     */
    fun getMacroBreakdown(): String {
        val proteinPct = getProteinPercentage().toInt()
        val carbsPct = getCarbsPercentage().toInt()
        val fatPct = getFatPercentage().toInt()

        return "P: ${proteinPct}% | C: ${carbsPct}% | F: ${fatPct}%"
    }

    /**
     * Checks if this food item is high in protein (>20% of calories).
     *
     * @return true if high protein food
     */
    fun isHighProtein(): Boolean {
        return getProteinPercentage() > 20.0
    }

    /**
     * Checks if this food item is high in fiber (>3g per serving).
     *
     * @return true if high fiber food
     */
    fun isHighFiber(): Boolean {
        return fiberGrams > 3.0
    }

    /**
     * Checks if this food item is low in sodium (<140mg per serving).
     *
     * @return true if low sodium food
     */
    fun isLowSodium(): Boolean {
        return sodiumMg < 140.0
    }
}
