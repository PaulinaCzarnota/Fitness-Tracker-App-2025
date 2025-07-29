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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.fitnesstrackerapp.data.Goal
import com.example.fitnesstrackerapp.viewmodel.GoalViewModel
import com.example.fitnesstrackerapp.viewmodel.GoalViewModelFactory
import com.example.fitnesstrackerapp.ui.components.BottomNavigationBar

/**
 * GoalScreen
 *
 * Allows users to set and manage fitness goals.
 * Provides functionality to create, track progress, and complete goals.
 *
 * @param navController Optional navigation controller for consistency with Navigation.kt.
 */
@Composable
fun GoalScreen(navController: NavHostController? = null) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Initialize ViewModel with application context
    val viewModel: GoalViewModel = viewModel(
        factory = GoalViewModelFactory(application)
    )

    // State variables for goal input fields
    var description by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }

    // Observe all goals from ViewModel
    val goals by viewModel.allGoals.collectAsStateWithLifecycle(initialValue = emptyList())

    // App layout using Scaffold with BottomNavigationBar
    Scaffold(
        bottomBar = { 
            navController?.let { BottomNavigationBar(navController = it) }
        }
    ) { innerPadding ->
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // --- Header and Goal Input Form ---
            Text("Set a Goal", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(12.dp))

            // Input for Goal Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Input for Goal Target Value
            OutlinedTextField(
                value = target,
                onValueChange = { target = it },
                label = { Text("Target (e.g. 10 sessions)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Button to Add New Goal
            Button(
                onClick = {
                    val desc = description.trim()
                    val targetValue = target.toIntOrNull()

                    // Basic input validation
                    when {
                        desc.isEmpty() -> {
                            Toast.makeText(context, "Please enter a goal description.", Toast.LENGTH_SHORT).show()
                        }
                        targetValue == null || targetValue <= 0 -> {
                            Toast.makeText(context, "Enter a valid target number.", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            viewModel.addGoal(Goal(description = desc, target = targetValue))
                            description = ""
                            target = ""
                            Toast.makeText(context, "Goal added!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Goal")
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- Goal List Section ---
            Text("Your Goals", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (goals.isEmpty()) {
                Text("No goals added yet.")
            } else {
                // Scrollable list of goal cards
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(goals, key = { it.id }) { goal ->
                        GoalCard(
                            goal = goal,
                            onUpdate = { viewModel.updateGoal(it) },
                            onDelete = { viewModel.deleteGoal(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Button to clear all goals
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
}

/**
 * GoalCard
 *
 * Renders a card for an individual goal.
 * Includes:
 * - Description, target, progress
 * - Add Progress button
 * - Delete button
 *
 * @param goal the goal instance to display
 * @param onUpdate callback when progress is incremented
 * @param onDelete callback to remove the goal
 */
@Composable
fun GoalCard(
    goal: Goal,
    onUpdate: (Goal) -> Unit,
    onDelete: (Goal) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                // Button to add progress (increments current count)
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

                // Button to delete this goal
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
