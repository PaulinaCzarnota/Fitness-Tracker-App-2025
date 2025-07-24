package com.example.fitnesstrackerapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackerapp.data.Goal
import com.example.fitnesstrackerapp.viewmodel.GoalViewModel

/**
 * GoalScreen displays a form for adding new fitness goals,
 * and a list of existing goals stored in Room database.
 */
@Composable
fun GoalScreen(viewModel: GoalViewModel = viewModel()) {
    val context = LocalContext.current

    // Input state for goal description and target number
    var description by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }

    // Collect goals from ViewModel using lifecycle-aware flow
    val goalList by viewModel.allGoals.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Set a Goal",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Input: Goal description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Goal Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Input: Target sessions
        OutlinedTextField(
            value = target,
            onValueChange = { target = it },
            label = { Text("Target (e.g., 5 sessions)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Button to submit a new goal
        Button(
            onClick = {
                val trimmedDesc = description.trim()
                val targetValue = target.toIntOrNull()

                if (trimmedDesc.isNotBlank() && targetValue != null && targetValue > 0) {
                    // Create and insert a new goal into Room DB
                    val newGoal = Goal(
                        description = trimmedDesc,
                        target = targetValue,
                        current = 0,
                        achieved = false
                    )
                    viewModel.insert(newGoal)

                    // Reset the form
                    description = ""
                    target = ""

                    Toast.makeText(context, "Goal added!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Enter valid description and numeric target", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Goal")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Title: Goals List
        Text(
            text = "Your Goals",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // LazyColumn displaying the saved goals
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
