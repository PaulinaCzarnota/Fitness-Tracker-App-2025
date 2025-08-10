package com.example.fitnesstrackerapp.data.entity

/**
 * Goal Type enumeration for the Fitness Tracker application.
 *
 * This file defines the comprehensive set of goal types supported by the application.
 * Each goal type represents a different category of fitness objective that users can
 * set and track. The enum provides a standardized way to categorize goals for
 * analytics, progress tracking, and motivational features.
 *
 * Usage:
 * - Goal classification and filtering
 * - Progress calculation algorithms
 * - Reminder and notification customization
 * - Analytics and reporting categorization
 *
 * The enum is aligned with both the database schema and UI service layers to ensure
 * consistent goal type handling across the application.
 */
enum class GoalType {
    // Core fitness goals - most commonly used
    STEP_COUNT, // Daily step count goals - aligned with distance tracking
    DISTANCE_RUNNING, // Running/walking distance targets - aligned with step tracking
    CALORIE_BURN, // Daily/weekly calorie burn targets - aligned with workout tracking

    // Body composition goals
    WEIGHT_LOSS, // Target weight reduction goals
    WEIGHT_GAIN, // Target weight increase goals (muscle building)
    MUSCLE_BUILDING, // Muscle mass increase goals
    BODY_FAT, // Body fat percentage goals

    // Training and performance goals
    WORKOUT_FREQUENCY, // Number of workouts per week/month
    DURATION_EXERCISE, // Total exercise time goals
    STRENGTH_TRAINING, // Strength/resistance training goals
    ENDURANCE, // Cardiovascular endurance goals
    FLEXIBILITY, // Flexibility and mobility goals

    // Lifestyle and wellness goals
    HYDRATION, // Daily water intake goals
    SLEEP, // Sleep quality and duration goals
    FITNESS, // General fitness improvement goals

    // Legacy and custom goals
    OTHER, // Custom or unlisted goal types

    ;

    companion object {
        /**
         * Gets the primary goal types that support distance-based completion logic
         */
        fun getDistanceTypes(): Set<GoalType> = setOf(
            DISTANCE_RUNNING,
            STEP_COUNT,
        )

        /**
         * Gets the primary goal types that support calorie-based completion logic
         */
        fun getCalorieTypes(): Set<GoalType> = setOf(
            CALORIE_BURN,
            WEIGHT_LOSS,
            WEIGHT_GAIN,
        )

        /**
         * Gets the primary goal types that support step-based completion logic
         */
        fun getStepTypes(): Set<GoalType> = setOf(
            STEP_COUNT,
        )

        /**
         * Gets all goal types that have specific completion logic requirements
         */
        fun getSpecialCompletionTypes(): Set<GoalType> =
            getDistanceTypes() + getCalorieTypes() + getStepTypes()
    }
}
