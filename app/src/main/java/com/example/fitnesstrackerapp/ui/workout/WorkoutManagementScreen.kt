/**
 * Enhanced Workout Management Screen
 *
 * This comprehensive screen provides full workout management capabilities including:
 * - Create new workouts with detailed information
 * - Edit existing workouts with live preview
 * - Delete workouts with confirmation
 * - Auto-calculate calories using MET tables
 * - Custom notes and workout details
 * - Support for running, cycling, weightlifting, and other activities
 */

package com.example.fitnesstrackerapp.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesstrackerapp.ViewModelFactoryProvider
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import com.example.fitnesstrackerapp.util.MetTableCalculator
import com.example.fitnesstrackerapp.util.WorkoutIntensity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutManagementScreen(
    modifier: Modifier = Modifier,
    authViewModel: com.example.fitnesstrackerapp.ui.auth.AuthViewModel,
    activity: androidx.activity.ComponentActivity,
) {
    LocalContext.current
    
    // Get the current user ID for the ViewModel (fallback to 1L for demo purposes)
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val userId = authState.user?.id ?: 1L
    val userWeight = authState.user?.weightKg?.toDouble() ?: 70.0 // Default 70kg

    // Initialize ViewModel
    val workoutManagementViewModel: WorkoutManagementViewModel = remember(userId) {
        ViewModelFactoryProvider.getWorkoutManagementViewModel(activity, userId)
    }
    val uiState by workoutManagementViewModel.uiState.collectAsStateWithLifecycle()
    
    // Dialog states
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedWorkout by remember { mutableStateOf<Workout?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with create button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Workout Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Workout")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Error message
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // Success message
        uiState.successMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF2E7D32)
                )
            }
            
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(3000)
                workoutManagementViewModel.clearSuccessMessage()
            }
        }

        // Workouts list
        if (uiState.workouts.isEmpty() && !uiState.isLoading) {
            EmptyWorkoutState(
                onCreateWorkout = { showCreateDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.workouts, key = { it.id }) { workout ->
                    WorkoutItemCard(
                        workout = workout,
                        userWeight = userWeight,
                        onEdit = { 
                            selectedWorkout = it
                            showEditDialog = true 
                        },
                        onDelete = { 
                            selectedWorkout = it
                            showDeleteDialog = true 
                        }
                    )
                }
            }
        }
    }

    // Create workout dialog
    if (showCreateDialog) {
        CreateWorkoutDialog(
            userWeight = userWeight,
            onDismiss = { showCreateDialog = false },
            onCreate = { workoutData ->
                workoutManagementViewModel.createWorkout(workoutData)
                showCreateDialog = false
            }
        )
    }
    
    // Edit workout dialog
    if (showEditDialog && selectedWorkout != null) {
        EditWorkoutDialog(
            workout = selectedWorkout!!,
            userWeight = userWeight,
            onDismiss = { 
                showEditDialog = false
                selectedWorkout = null
            },
            onUpdate = { updatedWorkout ->
                workoutManagementViewModel.updateWorkout(updatedWorkout)
                showEditDialog = false
                selectedWorkout = null
            }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog && selectedWorkout != null) {
        DeleteWorkoutDialog(
            workout = selectedWorkout!!,
            onDismiss = { 
                showDeleteDialog = false
                selectedWorkout = null
            },
            onDelete = { workoutToDelete ->
                workoutManagementViewModel.deleteWorkout(workoutToDelete)
                showDeleteDialog = false
                selectedWorkout = null
            }
        )
    }
}

@Composable
private fun EmptyWorkoutState(
    onCreateWorkout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Workouts Yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Start tracking your fitness journey by creating your first workout",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onCreateWorkout) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create First Workout")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutItemCard(
    workout: Workout,
    userWeight: Double,
    onEdit: (Workout) -> Unit,
    onDelete: (Workout) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    // Calculate estimated calories if not present
    val estimatedCalories = if (workout.caloriesBurned > 0) {
        workout.caloriesBurned
    } else {
        MetTableCalculator.calculateWorkoutCalories(
            workoutType = workout.workoutType,
            durationMinutes = workout.duration,
            weightKg = userWeight,
            avgHeartRate = workout.avgHeartRate
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onEdit(workout) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with workout type and menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Workout type icon
                    Icon(
                        imageVector = getWorkoutIcon(workout.workoutType),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = workout.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = workout.workoutType.name.replace("_", " "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = { onEdit(workout) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    
                    IconButton(onClick = { onDelete(workout) }) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Workout stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WorkoutStatItem(
                    label = "Duration",
                    value = workout.getFormattedDuration(),
                    icon = Icons.Default.Schedule
                )
                
                if (workout.distance > 0) {
                    WorkoutStatItem(
                        label = "Distance",
                        value = "${workout.distance} km",
                        icon = Icons.Default.Straighten
                    )
                }
                
                WorkoutStatItem(
                    label = "Calories",
                    value = "$estimatedCalories cal",
                    icon = Icons.Default.LocalFireDepartment
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Date and notes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = dateFormat.format(workout.startTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = timeFormat.format(workout.startTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                workout.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

internal fun getWorkoutIcon(workoutType: WorkoutType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (workoutType) {
        WorkoutType.RUNNING -> Icons.Default.DirectionsRun
        WorkoutType.CYCLING -> Icons.Default.DirectionsBike
        WorkoutType.WEIGHTLIFTING -> Icons.Default.FitnessCenter
        WorkoutType.SWIMMING -> Icons.Default.Pool
        WorkoutType.YOGA -> Icons.Default.SelfImprovement
        WorkoutType.BASKETBALL -> Icons.Default.SportsBasketball
        WorkoutType.SOCCER -> Icons.Default.SportsSoccer
        WorkoutType.TENNIS -> Icons.Default.SportsTennis
        WorkoutType.GOLF -> Icons.Default.SportsGolf
        else -> Icons.Default.DirectionsRun
    }
}

// Data class for workout creation/editing
data class WorkoutData(
    val workoutType: WorkoutType,
    val title: String,
    val duration: Int,
    val distance: Float = 0f,
    val intensity: WorkoutIntensity,
    val notes: String = "",
    val avgHeartRate: Int? = null,
    val maxHeartRate: Int? = null
)
