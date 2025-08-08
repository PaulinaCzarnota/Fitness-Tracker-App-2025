/**
 * ViewModel for progress tracking in the Fitness Tracker App.
 *
 * Responsibilities:
 * - Manages progress tracking data and business logic for the UI.
 * - Handles workout and step data aggregation and analysis.
 * - Prepares data for charts and progress visualization.
 * - Uses StateFlow for reactive updates.
 * - Manages loading, error, and success states.
 * - Provides methods for refreshing data and handling user interactions.
 *
 * @property progressRepository Repository for workout progress/statistics.
 * @property stepRepository Repository for step tracking.
 * @property authRepository Repository for authentication state.
 */

package com.example.fitnesstrackerapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.entity.Step
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.repository.AuthRepository
import com.example.fitnesstrackerapp.repository.StepRepository
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * UI state for the progress tracking screen.
 *
 * Holds all data needed to render the progress tracking UI, including weekly/monthly summaries
 * and chart data. Uses a sealed class to represent different states of the UI.
 */
sealed class ProgressUiState {
    /**
     * Represents the loading state when data is being fetched.
     */
    object Loading : ProgressUiState()

    /**
     * Represents the success state with all the required data.
     *
     * @property weeklySummary Summary of workouts from the past week
     * @property monthlySummary Summary of workouts from the past month
     * @property weeklyChartData Data for the weekly chart visualization
     * @property recentWorkouts List of recent workouts
     * @property stepHistory List of step entries
     * @property lastUpdated Timestamp when the data was last updated
     */
    data class Success(
        val weeklySummary: WorkoutSummary = WorkoutSummary(),
        val monthlySummary: WorkoutSummary = WorkoutSummary(),
        val weeklyChartData: List<Float> = emptyList(),
        val recentWorkouts: List<Workout> = emptyList(),
        val stepHistory: List<Step> = emptyList(),
        val lastUpdated: Long = System.currentTimeMillis()
    ) : ProgressUiState()

    /**
     * Represents an error state with an error message.
     *
     * @property message Error message to display to the user
     * @property retryAction Callback to retry the failed operation
     */
    data class Error(
        val message: String,
        val retryAction: () -> Unit = {}
    ) : ProgressUiState()
}

/**
 * Summary statistics for workout data.
 *
 * Aggregates key workout metrics for displaying summary cards in the UI.
 * Includes both total and average values for better insights.
 *
 * @property totalWorkouts Total number of workouts
 * @property totalCalories Total calories burned across all workouts
 * @property totalDuration Total duration of all workouts in minutes
 * @property totalDistance Total distance covered in all workouts in kilometers
 * @property averageCalories Average calories burned per workout
 * @property averageDuration Average duration of workouts in minutes
 * @property averageDistance Average distance covered per workout in kilometers
 * @property startDate Timestamp of the earliest workout in the summary
 * @property endDate Timestamp of the latest workout in the summary
 */
data class WorkoutSummary(
    val totalWorkouts: Int = 0,
    val totalCalories: Int = 0,
    val totalDuration: Int = 0, // in minutes
    val totalDistance: Float = 0f, // in km
    val averageCalories: Int = 0,
    val averageDuration: Int = 0, // in minutes
    val averageDistance: Float = 0f, // in km
    val startDate: Long = 0L,
    val endDate: Long = 0L
) {
    /**
     * Returns true if the summary contains no data.
     */
    val isEmpty: Boolean
        get() = totalWorkouts == 0 && totalCalories == 0 && totalDuration == 0 && totalDistance == 0f
}

/**
 * ViewModel for the progress tracking screen.
 *
 * Manages the state and business logic for the progress tracking feature.
 * Handles data loading, error states, and user interactions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModel(
    private val workoutRepository: WorkoutRepository,
    private val stepRepository: StepRepository,
    authRepository: AuthRepository
) : ViewModel() {
    companion object {
        private const val DAYS_IN_WEEK = 7
        private const val DAYS_IN_MONTH = 30
        private const val RECENT_WORKOUTS_LIMIT = 5
    }

    // StateFlow for the current UI state
    private val _uiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Loading)
    val uiState: StateFlow<ProgressUiState> = _uiState

    // StateFlow for the current user's UID (null if not logged in)
    private val userId: StateFlow<Long?> = authRepository.authState
        .map { it?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Refresh trigger
    private val refreshTrigger = MutableStateFlow(0)

    init {
        // Set up data collection when the ViewModel is created
        collectProgressData()
    }

    /**
     * Refreshes all progress data.
     */
    fun refreshData() {
        refreshTrigger.value++
    }

    /**
     * Sets up data collection from all sources and combines them into the UI state.
     */
    private fun collectProgressData() {
        viewModelScope.launch {
            // Combine user ID and refresh trigger
            combine(
                userId,
                refreshTrigger,
                ::Pair
            ).flatMapLatest { (userId, _) ->
                if (userId == null) {
                    flow<ProgressUiState> { emit(ProgressUiState.Error("Not logged in")) }
                } else {
                    val epochStart = Date(0L)
                    val now = Date()
                    combine(
                        workoutRepository.getWorkoutsByUserId(userId),
                        stepRepository.getStepsInDateRange(userId, epochStart, now)
                    ) { workouts: List<Workout>, steps: List<Step> ->
                        // Compute weekly summary
                        val weeklyWorkouts = workouts.filter { isWithinLastDays(it, DAYS_IN_WEEK) }
                        val weeklySummary = createSummary(weeklyWorkouts)
                        // Compute monthly summary
                        val monthlyWorkouts =
                            workouts.filter { isWithinLastDays(it, DAYS_IN_MONTH) }
                        val monthlySummary = createSummary(monthlyWorkouts)
                        // Prepare recentWorkouts, chartData, and stepHistory
                        val recentWorkouts =
                            workouts.sortedByDescending { it.startTime }.take(RECENT_WORKOUTS_LIMIT)
                        val chartData = prepareWeeklyChartData(weeklyWorkouts)
                        val stepHistory = steps.sortedByDescending { it.date }
                        ProgressUiState.Success(
                            weeklySummary = weeklySummary,
                            monthlySummary = monthlySummary,
                            weeklyChartData = chartData,
                            recentWorkouts = recentWorkouts,
                            stepHistory = stepHistory,
                            lastUpdated = System.currentTimeMillis()
                        )
                    }
                }
            }
                .onStart { emit(ProgressUiState.Loading) }
                .catch { e -> emit(ProgressUiState.Error("An error occurred: ${e.message}")) }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    /**
     * Fetches step history for the current user.
     *
     * @param userId ID of the current user
     * @return Flow emitting the step history
     */
    private fun getStepHistory(userId: Long): Flow<List<Step>> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -DAYS_IN_MONTH)
        val startDate = calendar.time
        return stepRepository.getStepsInDateRange(userId, startDate, endDate)
    }

    /**
     * Prepares data for the weekly chart.
     * - Transforms workout data into chart-compatible format.
     * - Groups calories by day for the past 7 days.
     *
     * @param workouts List of workouts to process
     * @return List of calorie values for each of the past 7 days (most recent last)
     */
    private fun prepareWeeklyChartData(workouts: List<Workout>): List<Float> {
        val dailyTotals = FloatArray(DAYS_IN_WEEK)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        workouts.forEach { workout ->
            val workoutDate = Calendar.getInstance().apply { time = workout.startTime }.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            val daysAgo = ((today.timeInMillis - workoutDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (daysAgo in 0 until DAYS_IN_WEEK) {
                // Index 0 is 6 days ago, index 6 is today
                val dayIndex = DAYS_IN_WEEK - 1 - daysAgo
                dailyTotals[dayIndex] += workout.caloriesBurned.toFloat()
            }
        }
        
        return dailyTotals.toList()
    }
    
    /**
     * Gets the current user ID if available.
     *
     * @return The current user ID or null if not logged in
     */
    private fun getCurrentUserId(): Long? = userId.value

    // Helper: checks if workout is within the last n days
    private fun isWithinLastDays(workout: Workout, days: Int): Boolean {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { time = workout.startTime }
        now.add(Calendar.DAY_OF_YEAR, -days)
        return then.timeInMillis >= now.timeInMillis
    }

    // Helper: generates WorkoutSummary from workout list
    private fun createSummary(workouts: List<Workout>): WorkoutSummary {
        if (workouts.isEmpty()) return WorkoutSummary()
        val totalCalories = workouts.sumOf { it.caloriesBurned }
        val totalDuration = workouts.sumOf { it.duration }
        val totalDistance = workouts.sumOf { it.distance.toDouble() }.toFloat()
        val startDate = workouts.minOf { it.startTime.time }
        val endDate = workouts.maxOf { it.startTime.time }
        return WorkoutSummary(
            totalWorkouts = workouts.size,
            totalCalories = totalCalories,
            totalDuration = totalDuration,
            totalDistance = totalDistance,
            averageCalories = if (workouts.isNotEmpty()) totalCalories / workouts.size else 0,
            averageDuration = if (workouts.isNotEmpty()) totalDuration / workouts.size else 0,
            averageDistance = if (workouts.isNotEmpty()) totalDistance / workouts.size else 0f,
            startDate = startDate,
            endDate = endDate
        )
    }
}
