/**
 * Food Item model for offline food database in the Fitness Tracker application.
 *
 * This data class represents a food item with comprehensive nutritional information
 * used for food search and entry creation. It serves as a template for creating
 * FoodEntry entities with accurate nutritional data.
 *
 * Key Features:
 * - Complete macronutrient information per serving
 * - Standardized serving size descriptions
 * - Calorie and macro calculations
 * - Food database structure for offline usage
 */

package com.example.fitnesstrackerapp.data.model

/**
 * Food item data class for offline food database
 *
 * Represents a food item with nutritional information per standard serving.
 * This is used to populate the food search database and create food entries
 * with accurate nutritional data.
 *
 * @param name Display name of the food item
 * @param caloriesPerServing Calories per standard serving
 * @param proteinPerServing Protein in grams per serving  
 * @param carbsPerServing Carbohydrates in grams per serving
 * @param fatPerServing Fat in grams per serving
 * @param fiberPerServing Fiber in grams per serving
 * @param servingSize Description of standard serving size (e.g., "100g", "1 cup")
 * @param category Optional food category for filtering
 * @param brand Optional brand name
 * @param sugarPerServing Sugar in grams per serving
 * @param sodiumPerServing Sodium in mg per serving
 */
data class FoodItem(
    val name: String,
    val caloriesPerServing: Double,
    val proteinPerServing: Double,
    val carbsPerServing: Double,
    val fatPerServing: Double,
    val fiberPerServing: Double,
    val servingSize: String,
    val category: String? = null,
    val brand: String? = null,
    val sugarPerServing: Double = 0.0,
    val sodiumPerServing: Double = 0.0,
) {
    /**
     * Gets the calorie density (calories per gram) if serving size is in grams
     */
    fun getCalorieDensity(): Double? {
        val gramsMatch = Regex("""(\d+(?:\.\d+)?)g""").find(servingSize)
        return gramsMatch?.let { match ->
            val grams = match.groupValues[1].toDouble()
            caloriesPerServing / grams
        }
    }

    /**
     * Calculates total nutrition for a given quantity
     */
    fun calculateNutrition(quantity: Float): NutritionValues {
        return NutritionValues(
            calories = caloriesPerServing * quantity,
            protein = proteinPerServing * quantity,
            carbs = carbsPerServing * quantity,
            fat = fatPerServing * quantity,
            fiber = fiberPerServing * quantity,
            sugar = sugarPerServing * quantity,
            sodium = sodiumPerServing * quantity
        )
    }

    /**
     * Gets formatted display name with brand if available
     */
    fun getDisplayName(): String {
        return if (brand != null) "$brand $name" else name
    }

    /**
     * Checks if this is a high-protein food (>20% calories from protein)
     */
    fun isHighProtein(): Boolean {
        return if (caloriesPerServing > 0) {
            val proteinCalories = proteinPerServing * 4
            (proteinCalories / caloriesPerServing) >= 0.2
        } else false
    }

    /**
     * Checks if this is a low-calorie food (<40 calories per serving)
     */
    fun isLowCalorie(): Boolean {
        return caloriesPerServing < 40
    }

    /**
     * Checks if this is high in fiber (>3g per serving)
     */
    fun isHighFiber(): Boolean {
        return fiberPerServing > 3.0
    }
}

/**
 * Data class to hold calculated nutrition values for a specific quantity
 */
data class NutritionValues(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val sugar: Double,
    val sodium: Double,
)

/**
 * Predefined food database for offline usage
 */
object FoodDatabase {
    /**
     * Gets the default food database with common foods and their nutritional information
     */
    fun getDefaultFoods(): List<FoodItem> = listOf(
        // Fruits
        FoodItem(
            name = "Apple",
            caloriesPerServing = 52.0,
            proteinPerServing = 0.3,
            carbsPerServing = 14.0,
            fatPerServing = 0.2,
            fiberPerServing = 2.4,
            servingSize = "medium (182g)",
            category = "Fruits",
            sugarPerServing = 10.4,
            sodiumPerServing = 1.0
        ),
        FoodItem(
            name = "Banana",
            caloriesPerServing = 89.0,
            proteinPerServing = 1.1,
            carbsPerServing = 23.0,
            fatPerServing = 0.3,
            fiberPerServing = 2.6,
            servingSize = "medium (118g)",
            category = "Fruits",
            sugarPerServing = 12.2,
            sodiumPerServing = 1.0
        ),
        FoodItem(
            name = "Orange",
            caloriesPerServing = 47.0,
            proteinPerServing = 0.9,
            carbsPerServing = 12.0,
            fatPerServing = 0.1,
            fiberPerServing = 2.4,
            servingSize = "medium (154g)",
            category = "Fruits",
            sugarPerServing = 9.4,
            sodiumPerServing = 0.0
        ),

        // Vegetables
        FoodItem(
            name = "Broccoli",
            caloriesPerServing = 34.0,
            proteinPerServing = 2.8,
            carbsPerServing = 7.0,
            fatPerServing = 0.4,
            fiberPerServing = 2.6,
            servingSize = "100g",
            category = "Vegetables",
            sugarPerServing = 1.5,
            sodiumPerServing = 33.0
        ),
        FoodItem(
            name = "Sweet Potato",
            caloriesPerServing = 86.0,
            proteinPerServing = 1.6,
            carbsPerServing = 20.0,
            fatPerServing = 0.1,
            fiberPerServing = 3.0,
            servingSize = "100g",
            category = "Vegetables",
            sugarPerServing = 4.2,
            sodiumPerServing = 7.0
        ),
        FoodItem(
            name = "Spinach",
            caloriesPerServing = 23.0,
            proteinPerServing = 2.9,
            carbsPerServing = 3.6,
            fatPerServing = 0.4,
            fiberPerServing = 2.2,
            servingSize = "100g",
            category = "Vegetables",
            sugarPerServing = 0.4,
            sodiumPerServing = 79.0
        ),

        // Grains
        FoodItem(
            name = "White Rice",
            caloriesPerServing = 130.0,
            proteinPerServing = 2.7,
            carbsPerServing = 28.0,
            fatPerServing = 0.3,
            fiberPerServing = 0.4,
            servingSize = "100g",
            category = "Grains",
            sugarPerServing = 0.1,
            sodiumPerServing = 5.0
        ),
        FoodItem(
            name = "Brown Rice",
            caloriesPerServing = 111.0,
            proteinPerServing = 2.6,
            carbsPerServing = 23.0,
            fatPerServing = 0.9,
            fiberPerServing = 1.8,
            servingSize = "100g",
            category = "Grains",
            sugarPerServing = 0.4,
            sodiumPerServing = 5.0
        ),
        FoodItem(
            name = "Whole Wheat Bread",
            caloriesPerServing = 247.0,
            proteinPerServing = 13.0,
            carbsPerServing = 41.0,
            fatPerServing = 4.2,
            fiberPerServing = 7.0,
            servingSize = "100g",
            category = "Grains",
            sugarPerServing = 5.4,
            sodiumPerServing = 540.0
        ),
        FoodItem(
            name = "Oatmeal",
            caloriesPerServing = 68.0,
            proteinPerServing = 2.4,
            carbsPerServing = 12.0,
            fatPerServing = 1.4,
            fiberPerServing = 1.7,
            servingSize = "100g",
            category = "Grains",
            sugarPerServing = 0.3,
            sodiumPerServing = 4.0
        ),

        // Proteins
        FoodItem(
            name = "Chicken Breast",
            caloriesPerServing = 165.0,
            proteinPerServing = 31.0,
            carbsPerServing = 0.0,
            fatPerServing = 3.6,
            fiberPerServing = 0.0,
            servingSize = "100g",
            category = "Proteins",
            sugarPerServing = 0.0,
            sodiumPerServing = 74.0
        ),
        FoodItem(
            name = "Salmon",
            caloriesPerServing = 208.0,
            proteinPerServing = 25.0,
            carbsPerServing = 0.0,
            fatPerServing = 12.0,
            fiberPerServing = 0.0,
            servingSize = "100g",
            category = "Proteins",
            sugarPerServing = 0.0,
            sodiumPerServing = 59.0
        ),
        FoodItem(
            name = "Eggs",
            caloriesPerServing = 155.0,
            proteinPerServing = 13.0,
            carbsPerServing = 1.1,
            fatPerServing = 11.0,
            fiberPerServing = 0.0,
            servingSize = "100g",
            category = "Proteins",
            sugarPerServing = 1.1,
            sodiumPerServing = 124.0
        ),
        FoodItem(
            name = "Greek Yogurt",
            caloriesPerServing = 59.0,
            proteinPerServing = 10.0,
            carbsPerServing = 3.6,
            fatPerServing = 0.4,
            fiberPerServing = 0.0,
            servingSize = "100g",
            category = "Proteins",
            sugarPerServing = 3.6,
            sodiumPerServing = 36.0
        ),

        // Nuts & Seeds
        FoodItem(
            name = "Almonds",
            caloriesPerServing = 579.0,
            proteinPerServing = 21.0,
            carbsPerServing = 22.0,
            fatPerServing = 50.0,
            fiberPerServing = 12.0,
            servingSize = "100g",
            category = "Nuts & Seeds",
            sugarPerServing = 4.4,
            sodiumPerServing = 1.0
        ),
        FoodItem(
            name = "Walnuts",
            caloriesPerServing = 654.0,
            proteinPerServing = 15.0,
            carbsPerServing = 14.0,
            fatPerServing = 65.0,
            fiberPerServing = 6.7,
            servingSize = "100g",
            category = "Nuts & Seeds",
            sugarPerServing = 2.6,
            sodiumPerServing = 2.0
        ),

        // Legumes
        FoodItem(
            name = "Black Beans",
            caloriesPerServing = 132.0,
            proteinPerServing = 8.9,
            carbsPerServing = 24.0,
            fatPerServing = 0.5,
            fiberPerServing = 8.7,
            servingSize = "100g",
            category = "Legumes",
            sugarPerServing = 0.3,
            sodiumPerServing = 2.0
        ),
        FoodItem(
            name = "Lentils",
            caloriesPerServing = 116.0,
            proteinPerServing = 9.0,
            carbsPerServing = 20.0,
            fatPerServing = 0.4,
            fiberPerServing = 7.9,
            servingSize = "100g",
            category = "Legumes",
            sugarPerServing = 1.8,
            sodiumPerServing = 2.0
        )
    )

    /**
     * Searches foods by name
     */
    fun searchFoods(query: String): List<FoodItem> {
        return if (query.isBlank()) {
            getDefaultFoods()
        } else {
            getDefaultFoods().filter {
                it.name.contains(query, ignoreCase = true) ||
                it.category?.contains(query, ignoreCase = true) == true ||
                it.brand?.contains(query, ignoreCase = true) == true
            }
        }
    }

    /**
     * Gets foods by category
     */
    fun getFoodsByCategory(category: String): List<FoodItem> {
        return getDefaultFoods().filter { it.category == category }
    }

    /**
     * Gets all available categories
     */
    fun getCategories(): List<String> {
        return getDefaultFoods().mapNotNull { it.category }.distinct().sorted()
    }
}
