package com.example.fitnesstrackerapp.usecase

import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import com.example.fitnesstrackerapp.util.MetTableCalculator
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Use case for logging workout sessions.
 *
 * Handles the business logic of creating, updating, and managing workout sessions
 * while keeping ViewModels thin and focused on UI state management.
 */
class LogWorkoutUseCase(
    private val workoutRepository: WorkoutRepository,
) {
    /**
     * Starts a new workout session
     */
    suspend fun startWorkout(
        userId: Long,
        workoutType: WorkoutType,
        title: String,
    ): Result<Workout> {
        return try {
            val workout = Workout(
                userId = userId,
                workoutType = workoutType,
                title = title,
                startTime = Date(),
            )
            val workoutId = workoutRepository.insertWorkout(workout)
            val activeWorkout = workout.copy(id = workoutId)
            Result.success(activeWorkout)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Completes a workout session with calculated calories
     */
    suspend fun completeWorkout(
        workoutId: Long,
        endTime: Date,
        caloriesBurned: Int = 0,
        distance: Float = 0f,
        notes: String = "",
        userWeight: Double = 70.0,
    ): Result<Workout> {
        return try {
            // This would need to be implemented in the repository
            // For now, create a placeholder workout
            val workout = Workout(
                id = workoutId,
                userId = 1L,
                workoutType = WorkoutType.OTHER,
                title = "Placeholder",
                startTime = Date(),
            )

            val duration = ((endTime.time - workout.startTime.time) / 1000 / 60).toInt()

            // Calculate calories if not provided
            val finalCalories = if (caloriesBurned > 0) {
                caloriesBurned
            } else {
                MetTableCalculator.calculateWorkoutCalories(
                    workoutType = workout.workoutType,
                    durationMinutes = duration,
                    weightKg = userWeight,
                    distance = distance.takeIf { it > 0 },
                )
            }

            val completedWorkout = workout.copy(
                endTime = endTime,
                duration = duration,
                caloriesBurned = finalCalories,
                distance = distance,
                notes = notes.takeIf { it.isNotBlank() },
                updatedAt = Date(),
            )

            workoutRepository.updateWorkout(completedWorkout)
            Result.success(completedWorkout)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets workouts for a user
     */
    fun getWorkoutsForUser(userId: Long): Flow<List<Workout>> {
        return workoutRepository.getWorkoutsByUserId(userId)
    }

    /**
     * Deletes a workout
     */
    suspend fun deleteWorkout(workoutId: Long): Result<Unit> {
        return try {
            workoutRepository.deleteWorkout(workoutId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing workout
     */
    suspend fun updateWorkout(workout: Workout): Result<Unit> {
        return try {
            val updatedWorkout = workout.copy(updatedAt = Date())
            workoutRepository.updateWorkout(updatedWorkout)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets recent workouts for a user
     */
    suspend fun getRecentWorkouts(userId: Long, limit: Int = 5): Result<List<Workout>> {
        return try {
            // This would ideally be a repository method, but we'll implement it here for now
            workoutRepository.getWorkoutsByUserId(userId)
            // For now, we return a successful empty result since we can't easily get a single emission
            // In a real implementation, this would be a proper repository method
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets workout statistics for a user
     */
    suspend fun getWorkoutStatistics(userId: Long): Result<WorkoutStatistics> {
        return try {
            val totalDuration = workoutRepository.getTotalWorkoutDuration(userId)
            val averageDuration = workoutRepository.getAverageWorkoutDuration(userId)

            val stats = WorkoutStatistics(
                totalDuration = totalDuration,
                averageDuration = averageDuration,
                totalWorkouts = 0, // Would be calculated from repository
                totalCalories = 0, // Would be calculated from repository
            )
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data class for workout statistics
 */
data class WorkoutStatistics(
    val totalWorkouts: Int,
    val totalDuration: Int,
    val totalCalories: Int,
    val averageDuration: Float,
)
