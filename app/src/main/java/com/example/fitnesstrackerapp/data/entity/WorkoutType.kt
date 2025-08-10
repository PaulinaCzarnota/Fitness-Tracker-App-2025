package com.example.fitnesstrackerapp.data.entity

/**
 * Workout Type enumeration for the Fitness Tracker application.
 *
 * This file defines the comprehensive set of workout types supported by the application.
 * Each workout type represents a different category of physical activity that users can
 * log and track. The enum provides a standardized way to categorize workouts for
 * analytics, goal setting, and progress tracking.
 *
 * Usage:
 * - Workout classification and filtering
 * - Goal setting by activity type
 * - Progress analytics and reporting
 * - Calorie calculation algorithms
 */

/**
 * Enumeration of supported workout types in the Fitness Tracker application.
 *
 * Each workout type represents a different category of physical activity with distinct
 * characteristics for tracking purposes. Types are organized from most common to
 * specialized activities, with OTHER as a catch-all for unlisted activities.
 */
enum class WorkoutType {
    RUNNING, // Outdoor and treadmill running
    WALKING, // Casual walking, hiking, and walking workouts
    CYCLING, // Indoor and outdoor cycling, stationary bike
    WEIGHTLIFTING, // Resistance training, strength training
    CARDIO, // General cardio exercises, aerobics
    SWIMMING, // Pool swimming, water aerobics
    YOGA, // Yoga sessions, stretching, flexibility
    PILATES, // Pilates exercises and mat work
    HIIT, // High-Intensity Interval Training
    CROSSFIT, // CrossFit workouts and functional fitness
    BOXING, // Boxing training, martial arts
    BASKETBALL, // Basketball games and practice
    SOCCER, // Soccer/football games and training
    TENNIS, // Tennis matches and practice
    GOLF, // Golf rounds and practice
    DANCE, // Dance workouts and classes
    ROWING, // Rowing machine and water rowing
    CLIMBING, // Rock climbing, wall climbing
    SKIING, // Downhill and cross-country skiing
    SNOWBOARDING, // Snowboarding activities
    VOLLEYBALL, // Volleyball games and practice
    BADMINTON, // Badminton matches
    TABLE_TENNIS, // Table tennis/ping pong
    GYMNASTICS, // Gymnastics training and routines
    OTHER, // Any other unlisted workout type
}
