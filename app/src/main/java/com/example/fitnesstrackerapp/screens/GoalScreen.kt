package com.example.fitnesstrackerapp.screens

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
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackerapp.data.Goal
import com.example.fitnesstrackerapp.viewmodel.GoalViewModel

/**
 * GoalScreen
 *
 * Allows users to create and view fitness goals.
 * Uses GoalViewModel to persist data with Room.
 */
@Composable
fun GoalScreen(viewModel: GoalViewModel = viewModel()) {
    val context = LocalContext.current

    // User input fields
    var description by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }

    // Observe goals as LiveData using observeAsState
    val goalList by viewModel.allGoals.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text("Set a Goal", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        // Goal description input
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Goal Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Goal target input (numeric)
        OutlinedTextField(
            value = target,
            onValueChange = { target = it },
            label = { Text("Target (e.g., 5 sessions)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Submit button to add new goal
        Button(
            onClick = {
                val trimmedDesc = description.trim()
                val targetValue = target.toIntOrNull()

                if (trimmedDesc.isNotBlank() && targetValue != null && targetValue > 0) {
                    val newGoal = Goal(
                        description = trimmedDesc,
                        target = targetValue,
                        current = 0,
                        achieved = false
                    )
                    viewModel.addGoal(newGoal)

                    // Reset input fields
                    description = ""
                    target = ""

                    Toast.makeText(context, "Goal added!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        "Enter a valid description and numeric target",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Goal")
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Your Goals", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(goalList) { goal ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
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
