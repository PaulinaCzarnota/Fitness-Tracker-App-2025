/**
 * Food Entry entity and related classes for the Fitness Tracker application.
 *
 * This file contains the FoodEntry entity which stores comprehensive nutrition tracking data
 * including food information, nutritional values, serving sizes, and meal categorization.
 * The entity uses Room database annotations for optimal storage and retrieval performance.
 *
 * Key Features:
 * - Detailed nutritional information tracking (calories, macros, micronutrients)
 * - Meal type categorization for comprehensive daily tracking
 * - Serving size management with flexible units
 * - Brand and food name tracking for accurate identification
 * - Date and time logging for historical analysis
 * - Foreign key relationship with User entity for data integrity
 */

package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date

/**
 * Entity representing a food entry for nutrition tracking in the Fitness Tracker application.
 *
 * This entity stores comprehensive nutrition information including food details,
 * nutritional values, serving information, and meal categorization. All food entries
 * are associated with a specific user through foreign key relationship.
 *
 * Database Features:
 * - Indexed for efficient querying by user, meal type, date, and food name
 * - Foreign key constraint ensures data integrity with User entity
 * - Cascading delete removes food entries when user is deleted
 */
@Entity(
    tableName = "food_entries",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["meal_type"]),
        Index(value = ["date_consumed"]),
        Index(value = ["food_name"]),
        Index(value = ["user_id", "date_consumed"]),
    ],
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
    val loggedAt: Date = Date(),
) {
    /**
     * Calculates total calories for this food entry.
     * @return Total calories based on serving size
     */
    fun getTotalCalories(): Double {
        return caloriesPerServing * servingSize
    }

    /**
     * Gets calories as alias for compatibility.
     */
    val calories: Double
        get() = getTotalCalories()

    /**
     * Calculates total protein for this food entry.
     * @return Total protein in grams based on serving size
     */
    fun getTotalProtein(): Double {
        return proteinGrams * servingSize
    }

    /**
     * Calculates total carbohydrates for this food entry.
     * @return Total carbs in grams based on serving size
     */
    fun getTotalCarbs(): Double {
        return carbsGrams * servingSize
    }

    /**
     * Calculates total fat for this food entry.
     * @return Total fat in grams based on serving size
     */
    fun getTotalFat(): Double {
        return fatGrams * servingSize
    }

    /**
     * Calculates total fiber for this food entry.
     * @return Total fiber in grams based on serving size
     */
    fun getTotalFiber(): Double {
        return fiberGrams * servingSize
    }

    /**
     * Calculates total sugar for this food entry.
     * @return Total sugar in grams based on serving size
     */
    fun getTotalSugar(): Double {
        return sugarGrams * servingSize
    }

    /**
     * Calculates total sodium for this food entry.
     * @return Total sodium in mg based on serving size
     */
    fun getTotalSodium(): Double {
        return sodiumMg * servingSize
    }

    /**
     * Gets formatted date string for display.
     * @return Formatted date string (DD/MM/YYYY)
     */
    fun getFormattedDate(): String {
        val calendar = Calendar.getInstance()
        calendar.time = dateConsumed
        return "${calendar.get(
            Calendar.DAY_OF_MONTH,
        )}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
    }

    /**
     * Gets formatted serving size string for display.
     * @return Formatted serving size with unit
     */
    fun getFormattedServingSize(): String {
        return "%.1f %s".format(servingSize, servingUnit)
    }

    /**
     * Gets formatted nutritional summary for display.
     * @return Nutritional summary string
     */
    fun getNutritionalSummary(): String {
        return "Calories: %.0f | Protein: %.1fg | Carbs: %.1fg | Fat: %.1fg".format(
            getTotalCalories(),
            getTotalProtein(),
            getTotalCarbs(),
            getTotalFat(),
        )
    }

    /**
     * Calculates macronutrient distribution percentages.
     * @return Triple of (protein%, carbs%, fat%) percentages
     */
    fun getMacroDistribution(): Triple<Double, Double, Double> {
        val totalCalories = getTotalCalories()
        if (totalCalories <= 0) return Triple(0.0, 0.0, 0.0)

        val proteinCalories = getTotalProtein() * 4 // 4 calories per gram
        val carbCalories = getTotalCarbs() * 4 // 4 calories per gram
        val fatCalories = getTotalFat() * 9 // 9 calories per gram

        val proteinPercent = (proteinCalories / totalCalories) * 100
        val carbPercent = (carbCalories / totalCalories) * 100
        val fatPercent = (fatCalories / totalCalories) * 100

        return Triple(proteinPercent, carbPercent, fatPercent)
    }

    /**
     * Gets full food display name including brand if available.
     * @return Full food name with brand information
     */
    fun getFullFoodName(): String {
        return if (brandName != null) "$brandName $foodName" else foodName
    }

    /**
     * Validates if the food entry data is consistent and valid.
     * @return true if food entry data is valid, false otherwise
     */
    fun isValid(): Boolean {
        return foodName.isNotBlank() &&
            servingSize > 0 &&
            servingUnit.isNotBlank() &&
            caloriesPerServing >= 0 &&
            proteinGrams >= 0 &&
            carbsGrams >= 0 &&
            fatGrams >= 0 &&
            fiberGrams >= 0 &&
            sugarGrams >= 0 &&
            sodiumMg >= 0
    }

    /**
     * Checks if this is a high-calorie food item.
     * @return true if calories per serving exceed 400
     */
    fun isHighCalorie(): Boolean {
        return caloriesPerServing > 400
    }

    /**
     * Checks if this is a high-protein food item.
     * @return true if protein content is more than 20% of calories
     */
    fun isHighProtein(): Boolean {
        val (proteinPercent, _, _) = getMacroDistribution()
        return proteinPercent > 20
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

    companion object {
        const val DEFAULT_SERVING_SIZE = 1.0
        const val HIGH_CALORIE_THRESHOLD = 400.0
        const val HIGH_PROTEIN_THRESHOLD_PERCENT = 20.0
        const val CALORIES_PER_GRAM_PROTEIN = 4
        const val CALORIES_PER_GRAM_CARBS = 4
        const val CALORIES_PER_GRAM_FAT = 9
    }
}
