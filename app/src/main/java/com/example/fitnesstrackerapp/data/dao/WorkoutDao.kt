/**
 * Workout Data Access Object for the Fitness Tracker application.
 *
 * This DAO provides comprehensive database operations for Workout entities including
 * workout session management, performance tracking, analytics, and historical data.
 * All operations are coroutine-based for optimal performance and UI responsiveness.
 *
 * Key Features:
 * - Workout session creation, updates, and deletion
 * - Performance metrics and statistics calculation
 * - Date range queries for progress tracking
 * - Workout type filtering and categorization
 * - Analytics queries for charts and reports
 * - Batch operations for data management
 */

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
import java.util.Date

/**
 * Data Access Object for Workout entity operations.
 *
 * Provides comprehensive database operations for workout management including
 * session tracking, performance analysis, and historical data queries.
 * All operations are suspend functions for coroutine compatibility.
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
     * Gets all workouts for a specific user as a Flow.
     *
     * @param userId User ID
     * @return Flow of list of workouts ordered by start time descending
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY startTime DESC")
    fun getWorkoutsByUserId(userId: Long): Flow<List<Workout>>

    /**
     * Gets all workouts (admin function) as a Flow.
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
     * Gets workouts within a date range for a user.
     *
     * @param userId User ID
     * @param startDate Start date of range
     * @param endDate End date of range
     * @return Flow of list of workouts in date range
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId AND startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getWorkoutsInDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Workout>>

    /**
     * Gets workouts for a specific date.
     *
     * @param userId User ID
     * @param date Specific date
     * @return Flow of list of workouts for the date
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId AND DATE(startTime/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch') ORDER BY startTime DESC")
    fun getWorkoutsForDate(userId: Long, date: Date): Flow<List<Workout>>

    /**
     * Gets total workout count for a user.
     *
     * @param userId User ID
     * @return Total number of workouts
     */
    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId")
    suspend fun getTotalWorkoutCount(userId: Long): Int

    /**
     * Gets total calories burned for a user.
     *
     * @param userId User ID
     * @return Total calories burned across all workouts
     */
    @Query("SELECT SUM(caloriesBurned) FROM workouts WHERE userId = :userId")
    suspend fun getTotalCaloriesBurned(userId: Long): Int?

    /**
     * Gets total workout duration for a user in minutes.
     *
     * @param userId User ID
     * @return Total workout duration in minutes
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
     * Gets workout count for a specific type.
     *
     * @param userId User ID
     * @param workoutType Workout type
     * @return Count of workouts for the type
     */
    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId AND workoutType = :workoutType")
    suspend fun getWorkoutCountByType(userId: Long, workoutType: WorkoutType): Int

    /**
     * Gets total calories for a specific workout type.
     *
     * @param userId User ID
     * @param workoutType Workout type
     * @return Total calories for the workout type
     */
    @Query("SELECT SUM(caloriesBurned) FROM workouts WHERE userId = :userId AND workoutType = :workoutType")
    suspend fun getTotalCaloriesByType(userId: Long, workoutType: WorkoutType): Int?

    /**
     * Gets total duration for a specific workout type.
     *
     * @param userId User ID
     * @param workoutType Workout type
     * @return Total duration for the workout type
     */
    @Query("SELECT SUM(duration) FROM workouts WHERE userId = :userId AND workoutType = :workoutType")
    suspend fun getTotalDurationByType(userId: Long, workoutType: WorkoutType): Int?

    /**
     * Gets average workout duration for a user.
     *
     * @param userId User ID
     * @return Average workout duration in minutes
     */
    @Query("SELECT AVG(duration) FROM workouts WHERE userId = :userId AND duration > 0")
    suspend fun getAverageWorkoutDuration(userId: Long): Float?

    /**
     * Gets average calories burned per workout.
     *
     * @param userId User ID
     * @return Average calories burned per workout
     */
    @Query("SELECT AVG(caloriesBurned) FROM workouts WHERE userId = :userId AND caloriesBurned > 0")
    suspend fun getAverageCaloriesBurned(userId: Long): Float?

    /**
     * Gets total workout count for analytics.
     *
     * @param userId User ID
     * @return Total workout count
     */
    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId")
    suspend fun getWorkoutCountForAnalytics(userId: Long): Int

    /**
     * Gets monthly workout count.
     *
     * @param userId User ID
     * @param year Year
     * @param month Month (1-12)
     * @return Count of workouts in the month
     */
    @Query("""
        SELECT COUNT(*) 
        FROM workouts 
        WHERE userId = :userId 
        AND strftime('%Y', startTime/1000, 'unixepoch') = :year 
        AND strftime('%m', startTime/1000, 'unixepoch') = :month
    """)
    suspend fun getMonthlyWorkoutCount(userId: Long, year: String, month: String): Int

    /**
     * Gets monthly calories burned.
     *
     * @param userId User ID
     * @param year Year
     * @param month Month (1-12)
     * @return Total calories burned in the month
     */
    @Query("""
        SELECT SUM(caloriesBurned) 
        FROM workouts 
        WHERE userId = :userId 
        AND strftime('%Y', startTime/1000, 'unixepoch') = :year 
        AND strftime('%m', startTime/1000, 'unixepoch') = :month
    """)
    suspend fun getMonthlyCaloriesBurned(userId: Long, year: String, month: String): Int?

    /**
     * Gets monthly workout duration.
     *
     * @param userId User ID
     * @param year Year
     * @param month Month (1-12)
     * @return Total duration in the month
     */
    @Query("""
        SELECT SUM(duration) 
        FROM workouts 
        WHERE userId = :userId 
        AND strftime('%Y', startTime/1000, 'unixepoch') = :year 
        AND strftime('%m', startTime/1000, 'unixepoch') = :month
    """)
    suspend fun getMonthlyWorkoutDuration(userId: Long, year: String, month: String): Int?

    /**
     * Gets weekly workout count.
     *
     * @param userId User ID
     * @param weekStart Start of the week
     * @param weekEnd End of the week
     * @return Count of workouts in the week
     */
    @Query("""
        SELECT COUNT(*) 
        FROM workouts 
        WHERE userId = :userId 
        AND startTime BETWEEN :weekStart AND :weekEnd
    """)
    suspend fun getWeeklyWorkoutCount(userId: Long, weekStart: Date, weekEnd: Date): Int

    /**
     * Gets weekly calories burned.
     *
     * @param userId User ID
     * @param weekStart Start of the week
     * @param weekEnd End of the week
     * @return Total calories burned in the week
     */
    @Query("""
        SELECT SUM(caloriesBurned) 
        FROM workouts 
        WHERE userId = :userId 
        AND startTime BETWEEN :weekStart AND :weekEnd
    """)
    suspend fun getWeeklyCaloriesBurned(userId: Long, weekStart: Date, weekEnd: Date): Int?

    /**
     * Gets weekly workout duration.
     *
     * @param userId User ID
     * @param weekStart Start of the week
     * @param weekEnd End of the week
     * @return Total duration in the week
     */
    @Query("""
        SELECT SUM(duration) 
        FROM workouts 
        WHERE userId = :userId 
        AND startTime BETWEEN :weekStart AND :weekEnd
    """)
    suspend fun getWeeklyWorkoutDuration(userId: Long, weekStart: Date, weekEnd: Date): Int?

    /**
     * Gets the user's best workout by calories burned.
     *
     * @param userId User ID
     * @return Workout with highest calories burned
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY caloriesBurned DESC LIMIT 1")
    suspend fun getBestWorkoutByCalories(userId: Long): Workout?

    /**
     * Gets the user's longest workout by duration.
     *
     * @param userId User ID
     * @return Workout with longest duration
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY duration DESC LIMIT 1")
    suspend fun getLongestWorkout(userId: Long): Workout?

    /**
     * Gets workouts that are currently in progress (no end time).
     *
     * @param userId User ID
     * @return List of ongoing workouts
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId AND endTime IS NULL ORDER BY startTime DESC")
    suspend fun getOngoingWorkouts(userId: Long): List<Workout>

    /**
     * Gets completed workouts (have end time).
     *
     * @param userId User ID
     * @return Flow of completed workouts
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId AND endTime IS NOT NULL ORDER BY startTime DESC")
    fun getCompletedWorkouts(userId: Long): Flow<List<Workout>>

    /**
     * Updates workout end time and recalculates duration.
     *
     * @param workoutId Workout ID
     * @param endTime End time
     * @param duration Duration in minutes
     */
    @Query("UPDATE workouts SET endTime = :endTime, duration = :duration, updatedAt = :endTime WHERE id = :workoutId")
    suspend fun updateWorkoutEndTime(workoutId: Long, endTime: Date, duration: Int)

    /**
     * Deletes all workouts for a user (for account deletion).
     *
     * @param userId User ID
     */
    @Query("DELETE FROM workouts WHERE userId = :userId")
    suspend fun deleteAllWorkoutsForUser(userId: Long)

    /**
     * Deletes all workouts (for testing purposes only).
     */
    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()
}
