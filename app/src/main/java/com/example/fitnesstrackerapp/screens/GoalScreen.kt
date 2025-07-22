package com.example.fitnesstrackerapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackerapp.data.Goal
import com.example.fitnesstrackerapp.viewmodel.GoalViewModel

/**
 * Composable screen for setting and tracking fitness goals.
 */
@Composable
fun GoalScreen(viewModel: GoalViewModel = viewModel()) {
    // Local state for user input
    var description by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }

    // Observe list of goals from the ViewModel
    val goalList by viewModel.allGoals.observeAsState(emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Set a Goal",
            style = MaterialTheme.typography.headlineSmall
        )

        // Goal description input
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Goal Description") },
            modifier = Modifier.fillMaxWidth()
        )

        // Goal target input
        OutlinedTextField(
            value = target,
            onValueChange = { target = it },
            label = { Text("Target (e.g., 5 sessions)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Button to submit new goal
        Button(
            onClick = {
                if (description.isNotBlank() && target.isNotBlank()) {
                    val targetValue = target.toIntOrNull() ?: 0
                    if (targetValue > 0) {
                        viewModel.addGoal(
                            Goal(
                                description = description.trim(),
                                target = targetValue
                            )
                        )
                        // Clear input fields
                        description = ""
                        target = ""
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Add Goal")
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Your Goals",
            style = MaterialTheme.typography.titleMedium
        )

        // List of current goals
        LazyColumn {
            items(goalList) { goal ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Goal: ${goal.description}")
                        Text("Target: ${goal.target}")
                        Text("Progress: ${goal.current}/${goal.target}")
                        Text("Status: ${if (goal.achieved) "Completed" else "In Progress"}")
                    }
                }
            }
        }
    }
}
