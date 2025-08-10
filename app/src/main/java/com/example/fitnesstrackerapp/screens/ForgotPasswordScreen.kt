package com.example.fitnesstrackerapp.screens

/**
 * Forgot password screen for the Fitness Tracker App.
 *
 * Provides a user interface for users to reset their password via email.
 * Features input validation, loading states, and success/error messaging.
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fitnesstrackerapp.ui.auth.AuthViewModel

/**
 * Top-level composable for the forgot password screen.
 *
 * Displays the forgot password form and handles password reset logic.
 *
 * @param navController NavController for navigation between screens.
 * @param onNavigateToLogin Callback to navigate to the login screen.
 * @param viewModel AuthViewModel that handles authentication logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    @Suppress("UNUSED_PARAMETER") navController: NavController,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel,
) {
    var email by remember { mutableStateOf("") }
    var resetToken by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(PasswordResetStep.REQUEST_RESET) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Handle navigation after successful password reset
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage?.contains("reset successfully") == true) {
            // Password was reset successfully, navigate back to login
            kotlinx.coroutines.delay(2000) // Show success message for 2 seconds
            onNavigateToLogin()
        } else if (uiState.successMessage?.contains("reset instructions sent") == true) {
            // Reset request was sent, move to next step
            currentStep = PasswordResetStep.ENTER_TOKEN
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (currentStep) {
            PasswordResetStep.REQUEST_RESET -> {
                RequestResetStep(
                    email = email,
                    onEmailChange = { email = it },
                    onRequestReset = {
                        viewModel.initiatePasswordReset(email)
                    },
                    onNavigateToLogin = onNavigateToLogin,
                    uiState = uiState,
                    viewModel = viewModel,
                )
            }

            PasswordResetStep.ENTER_TOKEN -> {
                EnterTokenStep(
                    email = email,
                    resetToken = resetToken,
                    onTokenChange = { resetToken = it },
                    newPassword = newPassword,
                    onNewPasswordChange = { newPassword = it },
                    confirmPassword = confirmPassword,
                    onConfirmPasswordChange = { confirmPassword = it },
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = { passwordVisible = it },
                    confirmPasswordVisible = confirmPasswordVisible,
                    onConfirmPasswordVisibilityChange = { confirmPasswordVisible = it },
                    onResetPassword = {
                        viewModel.resetPassword(email, resetToken, newPassword)
                    },
                    onBackToRequest = {
                        currentStep = PasswordResetStep.REQUEST_RESET
                    },
                    uiState = uiState,
                    viewModel = viewModel,
                    focusManager = focusManager,
                )
            }
        }
    }
}

@Composable
private fun RequestResetStep(
    email: String,
    onEmailChange: (String) -> Unit,
    onRequestReset: () -> Unit,
    onNavigateToLogin: () -> Unit,
    uiState: AuthViewModel.AuthUiState,
    viewModel: AuthViewModel,
) {
    // Title for the forgot password step
    Text(
        text = "Forgot Password",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Instructional text for the user
    Text(
        text = "Enter your email address and we'll send you a reset token to create a new password.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Email input field with validation and icon
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (viewModel.isValidEmail(email)) {
                    onRequestReset()
                }
            },
        ),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
            )
        },
        isError = email.isNotBlank() && !viewModel.isValidEmail(email),
    )

    // Show error message if email is invalid
    if (email.isNotBlank() && !viewModel.isValidEmail(email)) {
        Text(
            text = "Please enter a valid email address",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp),
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Request reset button
    Button(
        onClick = onRequestReset,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = email.isNotBlank() && viewModel.isValidEmail(email) && !uiState.isLoading,
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text("Send Reset Instructions")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Navigation button to go back to login
    TextButton(onClick = onNavigateToLogin) {
        Text("Back to Login")
    }

    // Display messages
    DisplayMessages(uiState)
}

@Composable
private fun EnterTokenStep(
    email: String,
    resetToken: String,
    onTokenChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibilityChange: (Boolean) -> Unit,
    onResetPassword: () -> Unit,
    onBackToRequest: () -> Unit,
    uiState: AuthViewModel.AuthUiState,
    viewModel: AuthViewModel,
    focusManager: androidx.compose.ui.focus.FocusManager,
) {
    Text(
        text = "Reset Your Password",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Enter the reset token sent to $email and your new password.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Reset Token Field
    OutlinedTextField(
        value = resetToken,
        onValueChange = onTokenChange,
        label = { Text("Reset Token") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
        ),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Reset Token",
            )
        },
    )

    Spacer(modifier = Modifier.height(16.dp))

    // New Password Field
    OutlinedTextField(
        value = newPassword,
        onValueChange = onNewPasswordChange,
        label = { Text("New Password") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
        ),
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                )
            }
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Password",
            )
        },
        singleLine = true,
    )

    // Password strength indicator
    if (newPassword.isNotBlank()) {
        val validation = viewModel.validatePasswordStrength(newPassword)
        Text(
            text = validation.message,
            color = if (validation.isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp),
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Confirm Password Field
    OutlinedTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = { Text("Confirm New Password") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                if (canResetPassword(resetToken, newPassword, confirmPassword, viewModel)) {
                    onResetPassword()
                }
            },
        ),
        visualTransformation = if (confirmPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            IconButton(onClick = { onConfirmPasswordVisibilityChange(!confirmPasswordVisible) }) {
                Icon(
                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                )
            }
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Confirm Password",
            )
        },
        singleLine = true,
        isError = confirmPassword.isNotBlank() && newPassword != confirmPassword,
    )

    // Show error message if passwords do not match
    if (confirmPassword.isNotBlank() && newPassword != confirmPassword) {
        Text(
            text = "Passwords do not match",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp),
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Reset Password button
    Button(
        onClick = onResetPassword,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = canResetPassword(resetToken, newPassword, confirmPassword, viewModel) && !uiState.isLoading,
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text("Reset Password")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Navigation buttons to go back or resend token
    Row {
        TextButton(onClick = onBackToRequest) {
            Text("Back")
        }

        Spacer(modifier = Modifier.width(16.dp))

        TextButton(onClick = onBackToRequest) {
            Text("Resend Token")
        }
    }

    // Display messages
    DisplayMessages(uiState)
}

@Composable
private fun DisplayMessages(uiState: AuthViewModel.AuthUiState) {
    uiState.error?.let { error ->
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        ) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    uiState.successMessage?.let { success ->
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Text(
                text = success,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

private fun canResetPassword(
    resetToken: String,
    newPassword: String,
    confirmPassword: String,
    viewModel: AuthViewModel,
): Boolean {
    return resetToken.isNotBlank() &&
        newPassword.isNotBlank() &&
        confirmPassword.isNotBlank() &&
        newPassword == confirmPassword &&
        viewModel.validatePasswordStrength(newPassword).isValid
}

private enum class PasswordResetStep {
    REQUEST_RESET,
    ENTER_TOKEN,
}
