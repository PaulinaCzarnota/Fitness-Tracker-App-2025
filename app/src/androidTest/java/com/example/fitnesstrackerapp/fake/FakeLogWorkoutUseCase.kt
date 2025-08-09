package com.example.fitnesstrackerapp.fake

import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import com.example.fitnesstrackerapp.usecase.WorkoutStatistics
import java.util.Date
import java.util.concurrent.atomic.AtomicLong

/**
 * Fake implementation of workout use case for Android instrumented tests.
 * Provides minimal functionality with in-memory storage to prevent compilation errors.
 */
class FakeLogWorkoutUseCase {

    private val workouts = mutableMapOf<Long, Workout>()
    private val idCounter = AtomicLong(1)

    /**
     * Starts a new workout session.
     */
    suspend fun startWorkout(
        userId: Long,
        workoutType: WorkoutType,
        title: String,
    ): Result<Workout> {
        return try {
            val id = idCounter.getAndIncrement()
            val workout = Workout(
                id = id,
                userId = userId,
                workoutType = workoutType,
                title = title,
                startTime = Date(),
            )
            workouts[id] = workout
            Result.success(workout)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Completes a workout session with calculated calories.
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
            val workout = workouts[workoutId]
                ?: return Result.failure(IllegalArgumentException("Workout not found"))

            val duration = ((endTime.time - workout.startTime.time) / 1000 / 60).toInt()
            val finalCalories = if (caloriesBurned > 0) caloriesBurned else (duration * 8) // Simple estimate

            val completedWorkout = workout.copy(
                endTime = endTime,
                duration = duration,
                caloriesBurned = finalCalories,
                distance = distance,
                notes = notes.takeIf { it.isNotBlank() },
            )

            workouts[workoutId] = completedWorkout
            Result.success(completedWorkout)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a workout.
     */
    suspend fun deleteWorkout(workoutId: Long): Result<Unit> {
        return try {
            workouts.remove(workoutId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets workout statistics for a user.
     */
    suspend fun getWorkoutStatistics(userId: Long): Result<WorkoutStatistics> {
        return try {
            val userWorkouts = workouts.values.filter { it.userId == userId }
            val stats = WorkoutStatistics(
                totalWorkouts = userWorkouts.size,
                totalDuration = userWorkouts.sumOf { it.duration },
                totalCalories = userWorkouts.sumOf { it.caloriesBurned },
                averageDuration = if (userWorkouts.isNotEmpty()) {
                    userWorkouts.sumOf { it.duration }.toFloat() / userWorkouts.size
                } else {
                    0f
                },
            )
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Checks if a workout exists.
     */
    fun hasWorkout(workoutId: Long): Boolean {
        return workouts.containsKey(workoutId)
    }
}
