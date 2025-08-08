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
 */

package com.example.fitnesstrackerapp.data.entity

/**
 * Enumeration of supported goal types in the Fitness Tracker application.
 *
 * Each goal type represents a different category of fitness objective with distinct
 * tracking requirements and measurement units. Types are organized from most common
 * to specialized goals, with OTHER as a catch-all for custom objectives.
 */
enum class GoalType {
    WEIGHT_LOSS, // Target weight reduction goals
    WEIGHT_GAIN, // Target weight increase goals (muscle building)
    DISTANCE_RUNNING, // Running/walking distance targets
    WORKOUT_FREQUENCY, // Number of workouts per week/month
    CALORIE_BURN, // Daily/weekly calorie burn targets
    STEP_COUNT, // Daily step count goals
    DURATION_EXERCISE, // Total exercise time goals
    STRENGTH_TRAINING, // Strength/resistance training goals
    MUSCLE_BUILDING, // Muscle mass increase goals
    ENDURANCE, // Cardiovascular endurance goals
    FLEXIBILITY, // Flexibility and mobility goals
    BODY_FAT, // Body fat percentage goals
    HYDRATION, // Daily water intake goals
    SLEEP, // Sleep quality and duration goals
    OTHER, // Custom or unlisted goal types
}
