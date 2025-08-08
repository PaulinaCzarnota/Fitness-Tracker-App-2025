/**
 * WorkoutViewModel Tests
 *
 * Unit tests for WorkoutViewModel functionality including:
 * - Tests ViewModel workout state management
 * - Validates workout start/pause/end operations
 * - Verifies business logic correctness
 */

package com.example.fitnesstrackerapp.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import com.example.fitnesstrackerapp.ui.workout.WorkoutViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlinx.coroutines.flow.flowOf
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    @Mock
    private lateinit var workoutRepository: WorkoutRepository
    
    private lateinit var viewModel: WorkoutViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    
    companion object {
        private const val TEST_USER_ID = 1L
    }
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Setup mock responses
        whenever(workoutRepository.getWorkoutsByUserId(any())).thenReturn(flowOf(emptyList()))
        
        viewModel = WorkoutViewModel(
            workoutRepository = workoutRepository,
            userId = TEST_USER_ID
        )
    }

    @Test
    fun `initial UI state is correct`() = runTest {
        // Given - ViewModel initialized in setup
        
        // When - checking initial state
        val uiState = viewModel.uiState.value
        
        // Then - should have default values
        assertEquals(false, uiState.isLoading)
        assertEquals(null, uiState.error)
        assertTrue(uiState.workouts.isEmpty())
    }
    
    @Test
    fun `start and stop workout update state correctly`() = runTest {
        // Given
        whenever(workoutRepository.insertWorkout(any())).thenReturn(1L)
        val workoutType = WorkoutType.RUNNING
        val title = "Morning Run"
        
        // When - start workout
        viewModel.startWorkout(workoutType, title)
        var uiState = viewModel.uiState.value
        assertEquals(true, uiState.isWorkoutActive)
        assertTrue(uiState.activeWorkout != null)
        
        // When - stop workout
        viewModel.stopWorkout(caloriesBurned = 200, distance = 5.0f, notes = "Good pace")
        uiState = viewModel.uiState.value
        assertEquals(false, uiState.isWorkoutActive)
        assertEquals(null, uiState.activeWorkout)
    }
    
    @Test
    fun `deleting workout does not crash`() = runTest {
        // Given
        val workout = Workout(
            id = 1L,
            userId = TEST_USER_ID,
            workoutType = WorkoutType.CYCLING,
            title = "Evening Ride",
            startTime = Date()
        )
        
        // When
        viewModel.deleteWorkout(workout)
        
        // Then - verify no error
        val uiState = viewModel.uiState.value
        assertEquals(null, uiState.error)
    }
}
