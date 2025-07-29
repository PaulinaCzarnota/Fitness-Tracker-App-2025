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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.fitnesstrackerapp.sensors.StepCounterService
import com.example.fitnesstrackerapp.viewmodel.StepCounterViewModel
import com.example.fitnesstrackerapp.ui.components.BottomNavigationBar

/**
 * StepTrackerScreen
 *
 * Displays the user's step count, listens for updates from StepCounterService via BroadcastReceiver,
 * and provides a reset button. This screen is Compose-safe and lifecycle-aware.
 *
 * @param navController Optional NavHostController for screen transitions
 * @param stepCounterViewModel ViewModel containing step tracking logic and state
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag")
@Composable
fun StepTrackerScreen(
    navController: NavHostController? = null,
    stepCounterViewModel: StepCounterViewModel = viewModel()
) {
    val context = LocalContext.current

    // Collect step count as state (from ViewModel's StateFlow or LiveData)
    val stepCount by stepCounterViewModel.stepCount.collectAsStateWithLifecycle(
        initialValue = 0,
        lifecycle = LocalLifecycleOwner.current.lifecycle
    )

    // Lifecycle-aware registration of the BroadcastReceiver
    DisposableEffect(Unit) {
        val stepReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == StepCounterService.STEP_UPDATE_ACTION) {
                    val steps = intent.getIntExtra(StepCounterService.EXTRA_STEPS, 0)
                    stepCounterViewModel.updateStepCount(steps)
                }
            }
        }

        val filter = IntentFilter(StepCounterService.STEP_UPDATE_ACTION)

        // Handle receiver registration for API 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                stepReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(stepReceiver, filter)
        }

        // Unregister when Composable is removed from the composition
        onDispose {
            context.unregisterReceiver(stepReceiver)
        }
    }

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
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Step Tracker",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Section Label
            Text(
                text = "Steps Taken",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Step Count
            Text(
                text = stepCount.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Reset Button
            Button(onClick = { stepCounterViewModel.resetStepCount() }) {
                Text("Reset Steps")
            }
        }
    }
}
