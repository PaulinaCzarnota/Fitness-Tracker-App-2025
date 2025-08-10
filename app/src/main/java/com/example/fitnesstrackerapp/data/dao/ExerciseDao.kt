package com.example.fitnesstrackerapp.data.dao

/**
 * Exercise Data Access Object for the Fitness Tracker application.
 *
 * This DAO provides comprehensive database operations for Exercise entities including
 * exercise management, filtering by muscle groups, equipment types, and difficulty levels.
 * All operations are coroutine-based for optimal performance and UI responsiveness.
 *
 * Key Features:
 * - Exercise CRUD operations for custom and predefined exercises
 * - Filtering by muscle group, equipment, and exercise type
 * - Search functionality for finding exercises by name
 * - Support for user-created custom exercises
 * - Batch operations for seeding exercise database
 */

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.DifficultyLevel
import com.example.fitnesstrackerapp.data.entity.EquipmentType
import com.example.fitnesstrackerapp.data.entity.Exercise
import com.example.fitnesstrackerapp.data.entity.ExerciseType
import com.example.fitnesstrackerapp.data.entity.MuscleGroup
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Exercise entity operations.
 *
 * Provides comprehensive database operations for exercise management including
 * filtering, searching, and user-created custom exercises.
 * All operations are suspend functions for coroutine compatibility.
 */
@Dao
interface ExerciseDao {
    /**
     * Inserts a new exercise into the database.
     *
     * @param exercise Exercise entity to insert
     * @return The ID of the inserted exercise
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise): Long

    /**
     * Inserts multiple exercises in a single transaction.
     *
     * @param exercises List of exercises to insert
     * @return List of inserted exercise IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>): List<Long>

    /**
     * Updates an existing exercise in the database.
     *
     * @param exercise Exercise entity with updated data
     */
    @Update
    suspend fun updateExercise(exercise: Exercise)

    /**
     * Deletes an exercise from the database.
     *
     * @param exercise Exercise entity to delete
     */
    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    /**
     * Deletes an exercise by its ID.
     *
     * @param exerciseId Exercise ID to delete
     */
    @Query("DELETE FROM exercises WHERE id = :exerciseId")
    suspend fun deleteExerciseById(exerciseId: Long)

    /**
     * Gets an exercise by its ID.
     *
     * @param exerciseId Exercise ID to search for
     * @return Exercise entity or null if not found
     */
    @Query("SELECT * FROM exercises WHERE id = :exerciseId LIMIT 1")
    suspend fun getExerciseById(exerciseId: Long): Exercise?

    /**
     * Gets all exercises as a Flow.
     *
     * @return Flow of list of all exercises ordered by name
     */
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    /**
     * Gets exercises by muscle group.
     *
     * @param muscleGroup Target muscle group
     * @return Flow of exercises for the specified muscle group
     */
    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup ORDER BY name ASC")
    fun getExercisesByMuscleGroup(muscleGroup: MuscleGroup): Flow<List<Exercise>>

    /**
     * Gets exercises by equipment type.
     *
     * @param equipmentType Required equipment type
     * @return Flow of exercises for the specified equipment
     */
    @Query("SELECT * FROM exercises WHERE equipmentType = :equipmentType ORDER BY name ASC")
    fun getExercisesByEquipment(equipmentType: EquipmentType): Flow<List<Exercise>>

    /**
     * Gets exercises by exercise type.
     *
     * @param exerciseType Type of exercise (strength, cardio, etc.)
     * @return Flow of exercises for the specified type
     */
    @Query("SELECT * FROM exercises WHERE exerciseType = :exerciseType ORDER BY name ASC")
    fun getExercisesByType(exerciseType: ExerciseType): Flow<List<Exercise>>

    /**
     * Gets exercises by difficulty level.
     *
     * @param difficulty Exercise difficulty level
     * @return Flow of exercises for the specified difficulty
     */
    @Query("SELECT * FROM exercises WHERE difficulty = :difficulty ORDER BY name ASC")
    fun getExercisesByDifficulty(difficulty: DifficultyLevel): Flow<List<Exercise>>

    /**
     * Searches exercises by name (case-insensitive).
     *
     * @param query Search query
     * @return Flow of exercises matching the search query
     */
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchExercises(query: String): Flow<List<Exercise>>

    /**
     * Gets predefined (non-custom) exercises.
     *
     * @return Flow of system-defined exercises
     */
    @Query("SELECT * FROM exercises WHERE isCustom = 0 ORDER BY name ASC")
    fun getPredefinedExercises(): Flow<List<Exercise>>

    /**
     * Gets custom exercises created by a specific user.
     *
     * @param userId User ID who created the exercises
     * @return Flow of user-created custom exercises
     */
    @Query("SELECT * FROM exercises WHERE isCustom = 1 AND createdBy = :userId ORDER BY name ASC")
    fun getCustomExercisesByUser(userId: Long): Flow<List<Exercise>>

    /**
     * Gets exercises available to a user (predefined + their custom exercises).
     *
     * @param userId User ID
     * @return Flow of all exercises available to the user
     */
    @Query("SELECT * FROM exercises WHERE isCustom = 0 OR createdBy = :userId ORDER BY name ASC")
    fun getAvailableExercises(userId: Long): Flow<List<Exercise>>

    /**
     * Gets exercises by multiple muscle groups.
     *
     * @param muscleGroups List of target muscle groups
     * @return Flow of exercises targeting any of the specified muscle groups
     */
    @Query("SELECT * FROM exercises WHERE muscleGroup IN (:muscleGroups) ORDER BY name ASC")
    fun getExercisesByMuscleGroups(muscleGroups: List<MuscleGroup>): Flow<List<Exercise>>

    /**
     * Gets exercises by multiple equipment types.
     *
     * @param equipmentTypes List of available equipment types
     * @return Flow of exercises using any of the specified equipment
     */
    @Query("SELECT * FROM exercises WHERE equipmentType IN (:equipmentTypes) ORDER BY name ASC")
    fun getExercisesByEquipmentTypes(equipmentTypes: List<EquipmentType>): Flow<List<Exercise>>

    /**
     * Gets exercises filtered by muscle group and equipment.
     *
     * @param muscleGroup Target muscle group
     * @param equipmentType Available equipment
     * @return Flow of exercises matching both criteria
     */
    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup AND equipmentType = :equipmentType ORDER BY name ASC")
    fun getExercisesByMuscleGroupAndEquipment(
        muscleGroup: MuscleGroup,
        equipmentType: EquipmentType,
    ): Flow<List<Exercise>>

    /**
     * Gets the total count of exercises.
     *
     * @return Total number of exercises
     */
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getTotalExerciseCount(): Int

    /**
     * Gets the count of custom exercises by a user.
     *
     * @param userId User ID
     * @return Number of custom exercises created by the user
     */
    @Query("SELECT COUNT(*) FROM exercises WHERE isCustom = 1 AND createdBy = :userId")
    suspend fun getCustomExerciseCount(userId: Long): Int

    /**
     * Gets exercises by name (exact match).
     *
     * @param name Exercise name
     * @return Exercise with the exact name or null
     */
    @Query("SELECT * FROM exercises WHERE name = :name LIMIT 1")
    suspend fun getExerciseByName(name: String): Exercise?

    /**
     * Gets the most recently added exercises.
     *
     * @param limit Maximum number of exercises to return
     * @return Flow of recently added exercises
     */
    @Query("SELECT * FROM exercises ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentExercises(limit: Int): Flow<List<Exercise>>

    /**
     * Gets exercises suitable for beginners.
     *
     * @return Flow of beginner-friendly exercises
     */
    @Query("SELECT * FROM exercises WHERE difficulty IN ('BEGINNER', 'INTERMEDIATE') ORDER BY name ASC")
    fun getBeginnerExercises(): Flow<List<Exercise>>

    /**
     * Gets bodyweight exercises (no equipment needed).
     *
     * @return Flow of bodyweight exercises
     */
    @Query("SELECT * FROM exercises WHERE equipmentType = 'BODYWEIGHT' ORDER BY name ASC")
    fun getBodyweightExercises(): Flow<List<Exercise>>

    /**
     * Gets compound exercises that work multiple muscle groups.
     *
     * @return Flow of compound exercises
     */
    @Query("SELECT * FROM exercises WHERE exerciseType = 'COMPOUND' ORDER BY name ASC")
    fun getCompoundExercises(): Flow<List<Exercise>>

    /**
     * Deletes all custom exercises by a user.
     *
     * @param userId User ID whose custom exercises to delete
     */
    @Query("DELETE FROM exercises WHERE isCustom = 1 AND createdBy = :userId")
    suspend fun deleteCustomExercisesByUser(userId: Long)

    /**
     * Deletes all exercises (for testing purposes only).
     */
    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()

    /**
     * Checks if an exercise name already exists.
     *
     * @param name Exercise name to check
     * @return True if name exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM exercises WHERE name = :name LIMIT 1)")
    suspend fun exerciseNameExists(name: String): Boolean

    /**
     * Updates exercise difficulty level.
     *
     * @param exerciseId Exercise ID
     * @param difficulty New difficulty level
     */
    @Query("UPDATE exercises SET difficulty = :difficulty, updatedAt = :updatedAt WHERE id = :exerciseId")
    suspend fun updateExerciseDifficulty(
        exerciseId: Long,
        difficulty: DifficultyLevel,
        updatedAt: java.util.Date,
    )

    /**
     * Advanced search with multiple filters.
     *
     * @param query Search query for name
     * @param muscleGroup Target muscle group (optional)
     * @param equipmentType Required equipment (optional)
     * @param difficulty Exercise difficulty (optional)
     * @param exerciseType Type of exercise (optional)
     * @return Flow of exercises matching the criteria
     */
    @Query(
        """
        SELECT * FROM exercises
        WHERE (:query IS NULL OR name LIKE '%' || :query || '%')
        AND (:muscleGroup IS NULL OR muscleGroup = :muscleGroup)
        AND (:equipmentType IS NULL OR equipmentType = :equipmentType)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        AND (:exerciseType IS NULL OR exerciseType = :exerciseType)
        ORDER BY name ASC
    """,
    )
    fun searchExercisesAdvanced(
        query: String? = null,
        muscleGroup: MuscleGroup? = null,
        equipmentType: EquipmentType? = null,
        difficulty: DifficultyLevel? = null,
        exerciseType: ExerciseType? = null,
    ): Flow<List<Exercise>>
}
