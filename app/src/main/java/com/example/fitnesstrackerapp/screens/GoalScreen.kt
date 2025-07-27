package com.example.fitnesstrackerapp.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackerapp.data.Goal
import com.example.fitnesstrackerapp.viewmodel.GoalViewModel
import com.example.fitnesstrackerapp.viewmodel.GoalViewModelFactory

/**
 * GoalScreen
 *
 * Displays a form for users to set goals, view all saved goals,
 * track progress, and clear all goals. Uses ViewModel with DAO.
 */
@Composable
fun GoalScreen() {
    val context = LocalContext.current

    // Inject GoalViewModel using custom factory to pass Application context
    val viewModel: GoalViewModel = viewModel(
        factory = GoalViewModelFactory(context.applicationContext as Application)
    )

    // States for user input
    var description by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }

    // Observe list of goals from ViewModel (StateFlow)
    val goals by viewModel.allGoals.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Form Header ---
        Text("Set a Goal", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        // --- Goal Description Input ---
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // --- Target Number Input ---
        OutlinedTextField(
            value = target,
            onValueChange = { target = it },
            label = { Text("Target (e.g. 10 sessions)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- Add Goal Button ---
        Button(
            onClick = {
                val trimmedDesc = description.trim()
                val parsedTarget = target.toIntOrNull()

                if (trimmedDesc.isEmpty()) {
                    Toast.makeText(context, "Please enter a goal description.", Toast.LENGTH_SHORT).show()
                } else if (parsedTarget == null || parsedTarget <= 0) {
                    Toast.makeText(context, "Please enter a valid positive number.", Toast.LENGTH_SHORT).show()
                } else {
                    val newGoal = Goal(description = trimmedDesc, target = parsedTarget)
                    viewModel.addGoal(newGoal)
                    description = ""
                    target = ""
                    Toast.makeText(context, "Goal added!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Goal")
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider() // Updated to modern equivalent of Divider
        Spacer(modifier = Modifier.height(16.dp))

        // --- Goal List Header ---
        Text("Your Goals", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // --- Goal List Display ---
        if (goals.isEmpty()) {
            Text("No goals added yet.")
        } else {
            LazyColumn {
                items(goals) { goal ->
                    GoalCard(
                        goal = goal,
                        onUpdate = { viewModel.updateGoal(it) },
                        onDelete = { viewModel.deleteGoal(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Clear All Goals Button ---
            Button(
                onClick = {
                    viewModel.clearAllGoals()
                    Toast.makeText(context, "All goals cleared!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Clear All Goals")
            }
        }
    }
}

/**
 * GoalCard
 *
 * A card UI element that shows an individual goal with buttons to:
 * - Increment progress
 * - Delete the goal
 */
@Composable
fun GoalCard(
    goal: Goal,
    onUpdate: (Goal) -> Unit,
    onDelete: (Goal) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Description: ${goal.description}")
            Text("Target: ${goal.target}")
            Text("Progress: ${goal.current} / ${goal.target}")
            Text("Status: ${if (goal.achieved) "Achieved ✅" else "In Progress ⏳"}")
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // --- Add Progress Button ---
                Button(
                    onClick = {
                        val newProgress = goal.current + 1
                        val updatedGoal = goal.copy(
                            current = newProgress,
                            achieved = newProgress >= goal.target
                        )
                        onUpdate(updatedGoal)
                    }
                ) {
                    Text("Add Progress")
                }

                // --- Delete Goal Button ---
                Button(
                    onClick = { onDelete(goal) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
