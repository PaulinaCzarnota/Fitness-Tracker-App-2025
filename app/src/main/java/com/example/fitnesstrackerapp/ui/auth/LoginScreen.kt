/**
 * User authentication screen for the Fitness Tracker application.
 *
 * This Compose screen provides:
 * - User login functionality with email and password
 * - User registration with name, email, and password
 * - Real-time validation and error handling
 * - Loading states during authentication operations
 * - Seamless navigation between login and registration modes
 *
 * The screen integrates with AuthViewModel for state management and
 * authentication operations through the repository layer.
 */

package com.example.fitnesstrackerapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Composable function for the Login and Registration screen.
 *
 * @param modifier Modifier for styling the screen.
 * @param authViewModel The ViewModel for handling authentication logic.
 * @param onLoginSuccess A callback to be invoked when login is successful.
 * @param onNavigateToSignUp A callback to navigate to the sign-up screen (optional).
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    val uiState by authViewModel.uiState.collectAsState()

    // Navigate on successful authentication
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isRegistering) "Create Account" else "Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Show name field for registration
        if (isRegistering) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isRegistering) {
                    // Fixed: Include name parameter for registration
                    authViewModel.register(email, password, name)
                } else {
                    authViewModel.login(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(if (isRegistering) "Create Account" else "Login")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { isRegistering = !isRegistering }) {
            Text(if (isRegistering) "Already have an account? Login" else "Don't have an account? Create one")
        }

        // Display error messages
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // This could be a Snackbar, Toast, or a Text element
                // For simplicity, we'll just log it for now
                println("Auth Error: $error")
                authViewModel.clearError() // Clear error after showing
            }
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
