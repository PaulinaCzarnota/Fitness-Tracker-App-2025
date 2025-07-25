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
 * Displays the current step count and listens for updates from the StepCounterService.
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag")
@Composable
fun StepTrackerScreen(
    stepCounterViewModel: StepCounterViewModel = viewModel()
) {
    val context = LocalContext.current

    // Observe step count from ViewModel with lifecycle-awareness
    val stepCount by stepCounterViewModel.stepCount.collectAsStateWithLifecycle(0)

    // Register BroadcastReceiver when composable is active
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Pass the required receiver flag explicitly for Android 13+
            context.registerReceiver(
                stepReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED // Receiver is internal to app only
            )
        } else {
            context.registerReceiver(stepReceiver, filter)
        }

        // Unregister receiver when the composable leaves the composition
        onDispose {
            context.unregisterReceiver(stepReceiver)
        }
    }

    // --- UI ---
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Step Tracker", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            Text("Steps Taken", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stepCount.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { stepCounterViewModel.resetStepCount() }) {
                Text("Reset Steps")
            }
        }
    }
}
