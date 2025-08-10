package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.data.dao.WorkoutDao
import com.example.fitnesstrackerapp.data.entity.WeeklyStats
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date

/**
 * Repository for handling workout-related data operations.
 *
 * Provides a high-level interface for workout data management, including
 * persistence operations, analytics, and statistical calculations.
 * All operations are designed to be thread-safe and use coroutines for async execution.
 */
class WorkoutRepository(
    private val workoutDao: WorkoutDao,
) {
    /**
     * Inserts a new workout into the database.
     *
     * @param workout The workout entity to be inserted
     * @return The auto-generated ID of the inserted workout
     * @throws Exception if the insertion fails
     */
    suspend fun insertWorkout(workout: Workout): Long {
        return workoutDao.insertWorkout(workout)
    }

    /**
     * Updates an existing workout in the database.
     *
     * @param workout The workout entity with updated information
     * @throws Exception if the update fails or workout doesn't exist
     */
    suspend fun updateWorkout(workout: Workout) {
        workoutDao.updateWorkout(workout)
    }

    /**
     * Deletes a workout from the database by its ID.
     *
     * @param workoutId The unique identifier of the workout to delete
     * @throws Exception if the deletion fails
     */
    suspend fun deleteWorkout(workoutId: Long) {
        workoutDao.deleteWorkoutById(workoutId)
    }

    /**
     * Gets all workouts for a specific user as a reactive Flow.
     *
     * @param userId The unique identifier of the user
     * @return Flow emitting list of workouts, updates automatically when data changes
     */
    fun getWorkoutsByUserId(userId: Long): Flow<List<Workout>> {
        return workoutDao.getWorkoutsByUserId(userId)
    }

    /**
     * Gets workouts for a user within a specific date range.
     *
     * @param userId The unique identifier of the user
     * @param startDate The start date (inclusive) of the range
     * @param endDate The end date (inclusive) of the range
     * @return Flow emitting list of workouts within the specified date range
     */
    fun getWorkoutsByDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Workout>> {
        return workoutDao.getWorkoutsByDateRange(userId, startDate, endDate)
    }

    /**
     * Gets workouts of a specific type for a user.
     *
     * @param userId The unique identifier of the user
     * @param workoutType The type of workouts to retrieve (e.g., RUNNING, CYCLING)
     * @return Flow emitting list of workouts matching the specified type
     */
    fun getWorkoutsByType(userId: Long, workoutType: WorkoutType): Flow<List<Workout>> {
        return workoutDao.getWorkoutsByType(userId, workoutType)
    }

    /**
     * Gets the most recent workouts for a user, limited by count.
     *
     * @param userId The unique identifier of the user
     * @param limit The maximum number of recent workouts to retrieve
     * @return Flow emitting list of recent workouts ordered by date (newest first)
     */
    fun getRecentWorkouts(userId: Long, limit: Int): Flow<List<Workout>> {
        return workoutDao.getRecentWorkouts(userId, limit)
    }

    /**
     * Gets weekly statistics for a user from Monday to Sunday of the current week.
     *
     * Calculates comprehensive weekly statistics including workout count, total duration,
     * calories burned, and distance covered for the current calendar week.
     *
     * @param userId The unique identifier of the user
     * @return WeeklyStats object containing aggregated statistics for the current week
     */
    suspend fun getWeeklyStats(userId: Long): WeeklyStats {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weekStart = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val weekEnd = calendar.time

        val workoutCount = workoutDao.getWorkoutCountInDateRange(userId, weekStart, weekEnd)
        val totalDuration = workoutDao.getTotalDurationInDateRange(userId, weekStart.time, weekEnd.time)
        val totalCalories = workoutDao.getTotalCaloriesInDateRange(userId, weekStart.time, weekEnd.time)
        val totalDistance = workoutDao.getTotalDistanceInDateRange(userId, weekStart.time, weekEnd.time)

        return WeeklyStats(
            workoutCount = workoutCount,
            totalDuration = totalDuration.toInt(),
            totalCalories = totalCalories.toInt(),
            totalDistance = totalDistance.toFloat(),
        )
    }

    /**
     * Gets the total number of workouts completed by a user.
     *
     * @param userId The unique identifier of the user
     * @return Total count of all workouts for the user
     */
    suspend fun getTotalWorkoutCount(userId: Long): Int {
        return workoutDao.getTotalWorkoutCount(userId)
    }

    /**
     * Gets the total workout duration in minutes for a user.
     *
     * @param userId The unique identifier of the user
     * @return Total duration of all workouts in minutes, or 0 if no workouts found
     */
    suspend fun getTotalWorkoutDuration(userId: Long): Int {
        return workoutDao.getTotalWorkoutDuration(userId) ?: 0
    }

    /**
     * Gets the average workout duration for a user.
     *
     * @param userId The unique identifier of the user
     * @return Average duration per workout in minutes, or 0.0 if no workouts found
     */
    suspend fun getAverageWorkoutDuration(userId: Long): Float {
        return workoutDao.getAverageWorkoutDuration(userId) ?: 0f
    }

    /**
     * Gets the workout with the longest duration for a user.
     *
     * @param userId The unique identifier of the user
     * @return The longest workout, or null if no workouts found
     */
    suspend fun getLongestWorkout(userId: Long): Workout? {
        return workoutDao.getLongestWorkout(userId)
    }

    /**
     * Gets aggregated workout statistics filtered by workout type.
     *
     * @param userId The unique identifier of the user
     * @param workoutType The specific workout type to filter by
     * @return WeeklyStats object containing statistics for the specified workout type
     */
    suspend fun getWorkoutStatsByType(userId: Long, workoutType: WorkoutType): WeeklyStats {
        val count = workoutDao.getWorkoutCountByType(userId, workoutType)
        val duration = workoutDao.getTotalDurationByType(userId, workoutType) ?: 0
        val calories = workoutDao.getTotalCaloriesByType(userId, workoutType) ?: 0
        val distance = workoutDao.getTotalDistanceByType(userId, workoutType) ?: 0f

        return WeeklyStats(
            workoutCount = count,
            totalDuration = duration,
            totalCalories = calories,
            totalDistance = distance,
        )
    }
}
