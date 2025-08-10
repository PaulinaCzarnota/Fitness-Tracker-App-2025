package com.example.fitnesstrackerapp.ui.workout

/**
 * Enhanced Workout Logging System with MET Table Integration
 *
 * This comprehensive workout logging system provides:
 * - CRUD operations for workout activities
 * - Automatic calorie calculation using MET tables
 * - Exercise database with comprehensive activities
 * - Real-time workout tracking with timers
 * - Personal record tracking
 * - Workout templates and routines
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesstrackerapp.data.entity.Exercise
import com.example.fitnesstrackerapp.data.entity.ExerciseType
import com.example.fitnesstrackerapp.data.entity.SetType
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.data.entity.WorkoutSet
import com.example.fitnesstrackerapp.data.entity.WorkoutSetSummary
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import com.example.fitnesstrackerapp.util.MetTableCalculator
import com.example.fitnesstrackerapp.util.WorkoutIntensity

/**
 * Main workout logging screen with enhanced functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedWorkoutLoggingScreen(
    workoutLoggingViewModel: WorkoutLoggingViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by workoutLoggingViewModel.uiState.collectAsStateWithLifecycle()
    val workoutTimer by workoutLoggingViewModel.workoutTimer.collectAsStateWithLifecycle()
    val restTimer by workoutLoggingViewModel.restTimer.collectAsStateWithLifecycle()

    var showStartWorkoutDialog by remember { mutableStateOf(false) }
    var showFinishWorkoutDialog by remember { mutableStateOf(false) }
    var showExerciseSelector by remember { mutableStateOf(false) }
    var showSetLogger by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header with workout status and timer
        WorkoutHeader(
            isWorkoutActive = uiState.isWorkoutActive,
            workoutDuration = workoutTimer,
            restTimer = restTimer,
            onStartWorkout = { showStartWorkoutDialog = true },
            onFinishWorkout = { showFinishWorkoutDialog = true },
            onStopRestTimer = { workoutLoggingViewModel.stopRestTimer() },
        )

        if (uiState.isWorkoutActive) {
            // Active workout interface
            ActiveWorkoutContent(
                uiState = uiState,
                onAddExercise = { showExerciseSelector = true },
                onSelectExercise = { exercise ->
                    selectedExercise = exercise
                    workoutLoggingViewModel.selectExercise(exercise)
                },
                onLogSet = { exercise ->
                    selectedExercise = exercise
                    showSetLogger = true
                },
                onRemoveExercise = { workoutLoggingViewModel.removeExerciseFromWorkout(it) },
            )
        } else {
            // Workout history and templates
            WorkoutHistoryAndTemplates(
                recentWorkouts = emptyList(), // This would come from another ViewModel
                onStartFromTemplate = { /* Implement template functionality */ },
            )
        }
    }

    // Dialogs
    if (showStartWorkoutDialog) {
        StartWorkoutDialog(
            onDismiss = { showStartWorkoutDialog = false },
            onStartWorkout = { type, title ->
                workoutLoggingViewModel.startWorkout(type, title)
                showStartWorkoutDialog = false
            },
        )
    }

    if (showFinishWorkoutDialog) {
        FinishWorkoutDialog(
            workoutSummary = uiState.workoutSummary,
            onDismiss = { showFinishWorkoutDialog = false },
            onFinishWorkout = { notes ->
                workoutLoggingViewModel.finishWorkout(notes)
                showFinishWorkoutDialog = false
            },
        )
    }

    if (showExerciseSelector) {
        ExerciseSelectorDialog(
            exercises = uiState.availableExercises,
            selectedExercises = uiState.selectedExercises,
            searchQuery = uiState.exerciseSearchQuery,
            onSearchQueryChange = { workoutLoggingViewModel.searchExercises(it) },
            onExerciseSelect = {
                workoutLoggingViewModel.addExerciseToWorkout(it)
            },
            onDismiss = { showExerciseSelector = false },
        )
    }

    if (showSetLogger && selectedExercise != null) {
        SetLoggerDialog(
            exercise = selectedExercise!!,
            previousSets = uiState.recentSets,
            personalRecords = uiState.personalRecords,
            onLogSet = { setInput ->
                workoutLoggingViewModel.logSet(setInput)
                showSetLogger = false
            },
            onDismiss = { showSetLogger = false },
        )
    }

    // Error and success messages
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
            workoutLoggingViewModel.clearError()
        }
    }

    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or handle success
            workoutLoggingViewModel.clearSuccessMessage()
        }
    }
}

/**
 * Workout header with timer and controls
 */
@Composable
fun WorkoutHeader(
    isWorkoutActive: Boolean,
    workoutDuration: Long,
    restTimer: Int,
    onStartWorkout: () -> Unit,
    onFinishWorkout: () -> Unit,
    onStopRestTimer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (isWorkoutActive) "Active Workout" else "Workout Logging",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                if (isWorkoutActive) {
                    WorkoutTimer(duration = workoutDuration)
                }
            }

            if (restTimer > 0) {
                RestTimerDisplay(
                    timeLeft = restTimer,
                    onStop = onStopRestTimer,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isWorkoutActive) {
                    Button(
                        onClick = onFinishWorkout,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Finish Workout")
                    }
                } else {
                    Button(
                        onClick = onStartWorkout,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Workout")
                    }
                }
            }
        }
    }
}

/**
 * Workout timer display
 */
@Composable
fun WorkoutTimer(duration: Long) {
    val hours = duration / 3600
    val minutes = (duration % 3600) / 60
    val seconds = duration % 60

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Text(
            text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp),
        )
    }
}

/**
 * Rest timer display with countdown
 */
@Composable
fun RestTimerDisplay(
    timeLeft: Int,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rest: ${timeLeft}s",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            IconButton(onClick = onStop) {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = "Stop rest timer",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

/**
 * Active workout content showing selected exercises and sets
 */
@Composable
fun ActiveWorkoutContent(
    uiState: WorkoutLoggingUiState,
    onAddExercise: () -> Unit,
    onSelectExercise: (Exercise) -> Unit,
    onLogSet: (Exercise) -> Unit,
    onRemoveExercise: (Exercise) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Add exercise button
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddExercise() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Exercise",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // Selected exercises
        items(uiState.selectedExercises) { exercise ->
            ExerciseWorkoutCard(
                exercise = exercise,
                sets = uiState.exerciseSets.filter { it.exerciseId == exercise.id },
                isCurrentExercise = uiState.currentExercise?.id == exercise.id,
                onSelect = { onSelectExercise(exercise) },
                onLogSet = { onLogSet(exercise) },
                onRemove = { onRemoveExercise(exercise) },
            )
        }
    }
}

/**
 * Exercise workout card showing sets and controls
 */
@Composable
fun ExerciseWorkoutCard(
    exercise: Exercise,
    sets: List<WorkoutSet>,
    isCurrentExercise: Boolean,
    onSelect: () -> Unit,
    onLogSet: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentExercise) 8.dp else 2.dp,
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentExercise) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Exercise header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${exercise.muscleGroup.name} • ${exercise.equipmentType.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row {
                    IconButton(onClick = onLogSet) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Log set",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = onRemove) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove exercise",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            // Sets display
            if (sets.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Sets:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    sets.forEachIndexed { index, set ->
                        SetDisplayRow(
                            setNumber = index + 1,
                            set = set,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            } else {
                Text(
                    text = "No sets logged yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Display row for individual sets
 */
@Composable
fun SetDisplayRow(
    setNumber: Int,
    set: WorkoutSet,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp),
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Set $setNumber",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (set.repetitions > 0) {
                Text(
                    text = "${set.repetitions} reps",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (set.weight > 0) {
                Text(
                    text = "${set.weight}kg",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (set.duration > 0) {
                Text(
                    text = "${set.duration}s",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (set.isPersonalRecord) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = "Personal Record",
                tint = Color(0xFFFFD700), // Gold color
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Start workout dialog
 */
@Composable
fun StartWorkoutDialog(
    onDismiss: () -> Unit,
    onStartWorkout: (WorkoutType, String) -> Unit,
) {
    var selectedType by remember { mutableStateOf(WorkoutType.WEIGHTLIFTING) }
    var workoutTitle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start New Workout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Workout type selector
                Column {
                    Text(
                        "Workout Type:",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(WorkoutType.entries.take(6)) { type ->
                            FilterChip(
                                onClick = { selectedType = type },
                                label = { Text(type.name) },
                                selected = selectedType == type,
                            )
                        }
                    }
                }

                // Workout title
                OutlinedTextField(
                    value = workoutTitle,
                    onValueChange = { workoutTitle = it },
                    label = { Text("Workout Name (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val title = workoutTitle.ifBlank { selectedType.name }
                    onStartWorkout(selectedType, title)
                },
            ) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

/**
 * Enhanced set logger dialog with MET table integration
 */
@Composable
fun SetLoggerDialog(
    exercise: Exercise,
    previousSets: List<WorkoutSet>,
    personalRecords: List<WorkoutSet>,
    onLogSet: (SetInput) -> Unit,
    onDismiss: () -> Unit,
) {
    var repetitions by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var setType by remember { mutableStateOf(SetType.NORMAL) }
    var rpe by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var intensity by remember { mutableStateOf(WorkoutIntensity.MODERATE) }

    // Calculate calories based on MET table
    val estimatedCalories = remember(weight, duration, repetitions, intensity) {
        try {
            val userWeight = 70.0 // This should come from user profile
            val durationMinutes = duration.toIntOrNull() ?: 0
            val metValue = MetTableCalculator.getMetValue(
                workoutType = when (exercise.exerciseType) {
                    ExerciseType.STRENGTH -> WorkoutType.WEIGHTLIFTING
                    ExerciseType.CARDIO -> WorkoutType.CARDIO
                    ExerciseType.FLEXIBILITY -> WorkoutType.YOGA
                    ExerciseType.BALANCE -> WorkoutType.PILATES
                    else -> WorkoutType.OTHER
                },
                intensity = intensity,
            )

            if (durationMinutes > 0) {
                MetTableCalculator.calculateCaloriesBurned(metValue, userWeight, durationMinutes)
            } else {
                // Estimate for weightlifting sets
                val repsCount = repetitions.toIntOrNull() ?: 0
                val estimatedDuration = (repsCount * 3) / 60 // 3 seconds per rep
                MetTableCalculator.calculateCaloriesBurned(metValue, userWeight, estimatedDuration)
            }
        } catch (e: Exception) {
            0
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Text(
                    text = "Log Set: ${exercise.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                // Previous sets reference
                if (previousSets.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Previous Sets:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            previousSets.take(3).forEach { set ->
                                Text(
                                    "${set.repetitions} reps × ${set.weight}kg",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }

                // Set type selector
                Column {
                    Text("Set Type:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SetType.entries.forEach { type ->
                            FilterChip(
                                onClick = { setType = type },
                                label = { Text(type.name) },
                                selected = setType == type,
                            )
                        }
                    }
                }

                // Input fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = repetitions,
                        onValueChange = { repetitions = it },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )

                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration (s)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )

                    OutlinedTextField(
                        value = distance,
                        onValueChange = { distance = it },
                        label = { Text("Distance (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                }

                // Intensity selector for better calorie calculation
                Column {
                    Text("Intensity:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        WorkoutIntensity.entries.forEach { intensityLevel ->
                            FilterChip(
                                onClick = { intensity = intensityLevel },
                                label = { Text(intensityLevel.name) },
                                selected = intensity == intensityLevel,
                            )
                        }
                    }
                }

                // RPE and notes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = rpe,
                        onValueChange = { rpe = it },
                        label = { Text("RPE (1-10)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                )

                // Calorie estimation display
                if (estimatedCalories > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Estimated calories: $estimatedCalories kcal",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val setInput = SetInput(
                                repetitions = repetitions.toIntOrNull() ?: 0,
                                weight = weight.toDoubleOrNull() ?: 0.0,
                                duration = duration.toIntOrNull() ?: 0,
                                distance = distance.toFloatOrNull() ?: 0f,
                                setType = setType,
                                rpe = rpe.toIntOrNull(),
                                notes = notes,
                            )
                            onLogSet(setInput)
                        },
                        enabled = repetitions.isNotBlank() || duration.isNotBlank(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Log Set")
                    }
                }
            }
        }
    }
}

/**
 * Exercise selector dialog with search and filtering
 */
@Composable
fun ExerciseSelectorDialog(
    exercises: List<Exercise>,
    selectedExercises: List<Exercise>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onExerciseSelect: (Exercise) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Select Exercises",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Search exercises...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Exercise list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(exercises) { exercise ->
                        ExerciseSelectionItem(
                            exercise = exercise,
                            isSelected = selectedExercises.contains(exercise),
                            onSelect = { onExerciseSelect(exercise) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Exercise selection item
 */
@Composable
fun ExerciseSelectionItem(
    exercise: Exercise,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${exercise.muscleGroup.name} • ${exercise.equipmentType.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Finish workout dialog with summary
 */
@Composable
fun FinishWorkoutDialog(
    workoutSummary: List<WorkoutSetSummary>,
    onDismiss: () -> Unit,
    onFinishWorkout: (String) -> Unit,
) {
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finish Workout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Workout summary
                if (workoutSummary.isNotEmpty()) {
                    Column {
                        Text(
                            "Workout Summary:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        workoutSummary.forEach { summary ->
                            Text(
                                "${summary.exerciseName}: ${summary.totalSets} sets",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Workout Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onFinishWorkout(notes) },
            ) {
                Text("Finish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue")
            }
        },
    )
}

/**
 * Workout history and templates placeholder
 */
@Composable
fun WorkoutHistoryAndTemplates(
    recentWorkouts: List<Workout>,
    onStartFromTemplate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Workout history and templates will be displayed here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
