package com.example.fitnesstrackerapp.screens

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackerapp.viewmodel.StepCounterViewModel

/**
 * StepTrackerScreen
 *
 * Displays the user's current step count. Listens for real-time updates from the
 * StepCounterService via broadcast intents and updates ViewModel state.
 *
 * - Automatically registers/unregisters BroadcastReceiver
 * - Works across all Android versions safely
 * - Includes "Reset Steps" button to clear the count
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag")
@Composable
fun StepTrackerScreen(
    stepCounterViewModel: StepCounterViewModel = viewModel()
) {
    val context = LocalContext.current

    // Step count is observed using lifecycle-aware state collection
    val stepCount by stepCounterViewModel.stepCount.collectAsStateWithLifecycle(initialValue = 0)

    // Register BroadcastReceiver on composition and unregister on disposal
    DisposableEffect(Unit) {
        val stepReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "STEP_UPDATE") {
                    val steps = intent.getIntExtra("steps", 0)
                    stepCounterViewModel.updateStepCount(steps)
                }
            }
        }

        val filter = IntentFilter("STEP_UPDATE")

        // Register differently depending on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                stepReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(stepReceiver, filter)
        }

        // Clean up the receiver when this composable is disposed
        onDispose {
            context.unregisterReceiver(stepReceiver)
        }
    }

    // --- UI Layout ---
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text("Step Tracker", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            // Subheading
            Text("Steps Taken", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Step count value (live)
            Text(
                text = stepCount.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Reset steps button
            Button(
                onClick = {
                    stepCounterViewModel.resetStepCount()
                }
            ) {
                Text("Reset Steps")
            }
        }
    }
}
