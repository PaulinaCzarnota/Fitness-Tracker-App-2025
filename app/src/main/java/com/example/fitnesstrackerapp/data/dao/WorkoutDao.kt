package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for Workout entity operations.
 *
 * Responsibilities:
 * - Insert, update, delete workouts
 * - Query workouts by user, type, and date range
 * - Provide workout statistics and analytics
 * - Handle workout session management
 */
@Dao
interface WorkoutDao {

    /**
     * Inserts a new workout into the database.
     *
     * @param workout Workout entity to insert
     * @return The ID of the inserted workout
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long

    /**
     * Updates an existing workout in the database.
     *
     * @param workout Workout entity with updated data
     */
    @Update
    suspend fun updateWorkout(workout: Workout)

    /**
     * Deletes a workout from the database.
     *
     * @param workout Workout entity to delete
     */
    @Delete
    suspend fun deleteWorkout(workout: Workout)

    /**
     * Deletes a workout by its ID.
     *
     * @param workoutId Workout ID to delete
     */
    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkoutById(workoutId: Long)

    /**
     * Gets a workout by its ID.
     *
     * @param workoutId Workout ID to search for
     * @return Workout entity or null if not found
     */
    @Query("SELECT * FROM workouts WHERE id = :workoutId LIMIT 1")
    suspend fun getWorkoutById(workoutId: Long): Workout?

    /**
     * Gets all workouts for a specific user.
     *
     * @param userId User ID
     * @return Flow of list of workouts ordered by start time descending
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY startTime DESC")
    fun getWorkoutsByUser(userId: Long): Flow<List<Workout>>

    /**
     * Gets all workouts (admin function).
     *
     * @return Flow of list of all workouts ordered by start time descending
     */
    @Query("SELECT * FROM workouts ORDER BY startTime DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    /**
     * Gets recent workouts for a user with limit.
     *
     * @param userId User ID
     * @param limit Maximum number of workouts to return
     * @return Flow of list of recent workouts
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY startTime DESC LIMIT :limit")
    fun getRecentWorkouts(userId: Long, limit: Int): Flow<List<Workout>>

    /**
     * Gets workouts by type for a user.
     *
     * @param userId User ID
     * @param workoutType Type of workout
     * @return Flow of list of workouts
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId AND workoutType = :workoutType ORDER BY startTime DESC")
    fun getWorkoutsByType(userId: Long, workoutType: WorkoutType): Flow<List<Workout>>

    /**
     * Gets workouts within a date range.
     *
     * @param userId User ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return Flow of list of workouts
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId AND startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getWorkoutsInDateRange(userId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Workout>>

    /**
     * Gets the count of workouts in a date range.
     *
     * @param userId User ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return Number of workouts in the range
     */
    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId AND startTime BETWEEN :startDate AND :endDate")
    suspend fun getWorkoutCountInDateRange(userId: Long, startDate: LocalDateTime, endDate: LocalDateTime): Int

    /**
     * Gets the longest workout by duration for a user.
     *
     * @param userId User ID
     * @return Longest workout or null if no workouts exist
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY duration DESC LIMIT 1")
    suspend fun getLongestWorkout(userId: Long): Workout?

    /**
     * Gets the most recent workout for a user.
     *
     * @param userId User ID
     * @return Most recent workout or null if no workouts exist
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY startTime DESC LIMIT 1")
    suspend fun getMostRecentWorkout(userId: Long): Workout?

    /**
     * Gets total workout count for a user.
     *
     * @param userId User ID
     * @return Total number of workouts
     */
    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId")
    suspend fun getWorkoutCount(userId: Long): Int

    /**
     * Gets total workout duration for a user.
     *
     * @param userId User ID
     * @return Total duration in minutes
     */
    @Query("SELECT SUM(duration) FROM workouts WHERE userId = :userId")
    suspend fun getTotalWorkoutDuration(userId: Long): Int?

    /**
     * Gets total distance covered for a user.
     *
     * @param userId User ID
     * @return Total distance in kilometers
     */
    @Query("SELECT SUM(distance) FROM workouts WHERE userId = :userId")
    suspend fun getTotalDistance(userId: Long): Float?

    /**
     * Gets total calories burned for a user.
     *
     * @param userId User ID
     * @return Total calories burned
     */
    @Query("SELECT SUM(caloriesBurned) FROM workouts WHERE userId = :userId")
    suspend fun getTotalCaloriesBurned(userId: Long): Int?

    /**
     * Gets total steps for a user.
     *
     * @param userId User ID
     * @return Total steps
     */
    @Query("SELECT SUM(stepCount) FROM workouts WHERE userId = :userId")
    suspend fun getTotalSteps(userId: Long): Int?

    /**
     * Gets workout count by type for a user.
     *
     * @param userId User ID
     * @param workoutType Type of workout
     * @return Number of workouts of the specified type
     */
    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId AND workoutType = :workoutType")
    suspend fun getWorkoutCountByType(userId: Long, workoutType: WorkoutType): Int

    /**
     * Gets workouts for today for a user.
     *
     * @param userId User ID
     * @param dayStart Start of the day
     * @param dayEnd End of the day
     * @return Flow of list of today's workouts
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId AND startTime BETWEEN :dayStart AND :dayEnd ORDER BY startTime DESC")
    fun getTodaysWorkouts(userId: Long, dayStart: LocalDateTime, dayEnd: LocalDateTime): Flow<List<Workout>>

    /**
     * Gets workouts for this week for a user.
     *
     * @param userId User ID
     * @param weekStart Start of the week
     * @param weekEnd End of the week
     * @return Flow of list of this week's workouts
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId AND startTime BETWEEN :weekStart AND :weekEnd ORDER BY startTime DESC")
    fun getThisWeeksWorkouts(userId: Long, weekStart: LocalDateTime, weekEnd: LocalDateTime): Flow<List<Workout>>

    /**
     * Gets workouts for this month for a user.
     *
     * @param userId User ID
     * @param monthStart Start of the month
     * @param monthEnd End of the month
     * @return Flow of list of this month's workouts
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId AND startTime BETWEEN :monthStart AND :monthEnd ORDER BY startTime DESC")
    fun getThisMonthsWorkouts(userId: Long, monthStart: LocalDateTime, monthEnd: LocalDateTime): Flow<List<Workout>>

    /**
     * Gets average workout duration for a user.
     *
     * @param userId User ID
     * @return Average duration in minutes
     */
    @Query("SELECT AVG(duration) FROM workouts WHERE userId = :userId")
    suspend fun getAverageWorkoutDuration(userId: Long): Float?

    /**
     * Gets average distance per workout for a user.
     *
     * @param userId User ID
     * @return Average distance in kilometers
     */
    @Query("SELECT AVG(distance) FROM workouts WHERE userId = :userId")
    suspend fun getAverageDistance(userId: Long): Float?

    /**
     * Gets average calories burned per workout for a user.
     *
     * @param userId User ID
     * @return Average calories burned
     */
    @Query("SELECT AVG(caloriesBurned) FROM workouts WHERE userId = :userId")
    suspend fun getAverageCaloriesBurned(userId: Long): Float?

    /**
     * Deletes all workouts for a user.
     *
     * @param userId User ID
     */
    @Query("DELETE FROM workouts WHERE userId = :userId")
    suspend fun deleteAllUserWorkouts(userId: Long)

    /**
     * Gets workouts that are currently in progress (no end time).
     *
     * @param userId User ID
     * @return Flow of list of active workouts
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId AND endTime IS NULL ORDER BY startTime DESC")
    fun getActiveWorkouts(userId: Long): Flow<List<Workout>>

    /**
     * Updates workout end time.
     *
     * @param workoutId Workout ID
     * @param endTime End time to set
     */
    @Query("UPDATE workouts SET endTime = :endTime WHERE id = :workoutId")
    suspend fun updateWorkoutEndTime(workoutId: Long, endTime: LocalDateTime)

    /**
     * Updates workout notes.
     *
     * @param workoutId Workout ID
     * @param notes Notes to set
     */
    @Query("UPDATE workouts SET notes = :notes WHERE id = :workoutId")
    suspend fun updateWorkoutNotes(workoutId: Long, notes: String?)
}
