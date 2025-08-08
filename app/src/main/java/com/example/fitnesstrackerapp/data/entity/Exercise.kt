/**
 * Exercise entity and related classes for the Fitness Tracker application.
 *
 * This file contains the Exercise entity which defines individual exercises that can be
 * performed during workouts. Each exercise has a name, muscle groups, equipment requirements,
 * and instructions. Exercises are used as templates for creating workout sets.
 *
 * Key Features:
 * - Comprehensive exercise database with muscle group targeting
 * - Equipment requirements and difficulty levels
 * - Instructions and safety notes for proper form
 * - Categorization by body part and exercise type
 * - Support for both strength and cardio exercises
 */

package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing an exercise definition in the Fitness Tracker application.
 *
 * This entity stores the definition of exercises that can be performed during workouts.
 * It serves as a template for creating workout sets and provides information about
 * how to perform each exercise correctly.
 *
 * Database Features:
 * - Indexed for efficient querying by name and muscle group
 * - Supports both strength training and cardio exercises
 * - Includes metadata for proper exercise execution
 */
@Entity(
    tableName = "exercises",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["muscleGroup"]),
        Index(value = ["equipmentType"]),
        Index(value = ["exerciseType"]),
    ],
)
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "muscleGroup")
    val muscleGroup: MuscleGroup,

    @ColumnInfo(name = "equipmentType")
    val equipmentType: EquipmentType,

    @ColumnInfo(name = "exerciseType")
    val exerciseType: ExerciseType,

    @ColumnInfo(name = "difficulty")
    val difficulty: DifficultyLevel = DifficultyLevel.INTERMEDIATE,

    @ColumnInfo(name = "instructions")
    val instructions: String? = null,

    @ColumnInfo(name = "safetyNotes")
    val safetyNotes: String? = null,

    @ColumnInfo(name = "imageUrl")
    val imageUrl: String? = null,

    @ColumnInfo(name = "videoUrl")
    val videoUrl: String? = null,

    @ColumnInfo(name = "isCustom")
    val isCustom: Boolean = false, // User-created vs. pre-defined exercises

    @ColumnInfo(name = "createdBy")
    val createdBy: Long? = null, // User ID who created custom exercise

    @ColumnInfo(name = "createdAt")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Date = Date(),
)

/**
 * Enumeration of muscle groups for exercise categorization.
 */
enum class MuscleGroup {
    CHEST,
    BACK,
    SHOULDERS,
    BICEPS,
    TRICEPS,
    LEGS,
    QUADS,
    HAMSTRINGS,
    CALVES,
    GLUTES,
    ABS,
    CORE,
    CARDIO,
    FULL_BODY,
    FOREARMS,
    TRAPS,
    LATS,
    OBLIQUES,
}

/**
 * Enumeration of equipment types for exercises.
 */
enum class EquipmentType {
    BODYWEIGHT,
    DUMBBELLS,
    BARBELL,
    RESISTANCE_BANDS,
    KETTLEBELL,
    CABLE_MACHINE,
    SMITH_MACHINE,
    PULL_UP_BAR,
    BENCH,
    TREADMILL,
    STATIONARY_BIKE,
    ELLIPTICAL,
    ROWING_MACHINE,
    MEDICINE_BALL,
    STABILITY_BALL,
    FOAM_ROLLER,
    OTHER,
}

/**
 * Enumeration of exercise types.
 */
enum class ExerciseType {
    STRENGTH,
    CARDIO,
    FLEXIBILITY,
    BALANCE,
    PLYOMETRIC,
    ISOMETRIC,
    COMPOUND,
    ISOLATION,
    FUNCTIONAL,
}

/**
 * Enumeration of difficulty levels.
 */
enum class DifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT,
}
