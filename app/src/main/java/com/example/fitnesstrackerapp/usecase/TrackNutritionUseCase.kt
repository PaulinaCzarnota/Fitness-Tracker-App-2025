package com.example.fitnesstrackerapp.usecase

import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.MealType
import com.example.fitnesstrackerapp.repository.SimpleNutritionRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date

/**
 * Use case for nutrition tracking functionality.
 *
 * Handles the business logic of food logging, calorie calculations, and nutrition statistics
 * while keeping ViewModels focused on UI state management.
 */
class TrackNutritionUseCase(
    private val nutritionRepository: SimpleNutritionRepository,
) {

    /**
     * Adds a food entry for a user
     */
    suspend fun addFoodEntry(
        userId: Long,
        foodName: String,
        calories: Double,
        servingSize: Double = 1.0,
        servingUnit: String = "serving",
        mealType: MealType,
        dateConsumed: Date = Date(),
    ): Result<FoodEntry> {
        return try {
            val foodEntry = FoodEntry(
                userId = userId,
                foodName = foodName,
                servingSize = servingSize,
                servingUnit = servingUnit,
                caloriesPerServing = calories,
                mealType = mealType,
                dateConsumed = dateConsumed,
            )
            nutritionRepository.addFoodEntry(foodEntry)
            Result.success(foodEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets all food entries for a user
     */
    fun getFoodEntriesForUser(userId: Long): Flow<List<FoodEntry>> {
        return nutritionRepository.getAllFoodEntriesForUser(userId)
    }

    /**
     * Gets food entries for today
     */
    fun getTodaysFoodEntries(userId: Long): Flow<List<FoodEntry>> {
        // This method would need to be implemented in the repository
        return nutritionRepository.getAllFoodEntriesForUser(userId)
    }

    /**
     * Gets food entries for a specific date
     */
    fun getFoodEntriesForDate(userId: Long, date: Date): Flow<List<FoodEntry>> {
        return nutritionRepository.getFoodEntriesForDate(userId, date)
    }

    /**
     * Gets food entries for a date range
     */
    fun getFoodEntriesForDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<FoodEntry>> {
        // This method would need to be implemented in the repository
        return nutritionRepository.getAllFoodEntriesForUser(userId)
    }

    /**
     * Deletes a food entry
     */
    suspend fun deleteFoodEntry(foodEntry: FoodEntry): Result<Unit> {
        return try {
            nutritionRepository.deleteFoodEntry(foodEntry)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates a food entry
     */
    suspend fun updateFoodEntry(foodEntry: FoodEntry): Result<Unit> {
        return try {
            nutritionRepository.updateFoodEntry(foodEntry)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculates daily nutrition statistics
     */
    fun calculateDailyNutritionStats(foodEntries: List<FoodEntry>): NutritionStats {
        val totalCalories = foodEntries.sumOf { it.calories }
        val entriesByMeal = foodEntries.groupBy { it.mealType }

        return NutritionStats(
            totalCalories = totalCalories,
            breakfastCalories = entriesByMeal[MealType.BREAKFAST]?.sumOf { it.calories } ?: 0.0,
            lunchCalories = entriesByMeal[MealType.LUNCH]?.sumOf { it.calories } ?: 0.0,
            dinnerCalories = entriesByMeal[MealType.DINNER]?.sumOf { it.calories } ?: 0.0,
            snackCalories = entriesByMeal[MealType.SNACK]?.sumOf { it.calories } ?: 0.0,
            totalEntries = foodEntries.size,
            averageCaloriesPerMeal = if (foodEntries.isNotEmpty()) totalCalories / foodEntries.size else 0.0,
        )
    }

    /**
     * Calculates weekly nutrition statistics
     */
    suspend fun getWeeklyNutritionStats(userId: Long): Result<WeeklyNutritionStats> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            calendar.time

            // This would need to be implemented to collect a single value from the flow
            // For now, we'll return a placeholder
            val stats = WeeklyNutritionStats(
                totalCalories = 0.0,
                averageDailyCalories = 0.0,
                highestDayCalories = 0.0,
                lowestDayCalories = 0.0,
                totalEntries = 0,
                daysLogged = 0,
            )

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validates food entry input
     */
    fun validateFoodEntry(
        foodName: String,
        calories: Double,
        servingSize: Double,
    ): ValidationResult {
        return when {
            foodName.isBlank() -> ValidationResult(false, "Food name cannot be empty")
            calories < 0 -> ValidationResult(false, "Calories cannot be negative")
            calories > 10000 -> ValidationResult(false, "Calories seem too high (max 10,000)")
            servingSize <= 0 -> ValidationResult(false, "Serving size must be greater than 0")
            servingSize > 100 -> ValidationResult(false, "Serving size seems too large (max 100)")
            else -> ValidationResult(true, "Valid food entry")
        }
    }

    /**
     * Gets recommended daily calorie intake based on basic factors
     */
    fun getRecommendedDailyCalories(
        age: Int,
        gender: Gender,
        activityLevel: ActivityLevel = ActivityLevel.MODERATE,
        weightKg: Double = 70.0,
    ): Int {
        // Simplified BMR calculation using Mifflin-St Jeor Equation
        val bmr = when (gender) {
            Gender.MALE -> 10 * weightKg + 6.25 * 175 - 5 * age + 5 // Assuming 175cm height
            Gender.FEMALE -> 10 * weightKg + 6.25 * 165 - 5 * age - 161 // Assuming 165cm height
        }

        val activityMultiplier = when (activityLevel) {
            ActivityLevel.SEDENTARY -> 1.2
            ActivityLevel.LIGHT -> 1.375
            ActivityLevel.MODERATE -> 1.55
            ActivityLevel.ACTIVE -> 1.725
            ActivityLevel.VERY_ACTIVE -> 1.9
        }

        return (bmr * activityMultiplier).toInt()
    }

    /**
     * Calculates calorie deficit/surplus
     */
    fun calculateCalorieBalance(consumedCalories: Double, recommendedCalories: Int): CalorieBalance {
        val difference = consumedCalories - recommendedCalories
        return CalorieBalance(
            consumed = consumedCalories,
            recommended = recommendedCalories.toDouble(),
            difference = difference,
            isDeficit = difference < 0,
            isSurplus = difference > 0,
        )
    }

    /**
     * Gets nutrition tips based on calorie intake
     */
    fun getNutritionTips(stats: NutritionStats, recommendedCalories: Int): List<String> {
        val tips = mutableListOf<String>()

        if (stats.totalCalories < recommendedCalories * 0.8) {
            tips.add("You're eating significantly below your recommended calories. Consider adding healthy snacks.")
        }

        if (stats.totalCalories > recommendedCalories * 1.2) {
            tips.add("You're eating significantly above your recommended calories. Consider smaller portions.")
        }

        if (stats.breakfastCalories < stats.totalCalories * 0.15) {
            tips.add("Try eating a more substantial breakfast - it's important for metabolism.")
        }

        if (stats.snackCalories > stats.totalCalories * 0.3) {
            tips.add("Consider reducing snack intake and focusing on balanced meals.")
        }

        if (stats.totalEntries < 3) {
            tips.add("Try to eat at least 3 meals a day for better nutrition distribution.")
        }

        return tips
    }
}

/**
 * Data class for daily nutrition statistics
 */
data class NutritionStats(
    val totalCalories: Double,
    val breakfastCalories: Double,
    val lunchCalories: Double,
    val dinnerCalories: Double,
    val snackCalories: Double,
    val totalEntries: Int,
    val averageCaloriesPerMeal: Double,
)

/**
 * Data class for weekly nutrition statistics
 */
data class WeeklyNutritionStats(
    val totalCalories: Double,
    val averageDailyCalories: Double,
    val highestDayCalories: Double,
    val lowestDayCalories: Double,
    val totalEntries: Int,
    val daysLogged: Int,
)

/**
 * Data class for calorie balance information
 */
data class CalorieBalance(
    val consumed: Double,
    val recommended: Double,
    val difference: Double,
    val isDeficit: Boolean,
    val isSurplus: Boolean,
)

/**
 * Data class for validation results
 */
data class ValidationResult(
    val isValid: Boolean,
    val message: String,
)

/**
 * Enum for gender
 */
enum class Gender {
    MALE,
    FEMALE,
}

/**
 * Enum for activity levels
 */
enum class ActivityLevel {
    SEDENTARY,
    LIGHT,
    MODERATE,
    ACTIVE,
    VERY_ACTIVE,
}
