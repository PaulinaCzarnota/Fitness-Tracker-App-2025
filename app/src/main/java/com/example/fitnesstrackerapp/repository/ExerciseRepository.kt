/**
 * Repository for handling exercise-related data operations in the Fitness Tracker application.
 *
 * This repository provides a clean API for exercise data management, abstracting the
 * database layer and providing business logic for exercise operations. It handles
 * CRUD operations, filtering, searching, and exercise database seeding.
 *
 * Key Features:
 * - Complete CRUD operations for exercise entities
 * - Advanced filtering and searching capabilities
 * - Exercise database seeding with common exercises
 * - Custom exercise management for users
 * - Muscle group and equipment type categorization
 */

package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.data.dao.ExerciseDao
import com.example.fitnesstrackerapp.data.entity.DifficultyLevel
import com.example.fitnesstrackerapp.data.entity.EquipmentType
import com.example.fitnesstrackerapp.data.entity.Exercise
import com.example.fitnesstrackerapp.data.entity.ExerciseType
import com.example.fitnesstrackerapp.data.entity.MuscleGroup
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository for handling exercise-related data operations.
 *
 * Provides a high-level interface for exercise data management, including
 * persistence operations, filtering, searching, and seeding with predefined exercises.
 * All operations are designed to be thread-safe and use coroutines for async execution.
 */
class ExerciseRepository(
    private val exerciseDao: ExerciseDao,
) {
    /**
     * Inserts a new exercise into the database.
     *
     * @param exercise The exercise entity to be inserted
     * @return The auto-generated ID of the inserted exercise
     * @throws Exception if the insertion fails or name already exists
     */
    suspend fun insertExercise(exercise: Exercise): Long {
        return exerciseDao.insertExercise(exercise)
    }

    /**
     * Updates an existing exercise in the database.
     *
     * @param exercise The exercise entity with updated information
     * @throws Exception if the update fails or exercise doesn't exist
     */
    suspend fun updateExercise(exercise: Exercise) {
        exerciseDao.updateExercise(exercise.copy(updatedAt = Date()))
    }

    /**
     * Deletes an exercise from the database by its ID.
     *
     * @param exerciseId The unique identifier of the exercise to delete
     * @throws Exception if the deletion fails
     */
    suspend fun deleteExercise(exerciseId: Long) {
        exerciseDao.deleteExerciseById(exerciseId)
    }

    /**
     * Gets an exercise by its ID.
     *
     * @param exerciseId The unique identifier of the exercise
     * @return Exercise entity or null if not found
     */
    suspend fun getExerciseById(exerciseId: Long): Exercise? {
        return exerciseDao.getExerciseById(exerciseId)
    }

    /**
     * Gets all exercises available to a user as a reactive Flow.
     * Includes both predefined exercises and user's custom exercises.
     *
     * @param userId The unique identifier of the user
     * @return Flow emitting list of available exercises
     */
    fun getAvailableExercises(userId: Long): Flow<List<Exercise>> {
        return exerciseDao.getAvailableExercises(userId)
    }

    /**
     * Gets exercises filtered by muscle group.
     *
     * @param muscleGroup The target muscle group
     * @return Flow emitting list of exercises for the muscle group
     */
    fun getExercisesByMuscleGroup(muscleGroup: MuscleGroup): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByMuscleGroup(muscleGroup)
    }

    /**
     * Gets exercises filtered by equipment type.
     *
     * @param equipmentType The required equipment type
     * @return Flow emitting list of exercises using the equipment
     */
    fun getExercisesByEquipment(equipmentType: EquipmentType): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByEquipment(equipmentType)
    }

    /**
     * Gets exercises filtered by exercise type.
     *
     * @param exerciseType The type of exercise (strength, cardio, etc.)
     * @return Flow emitting list of exercises of the specified type
     */
    fun getExercisesByType(exerciseType: ExerciseType): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByType(exerciseType)
    }

    /**
     * Searches exercises by name (case-insensitive).
     *
     * @param query Search query string
     * @return Flow emitting list of exercises matching the search
     */
    fun searchExercises(query: String): Flow<List<Exercise>> {
        return exerciseDao.searchExercises(query)
    }

    /**
     * Advanced search with multiple filters.
     *
     * @param query Search query for name (optional)
     * @param muscleGroup Target muscle group (optional)
     * @param equipmentType Required equipment (optional)
     * @param difficulty Exercise difficulty (optional)
     * @param exerciseType Type of exercise (optional)
     * @return Flow emitting filtered exercises
     */
    fun searchExercisesAdvanced(
        query: String? = null,
        muscleGroup: MuscleGroup? = null,
        equipmentType: EquipmentType? = null,
        difficulty: DifficultyLevel? = null,
        exerciseType: ExerciseType? = null,
    ): Flow<List<Exercise>> {
        return exerciseDao.searchExercisesAdvanced(
            query = query,
            muscleGroup = muscleGroup,
            equipmentType = equipmentType,
            difficulty = difficulty,
            exerciseType = exerciseType,
        )
    }

    /**
     * Gets custom exercises created by a specific user.
     *
     * @param userId User ID who created the exercises
     * @return Flow emitting list of user's custom exercises
     */
    fun getCustomExercises(userId: Long): Flow<List<Exercise>> {
        return exerciseDao.getCustomExercisesByUser(userId)
    }

    /**
     * Gets bodyweight exercises (no equipment needed).
     *
     * @return Flow emitting list of bodyweight exercises
     */
    fun getBodyweightExercises(): Flow<List<Exercise>> {
        return exerciseDao.getBodyweightExercises()
    }

    /**
     * Gets compound exercises that work multiple muscle groups.
     *
     * @return Flow emitting list of compound exercises
     */
    fun getCompoundExercises(): Flow<List<Exercise>> {
        return exerciseDao.getCompoundExercises()
    }

    /**
     * Gets beginner-friendly exercises.
     *
     * @return Flow emitting list of beginner exercises
     */
    fun getBeginnerExercises(): Flow<List<Exercise>> {
        return exerciseDao.getBeginnerExercises()
    }

    /**
     * Creates a new custom exercise for a user.
     *
     * @param name Exercise name
     * @param muscleGroup Target muscle group
     * @param equipmentType Required equipment
     * @param exerciseType Type of exercise
     * @param userId User creating the exercise
     * @param description Optional description
     * @param difficulty Difficulty level (default: INTERMEDIATE)
     * @param instructions Optional instructions
     * @return ID of the created exercise
     */
    suspend fun createCustomExercise(
        name: String,
        muscleGroup: MuscleGroup,
        equipmentType: EquipmentType,
        exerciseType: ExerciseType,
        userId: Long,
        description: String? = null,
        difficulty: DifficultyLevel = DifficultyLevel.INTERMEDIATE,
        instructions: String? = null,
    ): Long {
        // Check if name already exists
        if (exerciseDao.exerciseNameExists(name)) {
            throw IllegalArgumentException("Exercise with name '$name' already exists")
        }

        val exercise = Exercise(
            name = name,
            description = description,
            muscleGroup = muscleGroup,
            equipmentType = equipmentType,
            exerciseType = exerciseType,
            difficulty = difficulty,
            instructions = instructions,
            isCustom = true,
            createdBy = userId,
        )

        return exerciseDao.insertExercise(exercise)
    }

    /**
     * Checks if an exercise name is available (not already taken).
     *
     * @param name Exercise name to check
     * @return True if name is available, false if already exists
     */
    suspend fun isExerciseNameAvailable(name: String): Boolean {
        return !exerciseDao.exerciseNameExists(name)
    }

    /**
     * Gets the total number of exercises in the database.
     *
     * @return Total exercise count
     */
    suspend fun getTotalExerciseCount(): Int {
        return exerciseDao.getTotalExerciseCount()
    }

    /**
     * Gets the number of custom exercises created by a user.
     *
     * @param userId User ID
     * @return Count of user's custom exercises
     */
    suspend fun getCustomExerciseCount(userId: Long): Int {
        return exerciseDao.getCustomExerciseCount(userId)
    }

    /**
     * Seeds the database with predefined common exercises.
     * This should be called on first app launch to populate the exercise database.
     *
     * @return Number of exercises seeded
     */
    suspend fun seedExerciseDatabase(): Int {
        val predefinedExercises = getPredefinedExercises()
        val insertedIds = exerciseDao.insertExercises(predefinedExercises)
        return insertedIds.size
    }

    /**
     * Gets a list of predefined exercises to seed the database.
     */
    private fun getPredefinedExercises(): List<Exercise> {
        return listOf(
            // Chest exercises
            Exercise(
                name = "Push-ups",
                description = "Classic bodyweight chest exercise",
                muscleGroup = MuscleGroup.CHEST,
                equipmentType = EquipmentType.BODYWEIGHT,
                exerciseType = ExerciseType.COMPOUND,
                difficulty = DifficultyLevel.BEGINNER,
                instructions = "Start in plank position, lower body to ground, push back up",
            ),
            Exercise(
                name = "Bench Press",
                description = "Barbell bench press for chest development",
                muscleGroup = MuscleGroup.CHEST,
                equipmentType = EquipmentType.BARBELL,
                exerciseType = ExerciseType.COMPOUND,
                difficulty = DifficultyLevel.INTERMEDIATE,
                instructions = "Lie on bench, lower barbell to chest, press up",
            ),
            Exercise(
                name = "Dumbbell Flyes",
                description = "Isolation exercise for chest",
                muscleGroup = MuscleGroup.CHEST,
                equipmentType = EquipmentType.DUMBBELLS,
                exerciseType = ExerciseType.ISOLATION,
                difficulty = DifficultyLevel.INTERMEDIATE,
                instructions = "Lie on bench, arc dumbbells from chest to sides and back",
            ),
            // Back exercises
            Exercise(
                name = "Pull-ups",
                description = "Bodyweight back exercise",
                muscleGroup = MuscleGroup.BACK,
                equipmentType = EquipmentType.PULL_UP_BAR,
                exerciseType = ExerciseType.COMPOUND,
                difficulty = DifficultyLevel.INTERMEDIATE,
                instructions = "Hang from bar, pull body up until chin over bar",
            ),
            Exercise(
                name = "Deadlift",
                description = "Full body compound movement",
                muscleGroup = MuscleGroup.BACK,
                equipmentType = EquipmentType.BARBELL,
                exerciseType = ExerciseType.COMPOUND,
                difficulty = DifficultyLevel.ADVANCED,
                instructions = "Hip hinge movement, lift barbell from ground to standing",
            ),
            Exercise(
                name = "Bent-over Rows",
                description = "Barbell rowing for back development",
                muscleGroup = MuscleGroup.BACK,
                equipmentType = EquipmentType.BARBELL,
                exerciseType = ExerciseType.COMPOUND,
                difficulty = DifficultyLevel.INTERMEDIATE,
                instructions = "Bend over, row barbell to lower chest",
            ),
            // Leg exercises
            Exercise(
                name = "Squats",
                description = "Basic bodyweight or weighted squat",
                muscleGroup = MuscleGroup.LEGS,
                equipmentType = EquipmentType.BODYWEIGHT,
                exerciseType = ExerciseType.COMPOUND,
                difficulty = DifficultyLevel.BEGINNER,
                instructions = "Lower body as if sitting back into chair, return to standing",
            ),
            Exercise(
                name = "Barbell Back Squat",
                description = "Weighted squat with barbell on back",
                muscleGroup = MuscleGroup.QUADS,
                equipmentType = EquipmentType.BARBELL,
                exerciseType = ExerciseType.COMPOUND,
                difficulty = DifficultyLevel.INTERMEDIATE,
                instructions = "Barbell on upper back, squat down and drive up",
            ),
            Exercise(
                name = "Lunges",
                description = "Single-leg exercise for legs and glutes",
                muscleGroup = MuscleGroup.LEGS,
                equipmentType = EquipmentType.BODYWEIGHT,
                exerciseType = ExerciseType.COMPOUND,
                difficulty = DifficultyLevel.BEGINNER,
                instructions = "Step forward into lunge position, alternate legs",
            ),
            // Shoulder exercises
            Exercise(
                name = "Overhead Press",
                description = "Standing shoulder press",
                muscleGroup = MuscleGroup.SHOULDERS,
                equipmentType = EquipmentType.BARBELL,
                exerciseType = ExerciseType.COMPOUND,
                difficulty = DifficultyLevel.INTERMEDIATE,
                instructions = "Press barbell from shoulder height to overhead",
            ),
            Exercise(
                name = "Lateral Raises",
                description = "Dumbbell side raises for shoulders",
                muscleGroup = MuscleGroup.SHOULDERS,
                equipmentType = EquipmentType.DUMBBELLS,
                exerciseType = ExerciseType.ISOLATION,
                difficulty = DifficultyLevel.BEGINNER,
                instructions = "Raise dumbbells to sides until parallel to floor",
            ),
            // Arm exercises
            Exercise(
                name = "Bicep Curls",
                description = "Classic bicep exercise",
                muscleGroup = MuscleGroup.BICEPS,
                equipmentType = EquipmentType.DUMBBELLS,
                exerciseType = ExerciseType.ISOLATION,
                difficulty = DifficultyLevel.BEGINNER,
                instructions = "Curl weights up to shoulders, control down",
            ),
            Exercise(
                name = "Tricep Dips",
                description = "Bodyweight tricep exercise",
                muscleGroup = MuscleGroup.TRICEPS,
                equipmentType = EquipmentType.BODYWEIGHT,
                exerciseType = ExerciseType.ISOLATION,
                difficulty = DifficultyLevel.INTERMEDIATE,
                instructions = "Lower body by bending arms, push back up",
            ),
            // Core exercises
            Exercise(
                name = "Plank",
                description = "Core stability exercise",
                muscleGroup = MuscleGroup.CORE,
                equipmentType = EquipmentType.BODYWEIGHT,
                exerciseType = ExerciseType.ISOMETRIC,
                difficulty = DifficultyLevel.BEGINNER,
                instructions = "Hold straight body position on forearms",
            ),
            Exercise(
                name = "Crunches",
                description = "Basic abdominal exercise",
                muscleGroup = MuscleGroup.ABS,
                equipmentType = EquipmentType.BODYWEIGHT,
                exerciseType = ExerciseType.ISOLATION,
                difficulty = DifficultyLevel.BEGINNER,
                instructions = "Curl upper body towards knees, focus on abs",
            ),
            // Cardio exercises
            Exercise(
                name = "Running",
                description = "Outdoor or treadmill running",
                muscleGroup = MuscleGroup.CARDIO,
                equipmentType = EquipmentType.TREADMILL,
                exerciseType = ExerciseType.CARDIO,
                difficulty = DifficultyLevel.BEGINNER,
                instructions = "Maintain steady pace, focus on breathing",
            ),
            Exercise(
                name = "Cycling",
                description = "Stationary or outdoor cycling",
                muscleGroup = MuscleGroup.CARDIO,
                equipmentType = EquipmentType.STATIONARY_BIKE,
                exerciseType = ExerciseType.CARDIO,
                difficulty = DifficultyLevel.BEGINNER,
                instructions = "Maintain consistent pedaling rhythm",
            ),
        )
    }
}
