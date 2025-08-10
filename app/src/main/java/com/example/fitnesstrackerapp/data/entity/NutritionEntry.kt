package com.example.fitnesstrackerapp.data.entity

/**
 * Nutrition Entry entity and related classes for the Fitness Tracker application.
 *
 * This file contains the NutritionEntry entity which stores comprehensive nutrition tracking data
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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date

/**
 * This entity stores comprehensive nutrition information including food details,
 * nutritional values, serving information, and meal categorization. All nutrition entries
 * are associated with a specific user through foreign key relationship.
 *
 * Database Features:
 * - Indexed for efficient querying by user, meal type, date, and food name
 * - Foreign key constraint ensures data integrity with User entity
 * - Cascading delete removes nutrition entries when user is deleted
 * - Enhanced with additional micronutrient tracking
 */
@Entity(
    tableName = "nutrition_entries",
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
        Index(value = ["user_id", "meal_type"]),
    ],
)
data class NutritionEntry(
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
    @ColumnInfo(name = "saturated_fat_grams")
    val saturatedFatGrams: Double = 0.0,
    @ColumnInfo(name = "trans_fat_grams")
    val transFatGrams: Double = 0.0,
    @ColumnInfo(name = "cholesterol_mg")
    val cholesterolMg: Double = 0.0,
    @ColumnInfo(name = "fiber_grams")
    val fiberGrams: Double = 0.0,
    @ColumnInfo(name = "sugar_grams")
    val sugarGrams: Double = 0.0,
    @ColumnInfo(name = "added_sugars_grams")
    val addedSugarsGrams: Double = 0.0,
    @ColumnInfo(name = "sodium_mg")
    val sodiumMg: Double = 0.0,
    @ColumnInfo(name = "potassium_mg")
    val potassiumMg: Double = 0.0,
    @ColumnInfo(name = "vitamin_c_mg")
    val vitaminCMg: Double = 0.0,
    @ColumnInfo(name = "vitamin_d_mcg")
    val vitaminDMcg: Double = 0.0,
    @ColumnInfo(name = "calcium_mg")
    val calciumMg: Double = 0.0,
    @ColumnInfo(name = "iron_mg")
    val ironMg: Double = 0.0,
    @ColumnInfo(name = "meal_type")
    val mealType: MealType,
    @ColumnInfo(name = "date_consumed")
    val dateConsumed: Date = Date(),
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    @ColumnInfo(name = "barcode")
    val barcode: String? = null,
    @ColumnInfo(name = "recipe_id")
    val recipeId: Long? = null,
    @ColumnInfo(name = "is_homemade")
    val isHomemade: Boolean = false,
    @ColumnInfo(name = "confidence_level")
    val confidenceLevel: Double = 1.0, // 0.0 to 1.0, accuracy of nutrition data
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),
    @ColumnInfo(name = "logged_at")
    val loggedAt: Date = Date(),
) {
    /**
     * Calculates total calories for this nutrition entry.
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
     * Calculates total protein for this nutrition entry.
     * @return Total protein in grams based on serving size
     */
    fun getTotalProtein(): Double {
        return proteinGrams * servingSize
    }

    /**
     * Calculates total carbohydrates for this nutrition entry.
     * @return Total carbs in grams based on serving size
     */
    fun getTotalCarbs(): Double {
        return carbsGrams * servingSize
    }

    /**
     * Calculates total fat for this nutrition entry.
     * @return Total fat in grams based on serving size
     */
    fun getTotalFat(): Double {
        return fatGrams * servingSize
    }

    /**
     * Calculates total saturated fat for this nutrition entry.
     * @return Total saturated fat in grams based on serving size
     */
    fun getTotalSaturatedFat(): Double {
        return saturatedFatGrams * servingSize
    }

    /**
     * Calculates total fiber for this nutrition entry.
     * @return Total fiber in grams based on serving size
     */
    fun getTotalFiber(): Double {
        return fiberGrams * servingSize
    }

    /**
     * Calculates total sugar for this nutrition entry.
     * @return Total sugar in grams based on serving size
     */
    fun getTotalSugar(): Double {
        return sugarGrams * servingSize
    }

    /**
     * Calculates total sodium for this nutrition entry.
     * @return Total sodium in mg based on serving size
     */
    fun getTotalSodium(): Double {
        return sodiumMg * servingSize
    }

    /**
     * Calculates total potassium for this nutrition entry.
     * @return Total potassium in mg based on serving size
     */
    fun getTotalPotassium(): Double {
        return potassiumMg * servingSize
    }

    /**
     * Calculates total vitamin C for this nutrition entry.
     * @return Total vitamin C in mg based on serving size
     */
    fun getTotalVitaminC(): Double {
        return vitaminCMg * servingSize
    }

    /**
     * Calculates total calcium for this nutrition entry.
     * @return Total calcium in mg based on serving size
     */
    fun getTotalCalcium(): Double {
        return calciumMg * servingSize
    }

    /**
     * Calculates total iron for this nutrition entry.
     * @return Total iron in mg based on serving size
     */
    fun getTotalIron(): Double {
        return ironMg * servingSize
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

        val proteinCalories = getTotalProtein() * CALORIES_PER_GRAM_PROTEIN
        val carbCalories = getTotalCarbs() * CALORIES_PER_GRAM_CARBS
        val fatCalories = getTotalFat() * CALORIES_PER_GRAM_FAT

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
     * Gets nutrition quality score based on nutrient density.
     * @return Quality score from 0.0 to 10.0
     */
    fun getNutritionQualityScore(): Double {
        val calories = getTotalCalories()
        if (calories <= 0) return 0.0

        var score = 0.0

        // Positive factors
        val proteinPercentage = (getTotalProtein() * CALORIES_PER_GRAM_PROTEIN) / calories
        val fiberPer100Cal = (getTotalFiber() / calories) * 100
        val vitaminCPer100Cal = (getTotalVitaminC() / calories) * 100
        val calciumPer100Cal = (getTotalCalcium() / calories) * 100
        val ironPer100Cal = (getTotalIron() / calories) * 100

        // Protein score (up to 2 points)
        score += (proteinPercentage * 10).coerceAtMost(2.0)

        // Fiber score (up to 2 points)
        score += (fiberPer100Cal * 0.5).coerceAtMost(2.0)

        // Micronutrient score (up to 3 points)
        score += ((vitaminCPer100Cal + calciumPer100Cal + ironPer100Cal) * 0.01).coerceAtMost(3.0)

        // Negative factors
        val saturatedFatPercentage = (getTotalSaturatedFat() * CALORIES_PER_GRAM_FAT) / calories
        val addedSugarPercentage = (getTotalSugar() * CALORIES_PER_GRAM_CARBS) / calories
        val sodiumPer100Cal = (getTotalSodium() / calories) * 100

        // Deduct for unhealthy components (up to 3 points deducted)
        score -= (saturatedFatPercentage * 5).coerceAtMost(1.5)
        score -= (addedSugarPercentage * 5).coerceAtMost(1.5)
        score -= (sodiumPer100Cal * 0.01).coerceAtMost(1.0)

        // Base quality points (up to 3 points)
        score += 3.0

        return score.coerceIn(0.0, 10.0)
    }

    /**
     * Validates if the nutrition entry data is consistent and valid.
     * @return true if nutrition entry data is valid, false otherwise
     */
    fun isValid(): Boolean {
        return foodName.isNotBlank() &&
            servingSize > 0 &&
            servingUnit.isNotBlank() &&
            caloriesPerServing >= 0 &&
            proteinGrams >= 0 &&
            carbsGrams >= 0 &&
            fatGrams >= 0 &&
            saturatedFatGrams >= 0 &&
            transFatGrams >= 0 &&
            cholesterolMg >= 0 &&
            fiberGrams >= 0 &&
            sugarGrams >= 0 &&
            addedSugarsGrams >= 0 &&
            sodiumMg >= 0 &&
            potassiumMg >= 0 &&
            vitaminCMg >= 0 &&
            vitaminDMcg >= 0 &&
            calciumMg >= 0 &&
            ironMg >= 0 &&
            confidenceLevel in 0.0..1.0
    }

    /**
     * Checks if this is a high-calorie food item.
     * @return true if calories per serving exceed 400
     */
    fun isHighCalorie(): Boolean {
        return caloriesPerServing > HIGH_CALORIE_THRESHOLD
    }

    /**
     * Checks if this is a high-protein food item.
     * @return true if protein content is more than 20% of calories
     */
    fun isHighProtein(): Boolean {
        val (proteinPercent, _, _) = getMacroDistribution()
        return proteinPercent > HIGH_PROTEIN_THRESHOLD_PERCENT
    }

    /**
     * Checks if this food item is high in fiber (>3g per serving).
     * @return true if high fiber food
     */
    fun isHighFiber(): Boolean {
        return fiberGrams > HIGH_FIBER_THRESHOLD
    }

    /**
     * Checks if this food item is low in sodium (<140mg per serving).
     * @return true if low sodium food
     */
    fun isLowSodium(): Boolean {
        return sodiumMg < LOW_SODIUM_THRESHOLD
    }

    /**
     * Checks if this is a whole food item (unprocessed).
     * @return true if likely to be a whole food
     */
    fun isWholeFood(): Boolean {
        return isHomemade ||
            (brandName == null && !foodName.contains("processed", ignoreCase = true)) ||
            confidenceLevel > 0.8
    }

    /**
     * Gets nutritional density rating.
     * @return Rating from "Poor" to "Excellent"
     */
    fun getNutritionalDensityRating(): String {
        val score = getNutritionQualityScore()
        return when {
            score >= 8.0 -> "Excellent"
            score >= 6.0 -> "Good"
            score >= 4.0 -> "Fair"
            score >= 2.0 -> "Poor"
            else -> "Very Poor"
        }
    }

    companion object {
        const val DEFAULT_SERVING_SIZE = 1.0
        const val HIGH_CALORIE_THRESHOLD = 400.0
        const val HIGH_PROTEIN_THRESHOLD_PERCENT = 20.0
        const val HIGH_FIBER_THRESHOLD = 3.0
        const val LOW_SODIUM_THRESHOLD = 140.0
        const val CALORIES_PER_GRAM_PROTEIN = 4
        const val CALORIES_PER_GRAM_CARBS = 4
        const val CALORIES_PER_GRAM_FAT = 9
        const val MAX_CONFIDENCE_LEVEL = 1.0
        const val MIN_CONFIDENCE_LEVEL = 0.0
    }
}
