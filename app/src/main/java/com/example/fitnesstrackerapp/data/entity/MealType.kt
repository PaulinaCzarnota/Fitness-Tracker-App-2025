/**
 * Meal Type enumeration for the Fitness Tracker application.
 *
 * This file defines the comprehensive set of meal types for nutrition tracking.
 * Each meal type represents a different eating occasion throughout the day,
 * providing detailed categorization for dietary analysis and meal planning.
 *
 * Usage:
 * - Food entry categorization and organization
 * - Daily nutrition analysis and reporting
 * - Meal timing optimization for fitness goals
 * - Dietary pattern analysis and recommendations
 */

package com.example.fitnesstrackerapp.data.entity

/**
 * Enumeration of meal types in the Fitness Tracker application.
 *
 * Each meal type represents a different eating occasion with specific
 * timing and nutritional considerations for comprehensive diet tracking:
 * - BREAKFAST: Morning meal to start the day
 * - LUNCH: Midday meal for sustained energy
 * - DINNER: Evening meal for recovery and satisfaction
 * - SNACK: Small meals between main meals
 * - PRE_WORKOUT: Foods consumed before exercise for energy
 * - POST_WORKOUT: Foods consumed after exercise for recovery
 * - LATE_NIGHT: Evening/night snacks or meals
 */
enum class MealType {
    BREAKFAST,     // Morning meal to start the day
    LUNCH,         // Midday meal for sustained energy
    DINNER,        // Evening meal for recovery and satisfaction
    SNACK,         // Small meals between main meals
    PRE_WORKOUT,   // Foods consumed before exercise for energy
    POST_WORKOUT,  // Foods consumed after exercise for recovery
    LATE_NIGHT     // Evening/night snacks or meals
}
