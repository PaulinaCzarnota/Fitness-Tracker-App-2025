package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing nutrition/food intake in the Fitness Tracker application.
 *
 * This entity stores daily food consumption with nutritional information including:
 * - Food items and meal categorization
 * - Caloric and macronutrient tracking
 * - Portion sizes and preparation methods
 * - Integration with fitness goals and progress tracking
 */
@Entity(
    tableName = "nutrition",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["date"]),
        Index(value = ["mealType"]),
        Index(value = ["userId", "date"])
    ]
)
data class Nutrition(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "userId")
    val userId: Long,

    @ColumnInfo(name = "foodName")
    val foodName: String,

    @ColumnInfo(name = "mealType")
    val mealType: MealType,

    @ColumnInfo(name = "quantity")
    val quantity: Float,

    @ColumnInfo(name = "unit")
    val unit: String, // grams, cups, pieces, etc.

    @ColumnInfo(name = "calories")
    val calories: Float,

    @ColumnInfo(name = "protein")
    val protein: Float = 0f, // in grams

    @ColumnInfo(name = "carbohydrates")
    val carbohydrates: Float = 0f, // in grams

    @ColumnInfo(name = "fat")
    val fat: Float = 0f, // in grams

    @ColumnInfo(name = "fiber")
    val fiber: Float = 0f, // in grams

    @ColumnInfo(name = "sugar")
    val sugar: Float = 0f, // in grams

    @ColumnInfo(name = "sodium")
    val sodium: Float = 0f, // in mg

    @ColumnInfo(name = "date")
    val date: Date,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "createdAt")
    val createdAt: Date = Date()
) {
    /**
     * Calculates calories per 100g for comparison purposes.
     *
     * @return Calories per 100g or null if calculation not possible
     */
    fun getCaloriesPer100g(): Float? {
        return if (quantity > 0 && unit == "grams") {
            (calories / quantity) * 100f
        } else {
            null
        }
    }

    /**
     * Calculates protein percentage of total calories.
     *
     * @return Protein percentage (0-100)
     */
    fun getProteinPercentage(): Float {
        return if (calories > 0) {
            ((protein * 4f) / calories) * 100f
        } else {
            0f
        }
    }

    /**
     * Calculates carbohydrate percentage of total calories.
     *
     * @return Carbohydrate percentage (0-100)
     */
    fun getCarbsPercentage(): Float {
        return if (calories > 0) {
            ((carbohydrates * 4f) / calories) * 100f
        } else {
            0f
        }
    }

    /**
     * Calculates fat percentage of total calories.
     *
     * @return Fat percentage (0-100)
     */
    fun getFatPercentage(): Float {
        return if (calories > 0) {
            ((fat * 9f) / calories) * 100f
        } else {
            0f
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
        return getProteinPercentage() > 20f
    }

    /**
     * Checks if this food item is high in fiber (>3g per serving).
     *
     * @return true if high fiber food
     */
    fun isHighFiber(): Boolean {
        return fiber > 3f
    }

    /**
     * Checks if this food item is low in sodium (<140mg per serving).
     *
     * @return true if low sodium food
     */
    fun isLowSodium(): Boolean {
        return sodium < 140f
    }

    companion object {
        const val UNIT_GRAMS = "grams"
        const val UNIT_CUPS = "cups"
        const val UNIT_PIECES = "pieces"
        const val UNIT_TABLESPOONS = "tablespoons"
        const val UNIT_OUNCES = "ounces"
        const val UNIT_MILLILITERS = "milliliters"

        const val PREPARATION_RAW = "raw"
        const val PREPARATION_GRILLED = "grilled"
        const val PREPARATION_FRIED = "fried"
        const val PREPARATION_BAKED = "baked"
        const val PREPARATION_STEAMED = "steamed"
        const val PREPARATION_BOILED = "boiled"
    }
}
